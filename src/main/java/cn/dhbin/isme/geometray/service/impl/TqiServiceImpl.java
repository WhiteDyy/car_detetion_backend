package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.Tqi;
import cn.dhbin.isme.geometray.domain.requeset.TqiRequest;
import cn.dhbin.isme.geometray.mapper.TqiMapper;
import cn.dhbin.isme.geometray.service.TqiService;
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
public class TqiServiceImpl implements TqiService {

    @Resource
    TqiMapper tqiMapper;

    @Override
    public Page<Tqi> queryPage(TqiRequest request) {
        IPage<Tqi> page = request.toPage();
        LambdaQueryWrapper<Tqi> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(StrUtil.isNotBlank(request.getTqi()), Tqi::getTqi, request.getTqi())
                .eq(StrUtil.isNotBlank(request.getGauge()), Tqi::getGauge, request.getGauge())
                .eq(StrUtil.isNotBlank(request.getMileage()), Tqi::getMileage, request.getMileage())
                .eq(StrUtil.isNotBlank(request.getLength()), Tqi::getLength, request.getLength());;
        IPage<Tqi> iPage = tqiMapper.selectPage(page, queryWrapper);
        return Page.convert(iPage);
    }

    @Override
    public boolean saveBatch(Collection<Tqi> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<Tqi> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<Tqi> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(Tqi entity) {
        return false;
    }

    @Override
    public Tqi getOne(Wrapper<Tqi> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<Tqi> getOneOpt(Wrapper<Tqi> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<Tqi> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<Tqi> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<Tqi> getBaseMapper() {
        return tqiMapper;
    }

    @Override
    public Class<Tqi> getEntityClass() {
        return null;
    }



}
