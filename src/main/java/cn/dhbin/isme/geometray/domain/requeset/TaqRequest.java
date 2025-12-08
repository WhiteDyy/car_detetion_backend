package cn.dhbin.isme.geometray.domain.requeset;

import cn.dhbin.isme.common.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaqRequest extends PageRequest {
    private String number;

    private String direction;

    private String standardValue;

    private String measurementValue;
}
