package cn.dhbin.isme.rabbitmqconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ控制命令生产者
 * 用于向Python程序发送开始/结束采集的命令
 */
@Slf4j
@Component
public class RabbitMQControlProducer {
    
    private static final String CONTROL_QUEUE_NAME = "collection_control";
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 发送开始采集命令
     * @return 是否发送成功
     */
    public boolean sendStartCommand() {
        return sendStartCommand(null);
    }
    
    /**
     * 发送开始采集命令（带任务ID）
     * @param jobId 任务ID
     * @return 是否发送成功
     */
    public boolean sendStartCommand(Long jobId) {
        try {
            Map<String, Object> command = new HashMap<>();
            command.put("command", "start");
            if (jobId != null) {
                command.put("jobId", jobId);
            }
            String message = objectMapper.writeValueAsString(command);
            rabbitTemplate.convertAndSend(CONTROL_QUEUE_NAME, message);
            log.info("已发送开始采集命令到队列: {}，任务ID: {}", CONTROL_QUEUE_NAME, jobId);
            return true;
        } catch (Exception e) {
            log.error("发送开始采集命令失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 发送结束采集命令
     * @return 是否发送成功
     */
    public boolean sendStopCommand() {
        try {
            Map<String, String> command = new HashMap<>();
            command.put("command", "stop");
            String message = objectMapper.writeValueAsString(command);
            rabbitTemplate.convertAndSend(CONTROL_QUEUE_NAME, message);
            log.info("已发送结束采集命令到队列: {}", CONTROL_QUEUE_NAME);
            return true;
        } catch (Exception e) {
            log.error("发送结束采集命令失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 发送自定义命令
     * @param command 命令内容
     * @return 是否发送成功
     */
    public boolean sendCommand(String command) {
        try {
            rabbitTemplate.convertAndSend(CONTROL_QUEUE_NAME, command);
            log.info("已发送命令到队列 {}: {}", CONTROL_QUEUE_NAME, command);
            return true;
        } catch (Exception e) {
            log.error("发送命令失败: {}", e.getMessage(), e);
            return false;
        }
    }
}

