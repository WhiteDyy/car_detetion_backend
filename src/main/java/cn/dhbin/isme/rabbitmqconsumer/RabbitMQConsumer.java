//package cn.dhbin.isme.rabbitmqconsumer;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
//import org.springframework.amqp.support.AmqpHeaders;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import com.rabbitmq.client.Channel;
//
//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Slf4j
//@Component
//public class RabbitMQConsumer implements ChannelAwareMessageListener {
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final SensorDataRepository sensorDataRepository;
//    private final List<SensorData> batch = new ArrayList<>();
//    private static final int BATCH_SIZE = 100;
//
//    // 共享的 BlockingQueue，用于存储 SensorData
//    public static final BlockingQueue<SensorData> MESSAGE_QUEUE = new LinkedBlockingQueue<>();
//
//    //通过队列传递算法结果,由sse推送到前端实时展示页面
////    public static final BlockingQueue<SensorData> MESSAGE_QUEUE = new LinkedBlockingQueue<>();
//
//    private final Random random = new Random();
//
//    @Autowired
//    public RabbitMQConsumer(SensorDataRepository sensorDataRepository) {
//        this.sensorDataRepository = sensorDataRepository;
//    }
//
//
//    /// /    @RabbitListener(queues = "sensor_queue")
////    @RabbitListener(queues = "raw_sensor_data")
////    @Override
////    public void onMessage(Message message, Channel channel) throws Exception {
////        try {
////            String messageBody = new String(message.getBody());
////            JsonNode jsonNode = objectMapper.readTree(messageBody);
////
////            SensorData sensorData = new SensorData();
////            sensorData.setSequence(jsonNode.has("sequence") ? jsonNode.get("sequence").asLong() : null);
////            sensorData.setGA(jsonNode.has("accel") && jsonNode.get("accel").has("g_a") ? jsonNode.get("accel").get("g_a").asDouble() : null);
////            sensorData.setGB(jsonNode.has("accel") && jsonNode.get("accel").has("g_b") ? jsonNode.get("accel").get("g_b").asDouble() : null);
////            sensorData.setGC(jsonNode.has("accel") && jsonNode.get("accel").has("g_c") ? jsonNode.get("accel").get("g_c").asDouble() : null);
////            sensorData.setCnt(jsonNode.has("codeE40") && jsonNode.get("codeE40").has("cnt") ? jsonNode.get("codeE40").get("cnt").asInt() : null);
////            sensorData.setDipmeter(jsonNode.has("dipmLSOX") && jsonNode.get("dipmLSOX").has("dipmeter") ? jsonNode.get("dipmLSOX").get("dipmeter").asDouble() : null);
////            sensorData.setGroa(jsonNode.has("groaF98") && jsonNode.get("groaF98").has("groa") ? jsonNode.get("groaF98").get("groa").asInt() : null);
////            sensorData.setGrob(jsonNode.has("groaF98") && jsonNode.get("groaF98").has("grob") ? jsonNode.get("groaF98").get("grob").asInt() : null);
////            sensorData.setMileage(jsonNode.has("mileage") ? jsonNode.get("mileage").asInt() : null);
////            sensorData.setSleeper(jsonNode.has("sleeper") ? jsonNode.get("sleeper").asInt() : null);
////            sensorData.setStartTime(jsonNode.has("startTime") ? LocalDateTime.parse(jsonNode.get("startTime").asText()) : null);
////
////            System.out.println("Received message: " + sensorData);
////
////            // 将消息放入 BlockingQueue
////            MESSAGE_QUEUE.offer(sensorData);
////
////            synchronized (batch) {
////                batch.add(sensorData);
////                if (batch.size() >= BATCH_SIZE) {
////                    sensorDataRepository.saveAll(batch);
////                    sensorDataRepository.flush();
////                    batch.clear();
////                }
////            }
////
////            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
////        } catch (Exception e) {
////            System.err.println("Error processing message: " + e.getMessage());
////            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
////        }
////    }
//
//
////
////    // 每三秒生成 100 条模拟数据
////    @Scheduled(fixedRate = 3000)
////    public void generateMockData() {
////        List<SensorData> mockBatch = new ArrayList<>();
////        for (int i = 0; i < 100; i++) {
////            SensorData sensorData = createMockSensorData();
////            mockBatch.add(sensorData);
////            MESSAGE_QUEUE.offer(sensorData); // 放入 BlockingQueue
////        }
////        System.out.println("Generated and queued 100 mock sensor data entries.");
////
////        // 可选：将模拟数据也存入数据库
////        synchronized (batch) {
////            batch.addAll(mockBatch);
////            if (batch.size() >= BATCH_SIZE) {
////                sensorDataRepository.saveAll(batch);
////                sensorDataRepository.flush();
////                batch.clear();
////            }
////        }
////    }
////
////    // 生成单条模拟 SensorData
////    private SensorData createMockSensorData() {
////        SensorData sensorData = new SensorData();
////        sensorData.setSequence(System.currentTimeMillis()); // 使用时间戳作为序列号
////        sensorData.setGA(random.nextDouble() * 10 - 5); // 模拟加速度 gA，范围 [-5, 5]
////        sensorData.setGB(random.nextDouble() * 10 - 5); // 模拟加速度 gB，范围 [-5, 5]
////        sensorData.setGC(random.nextDouble() * 10 - 5); // 模拟加速度 gC，范围 [-5, 5]
////        sensorData.setCnt(random.nextInt(100)); // 模拟计数器，范围 [0, 99]
////        sensorData.setDipmeter(random.nextDouble() * 90 - 45); // 模拟倾角计，范围 [-45, 45]
////        sensorData.setGroa(random.nextInt(360)); // 模拟角度 groa，范围 [0, 359]
////        sensorData.setGrob(random.nextInt(360)); // 模拟角度 grob，范围 [0, 359]
////        sensorData.setMileage(random.nextInt(10000)); // 模拟里程，范围 [0, 9999]
////        sensorData.setSleeper(random.nextInt(10)); // 模拟枕木数，范围 [0, 9]
////        sensorData.setStartTime(LocalDateTime.now());
////        log.info("Generated sensor data: {}", sensorData);
////        return sensorData;
////    }
//    @Override
//    public void onMessage(Message message, Channel channel) throws Exception {
//
//    }
//
//
//    // 统一监听多个队列
//    @RabbitListener(queues = {"raw_sensor_data", "rail_geometry_results"})
//    public void onMessage(Message message, Channel channel, @Header(AmqpHeaders.CONSUMER_QUEUE) String queueName) throws Exception {
//        try {
//            String messageBody = new String(message.getBody());
//            SensorData sensorData;
//
////            System.out.println("Received message from queue: " + queueName);
//            System.out.println("Received message from queue: " + messageBody);
//
////            // 根据队列名称选择不同的处理方式
////            switch (queueName) {
////                case "raw_sensor_data":
////                    // 直接反序列化整个消息
////                    sensorData = objectMapper.readValue(messageBody, SensorData.class);
////                    break;
////
////                case "rail_geometry_results":
////                    // 提取复杂结构中的sensor_data部分
////                    JsonNode jsonNode = objectMapper.readTree(messageBody);
////                    sensorData = objectMapper.treeToValue(jsonNode.get("sensor_data"), SensorData.class);
////
////                    // 可选：提取其他几何数据
////                    Integer encoder = jsonNode.get("encoder").asInt();
////                    JsonNode lsf01Level = jsonNode.get("lsf01_level");
////                    Double tdf01Gauge = jsonNode.get("tdf01_gauge").asDouble();
////
////                    // 可在此处添加对几何数据的处理
////                    System.out.println("Received encoder value: " + encoder);
////                    break;
////
////                default:
////                    // 未知队列类型
////                    throw new IllegalArgumentException("Unknown queue: " + queueName);
////            }
////
////            System.out.println("Processed sensor data: " + sensorData);
//
//            // 通用处理逻辑
////            processSensorData(sensorData);
//
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//        } catch (Exception e) {
//            System.err.println("Error processing message from queue " + queueName + ": " + e.getMessage());
//            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
//        }
//    }
//
//    // 通用的传感器数据处理逻辑
//    private void processSensorData(SensorData sensorData) {
//        // 将消息放入BlockingQueue
//        MESSAGE_QUEUE.offer(sensorData);
//
//        // 批处理写入
//        synchronized (batch) {
//            batch.add(sensorData);
//            if (batch.size() >= BATCH_SIZE) {
//                sensorDataRepository.saveAll(batch);
//                sensorDataRepository.flush();
//                batch.clear();
//                System.out.println("Flushed batch of " + BATCH_SIZE + " records");
//            }
//        }
//    }
//
//    // 最终批量写入剩余数据
//    @PreDestroy
//    public void flushRemainingData() {
//        synchronized (batch) {
//            if (!batch.isEmpty()) {
//                sensorDataRepository.saveAll(batch);
//                sensorDataRepository.flush();
//                System.out.println("Flushed remaining " + batch.size() + " records");
//                batch.clear();
//            }
//        }
//    }
//}

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
    private final List<SensorData> batch = new ArrayList<>();
    private static final int BATCH_SIZE = 100;

    // 共享的 BlockingQueue，用于存储 SensorData
    public static final BlockingQueue<SensorData> MESSAGE_QUEUE = new LinkedBlockingQueue<>();

    // 新增：用于存储几何结果的队列
    public static final BlockingQueue<GeometryResult> GEOMETRY_RESULT_QUEUE = new LinkedBlockingQueue<>();

    @Autowired
    public RabbitMQConsumer(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
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

    // 处理原始传感器数据
    private void processRawSensorData(String messageBody) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(messageBody);

        SensorData sensorData = new SensorData();
        sensorData.setSequence(jsonNode.has("sequence") ? jsonNode.get("sequence").asLong() : null);
        sensorData.setGA(jsonNode.has("accel") && jsonNode.get("accel").has("g_a") ? jsonNode.get("accel").get("g_a").asDouble() : null);
        sensorData.setGB(jsonNode.has("accel") && jsonNode.get("accel").has("g_b") ? jsonNode.get("accel").get("g_b").asDouble() : null);
        sensorData.setGC(jsonNode.has("accel") && jsonNode.get("accel").has("g_c") ? jsonNode.get("accel").get("g_c").asDouble() : null);
        sensorData.setCnt(jsonNode.has("codeE40") && jsonNode.get("codeE40").has("cnt") ? jsonNode.get("codeE40").get("cnt").asInt() : null);
        sensorData.setDipmeter(jsonNode.has("dipmLSOX") && jsonNode.get("dipmLSOX").has("d极meter") ? jsonNode.get("dipmLSOX").get("dipmeter").asDouble() : null);
        sensorData.setGroa(jsonNode.has("groaF98") && jsonNode.get("groaF98").has("groa") ? jsonNode.get("groaF98").get("groa").asInt() : null);
        sensorData.setGrob(jsonNode.has("groaF98") && jsonNode.get("groaF98").has("grob") ? jsonNode.get("groaF98").get("grob").asInt() : null);
        sensorData.setMileage(jsonNode.has("mileage") ? jsonNode.get("mileage").asInt() : null);
        sensorData.setSleeper(jsonNode.has("sleeper") ? jsonNode.get("sleeper").asInt() : null);
        sensorData.setStartTime(jsonNode.has("startTime") ? LocalDateTime.parse(jsonNode.get("startTime").asText()) : null);

        log.debug("Received sensor data: {}", sensorData);

        // 将消息放入 BlockingQueue
        MESSAGE_QUEUE.offer(sensorData);

        synchronized (batch) {
            batch.add(sensorData);
            if (batch.size() >= BATCH_SIZE) {
                sensorDataRepository.saveAll(batch);
                sensorDataRepository.flush();
                batch.clear();
            }
        }
    }

    // 处理几何结果数据
    private void processGeometryResult(String messageBody) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(messageBody);

        GeometryResult geometryResult = new GeometryResult();
        geometryResult.setEncoder(jsonNode.get("encoder").asInt());

        // 处理sensor_data部分
        JsonNode sensorDataNode = jsonNode.get("sensor_data");
        if (sensorDataNode != null) {
            GeometrySensorData sensorData = new GeometrySensorData();
            sensorData.setTime(sensorDataNode.has("time") ? LocalDateTime.parse(sensorDataNode.get("time").asText().replace(" ", "T")) : null);
            sensorData.setSequence(sensorDataNode.has("sequence") ? sensorDataNode.get("sequence").asLong() : null);
            sensorData.setGroa(sensorDataNode.has("groa") ? sensorDataNode.get("groa").asInt() : null);
            sensorData.setGrob(sensorDataNode.has("grob") ? sensorDataNode.get("grob").asInt() : null);
            sensorData.setDipmeter(sensorDataNode.has("dipmeter") ? sensorDataNode.get("dipmeter").asDouble() : null);
            sensorData.setAcc1(sensorDataNode.has("acc1") ? sensorDataNode.get("acc1").asDouble() : null);
            sensorData.setAcc2(sensorDataNode.has("acc2") ? sensorDataNode.get("acc2").asDouble() : null);
            sensorData.setAcc3(sensorDataNode.has("acc3") ? sensorDataNode.get("acc3").asDouble() : null);
            sensorData.setCodee40(sensorDataNode.has("codee40") ? sensorDataNode.get("codee40").asInt() : null);
            sensorData.setSleeper(sensorDataNode.has("sleeper") ? sensorDataNode.get("sleeper").asInt() : null);
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

        // 处理lsf01_level数组
        JsonNode lsf01LevelNode = jsonNode.get("lsf01_level");
        if (lsf01LevelNode != null && lsf01LevelNode.isArray()) {
            List<Double> lsf01Level = new ArrayList<>();
            for (JsonNode node : lsf01LevelNode) {
                lsf01Level.add(node.asDouble());
            }
            geometryResult.setLsf01Level(lsf01Level);
        }

        geometryResult.setTdf01Gauge(jsonNode.has("tdf01_gauge") ? jsonNode.get("tdf01_gauge").asDouble() : null);
        geometryResult.setTrackGeometry(jsonNode.has("track_geometry") ? jsonNode.get("track_geometry").asText() : null);

        log.debug("Received geometry result: {}", geometryResult);

        // 将几何结果放入队列
        GEOMETRY_RESULT_QUEUE.offer(geometryResult);
    }

    // 最终批量写入剩余数据
    @PreDestroy
    public void flushRemainingData() {
        synchronized (batch) {
            if (!batch.isEmpty()) {
                sensorDataRepository.saveAll(batch);
                sensorDataRepository.flush();
                log.info("Flushed remaining {} records", batch.size());
                batch.clear();
            }
        }
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
