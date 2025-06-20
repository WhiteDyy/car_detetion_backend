package cn.dhbin.isme.geometray.service.impl;


import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.CurseInspect;
import cn.dhbin.isme.geometray.domain.requeset.CurseInspectRequest;
import cn.dhbin.isme.geometray.mapper.CurseInspectMapper;
import cn.dhbin.isme.geometray.service.CurseInspectService;
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
public class CurseInspectServiceImpl implements CurseInspectService {

    @Resource
    CurseInspectMapper curseInspectMapper;

    @Override
    public Page<CurseInspect> queryPage(CurseInspectRequest request) {
        IPage<CurseInspect> page = request.toPage();
        LambdaQueryWrapper<CurseInspect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(StrUtil.isNotBlank(request.getMileage()), CurseInspect::getMileage,request.getMileage());
        IPage<CurseInspect> iPage = curseInspectMapper.selectPage(page, queryWrapper);
        return Page.convert(iPage);
    }

    @Override
    public boolean saveBatch(Collection<CurseInspect> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<CurseInspect> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<CurseInspect> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(CurseInspect entity) {
        return false;
    }

    @Override
    public CurseInspect getOne(Wrapper<CurseInspect> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<CurseInspect> getOneOpt(Wrapper<CurseInspect> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<CurseInspect> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<CurseInspect> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<CurseInspect> getBaseMapper() {
        return curseInspectMapper;
    }

    @Override
    public Class<CurseInspect> getEntityClass() {
        return null;
    }
}
