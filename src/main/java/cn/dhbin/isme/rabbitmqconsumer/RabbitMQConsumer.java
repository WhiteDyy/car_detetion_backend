package cn.dhbin.isme.rabbitmqconsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class RabbitMQConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SensorDataRepository sensorDataRepository;
    private final GeometryResultRepository geometryResultRepository;
    private final SensorStatusRepository sensorStatusRepository;
    private final JobIdManager jobIdManager;
    
    private final List<SensorData> batch = new ArrayList<>();
    private final List<GeometryResultEntity> geometryBatch = new ArrayList<>();
    
    private static final int BATCH_SIZE = 100;
    private static final int GEOMETRY_BATCH_SIZE = 100;

    // 共享的 BlockingQueue，用于存储 SensorData
    public static final BlockingQueue<SensorData> MESSAGE_QUEUE = new LinkedBlockingQueue<>();

    // 新增：用于存储几何结果的队列
    public static final BlockingQueue<GeometryResult> GEOMETRY_RESULT_QUEUE = new LinkedBlockingQueue<>();

    @Autowired
    public RabbitMQConsumer(SensorDataRepository sensorDataRepository,
                            GeometryResultRepository geometryResultRepository,
                            SensorStatusRepository sensorStatusRepository,
                            JobIdManager jobIdManager) {
        this.sensorDataRepository = sensorDataRepository;
        this.geometryResultRepository = geometryResultRepository;
        this.sensorStatusRepository = sensorStatusRepository;
        this.jobIdManager = jobIdManager;
    }

    // 监听原始传感器数据队列
    @RabbitListener(queues = "raw_sensor_data")
    public void onRawSensorMessage(Message message, Channel channel) throws Exception {
        try {
            String messageBody = new String(message.getBody());
            processRawSensorData(messageBody);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("Error processing raw sensor message: {}", e.getMessage());
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    // 监听几何结果队列
    @RabbitListener(queues = "rail_geometry_results")
    public void onGeometryResultMessage(Message message, Channel channel) throws Exception {
        try {
            String messageBody = new String(message.getBody());
            processGeometryResult(messageBody);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("Error processing geometry result message: {}", e.getMessage());
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    // 监听传感器状态队列
    @RabbitListener(queues = "sensor_status")
    public void onSensorStatusMessage(Message message, Channel channel) throws Exception {
        try {
            String messageBody = new String(message.getBody());
            processSensorStatus(messageBody);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("Error processing sensor status message: {}", e.getMessage());
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    // 处理原始传感器数据
    private void processRawSensorData(String messageBody) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(messageBody);

        SensorData sensorData = new SensorData();
        
        // 修复字段映射：Python发送的是扁平结构，直接读取
        sensorData.setSequence(jsonNode.has("sequence") ? jsonNode.get("sequence").asLong() : null);
        sensorData.setGroa(jsonNode.has("groa") ? jsonNode.get("groa").asInt() : null);
        sensorData.setGrob(jsonNode.has("grob") ? jsonNode.get("grob").asInt() : null);
        sensorData.setDipmeter(jsonNode.has("dipmeter") ? jsonNode.get("dipmeter").asDouble() : null);
        sensorData.setCodee40(jsonNode.has("codee40") ? jsonNode.get("codee40").asInt() : null);
        sensorData.setMileage(jsonNode.has("mileage") ? jsonNode.get("mileage").asInt() : null);
        // 处理sleeper字段：float类型，代表电压
        sensorData.setSleeper(jsonNode.has("sleeper") ? jsonNode.get("sleeper").asDouble() : null);
        
        // 处理时间字段
        if (jsonNode.has("time")) {
            try {
                String timeStr = jsonNode.get("time").asText();
                // Python发送格式: "YYYY-MM-DD HH:MM:SS:mmm" 或 "YYYY-MM-DD HH:MM:SS.mmm"
                // 转换为Java LocalDateTime格式: "YYYY-MM-DDTHH:MM:SS"
                if (timeStr.contains(":")) {
                    // 移除毫秒部分，只保留到秒
                    if (timeStr.length() > 19) {
                        timeStr = timeStr.substring(0, 19);
                    }
                    // 将空格替换为T
                    timeStr = timeStr.replace(" ", "T");
                    sensorData.setStartTime(LocalDateTime.parse(timeStr));
                }
            } catch (Exception e) {
                log.warn("解析时间字段失败: {}, 错误: {}", jsonNode.get("time").asText(), e.getMessage());
            }
        }
        
        // 获取当前任务ID并设置
        Long jobId = jobIdManager.getCurrentJobId();
        if (jobId != null) {
            sensorData.setJobId(jobId);
        } else {
            log.warn("当前没有活动的任务ID，传感器数据将不关联任务");
        }

        log.debug("Received sensor data: {}", sensorData);

        // 将消息放入 BlockingQueue（用于前端展示）
        MESSAGE_QUEUE.offer(sensorData);

        // 批量持久化到数据库
        synchronized (batch) {
            batch.add(sensorData);
            if (batch.size() >= BATCH_SIZE) {
                sensorDataRepository.saveAll(batch);
                sensorDataRepository.flush();
                log.info("批量保存 {} 条原始传感器数据", batch.size());
                batch.clear();
            }
        }
    }

    // 处理几何结果数据
    private void processGeometryResult(String messageBody) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(messageBody);

        // 获取当前任务ID
        Long jobId = jobIdManager.getCurrentJobId();
        if (jobId == null) {
            log.warn("当前没有活动的任务ID，几何结果数据将不关联任务");
            // 仍然处理数据，但不持久化
        }

        // 创建用于前端展示的DTO对象
        GeometryResult geometryResult = new GeometryResult();
        geometryResult.setEncoder(jsonNode.has("encoder") ? jsonNode.get("encoder").asInt() : null);

        // 处理sensor_data部分
        JsonNode sensorDataNode = jsonNode.get("sensor_data");
        if (sensorDataNode != null) {
            GeometrySensorData sensorData = new GeometrySensorData();
            if (sensorDataNode.has("time")) {
                try {
                    String timeStr = sensorDataNode.get("time").asText();
                    // Python发送格式: "YYYY-MM-DD HH:MM:SS:mmm"
                    // 转换为Java LocalDateTime格式: "YYYY-MM-DDTHH:MM:SS"
                    if (timeStr.contains(":")) {
                        if (timeStr.length() > 19) {
                            timeStr = timeStr.substring(0, 19);
                        }
                        timeStr = timeStr.replace(" ", "T");
                        sensorData.setTime(LocalDateTime.parse(timeStr));
                    }
                } catch (Exception e) {
                    log.warn("解析传感器时间字段失败: {}, 错误: {}", sensorDataNode.get("time").asText(), e.getMessage());
                }
            }
            sensorData.setSequence(sensorDataNode.has("sequence") ? sensorDataNode.get("sequence").asLong() : null);
            sensorData.setGroa(sensorDataNode.has("groa") ? sensorDataNode.get("groa").asInt() : null);
            sensorData.setGrob(sensorDataNode.has("grob") ? sensorDataNode.get("grob").asInt() : null);
            sensorData.setDipmeter(sensorDataNode.has("dipmeter") ? sensorDataNode.get("dipmeter").asDouble() : null);
            sensorData.setAcc1(sensorDataNode.has("acc1") ? sensorDataNode.get("acc1").asDouble() : null);
            sensorData.setAcc2(sensorDataNode.has("acc2") ? sensorDataNode.get("acc2").asDouble() : null);
            sensorData.setAcc3(sensorDataNode.has("acc3") ? sensorDataNode.get("acc3").asDouble() : null);
            sensorData.setCodee40(sensorDataNode.has("codee40") ? sensorDataNode.get("codee40").asInt() : null);
            // 处理sleeper字段：float类型，代表电压
            sensorData.setSleeper(sensorDataNode.has("sleeper") ? sensorDataNode.get("sleeper").asDouble() : null);
            sensorData.setMileage(sensorDataNode.has("mileage") ? sensorDataNode.get("mileage").asInt() : null);
            sensorData.setCodee40A(sensorDataNode.has("codee40_a") ? sensorDataNode.get("codee40_a").asInt() : null);
            sensorData.setCodee40B(sensorDataNode.has("codee40_b") ? sensorDataNode.get("codee40_b").asInt() : null);
            sensorData.setImuAngleX(sensorDataNode.has("imu_angle_x") ? sensorDataNode.get("imu_angle_x").asDouble() : null);
            sensorData.setImuAngleY(sensorDataNode.has("imu_angle_y") ? sensorDataNode.get("imu_angle_y").asDouble() : null);
            sensorData.setImuAngleZ(sensorDataNode.has("imu_angle_z") ? sensorDataNode.get("imu_angle_z").asDouble(): null);
            sensorData.setImuAccX(sensorDataNode.has("imu_acc_x") ? sensorDataNode.get("imu_acc_x").asDouble() : null);
            sensorData.setImuAccY(sensorDataNode.has("imu_acc_y") ? sensorDataNode.get("imu_acc_y").asDouble() : null);
            sensorData.setImuAccZ(sensorDataNode.has("imu_acc_z") ? sensorDataNode.get("imu_acc_z").asDouble() : null);

            geometryResult.setSensorData(sensorData);
        }

        // 处理lsf01_level（可能是单个值或数组）
        JsonNode lsf01LevelNode = jsonNode.get("lsf01_level");
        if (lsf01LevelNode != null) {
            if (lsf01LevelNode.isArray()) {
                List<Double> lsf01Level = new ArrayList<>();
                for (JsonNode node : lsf01LevelNode) {
                    lsf01Level.add(node.asDouble());
                }
                geometryResult.setLsf01Level(lsf01Level);
            } else if (lsf01LevelNode.isNumber()) {
                // 如果是单个数值，转换为列表
                List<Double> lsf01Level = new ArrayList<>();
                lsf01Level.add(lsf01LevelNode.asDouble());
                geometryResult.setLsf01Level(lsf01Level);
            }
        }

        geometryResult.setTdf01Gauge(jsonNode.has("tdf01_gauge") ? jsonNode.get("tdf01_gauge").asDouble() : null);
        
        // 处理track_geometry（应该是数组，不是字符串）
        JsonNode trackGeoNode = jsonNode.get("track_geometry");
        if (trackGeoNode != null && trackGeoNode.isArray()) {
            geometryResult.setTrackGeometry(objectMapper.writeValueAsString(trackGeoNode));
        } else if (trackGeoNode != null) {
            geometryResult.setTrackGeometry(trackGeoNode.asText());
        }

        log.debug("Received geometry result: {}", geometryResult);

        // 将几何结果放入队列（用于前端展示）
        GEOMETRY_RESULT_QUEUE.offer(geometryResult);

        // 创建用于持久化的实体对象
        if (jobId != null) {
            GeometryResultEntity entity = new GeometryResultEntity();
            entity.setJobId(jobId);
            entity.setEncoder(jsonNode.has("encoder") ? jsonNode.get("encoder").asInt() : null);
            entity.setRailType(jsonNode.has("rail_type") ? jsonNode.get("rail_type").asInt() : null);
            
            // 处理lsf01_level（取第一个值，如果是数组）
            if (lsf01LevelNode != null) {
                if (lsf01LevelNode.isArray() && lsf01LevelNode.size() > 0) {
                    entity.setLsf01Level(lsf01LevelNode.get(0).asDouble());
                } else if (lsf01LevelNode.isNumber()) {
                    entity.setLsf01Level(lsf01LevelNode.asDouble());
                }
            }
            
            entity.setTdf01Gauge(jsonNode.has("tdf01_gauge") ? jsonNode.get("tdf01_gauge").asDouble() : null);
            
            // 处理track_geometry（存储为JSON字符串）
            if (trackGeoNode != null) {
                entity.setTrackGeometry(objectMapper.writeValueAsString(trackGeoNode));
            }
            
            // 处理磨损值
            JsonNode wearNode = jsonNode.get("wear_values");
            if (wearNode != null) {
                entity.setWearMile(wearNode.has("mile") ? wearNode.get("mile").asDouble() : null);
                entity.setHWearL(wearNode.has("h_wear_l") ? wearNode.get("h_wear_l").asDouble() : null);
                entity.setVWearL(wearNode.has("v_wear_l") ? wearNode.get("v_wear_l").asDouble() : null);
                entity.setHWearR(wearNode.has("h_wear_r") ? wearNode.get("h_wear_r").asDouble() : null);
                entity.setVWearR(wearNode.has("v_wear_r") ? wearNode.get("v_wear_r").asDouble() : null);
                entity.setWearAllL(wearNode.has("wear_all_l") ? wearNode.get("wear_all_l").asDouble() : null);
                entity.setWearAllR(wearNode.has("wear_all_r") ? wearNode.get("wear_all_r").asDouble() : null);
            }
            
            // 处理道岔相关字段
            entity.setPointName(jsonNode.has("point_name") ? jsonNode.get("point_name").asText() : null);
            entity.setDataCount(jsonNode.has("data_count") ? jsonNode.get("data_count").asInt() : null);
            entity.setAvgGuard(jsonNode.has("avg_guard") ? jsonNode.get("avg_guard").asDouble() : null);
            entity.setAvgBack(jsonNode.has("avg_back") ? jsonNode.get("avg_back").asDouble() : null);
            
            // 存储完整的sensor_data为JSON字符串
            if (sensorDataNode != null) {
                entity.setSensorData(objectMapper.writeValueAsString(sensorDataNode));
            }
            
            // 批量持久化到数据库
            synchronized (geometryBatch) {
                geometryBatch.add(entity);
                if (geometryBatch.size() >= GEOMETRY_BATCH_SIZE) {
                    geometryResultRepository.saveAll(geometryBatch);
                    geometryResultRepository.flush();
                    log.info("批量保存 {} 条几何结果数据", geometryBatch.size());
                    geometryBatch.clear();
                }
            }
        }
    }

    // 处理传感器状态数据
    // 注意：由于只在任务开始前接收一次传感器状态，所以立即保存，不使用批量保存
    private void processSensorStatus(String messageBody) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(messageBody);

        SensorStatus sensorStatus = new SensorStatus();
        
        // 处理时间字段
        if (jsonNode.has("time")) {
            try {
                String timeStr = jsonNode.get("time").asText();
                // Python发送格式: "YYYY-MM-DD HH:MM:SS:mmm" 或 "YYYY-MM-DD HH:MM:SS.mmm"
                // 转换为Java LocalDateTime格式
                if (timeStr.contains(":")) {
                    // 移除毫秒部分，只保留到秒
                    if (timeStr.length() > 19) {
                        timeStr = timeStr.substring(0, 19);
                    }
                    // 将空格替换为T
                    timeStr = timeStr.replace(" ", "T");
                    sensorStatus.setStatusTime(LocalDateTime.parse(timeStr));
                }
            } catch (Exception e) {
                log.warn("解析时间字段失败: {}, 错误: {}", jsonNode.get("time").asText(), e.getMessage());
                sensorStatus.setStatusTime(LocalDateTime.now());
            }
        } else {
            sensorStatus.setStatusTime(LocalDateTime.now());
        }
        
        // 处理任务ID（优先使用消息中的job_id，如果没有则从JobIdManager获取）
        if (jsonNode.has("job_id") && !jsonNode.get("job_id").isNull()) {
            sensorStatus.setJobId(jsonNode.get("job_id").asLong());
        } else {
            // 如果没有提供任务ID，尝试从JobIdManager获取
            Long currentJobId = jobIdManager.getCurrentJobId();
            if (currentJobId != null) {
                sensorStatus.setJobId(currentJobId);
            }
        }
        
        // 处理状态字
        sensorStatus.setStateWord(jsonNode.has("state_word") ? jsonNode.get("state_word").asInt() : null);
        
        // 处理各个设备状态（0-11位：陀螺A、陀螺B、倾角、编码器A、编码器B、IMU、INS、电子标签、点激光A、点激光B、超声A、超声B）
        sensorStatus.setGyroA(jsonNode.has("gyro_a") ? jsonNode.get("gyro_a").asInt() : null);
        sensorStatus.setGyroB(jsonNode.has("gyro_b") ? jsonNode.get("gyro_b").asInt() : null);
        sensorStatus.setDipmeter(jsonNode.has("dipmeter") ? jsonNode.get("dipmeter").asInt() : null);
        sensorStatus.setEncoderA(jsonNode.has("encoder_a") ? jsonNode.get("encoder_a").asInt() : null);
        sensorStatus.setEncoderB(jsonNode.has("encoder_b") ? jsonNode.get("encoder_b").asInt() : null);
        sensorStatus.setImu(jsonNode.has("imu") ? jsonNode.get("imu").asInt() : null);
        sensorStatus.setIns(jsonNode.has("ins") ? jsonNode.get("ins").asInt() : null);
        sensorStatus.setRfid(jsonNode.has("rfid") ? jsonNode.get("rfid").asInt() : null);
        sensorStatus.setPointLaserA(jsonNode.has("point_laser_a") ? jsonNode.get("point_laser_a").asInt() : null);
        sensorStatus.setPointLaserB(jsonNode.has("point_laser_b") ? jsonNode.get("point_laser_b").asInt() : null);
        sensorStatus.setUltrasonicA(jsonNode.has("ultrasonic_a") ? jsonNode.get("ultrasonic_a").asInt() : null);
        sensorStatus.setUltrasonicB(jsonNode.has("ultrasonic_b") ? jsonNode.get("ultrasonic_b").asInt() : null);
        
        // 计算所有设备是否正常
        boolean allOk = (sensorStatus.getGyroA() != null && sensorStatus.getGyroA() == 1) &&
                       (sensorStatus.getGyroB() != null && sensorStatus.getGyroB() == 1) &&
                       (sensorStatus.getDipmeter() != null && sensorStatus.getDipmeter() == 1) &&
                       (sensorStatus.getEncoderA() != null && sensorStatus.getEncoderA() == 1) &&
                       (sensorStatus.getEncoderB() != null && sensorStatus.getEncoderB() == 1) &&
                       (sensorStatus.getImu() != null && sensorStatus.getImu() == 1) &&
                       (sensorStatus.getIns() != null && sensorStatus.getIns() == 1) &&
                       (sensorStatus.getRfid() != null && sensorStatus.getRfid() == 1) &&
                       (sensorStatus.getPointLaserA() != null && sensorStatus.getPointLaserA() == 1) &&
                       (sensorStatus.getPointLaserB() != null && sensorStatus.getPointLaserB() == 1) &&
                       (sensorStatus.getUltrasonicA() != null && sensorStatus.getUltrasonicA() == 1) &&
                       (sensorStatus.getUltrasonicB() != null && sensorStatus.getUltrasonicB() == 1);
        sensorStatus.setAllOk(allOk);
        
        // 立即保存到数据库（因为只在任务开始前接收一次）
        sensorStatusRepository.save(sensorStatus);
        sensorStatusRepository.flush();
        log.info("已保存传感器状态数据，任务ID: {}, 所有设备正常: {}", sensorStatus.getJobId(), allOk);
    }

    // 最终批量写入剩余数据
    @PreDestroy
    public void flushRemainingData() {
        // 保存剩余的原始传感器数据
        synchronized (batch) {
            if (!batch.isEmpty()) {
                sensorDataRepository.saveAll(batch);
                sensorDataRepository.flush();
                log.info("Flushed remaining {} sensor data records", batch.size());
                batch.clear();
            }
        }
        
        // 保存剩余的几何结果数据
        synchronized (geometryBatch) {
            if (!geometryBatch.isEmpty()) {
                geometryResultRepository.saveAll(geometryBatch);
                geometryResultRepository.flush();
                log.info("Flushed remaining {} geometry result records", geometryBatch.size());
                geometryBatch.clear();
            }
        }
        
        // 传感器状态数据已经在接收时立即保存，这里不需要处理
    }
}

//    // 每三秒生成 100 条模拟数据
//    @Scheduled(fixedRate = 3000)
//    public void generateMockData() {
//        List<SensorData> mockBatch = new ArrayList<>();
//        for (int i = 0; i < 2; i++) {
//            SensorData sensorData = createMockSensorData();
//            mockBatch.add(sensorData);
//            MESSAGE_QUEUE.offer(sensorData); // 放入 BlockingQueue
//        }
//        System.out.println("Generated and queued 100 mock sensor data entries.");
//
//        // 可选：将模拟数据也存入数据库
//        synchronized (batch) {
//            batch.addAll(mockBatch);
//            if (batch.size() >= BATCH_SIZE) {
//                sensorDataRepository.saveAll(batch);
//                sensorDataRepository.flush();
//                batch.clear();
//            }
//        }
//    }
//
//    // 生成单条模拟 SensorData
//    private SensorData createMockSensorData() {
//        SensorData sensorData = new SensorData();
//        sensorData.setSequence(System.currentTimeMillis()); // 使用时间戳作为序列号
//        sensorData.setGA(random.nextDouble() * 10 - 5); // 模拟加速度 gA，范围 [-5, 5]
//        sensorData.setGB(random.nextDouble() * 10 - 5); // 模拟加速度 gB，范围 [-5, 5]
//        sensorData.setGC(random.nextDouble() * 10 - 5); // 模拟加速度 gC，范围 [-5, 5]
//        sensorData.setCnt(random.nextInt(100)); // 模拟计数器，范围 [0, 99]
//        sensorData.setDipmeter(random.nextDouble() * 90 - 45); // 模拟倾角计，范围 [-45, 45]
//        sensorData.setGroa(random.nextInt(360)); // 模拟角度 groa，范围 [0, 359]
//        sensorData.setGrob(random.nextInt(360)); // 模拟角度 grob，范围 [0, 359]
//        sensorData.setMileage(random.nextInt(10000)); // 模拟里程，范围 [0, 9999]
//        sensorData.setSleeper(random.nextInt(10)); // 模拟枕木数，范围 [0, 9]
//        sensorData.setStartTime(LocalDateTime.now());
//        log.info("Generated sensor data: {}", sensorData);
//        return sensorData;
//    }
