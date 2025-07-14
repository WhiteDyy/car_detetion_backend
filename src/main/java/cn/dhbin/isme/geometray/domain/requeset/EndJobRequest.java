package cn.dhbin.isme.geometray.domain.requeset;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EndJobRequest {

    /**
     * 关联作业ID
     */
    private Long jobId;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 结束站点
     */
    private String endStation;


    /**
     * 记录状态（进行中/已完成）
     */
    private String status;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

}