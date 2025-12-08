package cn.dhbin.isme.geometray.domain.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("curse_query_table")
public class CurveQuery {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("lineNo")
    private String lineNo;
    @TableField("direction")
    private String direction;
    @TableField("lineName")
    private String lineName;
    @TableField("startMileage")
    private String startMileage;
    @TableField("endMileage")
    private String endMileage;
    @TableField("curveDirection")
    private String curveDirection;
    @TableField("curveRadius")
    private String curveRadius;
    @TableField("turningAngle")
    private String turningAngle;
    @TableField("gaugeType")
    private String gaugeType;
    @TableField("gaugeWidening")
    private String gaugeWidening;
    @TableField("superelevation")
    private String superelevation;
    @TableField("gradientRate")
    private String gradientRate;
    @TableField("startTangentLength")
    private String startTangentLength;
    @TableField("startTransitionLength")
    private String startTransitionLength;
    @TableField("endTransitionLength")
    private String endTransitionLength;
    @TableField("endTangentLength")
    private String endTangentLength;
    @TableField("totalCurveLength")
    private String totalCurveLength;
    @TableField("averageSpeed")
    private String averageSpeed;
}
