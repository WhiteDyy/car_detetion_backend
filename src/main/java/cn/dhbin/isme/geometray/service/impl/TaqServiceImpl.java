package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.TaqData;
import cn.dhbin.isme.geometray.domain.requeset.TaqRequest;
import cn.dhbin.isme.geometray.mapper.TaqMapper;
import cn.dhbin.isme.geometray.service.TaqService;
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
public class TaqServiceImpl implements TaqService {

    @Resource
    TaqMapper taqMapper;

    @Override
    public Page<TaqData> queryPage(TaqRequest taqRequest) {
        IPage<TaqData> page = taqRequest.toPage();
        LambdaQueryWrapper<TaqData> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(StrUtil.isNotBlank(taqRequest.getNumber()), TaqData::getNumber, taqRequest.getNumber())
                .eq(StrUtil.isNotBlank(taqRequest.getDirection()), TaqData::getDirection, taqRequest.getDirection())
                .eq(StrUtil.isNotBlank(taqRequest.getStandardValue()), TaqData::getStandardValue, taqRequest.getStandardValue())
                .eq(StrUtil.isNotBlank(taqRequest.getStandardValue()), TaqData::getStandardValue, taqRequest.getStandardValue());
        IPage<TaqData> ipage = getBaseMapper().selectPage(page, wrapper);
        return Page.convert(ipage);
    }

    @Override
    public boolean saveBatch(Collection<TaqData> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<TaqData> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<TaqData> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(TaqData entity) {
        return false;
    }

    @Override
    public TaqData getOne(Wrapper<TaqData> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<TaqData> getOneOpt(Wrapper<TaqData> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<TaqData> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<TaqData> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<TaqData> getBaseMapper() {
        return taqMapper;
    }

    @Override
    public Class<TaqData> getEntityClass() {
        return null;
    }
}
