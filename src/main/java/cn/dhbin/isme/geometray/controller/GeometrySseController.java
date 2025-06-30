package cn.dhbin.isme.geometray.controller;

// package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.sse.SseManager; // 引入新的 SseManager
import cn.dhbin.isme.rabbitmqconsumer.RabbitMQConsumer;
import cn.dhbin.isme.rabbitmqconsumer.SensorData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api") // 建议为所有API加一个统一前缀
public class GeometrySseController { // 重命名以明确其职责

    @Autowired
    private SseManager sseManager;

    @Autowired
    private ObjectMapper objectMapper; // 从Spring容器注入，避免重复创建

    // 定义主题名称
    public static final String GEOMETRY_DATA_TOPIC = "geometryData";

    // 使用一个单独的线程来处理RabbitMQ的消息，避免阻塞Web线程
    private final ExecutorService rabbitMqProcessor = Executors.newSingleThreadExecutor();

    @GetMapping(path = "/geometry/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter(3600_000L); // 1小时超时
        // 将新的连接注册到 "geometryData" 主题
        sseManager.register(GEOMETRY_DATA_TOPIC, emitter);
        return emitter;
    }

    // 使用 @PostConstruct 注解，在Bean初始化后启动消息处理循环
    @PostConstruct
    public void startMessageProcessing() {
        rabbitMqProcessor.submit(() -> {
            log.info("Starting RabbitMQ message processing loop for geometry data...");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SensorData sensorData = RabbitMQConsumer.MESSAGE_QUEUE.poll(5, TimeUnit.SECONDS);
                    if (sensorData != null) {
                        String jsonData = objectMapper.writeValueAsString(sensorData);

                        SseEmitter.SseEventBuilder event = SseEmitter.event()
                                .name("geometry-data") // 与前端约定的事件名
                                .data(jsonData);

                        // 向 "geometryData" 主题广播
                        sseManager.broadcast(GEOMETRY_DATA_TOPIC, event);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 保持中断状态
                    log.warn("RabbitMQ processing thread interrupted.");
                } catch (Exception e) {
                    log.error("Error processing message from RabbitMQ for geometry data", e);
                }
            }
            log.info("RabbitMQ message processing loop for geometry data stopped.");
        });
    }

    // 在应用关闭时，关闭线程池
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down RabbitMQ processor for geometry data...");
        rabbitMqProcessor.shutdownNow();
    }
}