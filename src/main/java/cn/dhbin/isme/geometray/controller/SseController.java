package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.rabbitmqconsumer.RabbitMQConsumer;
import cn.dhbin.isme.rabbitmqconsumer.SensorData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class SseController {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 【修改1】使用两个独立的、专门的线程池
    // scheduledExecutor 用于定时发送心跳，保证周期性
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
    // businessExecutor 用于处理业务消息，与心跳线程分离，互不阻塞
    private final ScheduledExecutorService businessExecutor = Executors.newSingleThreadScheduledExecutor();


    public SseController() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 【修改2】启动业务数据处理任务。使用 scheduleWithFixedDelay 替代手动 while(true) 循环
        // 每隔1毫秒执行一次 processMessages 任务（在前一个任务完成后）
        businessExecutor.scheduleWithFixedDelay(this::processMessages, 1, 1, TimeUnit.MILLISECONDS);

        // 【修改3】启动独立的心跳发送任务
        // 每隔15秒，固定执行一次 sendHeartbeat
        scheduledExecutor.scheduleAtFixedRate(this::sendHeartbeat, 15, 15, TimeUnit.SECONDS);
    }

    @GetMapping(path = "/sensor", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleSse() {
        // 【修改4】适当增加超时时间，为网络波动等提供更多冗余
        SseEmitter emitter = new SseEmitter(90_000L);
        emitters.add(emitter);
        log.info("New SSE connection established, total emitters: {}", emitters.size());

        // onCompletion, onTimeout, onError 的逻辑保持不变，用于清理emitter
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.info("SSE 连接完成, remaining emitters: {}", emitters.size());
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.info("SSE 连接超时, remaining emitters: {}", emitters.size());
        });
        emitter.onError((e) -> {
            emitters.remove(emitter);
            log.error("SSE 错误: {}, remaining emitters: {}", e.getMessage(), emitters.size());
        });

        // 【修改5】在新连接建立时，立即发送一次初始心跳，可以用于客户端确认连接成功
        try {
            emitter.send(SseEmitter.event().name("heartbeat").data("connected").comment("initial connection"));
        } catch (IOException e) {
            log.warn("Failed to send initial heartbeat to new emitter: {}", e.getMessage());
        }

        return emitter;
    }

    /**
     * 【重构】此方法现在只负责处理业务数据，不再关心心跳
     */
    private void processMessages() {
        try {
            // poll的超时可以设短一些，因为它会被非常频繁地调用
            SensorData sensorData = RabbitMQConsumer.MESSAGE_QUEUE.poll(100, TimeUnit.MILLISECONDS);
            if (sensorData != null) {
                String jsonData = objectMapper.writeValueAsString(sensorData);
                log.info("Sending sensorData: {}", jsonData);
                sendToAllEmitters(SseEmitter.event().name("sensorData").data(jsonData));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 重新设置中断状态
            log.info("Message processing thread interrupted.");
        } catch (Exception e) {
            // 捕获所有异常，防止任务意外终止
            log.error("Error in processMessages task: {}", e.getMessage(), e);
        }
    }

    /**
     * 【新增】专门用于发送心跳的方法
     */
    private void sendHeartbeat() {
        // 只有在有客户端连接时才发送心跳
        if (!emitters.isEmpty()) {
            log.debug("Sending heartbeat to {} emitters.", emitters.size());
            // 同时发送一个SSE注释和一个具名事件，确保网络通畅和应用层逻辑
            sendToAllEmitters(SseEmitter.event().name("heartbeat").data("ping").comment("keep-alive"));
        }
    }

    /**
     * 【新增】抽取出的通用发送方法，以统一处理异常和emitter的移除
     *
     * @param event 要发送的事件
     */
    private void sendToAllEmitters(SseEmitter.SseEventBuilder event) {
        // 遍历所有连接并发送事件
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(event);
            } catch (Exception e) {
                // 任何发送异常都认为此连接已失效，进行移除
                log.error("Failed to send event to an emitter, removing it. Error: {}", e.getMessage());
                emitters.remove(emitter);
            }
        }
    }

    /**
     * 【修改6】确保在应用关闭时，两个线程池都被正确关闭
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SseController executors...");
        // 先关闭业务线程池
        businessExecutor.shutdown();
        try {
            if (!businessExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                businessExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            businessExecutor.shutdownNow();
        }

        // 再关闭心跳线程池
        scheduledExecutor.shutdown();
        try {
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutor.shutdownNow();
        }
        log.info("SseController executors shut down.");
    }
}