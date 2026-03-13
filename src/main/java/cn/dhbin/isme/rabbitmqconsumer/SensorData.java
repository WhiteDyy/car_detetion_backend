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
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 传感器数据实体类
 * 
 * 新协议字段映射说明：
 * - Python发送: time, sequence, groa, grob, codee40, codee41, codee42, dipmeter, 
 *              gaccel_0, gaccel_1, gaccel_2, gaccel_3, length, imu_*, ins_*
 * - 前端期望: sequence, groa, grob, dipmeter, ga, gb, gc, cnt, startTime
 * 
 * 字段映射：
 * - gaccel_0 (点激光右) -> ga
 * - gaccel_1 (点激光左) -> gb
 * - gaccel_2 (超声右) -> gc
 * - codee40 (编码器右) -> cnt, codee40
 * - length (里程) -> mileage
 */
@Entity
@Table(name = "sensor_data")
@Data
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sequence;

    /**
     * 加速度/点激光A（新协议: gaccel_0 点激光右）
     * 前端显示为"横向加速度"
     */
    @Column(name = "g_a")
    @JsonProperty("ga")
    private Double gA;

    /**
     * 加速度/点激光B（新协议: gaccel_1 点激光左）
     * 前端显示为"横移加速度"
     */
    @Column(name = "g_b")
    @JsonProperty("gb")
    private Double gB;

    /**
     * 加速度/超声A（新协议: gaccel_2 超声右）
     * 前端显示为"沉浮加速度"
     */
    @Column(name = "g_c")
    @JsonProperty("gc")
    private Double gC;

    /**
     * 编码器计数值（新协议: codee40 编码器右）
     * 前端图表X轴使用
     */
    private Integer cnt;

    /**
     * 倾角计
     */
    private Double dipmeter;

    /**
     * 陀螺仪A（新协议: 陀螺Z）
     * 前端显示为"点头陀螺"
     */
    private Integer groa;

    /**
     * 陀螺仪B（新协议: 陀螺X）
     * 前端显示为"摇头陀螺"
     */
    private Integer grob;

    /**
     * 里程（新协议: length字段）
     */
    private Integer mileage;

    /**
     * 轨枕计数（旧协议字段，新协议不再使用）
     */
    private Double sleeper;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 关联的任务ID
     */
    @Column(name = "job_id")
    private Long jobId;
    
    /**
     * 编码器值（新协议: codee40 编码器右）
     */
    @Column(name = "codee40")
    private Integer codee40;

    // ========= 以下为仅用于前端展示的瞬时字段，不参与数据库持久化 =========

    /**
     * 编码器左（新协议: codee41）
     */
    @Transient
    @JsonProperty("codee41")
    private Integer codee41;

    /**
     * 编码器对齐（新协议: codee42）
     */
    @Transient
    @JsonProperty("codee42")
    private Integer codee42;

    /**
     * 原始点激光/超声通道（新协议: gaccel_0 ~ gaccel_3）
     */
    @Transient
    @JsonProperty("gaccel_0")
    private Double gaccel0;

    @Transient
    @JsonProperty("gaccel_1")
    private Double gaccel1;

    @Transient
    @JsonProperty("gaccel_2")
    private Double gaccel2;

    @Transient
    @JsonProperty("gaccel_3")
    private Double gaccel3;

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
                ", codee40=" + codee40 +
                ", codee41=" + codee41 +
                ", codee42=" + codee42 +
                ", gaccel0=" + gaccel0 +
                ", gaccel1=" + gaccel1 +
                ", gaccel2=" + gaccel2 +
                ", gaccel3=" + gaccel3 +
                ", jobId=" + jobId +
                '}';
    }
}