package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.WearTable;
import cn.dhbin.isme.geometray.domain.requeset.WearRequest;
import cn.dhbin.isme.geometray.mapper.WearMapper;
import cn.dhbin.isme.geometray.service.WearService;
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
public class WearServiceImpl implements WearService {

    @Resource
    WearMapper wearMapper;

    @Override
    public Page<WearTable> queryPage(WearRequest request) {
        IPage<WearTable> wear = request.toPage();
        LambdaQueryWrapper<WearTable> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(StrUtil.isNotBlank(request.getMileage()), WearTable::getMileage, request.getMileage())
                .eq(StrUtil.isNotBlank(request.getLeftVerticalWear()), WearTable::getLeftVerticalWear, request.getLeftVerticalWear())
                .eq(StrUtil.isNotBlank(request.getLeftSideWear()), WearTable::getLeftSideWear, request.getLeftSideWear())
                .eq(StrUtil.isNotBlank(request.getLeftTotalWear()), WearTable::getLeftTotalWear, request.getLeftTotalWear())
                .eq(StrUtil.isNotBlank(request.getRightVerticalWear()), WearTable::getRightVerticalWear, request.getRightVerticalWear())
                .eq(StrUtil.isNotBlank(request.getRightSideWear()), WearTable::getRightSideWear, request.getRightSideWear())
                .eq(StrUtil.isNotBlank(request.getRightTotalWear()), WearTable::getRightTotalWear, request.getRightTotalWear());
        IPage<WearTable> ipage = getBaseMapper().selectPage(wear, wrapper);
        return Page.convert(ipage);
    }

    @Override
    public boolean saveBatch(Collection<WearTable> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<WearTable> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<WearTable> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(WearTable entity) {
        return false;
    }

    @Override
    public WearTable getOne(Wrapper<WearTable> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<WearTable> getOneOpt(Wrapper<WearTable> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<WearTable> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<WearTable> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<WearTable> getBaseMapper() {
        return wearMapper;
    }

    @Override
    public Class<WearTable> getEntityClass() {
        return null;
    }
}
