package cn.dhbin.isme.geometray.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 轨道检测数据传输对象（DTO），Lombok简化版
 */
@Data
@NoArgsConstructor // 生成一个无参构造函数
public class TrackDataPointDto {

    @JsonProperty("mileage")
    private double mileage;

    @JsonProperty("measurements")
    private Map<String, Double> measurements;

    @JsonProperty("tag")
    private TagDto tag;

    @JsonProperty("sleeper")
    private SleeperDto sleeper;


    @Data
    @NoArgsConstructor
    public static class TagDto {
        @JsonProperty("id")
        private long id;
    }


    @Data
    @NoArgsConstructor
    public static class SleeperDto {
        @JsonProperty("displayId")
        private int displayId;
    }
}