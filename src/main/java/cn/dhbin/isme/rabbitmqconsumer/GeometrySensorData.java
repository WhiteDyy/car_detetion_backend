package cn.dhbin.isme.rabbitmqconsumer;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GeometrySensorData {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime time;
    private Long sequence;
    private Integer groa;
    private Integer grob;
    private Double dipmeter;
    private Double acc1;
    private Double acc2;
    private Double acc3;
    private Integer codee40;
    private Double sleeper;
    private Integer mileage;
    private Integer codee40A;
    private Integer codee40B;
    private Double imuAngleX;
    private Double imuAngleY;
    private Double imuAngleZ;
    private Double imuAccX;
    private Double imuAccY;
    private Double imuAccZ;
}