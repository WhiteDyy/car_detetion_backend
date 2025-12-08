package cn.dhbin.isme.geometray.domain.dto;

import lombok.Data;

/**
 * 曲线检查信息DTO
 */
@Data
public class CurseInspectDTO {
    /**
     * 序号
     */
    private String indexs;

    /**
     * 测点里程 (km)
     */
    private String mileage;

    /**
     * 计划正矢 (mm)
     */
    private String plannedVector;

    /**
     * 实测正矢 (mm)
     */
    private String actualVector;

    /**
     * 正矢差 (mm)
     */
    private String vectorDifference;

    /**
     * 拨量 (mm)
     */
    private String adjustmentAmount;

    /**
     * 拨后正矢 (mm)
     */
    private String postAdjustmentVector;

    /**
     * 实测超高 (mm)
     */
    private String actualSuperelevation;

    /**
     * 超高差值 (mm)
     */
    private String superelevationDifference;

    /**
     * 超高顺坡率 (‰)
     */
    private String superelevationGradient;

    /**
     * 轨距 (mm)
     */
    private String gauge;

    /**
     * 轨距千分率 (‰)
     */
    private String gaugePermillage;
}