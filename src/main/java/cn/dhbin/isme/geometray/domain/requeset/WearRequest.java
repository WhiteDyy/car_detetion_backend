package cn.dhbin.isme.geometray.domain.requeset;

import cn.dhbin.isme.common.request.PageRequest;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WearRequest  extends PageRequest {
    private String mileage;
    private String leftVerticalWear;
    private String leftSideWear;
    private String leftTotalWear;
    private String rightVerticalWear;
    private String rightSideWear;
    private String rightTotalWear;
}
