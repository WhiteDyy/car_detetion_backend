package cn.dhbin.isme.rabbitmqconsumer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    
    /**
     * 根据任务ID查询所有原始传感器数据
     * @param jobId 任务ID
     * @return 传感器数据列表
     */
    List<SensorData> findByJobId(Long jobId);
    
    /**
     * 根据任务ID删除所有原始传感器数据
     * @param jobId 任务ID
     */
    void deleteByJobId(Long jobId);
}
