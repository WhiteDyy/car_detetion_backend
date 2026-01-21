package cn.dhbin.isme.rabbitmqconsumer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorStatusRepository extends JpaRepository<SensorStatus, Long> {
    
    /**
     * 根据任务ID查询传感器状态记录
     */
    List<SensorStatus> findByJobId(Long jobId);
    
    /**
     * 根据任务ID和状态时间范围查询
     */
    List<SensorStatus> findByJobIdAndStatusTimeBetween(Long jobId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
}

