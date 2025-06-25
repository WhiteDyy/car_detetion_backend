package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.geometray.domain.dto.TrackDataPointDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
public class GeometrySseController {

    // 1. 使用线程安全的列表来管理所有客户端连接
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 2. 使用一个定时的、单线程的执行器来模拟实时数据推送和心跳
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    // 3. 用于模拟动态数据的计数器
    private double currentMileage = 0.0;
    private int sleeperDisplayCounter = 1;
    private long tagCounter = 1;

    public GeometrySseController() {
        // 在控制器构造时，启动两个独立的定时任务：
        // a) 一个高频任务用于推送模拟的业务数据（每秒一次）
        scheduledExecutor.scheduleAtFixedRate(this::pushData, 2, 1, TimeUnit.SECONDS);
        // b) 一个低频任务用于发送心跳，保持连接活跃（每20秒一次）
        scheduledExecutor.scheduleAtFixedRate(this::sendHeartbeat, 20, 20, TimeUnit.SECONDS);
    }

    /**
     * 定义SSE接口端点
     * 客户端将连接到这个URL来接收实时数据
     */
    @GetMapping(path = "/geometry/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleGeometrySse() {
        // 设置一个较长的超时时间，例如3分钟，由我们的心跳机制来保活
        SseEmitter emitter = new SseEmitter(180_000L);
        emitters.add(emitter);
        log.info("New Geometry SSE connection established. Total connections: {}", emitters.size());

        // 设置生命周期回调，在连接完成、超时或出错时，从列表中移除emitter
        emitter.onCompletion(() -> removeEmitter(emitter, "completed"));
        emitter.onTimeout(() -> removeEmitter(emitter, "timed out"));
        emitter.onError(e -> removeEmitter(emitter, "errored"));

        // 立即发送一个连接成功事件，便于客户端调试
        sendToSingleEmitter(emitter, SseEmitter.event().name("connected").data("Connection established."));

        return emitter;
    }

    /**
     * 模拟生成并推送一条几何参数数据
     * 在真实场景中，您会在这里替换为从真实数据源（如RabbitMQ, Kafka等）获取数据的逻辑
     */
    private void pushData() {
        if (emitters.isEmpty()) {
            return; // 没有客户端连接，无需生成数据
        }

        // --- 开始构建模拟数据 ---
        TrackDataPointDto dataPoint = new TrackDataPointDto();

        // 1. 里程计算 (与JS中的 currentMileageInDecimeters 逻辑对应)
        // 每次增加0.1米
        currentMileage += 0.1;
        // 为了避免浮点数精度问题，我们先转成整数（分米）再进行判断
        long currentMileageInDecimeters = Math.round(currentMileage * 10.0);
        double currentMileageInMeters = currentMileageInDecimeters / 10.0;

        dataPoint.setMileage(currentMileageInMeters);

        // 2. 模拟所有10个测量值
        Map<String, Double> measurements = new HashMap<>();
        measurements.put("轨距", 1435.0 + Math.round(Math.random() * 4 - 2));
        measurements.put("轨距变化率", Math.random() * 0.2 - 0.1);
        measurements.put("左高低", 2.0 + Math.random() * 2);
        measurements.put("右高低", 2.0 + Math.random() * 2);
        measurements.put("左轨向", 1.0 + Math.random() * 1);
        measurements.put("右轨向", 1.0 + Math.random() * 1);
        measurements.put("水平", Math.random() * 2 - 1);
        measurements.put("三角坑", Math.random() * 0.5);
        measurements.put("垂直磨耗", 0.5 + Math.random() * 0.5);
        measurements.put("侧面磨耗", 0.3 + Math.random() * 0.3);
        dataPoint.setMeasurements(measurements);

        // 3. 模拟电子标签 (每10米)
        if (currentMileageInDecimeters > 0 && currentMileageInDecimeters % 100 == 0) {
            TrackDataPointDto.TagDto tag = new TrackDataPointDto.TagDto();
            tag.setId(tagCounter++);
            dataPoint.setTag(tag);
            sleeperDisplayCounter = 1; // 重置轨枕显示ID
        }

        // 4. 模拟轨枕 (每0.6米)
        if (currentMileageInDecimeters > 0 && currentMileageInDecimeters % 6 == 0) {
            TrackDataPointDto.SleeperDto sleeper = new TrackDataPointDto.SleeperDto();
            sleeper.setDisplayId(sleeperDisplayCounter++);
            dataPoint.setSleeper(sleeper);
        }
        // --- 模拟数据构建结束 ---

        try {
            // 将DTO对象序列化为JSON字符串
            String jsonData = objectMapper.writeValueAsString(dataPoint);
            // 构建一个名为 "geometry-data" 的SSE事件
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name("geometry-data")
                    .data(jsonData);

            log.info("Pushing geometry data: {}", jsonData);
            // 向所有连接的客户端广播此事件
            sendToAllEmitters(event);
        } catch (Exception e) {
            log.error("Error while creating or pushing geometry data", e);
        }
    }

    /**
     * 发送心跳以保持连接活跃
     */
    private void sendHeartbeat() {
        if (!emitters.isEmpty()) {
            // SSE注释是标准的保活方式，客户端会自动忽略，但能有效防止网络超时
            SseEmitter.SseEventBuilder heartbeatEvent = SseEmitter.event()
                    .name("heartbeat")
                    .comment("keep-alive");
            log.debug("Sending heartbeat to {} clients.", emitters.size());
            sendToAllEmitters(heartbeatEvent);
        }
    }

    /**
     * 统一的事件发送方法，循环所有连接并处理异常
     */
    private void sendToAllEmitters(SseEmitter.SseEventBuilder event) {
        for (SseEmitter emitter : emitters) {
            sendToSingleEmitter(emitter, event);
        }
    }

    /**
     * 向单个emitter发送事件，并处理可能发生的IO异常
     */
    private void sendToSingleEmitter(SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException e) {
            // 发送失败，意味着客户端可能已经断开，从列表中移除
            log.warn("Failed to send event to a client, removing emitter.", e);
            emitters.remove(emitter);
        }
    }

    /**
     * 统一的emitter移除方法，确保线程安全和日志记录
     */
    private void removeEmitter(SseEmitter emitter, String reason) {
        emitters.remove(emitter);
        log.info("Emitter {} [Reason: {}]. Remaining connections: {}", emitter, reason, emitters.size());
    }

    /**
     * 应用关闭时，优雅地关闭线程池
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SSE geometry executor service.");
        scheduledExecutor.shutdown();
        try {
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("SSE geometry executor service shut down.");
    }
}
