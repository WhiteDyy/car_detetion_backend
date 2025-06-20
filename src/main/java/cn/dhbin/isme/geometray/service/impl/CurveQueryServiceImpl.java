package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.CurveQuery;
import cn.dhbin.isme.geometray.domain.requeset.CurveQueryRequest;
import cn.dhbin.isme.geometray.mapper.CurveQueryMapper;

import cn.dhbin.isme.geometray.service.CurveQueryService;
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
public class CurveQueryServiceImpl implements CurveQueryService {
    @Resource
    CurveQueryMapper curveQueryMapper;

    @Override
    public Page<CurveQuery> queryPage(CurveQueryRequest request) {
        IPage<CurveQuery> page = request.toPage();
        LambdaQueryWrapper<CurveQuery> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper
                .eq(StrUtil.isNotBlank(request.getDirection()), CurveQuery::getDirection, request.getDirection())
                .eq(StrUtil.isNotBlank(request.getLineName()), CurveQuery::getLineName, request.getLineName())
                .eq(StrUtil.isNotBlank(request.getLineNo()), CurveQuery::getLineNo, request.getLineNo())
                .eq(StrUtil.isNotBlank(request.getCurveDirection()), CurveQuery::getCurveDirection, request.getCurveDirection());

        // 执行分页查询
        IPage<CurveQuery> curveQueryIPage = getBaseMapper().selectPage(page, queryWrapper);

        // 转换结果并返回
        return Page.convert(curveQueryIPage);
    }

    @Override
    public boolean saveBatch(Collection<CurveQuery> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<CurveQuery> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<CurveQuery> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(CurveQuery entity) {
        return false;
    }

    @Override
    public CurveQuery getOne(Wrapper<CurveQuery> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<CurveQuery> getOneOpt(Wrapper<CurveQuery> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<CurveQuery> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<CurveQuery> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<CurveQuery> getBaseMapper() {
        return curveQueryMapper;
    }

    @Override
    public Class<CurveQuery> getEntityClass() {
        return null;
    }

}
