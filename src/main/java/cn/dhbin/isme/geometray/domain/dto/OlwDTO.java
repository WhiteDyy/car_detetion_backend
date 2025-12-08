package cn.dhbin.isme.geometray.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class OlwDTO {
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
