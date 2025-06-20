package cn.dhbin.isme.geometray.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("wear_table")
public class WearTable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("mileage")
    private String mileage;
    @TableField("leftVerticalWear")
    private String leftVerticalWear;
    @TableField("leftSideWear")
    private String leftSideWear;
    @TableField("leftTotalWear")
    private String leftTotalWear;
    @TableField("rightVerticalWear")
    private String rightVerticalWear;
    @TableField("rightSideWear")
    private String rightSideWear;
    @TableField("rightTotalWear")
    private String rightTotalWear;
}
