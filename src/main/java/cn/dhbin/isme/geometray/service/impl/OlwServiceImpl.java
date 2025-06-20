package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.Olw;
import cn.dhbin.isme.geometray.domain.requeset.OlwRequest;
import cn.dhbin.isme.geometray.mapper.OlwMapper;
import cn.dhbin.isme.geometray.service.OlwService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class OlwServiceImpl implements OlwService {

    @Resource
    OlwMapper olwMapper;

    @Override
    public Page<Olw> queryPage(OlwRequest request) {
        IPage<Olw> page = request.toPage();
        LambdaQueryWrapper<Olw> wrapper = Wrappers.lambdaQuery();
        wrapper
                .eq(StrUtil.isNotBlank(request.getMileage()), Olw::getMileage, request.getMileage())
                .eq(StrUtil.isNotBlank(request.getSleeperNo()), Olw::getSleeperNo, request.getSleeperNo());
        IPage<Olw> ipage = getBaseMapper().selectPage(page, wrapper);
        return Page.convert(ipage);
    }

    @Override
    public boolean saveBatch(Collection<Olw> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<Olw> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<Olw> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(Olw entity) {
        return false;
    }

    @Override
    public Olw getOne(Wrapper<Olw> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<Olw> getOneOpt(Wrapper<Olw> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<Olw> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<Olw> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<Olw> getBaseMapper() {
        return olwMapper;
    }

    @Override
    public Class<Olw> getEntityClass() {
        return null;
    }
}
