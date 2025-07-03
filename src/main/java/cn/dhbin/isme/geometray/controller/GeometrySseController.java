package cn.dhbin.isme.geometray.controller;

// package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.sse.SseManager; // 引入新的 SseManager
import cn.dhbin.isme.geometray.domain.dto.TrackDataPointDto;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

//@Slf4j
//@RestController
//public class GeometrySseController { // 重命名以明确其职责
//
//    @Autowired
//    private SseManager sseManager;
//
//    @Autowired
//    private ObjectMapper objectMapper; // 从Spring容器注入，避免重复创建
//
//    // 定义主题名称
//    public static final String GEOMETRY_DATA_TOPIC = "geometryData";
//
//    // 使用一个单独的线程来处理RabbitMQ的消息，避免阻塞Web线程
//    private final ExecutorService rabbitMqProcessor = Executors.newSingleThreadExecutor();
//
//    @GetMapping(path = "/geometry/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter handleSse() {
//        SseEmitter emitter = new SseEmitter(3600_000L); // 1小时超时
//        // 将新的连接注册到 "geometryData" 主题
//        sseManager.register(GEOMETRY_DATA_TOPIC, emitter);
//        return emitter;
//    }
//
//    // 使用 @PostConstruct 注解，在Bean初始化后启动消息处理循环
//    @PostConstruct
//    public void startMessageProcessing() {
//        rabbitMqProcessor.submit(() -> {
//            log.info("Starting RabbitMQ message processing loop for geometry data...");
//            while (!Thread.currentThread().isInterrupted()) {
//                try {
//                    SensorData sensorData = RabbitMQConsumer.MESSAGE_QUEUE.poll(5, TimeUnit.SECONDS);
//                    if (sensorData != null) {
//                        String jsonData = objectMapper.writeValueAsString(sensorData);
//
//                        SseEmitter.SseEventBuilder event = SseEmitter.event()
//                                .name("geometry-data") // 与前端约定的事件名
//                                .data(jsonData);
//
//                        // 向 "geometryData" 主题广播
//                        sseManager.broadcast(GEOMETRY_DATA_TOPIC, event);
//                    }
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt(); // 保持中断状态
//                    log.warn("RabbitMQ processing thread interrupted.");
//                } catch (Exception e) {
//                    log.error("Error processing message from RabbitMQ for geometry data", e);
//                }
//            }
//            log.info("RabbitMQ message processing loop for geometry data stopped.");
//        });
//    }
//
//    // 在应用关闭时，关闭线程池
//    @PreDestroy
//    public void shutdown() {
//        log.info("Shutting down RabbitMQ processor for geometry data...");
//        rabbitMqProcessor.shutdownNow();
//    }
//
//
//
//    // 3. 用于模拟动态数据的计数器
//    private double currentMileage = 0.0;
//    private int sleeperDisplayCounter = 1;
//    private long tagCounter = 1;
//
//
//    private void pushData() {
//
//        // --- 开始构建模拟数据 ---
//        TrackDataPointDto dataPoint = new TrackDataPointDto();
//
//        // 1. 里程计算 (与JS中的 currentMileageInDecimeters 逻辑对应)
//        // 每次增加0.1米
//        currentMileage += 0.1;
//        // 为了避免浮点数精度问题，我们先转成整数（分米）再进行判断
//        long currentMileageInDecimeters = Math.round(currentMileage * 10.0);
//        double currentMileageInMeters = currentMileageInDecimeters / 10.0;
//
//        dataPoint.setMileage(currentMileageInMeters);
//
//        // 2. 模拟所有10个测量值
//        Map<String, Double> measurements = new HashMap<>();
//        measurements.put("轨距", 1435.0 + Math.round(Math.random() * 4 - 2));
//        measurements.put("轨距变化率", Math.random() * 0.2 - 0.1);
//        measurements.put("左高低", 2.0 + Math.random() * 2);
//        measurements.put("右高低", 2.0 + Math.random() * 2);
//        measurements.put("左轨向", 1.0 + Math.random() * 1);
//        measurements.put("右轨向", 1.0 + Math.random() * 1);
//        measurements.put("水平", Math.random() * 2 - 1);
//        measurements.put("三角坑", Math.random() * 0.5);
//        measurements.put("垂直磨耗", 0.5 + Math.random() * 0.5);
//        measurements.put("侧面磨耗", 0.3 + Math.random() * 0.3);
//        dataPoint.setMeasurements(measurements);
//
//        // 3. 模拟电子标签 (每10米)
//        if (currentMileageInDecimeters > 0 && currentMileageInDecimeters % 100 == 0) {
//            TrackDataPointDto.TagDto tag = new TrackDataPointDto.TagDto();
//            tag.setId(tagCounter++);
//            dataPoint.setTag(tag);
//            sleeperDisplayCounter = 1; // 重置轨枕显示ID
//        }
//
//        // 4. 模拟轨枕 (每0.6米)
//        if (currentMileageInDecimeters > 0 && currentMileageInDecimeters % 6 == 0) {
//            TrackDataPointDto.SleeperDto sleeper = new TrackDataPointDto.SleeperDto();
//            sleeper.setDisplayId(sleeperDisplayCounter++);
//            dataPoint.setSleeper(sleeper);
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
//
//        } catch (Exception e) {
//            log.error("Error while creating or pushing geometry data", e);
//        }
//    }
//}


@Slf4j
@RestController
public class GeometrySseController {

    @Autowired
    private SseManager sseManager;

    @Autowired
    private ObjectMapper objectMapper;

    // 定义主题名称
    public static final String GEOMETRY_DATA_TOPIC = "geometryData";

    // 用于模拟动态数据的计数器
    private double currentMileage = 0.0;
    private int sleeperDisplayCounter = 1;
    private final AtomicLong tagCounter = new AtomicLong(1);

    @GetMapping(path = "/geometry/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter(3600_000L); // 1小时超时
        // 注册到 SseManager 的 geometryData 主题
        sseManager.register(GEOMETRY_DATA_TOPIC, emitter);

        // 为每个 SSE 连接创建一个定时任务，定期推送模拟数据
        ScheduledExecutorService dataPushExecutor = Executors.newSingleThreadScheduledExecutor();
        dataPushExecutor.scheduleAtFixedRate(() -> {
            try {
                pushData();
            } catch (Exception e) {
                log.error("Error pushing data for SSE connection", e);
                emitter.completeWithError(e); // 发生错误时关闭连接
            }
        }, 0, 1, TimeUnit.SECONDS); // 每秒推送一次数据

        // 当 SSE 连接关闭时，清理定时任务
        emitter.onCompletion(() -> {
            log.info("SSE connection closed, shutting down data push executor.");
            dataPushExecutor.shutdownNow();
        });
        emitter.onTimeout(() -> {
            log.info("SSE connection timed out, shutting down data push executor.");
            dataPushExecutor.shutdownNow();
        });
        emitter.onError((e) -> {
            log.error("SSE connection error, shutting down data push executor.", e);
            dataPushExecutor.shutdownNow();
        });

        return emitter;
    }

    private void pushData() {
        // --- 开始构建模拟数据 ---
        TrackDataPointDto dataPoint = new TrackDataPointDto();

        // 1. 里程计算 (与JS中的 currentMileageInDecimeters 逻辑对应)
        // 每次增加0.1米
        synchronized (this) { // 同步以避免多线程竞争
            currentMileage += 0.1;
            // 为了避免浮点数精度问题，先转成整数（分米）再进行判断
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
                tag.setId(tagCounter.getAndIncrement());
                dataPoint.setTag(tag);
                sleeperDisplayCounter = 1; // 重置轨枕显示ID
            }

            // 4. 模拟轨枕 (每0.6米)
            if (currentMileageInDecimeters > 0 && currentMileageInDecimeters % 6 == 0) {
                TrackDataPointDto.SleeperDto sleeper = new TrackDataPointDto.SleeperDto();
                sleeper.setDisplayId(sleeperDisplayCounter++);
                dataPoint.setSleeper(sleeper);
            }
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
            // 通过 SseManager 向 geometryData 主题广播
            sseManager.broadcast(GEOMETRY_DATA_TOPIC, event);
        } catch (Exception e) {
            log.error("Error while creating or pushing geometry data", e);
            throw new RuntimeException("Failed to push geometry data", e);
        }
    }
}
