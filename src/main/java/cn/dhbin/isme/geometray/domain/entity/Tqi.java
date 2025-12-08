package cn.dhbin.isme.geometray.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tqi")
public class Tqi {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("mileage")
    private String mileage;

    @TableField("length")
    private String length;

    @TableField("gauge")
    private String gauge;

    @TableField("gaugeChangeRate")
    private String gaugeChangeRate;

    @TableField("level")
    private String level;

    @TableField("triangularPit")
    private String triangularPit;

    @TableField("rightHeight")
    private String rightHeight;

    @TableField("rightTrackDirection")
    private String rightTrackDirection;

    @TableField("leftHeight")
    private String leftHeight;

    @TableField("tqi")
    private String tqi;
}