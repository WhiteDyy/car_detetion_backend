package cn.dhbin.isme.geometray.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class SgDTO {
    private String point;

    private String position;

    private String direction;

    private String sleeper;

    private String paramName;

    private String paramValue;
}
