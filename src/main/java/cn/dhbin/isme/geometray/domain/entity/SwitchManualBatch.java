package cn.dhbin.isme.geometray.domain.entity;

import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@TableName("switch_manual_batch")
public class SwitchManualBatch implements Convert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchName;

    private String turnoutName;

    private LocalDateTime recordedAt;

    private Integer samplingFrequency;

    private String remark;

    private String nodesJson;

    private LocalDateTime createdAt;

    @TableField(exist = false)
    private List<Map<String, Object>> nodes;
}
