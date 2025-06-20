package cn.dhbin.isme.geometray.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SurfaceDTO {
    private BigDecimal mileage;

    private BigDecimal railSpallingWidth;

    private BigDecimal railSpallingDepth;

    private Integer railSpallingQuantity;

    private BigDecimal railAbrasionWidth;

    private BigDecimal railAbrasionDepth;

    private Integer railAbrasionQuantity;

    private BigDecimal railCorrugationWidth;

    private BigDecimal railCorrugationDepth;

    private Integer railCorrugationQuantity;

    private BigDecimal railScalePatternWidth;

    private Integer railScalePatternQuantity;

    private BigDecimal railCrackWidth;

    private Integer railCrackQuantity;

    private BigDecimal fishplateCrackWidth;

    private Integer fishplateCrackQuantity;

    private Integer fishplateBoltDamageQuantity;

    private BigDecimal fishplateWasherDamageWidth;

    private BigDecimal fishplateWasherDamageDepth;

    private Integer fishplateWasherDamageQuantity;

    private Integer fastenerDamageQuantity;

    private Integer fastenerDisplacementQuantity;

    private Integer fastenerWasherDamageQuantity;

    private Integer fastenerScrewSpikeDamageQuantity;

    private Integer fastenerBaseplateDamageQuantity;

    private BigDecimal sleeperCrackWidth;

    private Integer sleeperCrackQuantity;

    private BigDecimal sleeperSpallingWidth;

    private Integer sleeperSpallingQuantity;

    private BigDecimal ballastBedCrackWidth;

    private Integer ballastBedCrackQuantity;

    private String lineFeature;

    private LocalDateTime inspectionTime;

    private String responsiblePerson;
}
