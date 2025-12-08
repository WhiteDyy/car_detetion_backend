package cn.dhbin.isme.rabbitmqconsumer;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue sensorQueue() {
        return new Queue("sensor_queue", true); // durable=true，与 Python 生产者一致
    }
}