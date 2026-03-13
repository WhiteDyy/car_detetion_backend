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
 * 
 * 消息格式说明：
 * {
 *   "command": "start" | "stop",
 *   "jobId": 123,                    // 任务ID（数字）
 *   "taskName": "K123+456_检测任务"  // 任务名称（用于Python端数据目录命名）
 * }
 * 
 * 注意：jobId 是数据库中的任务ID，taskName 是任务的名称（jobName字段）
 * Python程序会使用 taskName 来命名数据保存的目录
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
     * 发送开始采集命令（无参数）
     * @return 是否发送成功
     */
    public boolean sendStartCommand() {
        return sendStartCommand(null, null);
    }
    
    /**
     * 发送开始采集命令（只带任务ID，向后兼容）
     * @param jobId 任务ID
     * @return 是否发送成功
     */
    public boolean sendStartCommand(Long jobId) {
        return sendStartCommand(jobId, null);
    }
    
    /**
     * 发送开始采集命令（带任务ID和任务名称）
     * 
     * @param jobId 任务ID - 数据库中的主键ID
     * @param taskName 任务名称 - Job实体的jobName字段，用于Python端目录命名
     * @return 是否发送成功
     * 
     * 重要说明：
     * - jobId: 用于关联数据库记录，便于后续数据查询和结果关联
     * - taskName: 用于Python端创建数据目录，格式为 "{taskName}_{timestamp}/"
     * - 这两个字段不要混淆：jobId是数字ID，taskName是用户可读的任务名称
     */
    public boolean sendStartCommand(Long jobId, String taskName) {
        try {
            Map<String, Object> command = new HashMap<>();
            command.put("command", "start");
            
            // 任务ID（数据库主键）
            if (jobId != null) {
                command.put("jobId", jobId);
            }
            
            // 任务名称（用于目录命名）
            // 注意：这里的taskName对应Job实体的jobName字段
            if (taskName != null && !taskName.trim().isEmpty()) {
                command.put("taskName", taskName.trim());
            }
            
            String message = objectMapper.writeValueAsString(command);
            rabbitTemplate.convertAndSend(CONTROL_QUEUE_NAME, message);
            log.info("已发送开始采集命令到队列: {}，任务ID: {}，任务名称: {}", 
                    CONTROL_QUEUE_NAME, jobId, taskName);
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

