package cn.dhbin.isme.geometray.domain.requeset;

import cn.dhbin.isme.common.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SgRequest extends PageRequest {
    private String point;

    private String position;

    private String direction;

    private String sleeper;

    private String paramName;

    private String paramValue;
}
