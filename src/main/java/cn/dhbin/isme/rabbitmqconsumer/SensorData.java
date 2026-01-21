//package cn.dhbin.isme.rabbitmqconsumer;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import jakarta.persistence.*;
//import lombok.Data;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "sensor_data")
//@Data
//public class SensorData {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "time")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
//    private LocalDateTime time;
//
//    private Long sequence;
//    private Integer groa;
//    private Integer grob;
//    private Double dipmeter;
//
//    @Column(name = "acc1")
//    private Double acc1;
//
//    @Column(name = "acc2")
//    private Double acc2;
//
//    @Column(name = "acc3")
//    private Double acc3;
//
//    @Column(name = "codee40")
//    private Integer codee40;
//
//    private Integer sleeper;
//    private Integer mileage;
//
//    @Column(name = "codee40_a")
//    private Integer codee40A;
//
//    @Column(name = "codee40_b")
//    private Integer codee40B;
//
//    @Column(name = "imu_angle_x")
//    private Double imuAngleX;
//
//    @Column(name = "imu_angle_y")
//    private Double imuAngleY;
//
//    @Column(name = "imu_angle_z")
//    private Double imuAngleZ;
//
//    @Column(name = "imu_acc_x")
//    private Double imuAccX;
//
//    @Column(name = "imu_acc_y")
//    private Double imuAccY;
//
//    @Column(name = "imu_acc_z")
//    private Double imuAccZ;
//
//    @Override
//    public String toString() {
//        return "SensorData{" +
//                "id=" + id +
//                ", time=" + time +
//                ", sequence=" + sequence +
//                ", groa=" + groa +
//                ", grob=" + grob +
//                ", dipmeter=" + dipmeter +
//                ", acc1=" + acc1 +
//                ", acc2=" + acc2 +
//                ", acc3=" + acc3 +
//                ", codee40=" + codee40 +
//                ", sleeper=" + sleeper +
//                ", mileage=" + mileage +
//                ", codee40A=" + codee40A +
//                ", codee40B=" + codee40B +
//                ", imuAngleX=" + imuAngleX +
//                ", imuAngleY=" + imuAngleY +
//                ", imuAngleZ=" + imuAngleZ +
//                ", imuAccX=" + imuAccX +
//                ", imuAccY=" + imuAccY +
//                ", imuAccZ=" + imuAccZ +
//                '}';
//    }
//}

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

    private Double sleeper;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 关联的任务ID
     */
    @Column(name = "job_id")
    private Long jobId;
    
    /**
     * 编码器值（兼容字段，从codee40映射）
     */
    @Column(name = "codee40")
    private Integer codee40;

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