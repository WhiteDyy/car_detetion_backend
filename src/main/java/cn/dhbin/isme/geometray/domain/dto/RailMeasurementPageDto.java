package cn.dhbin.isme.geometray.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class RailMeasurementPageDto {
    private Long jobId;
    private String jobName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private String deviceId;
    private String operator;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private Long railMeasurementId;
    private String lineType;
    private String lineName;
    private String direction;
    private Double mileage;
    private String rfidTag;
    private String sleeperId;
    private String pointId;
    private String measurePosition;
    private String trackType;
    private String measureParam;
    private Double measureValue;
    private Double designValue;
    private Double gauge;
    private Double gaugeChangeRate;
    private Double leftHeight;
    private Double rightHeight;
    private Double leftDirection;
    private Double rightDirection;
    private Double horizontal;
    private Double trianglePit;
    private Double verticalWear;
    private Double lateralWear;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime measurementTime;
}
