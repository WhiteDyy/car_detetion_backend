package cn.dhbin.isme.rabbitmqconsumer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 几何结果中的传感器数据
 * 
 * 新协议字段映射：
 * - codee40 (编码器右), codee41 (编码器左), codee42 (编码器对齐)
 * - gaccel_0-3 (点激光和超声)
 * - length (里程)
 * - imu_gyro_x/y/z (IMU角速度)
 * - imu_acc_x/y/z (IMU加速度)
 */
@Data
public class GeometrySensorData {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime time;
    
    private Long sequence;
    
    /** 陀螺仪A（陀螺Z） */
    private Integer groa;
    
    /** 陀螺仪B（陀螺X） */
    private Integer grob;
    
    /** 倾角计 */
    private Double dipmeter;
    
    /** 点激光/超声1（新协议: gaccel_0 点激光右） */
    private Double acc1;
    
    /** 点激光/超声2（新协议: gaccel_1 点激光左） */
    private Double acc2;
    
    /** 点激光/超声3（新协议: gaccel_2 超声右） */
    private Double acc3;
    
    /** 编码器右（新协议: codee40） */
    private Integer codee40;
    
    /** 编码器左（新协议: codee41） */
    private Integer codee41;
    
    /** 编码器对齐（新协议: codee42） */
    private Integer codee42;
    
    /** 轨枕计数（旧协议，新协议不再使用） */
    private Double sleeper;
    
    /** 里程（新协议: length） */
    private Integer mileage;
    
    /** 编码器A_A（兼容字段） */
    @JsonProperty("codee40_a")
    private Integer codee40A;
    
    /** 编码器A_B（兼容字段） */
    @JsonProperty("codee40_b")
    private Integer codee40B;
    
    /** IMU角速度X（新协议: imu_gyro_x） */
    @JsonProperty("imu_angle_x")
    private Double imuAngleX;
    
    /** IMU角速度Y（新协议: imu_gyro_y） */
    @JsonProperty("imu_angle_y")
    private Double imuAngleY;
    
    /** IMU角速度Z（新协议: imu_gyro_z） */
    @JsonProperty("imu_angle_z")
    private Double imuAngleZ;
    
    /** IMU加速度X（新协议: imu_acc_x） */
    @JsonProperty("imu_acc_x")
    private Double imuAccX;
    
    /** IMU加速度Y（新协议: imu_acc_y） */
    @JsonProperty("imu_acc_y")
    private Double imuAccY;
    
    /** IMU加速度Z（新协议: imu_acc_z） */
    @JsonProperty("imu_acc_z")
    private Double imuAccZ;
}