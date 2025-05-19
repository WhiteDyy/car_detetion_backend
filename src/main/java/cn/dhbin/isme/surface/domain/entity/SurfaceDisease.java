package cn.dhbin.isme.surface.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@TableName("`surface_disease`")
public class SurfaceDisease {

    @TableId(value = "disease_id", type = IdType.ASSIGN_UUID)
    private String diseaseId;

    @TableField("line_code")
    private String lineCode;

    @TableField("line_type")
    private String lineType;

    @TableField("start_longitude")
    private BigDecimal startLongitude;

    @TableField("start_measure")
    private BigDecimal startMeasure;

    @TableField("end_measure")
    private BigDecimal endMeasure;

    @TableField("detect_date")
    private Timestamp detectDate;

    @TableField("upload_date")
    private Timestamp uploadDate;

    @TableField("vehicle_no")
    private String vehicleNo;

    @TableField("equip_code")
    private String equipCode;

    @TableField("topic_code")
    private String topicCode;

    @TableField("disease_position")
    private String diseasePosition;

    @TableField("disease_type")
    private String diseaseType;

    @TableField("disease_detail_type")
    private String diseaseDetailType;

    @TableField("disease_level")
    private String diseaseLevel;

    @TableField("disease_descript")
    private String diseaseDescript;

    @TableField("disease_val")
    private BigDecimal diseaseVal;

    @TableField("disease_unit")
    private String diseaseUnit;

    @TableField("box_position")
    private String boxPosition;

    @TableField("create_date")
    private Timestamp createDate;

    @TableField("create_user_id")
    private String createUserId;

    @TableField("create_station_id")
    private String createStationId;

    @TableField("create_dept_id")
    private String createDeptId;

    @TableField("update_date")
    private Timestamp updateDate;

    @TableField("update_user_id")
    private String updateUserId;

    @TableField("update_station_id")
    private String updateStationId;

    @TableField("update_dept_id")
    private String updateDeptId;

    @TableField("lock_version")
    private BigDecimal lockVersion;

    @TableField("file_id")
    private String fileId;

    @TableField("start_latitude")
    private BigDecimal startLatitude;

    @TableField("end_longitude")
    private BigDecimal endLongitude;

    @TableField("end_latitude")
    private BigDecimal endLatitude;


}
