package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.enums.JobStatus;
import cn.dhbin.isme.common.exception.BizException;
import cn.dhbin.isme.common.response.BizResponseCode;
import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.JobRecord;
import cn.dhbin.isme.geometray.domain.entity.Jobs;
import cn.dhbin.isme.geometray.domain.requeset.EndJobRequest;
import cn.dhbin.isme.geometray.domain.requeset.JobsPageRequest;
import cn.dhbin.isme.geometray.domain.requeset.StartJobRequest;
import cn.dhbin.isme.geometray.mapper.JobRecordMapper;
import cn.dhbin.isme.geometray.mapper.JobsMapper;
import cn.dhbin.isme.geometray.service.JobsService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JobsServiceImpl implements JobsService {
    @Resource
    private JobsMapper jobsMapper;

    @Override
    public Page<Jobs> queryPage(JobsPageRequest request) {
        // 转换为MyBatis-Plus的分页对象
        IPage<Jobs> page = request.toPage();

        // 创建查询条件
        LambdaQueryWrapper<Jobs> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件，注意判空
        queryWrapper
                .eq(StrUtil.isNotBlank(request.getLineType()), Jobs::getLineType, request.getLineType())
                .eq(StrUtil.isNotBlank(request.getDirection()), Jobs::getDirection, request.getDirection())
                .eq(StrUtil.isNotBlank(request.getJobName()), Jobs::getJobName, request.getJobName())
                .eq(request.getStartTime() != null, Jobs::getStartTime, request.getStartTime())
                .eq(request.getEndTime() != null, Jobs::getEndTime, request.getEndTime())
                .eq(StrUtil.isNotBlank(request.getDeviceId()), Jobs::getDeviceId, request.getDeviceId())
                .eq(StrUtil.isNotBlank(request.getOperator()), Jobs::getOperator, request.getOperator())
                .like(StrUtil.isNotBlank(request.getDescription()), Jobs::getDescription, request.getDescription())
                .eq(request.getCreatedAt() != null, Jobs::getCreatedAt, request.getCreatedAt());

        // 执行分页查询
        IPage<Jobs> jobsPage = getBaseMapper().selectPage(page, queryWrapper);

        // 转换结果并返回
        return Page.convert(jobsPage);
    }

    @Resource
    JobRecordMapper jobRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobRecord startJob(StartJobRequest request) {
        // 1. 查询该作业ID的所有记录（按创建时间降序）
        LambdaQueryWrapper<JobRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JobRecord::getJobId, request.getJobId())
                .orderByDesc(JobRecord::getCreatedAt);

        List<JobRecord> records = jobRecordMapper.selectList(queryWrapper);

        // 2. 检查最新记录的状态
        if (!records.isEmpty()) {
            JobRecord latestRecord = records.getFirst();

            // 2.1 如果已有进行中的记录
            if (JobStatus.IN_PROGRESS.getCode().equals(latestRecord.getJobStatus())) {
                throw new BizException(BizResponseCode.ERR_20000, "该作业已有进行中的记录，不能重复开始");
            }
            // 2.2 如果是未开始状态，更新为进行中
            else if (JobStatus.NOT_STARTED.getCode().equals(latestRecord.getJobStatus())) {
                latestRecord.setStartTime(LocalDateTime.now());
                latestRecord.setStartStation(request.getStartStation());
                latestRecord.setOperator(request.getOperator());
                latestRecord.setJobStatus(JobStatus.IN_PROGRESS.getCode());
                latestRecord.setUpdatedAt(LocalDateTime.now());

                jobRecordMapper.updateById(latestRecord);
                return latestRecord;
            }
            // 2.3 如果是已完成或已取消状态，创建新记录
        }

        // 3. 创建新记录（适用于无记录或最新记录已完成/已取消的情况）
        JobRecord newRecord = new JobRecord();
        newRecord.setJobId(request.getJobId());
        newRecord.setStartTime(LocalDateTime.now());
        newRecord.setStartStation(request.getStartStation());
        newRecord.setOperator(request.getOperator());
        newRecord.setJobStatus(JobStatus.IN_PROGRESS.getCode()); // 新记录直接设为进行中
        newRecord.setCreatedAt(LocalDateTime.now());
        newRecord.setUpdatedAt(LocalDateTime.now());

        jobRecordMapper.insert(newRecord);
        return newRecord;
    }

    /**
     * @param request
     */
    /**
     * 结束作业任务 - 更新作业记录
     *
     * @param request 结束作业请求参数
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobRecord endJob(EndJobRequest request) {
        // 1. 查询进行中的记录
        LambdaQueryWrapper<JobRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JobRecord::getJobId, request.getJobId())
                .eq(JobRecord::getJobStatus, JobStatus.IN_PROGRESS.getCode());

        JobRecord record = jobRecordMapper.selectOne(queryWrapper);
        if (record == null) {
            throw new RuntimeException("没有找到进行中的作业记录");
        }

        // 2. 准备更新内容
        JobRecord updateRecord = new JobRecord();
        updateRecord.setId(record.getId());
        updateRecord.setEndTime(LocalDateTime.now());
        updateRecord.setEndStation(request.getEndStation());
        updateRecord.setJobStatus(JobStatus.COMPLETED.getCode()); // 使用枚举常量
        updateRecord.setUpdatedAt(LocalDateTime.now());

        // 3. 执行更新
        LambdaUpdateWrapper<JobRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(JobRecord::getId, record.getId())
                .eq(JobRecord::getJobStatus, JobStatus.IN_PROGRESS.getCode());

        int updateResult = jobRecordMapper.update(updateRecord, updateWrapper);
        if (updateResult <= 0) {
            throw new BizException(BizResponseCode.ERR_20001, "更新作业记录失败，可能记录已被修改");
        }
        return record;
    }


    /**
     * @param entityList
     * @param batchSize
     * @return
     */
    @Override
    public boolean saveBatch(Collection<Jobs> entityList, int batchSize) {
        return false;
    }

    /**
     * @param entityList
     * @param batchSize
     * @return
     */
    @Override
    public boolean saveOrUpdateBatch(Collection<Jobs> entityList, int batchSize) {
        return false;
    }

    /**
     * @param entityList
     * @param batchSize
     * @return
     */
    @Override
    public boolean updateBatchById(Collection<Jobs> entityList, int batchSize) {
        return false;
    }

    /**
     * @param entity
     * @return
     */
    @Override
    public boolean saveOrUpdate(Jobs entity) {
        return false;
    }

    /**
     * @param queryWrapper
     * @param throwEx
     * @return
     */
    @Override
    public Jobs getOne(Wrapper<Jobs> queryWrapper, boolean throwEx) {
        return null;
    }

    /**
     * @param queryWrapper
     * @param throwEx
     * @return
     */
    @Override
    public Optional<Jobs> getOneOpt(Wrapper<Jobs> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    /**
     * @param queryWrapper
     * @return
     */
    @Override
    public Map<String, Object> getMap(Wrapper<Jobs> queryWrapper) {
        return Map.of();
    }

    /**
     * @param queryWrapper
     * @param mapper
     * @param <V>
     * @return
     */
    @Override
    public <V> V getObj(Wrapper<Jobs> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public BaseMapper<Jobs> getBaseMapper() {
        return jobsMapper;
    }

    /**
     * @return
     */
    @Override
    public Class<Jobs> getEntityClass() {
        return null;
    }
}
