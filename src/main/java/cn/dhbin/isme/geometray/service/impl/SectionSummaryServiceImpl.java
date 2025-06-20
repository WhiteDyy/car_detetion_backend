package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.SectionSummary;
import cn.dhbin.isme.geometray.domain.requeset.SectionSummaryRequest;
import cn.dhbin.isme.geometray.mapper.SectionSummaryMapper;
import cn.dhbin.isme.geometray.service.SectionSummaryService;
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
public class SectionSummaryServiceImpl implements SectionSummaryService {

    @Resource
    SectionSummaryMapper sectionSummaryMapper;

    @Override
    public Page<SectionSummary> queryPage(SectionSummaryRequest request) {
        IPage<SectionSummary> page = request.toPage();
        LambdaQueryWrapper<SectionSummary> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(StrUtil.isNotBlank(request.getItemName()), SectionSummary::getItemName, request.getItemName())
                .eq(StrUtil.isNotBlank(request.getLevel4Extension()), SectionSummary::getLevel4Extension, request.getLevel4Extension())
                .eq(StrUtil.isNotBlank(request.getPercentage()), SectionSummary::getPercentage, request.getPercentage())
                .eq(StrUtil.isNotBlank(request.getDeduction()), SectionSummary::getDeduction, request.getDeduction())
                .eq(StrUtil.isNotBlank(request.getJobExtension()), SectionSummary::getJobExtension, request.getJobExtension());
        IPage<SectionSummary> result = sectionSummaryMapper.selectPage(page, wrapper);
        return Page.convert(result);
    }

    @Override
    public boolean saveBatch(Collection<SectionSummary> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<SectionSummary> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<SectionSummary> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(SectionSummary entity) {
        return false;
    }

    @Override
    public SectionSummary getOne(Wrapper<SectionSummary> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<SectionSummary> getOneOpt(Wrapper<SectionSummary> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<SectionSummary> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<SectionSummary> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<SectionSummary> getBaseMapper() {
        return sectionSummaryMapper;
    }

    @Override
    public Class<SectionSummary> getEntityClass() {
        return null;
    }
}
