package cn.dhbin.isme.geometray.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("section_summary_table")
public class SectionSummary {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("itemName")
    private String itemName;

    @TableField("jobPoints")
    private String jobPoints;

    @TableField("jobExtension")
    private String jobExtension;

    @TableField("compPoints")
    private String compPoints;

    @TableField("compExtension")
    private String compExtension;

    @TableField("tempPoints")
    private String tempPoints;

    @TableField("tempExtension")
    private String tempExtension;

    @TableField("level4Points")
    private String level4Points;

    @TableField("level4Extension")
    private String level4Extension;

    @TableField("deduction")
    private String deduction;

    @TableField("percentage")
    private String percentage;
}
