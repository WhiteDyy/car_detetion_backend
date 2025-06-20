package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.rabbitmqconsumer.RabbitMQConsumer;
import cn.dhbin.isme.rabbitmqconsumer.SensorData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class SseController {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SseController() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 禁用时间戳格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")); // 自定义格式
        executor.execute(this::processMessages);
        executor.execute(this::processMessages);
    }

    @GetMapping(path = "/sensor", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter(60_000L); // 延长超时到 60 秒
        emitters.add(emitter);
        log.info("New SSE connection established, total emitters: {}", emitters.size());

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

        return emitter;
    }

    private void processMessages() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 轮询队列，5 秒超时
                SensorData sensorData = RabbitMQConsumer.MESSAGE_QUEUE.poll(5, TimeUnit.SECONDS);
                if (sensorData != null) {
                    String jsonData = objectMapper.writeValueAsString(sensorData);
                    log.info("Sending sensorData: {}", jsonData);
                    for (SseEmitter emitter : emitters) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("sensorData")
                                    .data(jsonData));
                        } catch (IOException e) {
                            log.error("Failed to send to emitter: {}", e.getMessage());
                            emitters.remove(emitter);
                            emitter.completeWithError(e);
                        }
                    }
                } else {
                    // 无消息，发送心跳
                    log.debug("No messages, sending heartbeat");
                    for (SseEmitter emitter : emitters) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("heartbeat")
                                    .data("ping"));
                        } catch (IOException e) {
                            log.error("Failed to send heartbeat to emitter: {}", e.getMessage());
                            emitters.remove(emitter);
                            emitter.completeWithError(e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Message processing thread interrupted");
                break;
            } catch (Exception e) {
                log.error("Error in processMessages: {}", e.getMessage());
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down executor");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}