package cn.dhbin.isme.geometray.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class CurveQueryDTO {
    private String lineNo;
    private String direction;
    private String lineName;
    private String startMileage;
    private String endMileage;
    private String curveDirection;
    private String curveRadius;
    private String turningAngle;
    private String gaugeType;
    private String gaugeWidening;
    private String superelevation;
    private String gradientRate;
    private String startTangentLength;
    private String startTransitionLength;
    private String endTransitionLength;
    private String endTangentLength;
    private String totalCurveLength;
    private String averageSpeed;
}
