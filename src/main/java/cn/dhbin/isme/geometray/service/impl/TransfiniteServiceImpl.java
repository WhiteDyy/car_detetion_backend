package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.TransfiniteTable;
import cn.dhbin.isme.geometray.domain.requeset.TransfiniteRequest;
import cn.dhbin.isme.geometray.mapper.TransfiniteMapper;
import cn.dhbin.isme.geometray.service.TransfiniteService;
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
public class TransfiniteServiceImpl implements TransfiniteService {
    @Resource
    TransfiniteMapper transfiniteMapper;

    @Override
    public Page<TransfiniteTable> queryPage(TransfiniteRequest request){
        IPage<TransfiniteTable> page = request.toPage();
        LambdaQueryWrapper<TransfiniteTable> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(StrUtil.isNotBlank(request.getAlignmentRightMeasured()), TransfiniteTable::getAlignmentRightMeasured, request.getAlignmentRightMeasured())
                .eq(StrUtil.isNotBlank(request.getDirectionLeftWarning()), TransfiniteTable::getDirectionLeftWarning, request.getDirectionLeftWarning());
        IPage<TransfiniteTable> iPage = getBaseMapper().selectPage(page, queryWrapper);
        return Page.convert(iPage);
    }


    @Override
    public boolean saveBatch(Collection<TransfiniteTable> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<TransfiniteTable> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<TransfiniteTable> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(TransfiniteTable entity) {
        return false;
    }

    @Override
    public TransfiniteTable getOne(Wrapper<TransfiniteTable> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<TransfiniteTable> getOneOpt(Wrapper<TransfiniteTable> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<TransfiniteTable> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<TransfiniteTable> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<TransfiniteTable> getBaseMapper() {
        return transfiniteMapper;
    }

    @Override
    public Class<TransfiniteTable> getEntityClass() {
        return null;
    }
}
