package cn.dhbin.isme.rabbitmqconsumer;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
@Data
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sequence;

    @Column(name = "g_a")
    private Double gA;

    @Column(name = "g_b")
    private Double gB;

    @Column(name = "g_c")
    private Double gC;

    private Integer cnt;

    private Double dipmeter;

    private Integer groa;

    private Integer grob;

    private Integer mileage;

    private Integer sleeper;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;


    @Override
    public String toString() {
        return "SensorData{" +
                "id=" + id +
                ", sequence=" + sequence +
                ", gA=" + gA +
                ", gB=" + gB +
                ", gC=" + gC +
                ", cnt=" + cnt +
                ", dipmeter=" + dipmeter +
                ", groa=" + groa +
                ", grob=" + grob +
                ", mileage=" + mileage +
                ", sleeper=" + sleeper +
                ", startTime=" + startTime +
                '}';
    }
}