package cn.dhbin.isme.geometray.mapper;

import cn.dhbin.isme.geometray.domain.dto.JobsDto;
import cn.dhbin.isme.geometray.domain.entity.Jobs;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface JobsMapper extends BaseMapper<Jobs> {


    /**
     * 分页查询任务信息
     *
     * @param page 分页
     * @param lineId 线路表id
     * @param lineType 线路类型
     * @param direction 行别
     * @param jobName 任务名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deviceId 设备id
     * @param operator 操作人
     * @param description 描述信息
     * @param createdAt 创建时间
     * @return 分页列表
     */
    IPage<JobsDto> pageDetail(IPage<Jobs> page,
                              @Param("lineId") Long lineId,
                              @Param("lineType") String lineType,
                              @Param("direction") String direction,
                              @Param("jobName") String jobName,
                              @Param("startTime") Date startTime,
                              @Param("endTime") Date endTime,
                              @Param("deviceId") String deviceId,
                              @Param("operator") String operator,
                              @Param("description") String description,
                              @Param("createdAt") Date createdAt
    );
}
