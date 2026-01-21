package cn.dhbin.isme.rabbitmqconsumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 任务ID管理器
 * 用于维护当前正在执行的任务ID
 */
@Slf4j
@Component
public class JobIdManager {
    
    /**
     * 当前正在执行的任务ID
     * 使用AtomicLong确保线程安全
     */
    private final AtomicLong currentJobId = new AtomicLong(0);
    
    /**
     * 设置当前任务ID
     * @param jobId 任务ID
     */
    public void setCurrentJobId(Long jobId) {
        if (jobId != null && jobId > 0) {
            currentJobId.set(jobId);
            log.info("设置当前任务ID: {}", jobId);
        } else {
            log.warn("尝试设置无效的任务ID: {}", jobId);
        }
    }
    
    /**
     * 获取当前任务ID
     * @return 当前任务ID，如果没有则返回null
     */
    public Long getCurrentJobId() {
        long jobId = currentJobId.get();
        return jobId > 0 ? jobId : null;
    }
    
    /**
     * 清除当前任务ID（任务结束时调用）
     */
    public void clearCurrentJobId() {
        long oldJobId = currentJobId.getAndSet(0);
        if (oldJobId > 0) {
            log.info("清除当前任务ID: {}", oldJobId);
        }
    }
    
    /**
     * 检查是否有正在执行的任务
     * @return true如果有正在执行的任务
     */
    public boolean hasActiveJob() {
        return currentJobId.get() > 0;
    }
}

