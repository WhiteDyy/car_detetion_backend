package cn.dhbin.isme.rabbitmqconsumer;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 几何结果数据实体（用于数据库持久化）
 */
@Entity
@Table(name = "geometry_result")
@Data
public class GeometryResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的任务ID
     */
    @Column(name = "job_id", nullable = false)
    private Long jobId;

    /**
     * 编码器值
     */
    private Integer encoder;

    /**
     * 轨道类型（0=正线, 其他=道岔）
     */
    @Column(name = "rail_type")
    private Integer railType;

    /**
     * 超高（水平）
     */
    @Column(name = "lsf01_level")
    private Double lsf01Level;

    /**
     * 轨距
     */
    @Column(name = "tdf01_gauge")
    private Double tdf01Gauge;

    /**
     * 轨道几何结果（JSON格式存储）
     * 格式: [里程, gd0, gd1, zs0, zs1]
     */
    @Column(name = "track_geometry", columnDefinition = "TEXT")
    private String trackGeometry;

    /**
     * 磨损值 - 里程
     */
    @Column(name = "wear_mile")
    private Double wearMile;

    /**
     * 磨损值 - 左轨水平磨损
     */
    @Column(name = "h_wear_l")
    private Double hWearL;

    /**
     * 磨损值 - 左轨垂直磨损
     */
    @Column(name = "v_wear_l")
    private Double vWearL;

    /**
     * 磨损值 - 右轨水平磨损
     */
    @Column(name = "h_wear_r")
    private Double hWearR;

    /**
     * 磨损值 - 右轨垂直磨损
     */
    @Column(name = "v_wear_r")
    private Double vWearR;

    /**
     * 磨损值 - 左轨总磨损
     */
    @Column(name = "wear_all_l")
    private Double wearAllL;

    /**
     * 磨损值 - 右轨总磨损
     */
    @Column(name = "wear_all_r")
    private Double wearAllR;

    /**
     * 道岔名称（可选）
     */
    @Column(name = "point_name")
    private String pointName;

    /**
     * 数据计数（可选）
     */
    @Column(name = "data_count")
    private Integer dataCount;

    /**
     * 平均护轨值（可选）
     */
    @Column(name = "avg_guard")
    private Double avgGuard;

    /**
     * 平均后轨值（可选）
     */
    @Column(name = "avg_back")
    private Double avgBack;

    /**
     * 传感器数据（JSON格式存储，包含完整的传感器信息）
     */
    @Column(name = "sensor_data", columnDefinition = "TEXT")
    private String sensorData;

    /**
     * 记录创建时间
     */
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

