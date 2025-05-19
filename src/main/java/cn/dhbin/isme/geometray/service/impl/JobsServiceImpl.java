package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.Jobs;
import cn.dhbin.isme.geometray.domain.requeset.JobsPageRequest;
import cn.dhbin.isme.geometray.mapper.JobsMapper;
import cn.dhbin.isme.geometray.service.JobsService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
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
