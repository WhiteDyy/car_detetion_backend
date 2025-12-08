package cn.dhbin.isme.geometray.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("surface")
public class Surface {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("mileage")
    private BigDecimal mileage;

    @TableField("railSpallingWidth")
    private BigDecimal railSpallingWidth;

    @TableField("railSpallingDepth")
    private BigDecimal railSpallingDepth;

    @TableField("railSpallingQuantity")
    private Integer railSpallingQuantity;

    @TableField("railAbrasionWidth")
    private BigDecimal railAbrasionWidth;

    @TableField("railAbrasionDepth")
    private BigDecimal railAbrasionDepth;

    @TableField("railAbrasionQuantity")
    private Integer railAbrasionQuantity;

    @TableField("railCorrugationWidth")
    private BigDecimal railCorrugationWidth;

    @TableField("railCorrugationDepth")
    private BigDecimal railCorrugationDepth;

    @TableField("railCorrugationQuantity")
    private Integer railCorrugationQuantity;

    @TableField("railScalePatternWidth")
    private BigDecimal railScalePatternWidth;

    @TableField("railScalePatternQuantity")
    private Integer railScalePatternQuantity;

    @TableField("railCrackWidth")
    private BigDecimal railCrackWidth;

    @TableField("railCrackQuantity")
    private Integer railCrackQuantity;

    @TableField("fishplateCrackWidth")
    private BigDecimal fishplateCrackWidth;

    @TableField("fishplateCrackQuantity")
    private Integer fishplateCrackQuantity;

    @TableField("fishplateBoltDamageQuantity")
    private Integer fishplateBoltDamageQuantity;

    @TableField("fishplateWasherDamageWidth")
    private BigDecimal fishplateWasherDamageWidth;

    @TableField("fishplateWasherDamageDepth")
    private BigDecimal fishplateWasherDamageDepth;

    @TableField("fishplateWasherDamageQuantity")
    private Integer fishplateWasherDamageQuantity;

    @TableField("fastenerDamageQuantity")
    private Integer fastenerDamageQuantity;

    @TableField("fastenerDisplacementQuantity")
    private Integer fastenerDisplacementQuantity;

    @TableField("fastenerWasherDamageQuantity")
    private Integer fastenerWasherDamageQuantity;

    @TableField("fastenerScrewSpikeDamageQuantity")
    private Integer fastenerScrewSpikeDamageQuantity;

    @TableField("fastenerBaseplateDamageQuantity")
    private Integer fastenerBaseplateDamageQuantity;

    @TableField("sleeperCrackWidth")
    private BigDecimal sleeperCrackWidth;

    @TableField("sleeperCrackQuantity")
    private Integer sleeperCrackQuantity;

    @TableField("sleeperSpallingWidth")
    private BigDecimal sleeperSpallingWidth;

    @TableField("sleeperSpallingQuantity")
    private Integer sleeperSpallingQuantity;

    @TableField("ballastBedCrackWidth")
    private BigDecimal ballastBedCrackWidth;

    @TableField("ballastBedCrackQuantity")
    private Integer ballastBedCrackQuantity;

    @TableField("lineFeature")
    private String lineFeature;

    @TableField("inspectionTime")
    private LocalDateTime inspectionTime;

    @TableField("responsiblePerson")
    private String responsiblePerson;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}