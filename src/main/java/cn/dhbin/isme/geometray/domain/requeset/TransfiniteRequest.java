package cn.dhbin.isme.geometray.domain.requeset;

import cn.dhbin.isme.common.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransfiniteRequest extends PageRequest {
    private String mileage;
    private String gaugeMeasured;
    private String gaugeWarning;
    private String gaugeChangeRateMeasured;
    private String gaugeChangeRateWarning;
    private String levelMeasured;
    private String levelWarning;
    private String twistMeasured;
    private String twistWarning;
    private String alignmentRightMeasured;
    private String alignmentRightWarning;
    private String directionRightMeasured;
    private String directionRightWarning;
    private String versineRightMeasured;
    private String versineRightWarning;
    private String alignmentLeftMeasured;
    private String alignmentLeftWarning;
    private String directionLeftMeasured;
    private String directionLeftWarning;
    private String versineLeftMeasured;
    private String versineLeftWarning;
    private String operators;
    private String inspectionTime;
    private String responsiblePerson;
}
