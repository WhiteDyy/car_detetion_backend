package cn.dhbin.isme.geometray.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("condense_table")
public class CondenseTable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("actualMileage")
    private String actualMileage;

    @TableField("indicatedMileage")
    private String indicatedMileage;

    @TableField("actualGauge")
    private String actualGauge;

    @TableField("gaugeChangeRate")
    private String gaugeChangeRate;

    @TableField("actualLevel")
    private String actualLevel;

    @TableField("triangularPit")
    private String triangularPit;

    @TableField("rightHeight")
    private String rightHeight;

    @TableField("rightTrackDirection")
    private String rightTrackDirection;

    @TableField("leftHeight")
    private String leftHeight;

    @TableField("leftTrackDirection")
    private String leftTrackDirection;

    @TableField("leftTrackAdjustment")
    private String leftTrackAdjustment;

    @TableField("rightTrackAdjustment")
    private String rightTrackAdjustment;
}