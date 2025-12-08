package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.CondenseTable;
import cn.dhbin.isme.geometray.domain.requeset.CondenseRequest;
import cn.dhbin.isme.geometray.mapper.CondenseMapper;
import cn.dhbin.isme.geometray.service.CondenseService;
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
public class CondenseServiceImpl implements CondenseService {
   @Resource
    CondenseMapper condenseMapper;


    @Override
    public Page<CondenseTable> queryPage(CondenseRequest request) {
        IPage<CondenseTable> page = request.toPage();
        LambdaQueryWrapper<CondenseTable> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(StrUtil.isNotBlank(request.getStartMileage()),CondenseTable::getActualMileage,request.getStartMileage());
        IPage<CondenseTable> iPage = condenseMapper.selectPage(page, queryWrapper);
        return Page.convert(iPage);

    }
    @Override
    public boolean saveBatch(Collection<CondenseTable> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<CondenseTable> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<CondenseTable> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(CondenseTable entity) {
        return false;
    }

    @Override
    public CondenseTable getOne(Wrapper<CondenseTable> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<CondenseTable> getOneOpt(Wrapper<CondenseTable> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<CondenseTable> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<CondenseTable> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<CondenseTable> getBaseMapper() {
        return condenseMapper;
    }

    @Override
    public Class<CondenseTable> getEntityClass() {
        return null;
    }


}
