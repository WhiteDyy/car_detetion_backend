package cn.dhbin.isme.geometray.domain.requeset;


import cn.dhbin.isme.common.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TqiRequest extends PageRequest {
    private String mileage;
    private String length;
    private String gauge;
    private String gaugeChangeRate;
    private String level;
    private String triangularPit;
    private String rightHeight;
    private String rightTrackDirection;
    private String leftHeight;
    private String tqi;
}
