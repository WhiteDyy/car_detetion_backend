package cn.dhbin.isme.geometray.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`job_record`")
public class JobRecord implements Convert {
    /**
     * 自增主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联作业ID
     */
    private Long jobId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 开始站点
     */
    private String startStation;

    /**
     * 结束站点
     */
    private String endStation;

    /**
     * 操作人员
     */
    private String operator;

    /**
     * 记录状态（进行中/已完成）
     */
    private String jobStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;


}
