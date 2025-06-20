package cn.dhbin.isme.geometray.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("olw_table")
public class Olw {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("mileage")
    private String mileage;

    @TableField("sleeperNo")
    private String sleeperNo;

    @TableField("heightDesignDeviation")
    private String heightDesignDeviation;

    @TableField("trackDesignDeviation")
    private String trackDesignDeviation;

    @TableField("leftHeightActual")
    private String leftHeightActual;

    @TableField("leftHeightDeviation")
    private String leftHeightDeviation;

    @TableField("rightHeightActual")
    private String rightHeightActual;

    @TableField("rightHeightDeviation")
    private String rightHeightDeviation;

    @TableField("leftTrackActual")
    private String leftTrackActual;

    @TableField("leftTrackDeviation")
    private String leftTrackDeviation;

    @TableField("rightTrackActual")
    private String rightTrackActual;

    @TableField("rightTrackDeviation")
    private String rightTrackDeviation;
}