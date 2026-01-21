package cn.dhbin.isme.geometray.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 点云数据传输对象
 */
@Data
@NoArgsConstructor
public class PointCloudDto {

    /**
     * 传感器名称
     */
    @JsonProperty("sensorName")
    private String sensorName;

    /**
     * 点云数据列表，每个点包含 [x, y]
     */
    @JsonProperty("points")
    private List<double[]> points;

    /**
     * 传感器颜色（用于前端显示）
     */
    @JsonProperty("color")
    private String color;
}

