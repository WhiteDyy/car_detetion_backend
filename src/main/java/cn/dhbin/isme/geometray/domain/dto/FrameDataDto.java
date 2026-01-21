package cn.dhbin.isme.geometray.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 帧数据传输对象
 */
@Data
@NoArgsConstructor
public class FrameDataDto {

    /**
     * 帧号
     */
    @JsonProperty("frameNumber")
    private Integer frameNumber;

    /**
     * 所有传感器的点云数据
     */
    @JsonProperty("sensors")
    private List<PointCloudDto> sensors;
}

