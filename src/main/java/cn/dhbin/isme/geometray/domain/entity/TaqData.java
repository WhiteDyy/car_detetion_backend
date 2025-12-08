package cn.dhbin.isme.geometray.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("taq_table")
public class TaqData {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("number")
    private String number;

    @TableField("direction")
    private String direction;

    @TableField("standardValue")
    private String standardValue;

    @TableField("measurementValue")
    private String measurementValue;
}
