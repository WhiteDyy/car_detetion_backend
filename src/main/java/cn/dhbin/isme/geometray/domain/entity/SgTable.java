package cn.dhbin.isme.geometray.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sg_table")
public class SgTable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("point")
    private String point;

    @TableField("position")
    private String position;

    @TableField("direction")
    private String direction;

    @TableField("sleeper")
    private String sleeper;

    @TableField("paramName")
    private String paramName;

    @TableField("paramValue")
    private String paramValue;
}
