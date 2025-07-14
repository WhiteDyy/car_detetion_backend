package cn.dhbin.isme.geometray.domain.requeset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StartJobRequest {
    /**
     * 关联作业ID
     */
    private Long jobId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 开始站点
     */
    private String startStation;

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

}
