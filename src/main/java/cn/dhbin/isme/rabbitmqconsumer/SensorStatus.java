package cn.dhbin.isme.rabbitmqconsumer;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 传感器状态实体类
 * 用于存储传感器设备的连接状态
 */
@Entity
@Table(name = "sensor_status")
@Data
public class SensorStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 状态时间戳
     */
    @Column(name = "status_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime statusTime;

    /**
     * 关联的任务ID
     */
    @Column(name = "job_id")
    private Long jobId;

    /**
     * 16位状态字（原始位域值）
     */
    @Column(name = "state_word")
    private Integer stateWord;

    /**
     * 陀螺A状态 (0=未连接, 1=正常)
     */
    @Column(name = "gyro_a")
    private Integer gyroA;

    /**
     * 陀螺B状态 (0=未连接, 1=正常)
     */
    @Column(name = "gyro_b")
    private Integer gyroB;

    /**
     * 倾角传感器状态 (0=未连接, 1=正常)
     */
    @Column(name = "dipmeter")
    private Integer dipmeter;

    /**
     * 编码器A状态 (0=未连接, 1=正常)
     */
    @Column(name = "encoder_a")
    private Integer encoderA;

    /**
     * 编码器B状态 (0=未连接, 1=正常)
     */
    @Column(name = "encoder_b")
    private Integer encoderB;

    /**
     * IMU状态 (0=未连接, 1=正常)
     */
    @Column(name = "imu")
    private Integer imu;

    /**
     * INS状态 (0=未连接, 1=正常)
     */
    @Column(name = "ins")
    private Integer ins;

    /**
     * 电子标签(RFID)状态 (0=未连接, 1=正常)
     */
    @Column(name = "rfid")
    private Integer rfid;

    /**
     * 点激光A状态 (0=未连接, 1=正常)
     */
    @Column(name = "point_laser_a")
    private Integer pointLaserA;

    /**
     * 点激光B状态 (0=未连接, 1=正常)
     */
    @Column(name = "point_laser_b")
    private Integer pointLaserB;

    /**
     * 超声A状态 (0=未连接, 1=正常)
     */
    @Column(name = "ultrasonic_a")
    private Integer ultrasonicA;

    /**
     * 超声B状态 (0=未连接, 1=正常)
     */
    @Column(name = "ultrasonic_b")
    private Integer ultrasonicB;

    /**
     * 所有设备是否正常 (true=全部正常, false=有设备异常)
     */
    @Column(name = "all_ok")
    private Boolean allOk;
}

