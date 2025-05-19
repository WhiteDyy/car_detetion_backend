package cn.dhbin.isme.geometray.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`rail_measurements`")
public class RailMeasurement {
    /**
     * 自增主键，唯一标识每条记录
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作业ID，关联jobs表，标识测量任务
     */
    @TableField("job_id")
    private Long jobId;

    /**
     * 线路类型，标识正线或道岔（如“正线”或“道岔”）
     */
    @TableField("line_type")
    private String lineType;

    /**
     * 线路名称，如“京沪线”
     */
    @TableField("line_name")
    private String lineName;

    /**
     * 行别，标识行车方向，如“上行”或“下行”
     */
    @TableField("direction")
    private String direction;

    /**
     * 里程数，记录测量点的线路里程（单位：公里）
     */
    @TableField("mileage")
    private Double mileage;

    /**
     * 电子标签，标识测量点的RFID编号
     */
    @TableField("rfid_tag")
    private String rfidTag;

    /**
     * 轨枕序号，标识测量点的轨枕编号
     */
    @TableField("sleeper_id")
    private String sleeperId;

    /**
     * 测量点位，道岔专用，标识s1~s17测量点，正线记录可为空
     */
    @TableField("point_id")
    private String pointId;

    /**
     * 测量位置描述，道岔专用，文字描述测量点位置（如“尖轨部分”），正线记录可为空
     */
    @TableField("measure_position")
    private String measurePosition;

    /**
     * 轨向，道岔专用，标识A股（道岔正线部分）或B股（道岔曲线部分），正线记录可为空
     */
    @TableField("track_type")
    private String trackType;

    /**
     * 测量参数名称，道岔专用，标识具体测量参数（如“轨距”），正线记录可为空
     */
    @TableField("measure_param")
    private String measureParam;

    /**
     * 测量值，道岔专用，记录道岔测量参数的实际数值，正线记录可为空
     */
    @TableField("measure_value")
    private Double measureValue;

    /**
     * 设计值，道岔专用，记录道岔测量参数的设计标准值，正线记录可为空
     */
    @TableField("design_value")
    private Double designValue;

    /**
     * 轨距
     */
    @TableField("gauge")
    private Double gauge;

    /**
     * 轨距变化率
     */
    @TableField("gauge_change_rate")
    private Double gaugeChangeRate;

    /**
     * 左高低
     */
    @TableField("left_height")
    private Double leftHeight;

    /**
     * 右高低
     */
    @TableField("right_height")
    private Double rightHeight;

    /**
     * 左轨向
     */
    @TableField("left_direction")
    private Double leftDirection;

    /**
     * 右轨向
     */
    @TableField("right_direction")
    private Double rightDirection;

    /**
     * 水平
     */
    @TableField("horizontal")
    private Double horizontal;

    /**
     * 三角坑
     */
    @TableField("triangle_pit")
    private Double trianglePit;

    /**
     * 垂直磨耗，正线专用，记录正线轨道垂直磨耗值，道岔记录为空
     */
    @TableField("vertical_wear")
    private Double verticalWear;

    /**
     * 侧面磨耗，正线专用，记录正线轨道侧面磨耗值，道岔记录为空
     */
    @TableField("lateral_wear")
    private Double lateralWear;

    /**
     * 测量时间，记录数据采集的日期和时间
     */
    @TableField("measurement_time")
    private LocalDateTime measurementTime;

}
