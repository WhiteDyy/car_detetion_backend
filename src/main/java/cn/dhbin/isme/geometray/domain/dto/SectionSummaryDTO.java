package cn.dhbin.isme.geometray.domain.dto;

import lombok.Data;

@Data
public class SectionSummaryDTO {
    /**
     * 项目
     */
    private String itemName;

    /**
     * 作业验收点数
     */
    private String jobPoints;

    /**
     * 作业验收延长
     */
    private String jobExtension;

    /**
     * 综合保养点数
     */
    private String compPoints;

    /**
     * 综合保养延长
     */
    private String compExtension;

    /**
     * 临时补修点数
     */
    private String tempPoints;

    /**
     * 临时补修延长
     */
    private String tempExtension;

    /**
     * 四级超限点数
     */
    private String level4Points;

    /**
     * 四级超限延长
     */
    private String level4Extension;

    /**
     * 扣分
     */
    private String deduction;

    /**
     * 百分比
     */
    private String percentage;
}
