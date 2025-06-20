package cn.dhbin.isme.rabbitmqconsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class RabbitMQConsumer implements ChannelAwareMessageListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SensorDataRepository sensorDataRepository;
    private final List<SensorData> batch = new ArrayList<>();
    private static final int BATCH_SIZE = 100;

    // 共享的 BlockingQueue，用于存储 SensorData
    public static final BlockingQueue<SensorData> MESSAGE_QUEUE = new LinkedBlockingQueue<>();

    private final Random random = new Random();

    @Autowired
    public RabbitMQConsumer(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    @RabbitListener(queues = "sensor_queue")
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            String messageBody = new String(message.getBody());
            JsonNode jsonNode = objectMapper.readTree(messageBody);

            SensorData sensorData = new SensorData();
            sensorData.setSequence(jsonNode.has("sequence") ? jsonNode.get("sequence").asLong() : null);
            sensorData.setGA(jsonNode.has("accel") && jsonNode.get("accel").has("g_a") ? jsonNode.get("accel").get("g_a").asDouble() : null);
            sensorData.setGB(jsonNode.has("accel") && jsonNode.get("accel").has("g_b") ? jsonNode.get("accel").get("g_b").asDouble() : null);
            sensorData.setGC(jsonNode.has("accel") && jsonNode.get("accel").has("g_c") ? jsonNode.get("accel").get("g_c").asDouble() : null);
            sensorData.setCnt(jsonNode.has("codeE40") && jsonNode.get("codeE40").has("cnt") ? jsonNode.get("codeE40").get("cnt").asInt() : null);
            sensorData.setDipmeter(jsonNode.has("dipmLSOX") && jsonNode.get("dipmLSOX").has("dipmeter") ? jsonNode.get("dipmLSOX").get("dipmeter").asDouble() : null);
            sensorData.setGroa(jsonNode.has("groaF98") && jsonNode.get("groaF98").has("groa") ? jsonNode.get("groaF98").get("groa").asInt() : null);
            sensorData.setGrob(jsonNode.has("groaF98") && jsonNode.get("groaF98").has("grob") ? jsonNode.get("groaF98").get("grob").asInt() : null);
            sensorData.setMileage(jsonNode.has("mileage") ? jsonNode.get("mileage").asInt() : null);
            sensorData.setSleeper(jsonNode.has("sleeper") ? jsonNode.get("sleeper").asInt() : null);
            sensorData.setStartTime(jsonNode.has("startTime") ? LocalDateTime.parse(jsonNode.get("startTime").asText()) : null);

            System.out.println("Received message: " + sensorData);

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

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    // 每三秒生成 100 条模拟数据
    @Scheduled(fixedRate = 3000)
    public void generateMockData() {
        List<SensorData> mockBatch = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            SensorData sensorData = createMockSensorData();
            mockBatch.add(sensorData);
            MESSAGE_QUEUE.offer(sensorData); // 放入 BlockingQueue
        }
        System.out.println("Generated and queued 100 mock sensor data entries.");

        // 可选：将模拟数据也存入数据库
        synchronized (batch) {
            batch.addAll(mockBatch);
            if (batch.size() >= BATCH_SIZE) {
                sensorDataRepository.saveAll(batch);
                sensorDataRepository.flush();
                batch.clear();
            }
        }
    }

    // 生成单条模拟 SensorData
    private SensorData createMockSensorData() {
        SensorData sensorData = new SensorData();
        sensorData.setSequence(System.currentTimeMillis()); // 使用时间戳作为序列号
        sensorData.setGA(random.nextDouble() * 10 - 5); // 模拟加速度 gA，范围 [-5, 5]
        sensorData.setGB(random.nextDouble() * 10 - 5); // 模拟加速度 gB，范围 [-5, 5]
        sensorData.setGC(random.nextDouble() * 10 - 5); // 模拟加速度 gC，范围 [-5, 5]
        sensorData.setCnt(random.nextInt(100)); // 模拟计数器，范围 [0, 99]
        sensorData.setDipmeter(random.nextDouble() * 90 - 45); // 模拟倾角计，范围 [-45, 45]
        sensorData.setGroa(random.nextInt(360)); // 模拟角度 groa，范围 [0, 359]
        sensorData.setGrob(random.nextInt(360)); // 模拟角度 grob，范围 [0, 359]
        sensorData.setMileage(random.nextInt(10000)); // 模拟里程，范围 [0, 9999]
        sensorData.setSleeper(random.nextInt(10)); // 模拟枕木数，范围 [0, 9]
        sensorData.setStartTime(LocalDateTime.now());
        log.info("Generated sensor data: {}", sensorData);
        return sensorData;
    }
}