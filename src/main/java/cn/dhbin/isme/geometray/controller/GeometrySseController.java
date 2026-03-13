//package cn.dhbin.isme.geometray.controller;
//
//import cn.dhbin.isme.common.sse.SseManager; // 引入新的 SseManager
//import cn.dhbin.isme.geometray.domain.dto.TrackDataPointDto;
//import cn.dhbin.isme.rabbitmqconsumer.RabbitMQConsumer;
//import cn.dhbin.isme.rabbitmqconsumer.SensorData;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicLong;
//
//
//@Slf4j
//@RestController
//public class GeometrySseController {
//
//    @Autowired
//    private SseManager sseManager;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    // 定义主题名称
//    public static final String GEOMETRY_DATA_TOPIC = "geometryData";
//
//    public static final String SENSOR_DATA_TOPIC = "sensorData";
//
//    // 用于模拟动态数据的计数器
//    private double currentMileage = 0.0;
//    private int sleeperDisplayCounter = 1;
//    private final AtomicLong tagCounter = new AtomicLong(1);
//
//    // 用于处理传感器数据的线程池
//    private final ScheduledExecutorService sensorDataExecutor = Executors.newSingleThreadScheduledExecutor();
//
//    private void processSensorMessages() {
//        try {
//            SensorData sensorData = RabbitMQConsumer.MESSAGE_QUEUE.poll(100, TimeUnit.MILLISECONDS);
//            if (sensorData != null) {
//                String jsonData = objectMapper.writeValueAsString(sensorData);
//                log.debug("Sending sensorData: {}", jsonData);
//                SseEmitter.SseEventBuilder event = SseEmitter.event()
//                        .name("sensor-data")
//                        .data(jsonData);
//                sseManager.broadcast(SENSOR_DATA_TOPIC, event);
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            log.info("Sensor message processing thread interrupted.");
//        } catch (Exception e) {
//            log.error("Error in processSensorMessages task: {}", e.getMessage(), e);
//        }
//    }
//
//
//    @GetMapping(path = "/geometry/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter handleSse() {
//        SseEmitter emitter = new SseEmitter(3600_000L); // 1小时超时
//        // 注册到 SseManager 的 geometryData 主题
//        sseManager.register(GEOMETRY_DATA_TOPIC, emitter);
//
//        // 为每个 SSE 连接创建一个定时任务，定期推送模拟数据
//        ScheduledExecutorService dataPushExecutor = Executors.newSingleThreadScheduledExecutor();
//        dataPushExecutor.scheduleAtFixedRate(() -> {
//            try {
//                pushData();
//            } catch (Exception e) {
//                log.error("Error pushing data for SSE connection", e);
//                emitter.completeWithError(e); // 发生错误时关闭连接
//            }
//        }, 0, 1, TimeUnit.SECONDS); // 每秒推送一次数据
//
//        // 当 SSE 连接关闭时，清理定时任务
//        emitter.onCompletion(() -> {
//            log.info("SSE connection closed, shutting down data push executor.");
//            dataPushExecutor.shutdownNow();
//        });
//        emitter.onTimeout(() -> {
//            log.info("SSE connection timed out, shutting down data push executor.");
//            dataPushExecutor.shutdownNow();
//        });
//        emitter.onError((e) -> {
//            log.error("SSE connection error, shutting down data push executor.", e);
//            dataPushExecutor.shutdownNow();
//        });
//
//        return emitter;
//    }
//
//
//    @GetMapping(path = "/sensor", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter handleSensorSse() {
//        SseEmitter emitter = new SseEmitter(90_000L); // 90秒超时
//        sseManager.register(SENSOR_DATA_TOPIC, emitter);
//
//        // 立即发送一次初始心跳
//        try {
//            emitter.send(SseEmitter.event().name("heartbeat").data("connected").comment("initial connection"));
//        } catch (IOException e) {
//            log.warn("Failed to send initial heartbeat to new emitter: {}", e.getMessage());
//        }
//
//        // 启动传感器数据处理任务
//        sensorDataExecutor.scheduleAtFixedRate(() -> {
//            try {
//                processSensorMessages();
//            } catch (Exception e) {
//                log.error("Error processing sensor messages", e);
//            }
//        }, 0, 100, TimeUnit.MILLISECONDS); // 每100毫秒检查一次消息队列
//
//        setupEmitterCleanup(emitter, null);
//        return emitter;
//    }
//
//    private void setupEmitterCleanup(SseEmitter emitter, ScheduledExecutorService executor) {
//        emitter.onCompletion(() -> {
//            log.info("SSE connection closed");
//            if (executor != null) {
//                executor.shutdownNow();
//            }
//        });
//        emitter.onTimeout(() -> {
//            log.info("SSE connection timed out");
//            if (executor != null) {
//                executor.shutdownNow();
//            }
//        });
//        emitter.onError((e) -> {
//            log.error("SSE connection error", e);
//            if (executor != null) {
//                executor.shutdownNow();
//            }
//        });
//    }
//
//
//    private void pushData() {
//        // --- 开始构建模拟数据 ---
//        TrackDataPointDto dataPoint = new TrackDataPointDto();
//
//        // 1. 里程计算 (与JS中的 currentMileageInDecimeters 逻辑对应)
//        // 每次增加0.1米
//        synchronized (this) { // 同步以避免多线程竞争
//            currentMileage += 0.1;
//            // 为了避免浮点数精度问题，先转成整数（分米）再进行判断
//            long currentMileageInDecimeters = Math.round(currentMileage * 10.0);
//            double currentMileageInMeters = currentMileageInDecimeters / 10.0;
//
//            dataPoint.setMileage(currentMileageInMeters);
//
//            // 2. 模拟所有10个测量值
//            Map<String, Double> measurements = new HashMap<>();
//            measurements.put("轨距", 1435.0 + Math.round(Math.random() * 4 - 2));
//            measurements.put("轨距变化率", Math.random() * 0.2 - 0.1);
//            measurements.put("左高低", 2.0 + Math.random() * 2);
//            measurements.put("右高低", 2.0 + Math.random() * 2);
//            measurements.put("左轨向", 1.0 + Math.random() * 1);
//            measurements.put("右轨向", 1.0 + Math.random() * 1);
//            measurements.put("水平", Math.random() * 2 - 1);
//            measurements.put("三角坑", Math.random() * 0.5);
//            dataPoint.setMeasurements(measurements);
//
//            // 3. 模拟电子标签 (每10米)
//            if (currentMileageInDecimeters > 0 && currentMileageInDecimeters % 100 == 0) {
//                TrackDataPointDto.TagDto tag = new TrackDataPointDto.TagDto();
//                tag.setId(tagCounter.getAndIncrement());
//                dataPoint.setTag(tag);
//                sleeperDisplayCounter = 1; // 重置轨枕显示ID
//            }
//
//            // 4. 模拟轨枕 (每0.6米)
//            if (currentMileageInDecimeters > 0 && currentMileageInDecimeters % 6 == 0) {
//                TrackDataPointDto.SleeperDto sleeper = new TrackDataPointDto.SleeperDto();
//                sleeper.setDisplayId(sleeperDisplayCounter++);
//                dataPoint.setSleeper(sleeper);
//            }
//        }
//        // --- 模拟数据构建结束 ---
//
//        try {
//            // 将DTO对象序列化为JSON字符串
//            String jsonData = objectMapper.writeValueAsString(dataPoint);
//            // 构建一个名为 "geometry-data" 的SSE事件
//            SseEmitter.SseEventBuilder event = SseEmitter.event()
//                    .name("geometry-data")
//                    .data(jsonData);
//
//            log.info("Pushing geometry data: {}", jsonData);
//            // 通过 SseManager 向 geometryData 主题广播
//            sseManager.broadcast(GEOMETRY_DATA_TOPIC, event);
//        } catch (Exception e) {
//            log.error("Error while creating or pushing geometry data", e);
//            throw new RuntimeException("Failed to push geometry data", e);
//        }
//    }
//}
//


package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.sse.SseManager;
import cn.dhbin.isme.rabbitmqconsumer.GeometryResult;
import cn.dhbin.isme.rabbitmqconsumer.RabbitMQConsumer;
import cn.dhbin.isme.rabbitmqconsumer.SensorData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class GeometrySseController {

    @Autowired
    private SseManager sseManager;

    @Autowired
    private ObjectMapper objectMapper;

    // 定义主题名称
    public static final String GEOMETRY_DATA_TOPIC = "geometryData";
    public static final String SENSOR_DATA_TOPIC = "sensorData";

    // 使用单例线程池，避免重复创建和销毁
    private final ScheduledExecutorService sensorDataExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService geometryDataExecutor = Executors.newSingleThreadScheduledExecutor();

    // 标记线程池是否已启动
    private volatile boolean sensorExecutorStarted = false;
    private volatile boolean geometryExecutorStarted = false;

    /**
     * 传感器 SSE 每次推送的最大批量大小。
     * 通过 batch 推送提升吞吐，避免本地队列积压导致“回放旧数据”。
     */
    private static final int SENSOR_SSE_BATCH_SIZE = 50;

    // 处理传感器消息
    private void processSensorMessages() {
        try {
            // 按批次推送：尽量把 MESSAGE_QUEUE 中的数据实时、完整地送到前端，避免积压
            List<SensorData> batch = new ArrayList<>(SENSOR_SSE_BATCH_SIZE);

            SensorData first = RabbitMQConsumer.MESSAGE_QUEUE.poll(100, TimeUnit.MILLISECONDS);
            if (first == null) {
                return;
            }
            batch.add(first);

            for (int i = 1; i < SENSOR_SSE_BATCH_SIZE; i++) {
                SensorData next = RabbitMQConsumer.MESSAGE_QUEUE.poll();
                if (next == null) {
                    break;
                }
                batch.add(next);
            }

            String jsonData = objectMapper.writeValueAsString(batch);
            log.debug("Sending sensorData batch size={}", batch.size());
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name("sensor-data")
                    .data(jsonData);
            sseManager.broadcast(SENSOR_DATA_TOPIC, event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Sensor message processing thread interrupted.");
        } catch (Exception e) {
            log.error("Error in processSensorMessages task: {}", e.getMessage(), e);
        }
    }

    // 处理几何结果消息
    private void processGeometryMessages() {
        try {
            GeometryResult geometryResult = RabbitMQConsumer.GEOMETRY_RESULT_QUEUE.poll(100, TimeUnit.MILLISECONDS);
            if (geometryResult != null) {
                String jsonData = objectMapper.writeValueAsString(geometryResult);
                log.info("[SSE-GEOMETRY][SEND-TO-FRONTEND] {}", jsonData);
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .name("geometry-data")
                        .data(jsonData);
                sseManager.broadcast(GEOMETRY_DATA_TOPIC, event);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Geometry message processing thread interrupted.");
        } catch (Exception e) {
            log.error("Error in processGeometryMessages task: {}", e.getMessage(), e);
        }
    }

    @GetMapping(path = "/geometry/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleGeometrySse() {
        SseEmitter emitter = new SseEmitter(90_000L); // 90秒超时
        sseManager.register(GEOMETRY_DATA_TOPIC, emitter);

        // 立即发送一次初始心跳
        try {
            emitter.send(SseEmitter.event().name("heartbeat").data("connected").comment("initial connection"));
        } catch (IOException e) {
            log.warn("Failed to send initial heartbeat to new emitter: {}", e.getMessage());
        }

        // 启动几何数据处理任务（如果尚未启动）
        if (!geometryExecutorStarted) {
            geometryDataExecutor.scheduleAtFixedRate(() -> {
                try {
                    processGeometryMessages();
                } catch (Exception e) {
                    log.error("Error processing geometry messages", e);
                }
            }, 0, 100, TimeUnit.MILLISECONDS);
            geometryExecutorStarted = true;
        }

        setupEmitterCleanup(emitter);
        return emitter;
    }

    @GetMapping(path = "/sensor", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleSensorSse() {
        SseEmitter emitter = new SseEmitter(90_000L); // 90秒超时
        sseManager.register(SENSOR_DATA_TOPIC, emitter);

        // 立即发送一次初始心跳
        try {
            emitter.send(SseEmitter.event().name("heartbeat").data("connected").comment("initial connection"));
        } catch (IOException e) {
            log.warn("Failed to send initial heartbeat to new emitter: {}", e.getMessage());
        }

        // 启动传感器数据处理任务（如果尚未启动）
        if (!sensorExecutorStarted) {
            sensorDataExecutor.scheduleAtFixedRate(() -> {
                try {
                    processSensorMessages();
                } catch (Exception e) {
                    log.error("Error processing sensor messages", e);
                }
            }, 0, 100, TimeUnit.MILLISECONDS);
            sensorExecutorStarted = true;
        }

        setupEmitterCleanup(emitter);
        return emitter;
    }

    private void setupEmitterCleanup(SseEmitter emitter) {
        emitter.onCompletion(() -> {
            log.info("SSE connection closed");
            // 不再关闭线程池，因为它们是单例的
        });
        emitter.onTimeout(() -> {
            log.info("SSE connection timed out");
            // 不再关闭线程池，因为它们是单例的
        });
        emitter.onError((e) -> {
            log.error("SSE connection error", e);
            // 不再关闭线程池，因为它们是单例的
        });
    }
}