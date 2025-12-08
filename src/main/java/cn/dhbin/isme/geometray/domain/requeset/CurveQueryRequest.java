package cn.dhbin.isme.geometray.domain.requeset;


import cn.dhbin.isme.common.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class CurveQueryRequest extends PageRequest {
    private String lineNo;
    private String lineName;
    private String startMileage;
    private String endMileage;
    private String direction;
    private String curveDirection;
    private String gaugeType;
}
