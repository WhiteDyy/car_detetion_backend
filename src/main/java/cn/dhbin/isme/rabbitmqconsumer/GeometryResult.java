package cn.dhbin.isme.rabbitmqconsumer;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
public class GeometryResult {
    private Integer encoder;
    private GeometrySensorData sensorData;
    private List<Double> lsf01Level;
    private Double tdf01Gauge;
    private JsonNode trackGeometry;
}