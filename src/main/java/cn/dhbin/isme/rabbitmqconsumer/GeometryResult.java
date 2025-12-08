package cn.dhbin.isme.rabbitmqconsumer;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GeometryResult {
    private Integer encoder;
    private GeometrySensorData sensorData;
    private List<Double> lsf01Level;
    private Double tdf01Gauge;
    private String trackGeometry;
}