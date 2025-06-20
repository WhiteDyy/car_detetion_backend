package cn.dhbin.isme.geometray.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("curseinspect_table")
public class CurseInspect {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("indexs")
    private String indexs;

    @TableField("mileage")
    private String mileage;

    @TableField("plannedVector")
    private String plannedVector;

    @TableField("actualVector")
    private String actualVector;

    @TableField("vectorDifference")
    private String vectorDifference;

    @TableField("adjustmentAmount")
    private String adjustmentAmount;

    @TableField("postAdjustmentVector")
    private String postAdjustmentVector;

    @TableField("actualSuperelevation")
    private String actualSuperelevation;

    @TableField("superelevationDifference")
    private String superelevationDifference;

    @TableField("superelevationGradient")
    private String superelevationGradient;

    @TableField("gauge")
    private String gauge;

    @TableField("gaugePermillage")
    private String gaugePermillage;
}