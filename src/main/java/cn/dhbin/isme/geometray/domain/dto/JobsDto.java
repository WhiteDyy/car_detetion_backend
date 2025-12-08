package cn.dhbin.isme.geometray.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class JobsDto {

    /**
     * 自增主键ID
     */
    private Long id;
    /**
     * 关联线路ID
     */
    private Long lineId;
    /**
     * 线路类型（正线/道岔）
     */
    private String lineType;
    /**
     * 行别（上行/下行）
     */
    private String direction;
    /**
     * 作业名称
     */
    private String jobName;
    /**
     * 作业开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    /**
     * 作业结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    /**
     * 设备ID
     */
    private String deviceId;
    /**
     * 操作人员
     */
    private String operator;
    /**
     * 作业描述
     */
    private String description;
    /**
     * 记录创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
