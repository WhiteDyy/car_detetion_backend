package cn.dhbin.isme.geometray.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("transfinite_table")
public class TransfiniteTable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("mileage")
    private String mileage;
    @TableField("gaugeMeasured")
    private String gaugeMeasured;
    @TableField("gaugeWarning")
    private String gaugeWarning;
    @TableField("gaugeChangeRateMeasured")
    private String gaugeChangeRateMeasured;
    @TableField("gaugeChangeRateWarning")
    private String gaugeChangeRateWarning;
    @TableField("levelMeasured")
    private String levelMeasured;
    @TableField("levelWarning")
    private String levelWarning;
    @TableField("twistMeasured")
    private String twistMeasured;
    @TableField("twistWarning")
    private String twistWarning;
    @TableField("alignmentRightMeasured")
    private String alignmentRightMeasured;
    @TableField("alignmentRightWarning")
    private String alignmentRightWarning;
    @TableField("directionRightMeasured")
    private String directionRightMeasured;
    @TableField("directionRightWarning")
    private String directionRightWarning;
    @TableField("versineRightMeasured")
    private String versineRightMeasured;
    @TableField("versineRightWarning")
    private String versineRightWarning;
    @TableField("alignmentLeftMeasured")
    private String alignmentLeftMeasured;
    @TableField("alignmentLeftWarning")
    private String alignmentLeftWarning;
    @TableField("directionLeftMeasured")
    private String directionLeftMeasured;
    @TableField("directionLeftWarning")
    private String directionLeftWarning;
    @TableField("versineLeftMeasured")
    private String versineLeftMeasured;
    @TableField("versineLeftWarning")
    private String versineLeftWarning;
    @TableField("operators")
    private String operators;
    @TableField("inspectionTime")
    private String inspectionTime;
    @TableField("responsiblePerson")
    private String responsiblePerson;
}