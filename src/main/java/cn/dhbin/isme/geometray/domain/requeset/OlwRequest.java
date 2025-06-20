package cn.dhbin.isme.geometray.domain.requeset;

import cn.dhbin.isme.common.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OlwRequest extends PageRequest {
    private String mileage;

    private String sleeperNo;

    private String heightDesignDeviation;

    private String trackDesignDeviation;

    private String leftHeightActual;

    private String leftHeightDeviation;

    private String rightHeightActual;

    private String rightHeightDeviation;

    private String leftTrackActual;

    private String leftTrackDeviation;

    private String rightTrackActual;

    private String rightTrackDeviation;
}
