package cn.dhbin.isme.rabbitmqconsumer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeometryResultRepository extends JpaRepository<GeometryResultEntity, Long> {
    
    /**
     * 根据任务ID查询所有几何结果
     * @param jobId 任务ID
     * @return 几何结果列表
     */
    List<GeometryResultEntity> findByJobId(Long jobId);
    
    /**
     * 根据任务ID删除所有几何结果
     * @param jobId 任务ID
     */
    void deleteByJobId(Long jobId);
}

