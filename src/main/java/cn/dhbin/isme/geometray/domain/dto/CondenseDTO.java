package cn.dhbin.isme.geometray.domain.dto;

import lombok.Data;

/**
 * 轨道测量信息DTO
 */
@Data
public class CondenseDTO {
    /**
     * 实测里程 (km)
     */
    private String actualMileage;

    /**
     * 标示里程 (km)
     */
    private String indicatedMileage;

    /**
     * 实测轨距 (mm)
     */
    private String actualGauge;

    /**
     * 轨距变化率 (mm)
     */
    private String gaugeChangeRate;

    /**
     * 实测水平 (mm)
     */
    private String actualLevel;

    /**
     * 三角坑 (mm)
     */
    private String triangularPit;

    /**
     * 实测右高低 (mm)
     */
    private String rightHeight;

    /**
     * 实测右轨向 (正矢) (mm)
     */
    private String rightTrackDirection;

    /**
     * 实测左高低 (mm)
     */
    private String leftHeight;

    /**
     * 实测左轨向 (正矢) (mm)
     */
    private String leftTrackDirection;

    /**
     * 左轨横向调整量 (mm)
     */
    private String leftTrackAdjustment;

    /**
     * 右轨横向调整量 (mm)
     */
    private String rightTrackAdjustment;
}