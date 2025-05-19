package cn.dhbin.isme.geometray.domain.entity;


import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业信息表
 *
 * @author makejava
 */
@Data
@TableName("`Jobs`")
public class Jobs implements Convert {

    /**
     * 自增主键ID
     */
    @TableId(type = IdType.AUTO)
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
    private LocalDateTime startTime;

    /**
     * 作业结束时间
     */
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
    private LocalDateTime createdAt;

}
