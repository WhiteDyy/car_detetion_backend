package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.SgTable;
import cn.dhbin.isme.geometray.domain.requeset.SgRequest;
import cn.dhbin.isme.geometray.mapper.SgMapper;
import cn.dhbin.isme.geometray.service.SgService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SgServiceImpl implements SgService{

    @Resource
    SgMapper sgMapper;

    @Override
    public Page<SgTable> queryPage(SgRequest request) {
        IPage<SgTable> page = request.toPage();
        LambdaQueryWrapper<SgTable> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(StrUtil.isNotBlank(request.getPoint()), SgTable::getPoint, request.getPoint())
                .eq(StrUtil.isNotBlank(request.getPosition()), SgTable::getPosition, request.getPosition())
                .eq(StrUtil.isNotBlank(request.getDirection()), SgTable::getDirection, request.getDirection())
                .eq(StrUtil.isNotBlank(request.getSleeper()), SgTable::getSleeper, request.getSleeper())
                .eq(StrUtil.isNotBlank(request.getParamName()), SgTable::getParamName, request.getParamName())
                .eq(StrUtil.isNotBlank(request.getParamValue()), SgTable::getParamValue, request.getParamValue());
        IPage<SgTable> ipage = getBaseMapper().selectPage(page, queryWrapper);
        return Page.convert(ipage);
    }

    @Override
    public boolean saveBatch(Collection<SgTable> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<SgTable> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<SgTable> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(SgTable entity) {
        return false;
    }

    @Override
    public SgTable getOne(Wrapper<SgTable> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<SgTable> getOneOpt(Wrapper<SgTable> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<SgTable> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<SgTable> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<SgTable> getBaseMapper() {
        return sgMapper;
    }

    @Override
    public Class<SgTable> getEntityClass() {
        return null;
    }


}
