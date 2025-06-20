package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.Surface;
import cn.dhbin.isme.geometray.domain.requeset.SurfaceRequest;
import cn.dhbin.isme.geometray.mapper.SurfaceMapper;
import cn.dhbin.isme.geometray.service.SurfaceService;
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
public class SurfaceServiceImpl implements SurfaceService {

    @Resource
    SurfaceMapper surfaceMapper;

    @Override
    public Page<Surface> querPage(SurfaceRequest request) {
        IPage<Surface> page = request.toPage();
        LambdaQueryWrapper<Surface> queryWrapper = new LambdaQueryWrapper<>();
        // 里程
        if (request.getMileage() != null) {
            queryWrapper.eq(Surface::getMileage, request.getMileage());
        }

        // 钢轨剥离掉块
        if (request.getRailSpallingWidth() != null) {
            queryWrapper.eq(Surface::getRailSpallingWidth, request.getRailSpallingWidth());
        }
        if (request.getRailSpallingDepth() != null) {
            queryWrapper.eq(Surface::getRailSpallingDepth, request.getRailSpallingDepth());
        }
        if (request.getRailSpallingQuantity() != null) {
            queryWrapper.eq(Surface::getRailSpallingQuantity, request.getRailSpallingQuantity());
        }

        // 钢轨擦伤
        if (request.getRailAbrasionWidth() != null) {
            queryWrapper.eq(Surface::getRailAbrasionWidth, request.getRailAbrasionWidth());
        }
        if (request.getRailAbrasionDepth() != null) {
            queryWrapper.eq(Surface::getRailAbrasionDepth, request.getRailAbrasionDepth());
        }
        if (request.getRailAbrasionQuantity() != null) {
            queryWrapper.eq(Surface::getRailAbrasionQuantity, request.getRailAbrasionQuantity());
        }

        // 钢轨波磨
        if (request.getRailCorrugationWidth() != null) {
            queryWrapper.eq(Surface::getRailCorrugationWidth, request.getRailCorrugationWidth());
        }
        if (request.getRailCorrugationDepth() != null) {
            queryWrapper.eq(Surface::getRailCorrugationDepth, request.getRailCorrugationDepth());
        }
        if (request.getRailCorrugationQuantity() != null) {
            queryWrapper.eq(Surface::getRailCorrugationQuantity, request.getRailCorrugationQuantity());
        }

        // 钢轨鱼鳞纹
        if (request.getRailScalePatternWidth() != null) {
            queryWrapper.eq(Surface::getRailScalePatternWidth, request.getRailScalePatternWidth());
        }
        if (request.getRailScalePatternQuantity() != null) {
            queryWrapper.eq(Surface::getRailScalePatternQuantity, request.getRailScalePatternQuantity());
        }

        // 钢轨裂纹
        if (request.getRailCrackWidth() != null) {
            queryWrapper.eq(Surface::getRailCrackWidth, request.getRailCrackWidth());
        }
        if (request.getRailCrackQuantity() != null) {
            queryWrapper.eq(Surface::getRailCrackQuantity, request.getRailCrackQuantity());
        }

        // 夹板裂纹
        if (request.getFishplateCrackWidth() != null) {
            queryWrapper.eq(Surface::getFishplateCrackWidth, request.getFishplateCrackWidth());
        }
        if (request.getFishplateCrackQuantity() != null) {
            queryWrapper.eq(Surface::getFishplateCrackQuantity, request.getFishplateCrackQuantity());
        }

        // 夹板螺栓损坏
        if (request.getFishplateBoltDamageQuantity() != null) {
            queryWrapper.eq(Surface::getFishplateBoltDamageQuantity, request.getFishplateBoltDamageQuantity());
        }

        // 夹板垫圈损坏
        if (request.getFishplateWasherDamageWidth() != null) {
            queryWrapper.eq(Surface::getFishplateWasherDamageWidth, request.getFishplateWasherDamageWidth());
        }
        if (request.getFishplateWasherDamageDepth() != null) {
            queryWrapper.eq(Surface::getFishplateWasherDamageDepth, request.getFishplateWasherDamageDepth());
        }
        if (request.getFishplateWasherDamageQuantity() != null) {
            queryWrapper.eq(Surface::getFishplateWasherDamageQuantity, request.getFishplateWasherDamageQuantity());
        }

        // 扣件损坏
        if (request.getFastenerDamageQuantity() != null) {
            queryWrapper.eq(Surface::getFastenerDamageQuantity, request.getFastenerDamageQuantity());
        }

        // 扣件移位
        if (request.getFastenerDisplacementQuantity() != null) {
            queryWrapper.eq(Surface::getFastenerDisplacementQuantity, request.getFastenerDisplacementQuantity());
        }

        // 扣件垫圈损坏
        if (request.getFastenerWasherDamageQuantity() != null) {
            queryWrapper.eq(Surface::getFastenerWasherDamageQuantity, request.getFastenerWasherDamageQuantity());
        }

        // 扣件螺旋道钉损坏
        if (request.getFastenerScrewSpikeDamageQuantity() != null) {
            queryWrapper.eq(Surface::getFastenerScrewSpikeDamageQuantity, request.getFastenerScrewSpikeDamageQuantity());
        }

        // 扣件挡板座损坏
        if (request.getFastenerBaseplateDamageQuantity() != null) {
            queryWrapper.eq(Surface::getFastenerBaseplateDamageQuantity, request.getFastenerBaseplateDamageQuantity());
        }

        // 轨枕裂纹
        if (request.getSleeperCrackWidth() != null) {
            queryWrapper.eq(Surface::getSleeperCrackWidth, request.getSleeperCrackWidth());
        }
        if (request.getSleeperCrackQuantity() != null) {
            queryWrapper.eq(Surface::getSleeperCrackQuantity, request.getSleeperCrackQuantity());
        }

        // 轨枕掉块
        if (request.getSleeperSpallingWidth() != null) {
            queryWrapper.eq(Surface::getSleeperSpallingWidth, request.getSleeperSpallingWidth());
        }
        if (request.getSleeperSpallingQuantity() != null) {
            queryWrapper.eq(Surface::getSleeperSpallingQuantity, request.getSleeperSpallingQuantity());
        }

        // 道床裂纹
        if (request.getBallastBedCrackWidth() != null) {
            queryWrapper.eq(Surface::getBallastBedCrackWidth, request.getBallastBedCrackWidth());
        }
        if (request.getBallastBedCrackQuantity() != null) {
            queryWrapper.eq(Surface::getBallastBedCrackQuantity, request.getBallastBedCrackQuantity());
        }

        // 线路特征
        if (request.getLineFeature() != null && !request.getLineFeature().isEmpty()) {
            queryWrapper.eq(Surface::getLineFeature, request.getLineFeature());
        }

        // 完成时间
        if (request.getInspectionTime() != null) {
            queryWrapper.eq(Surface::getInspectionTime, request.getInspectionTime());
        }

        // 负责人员
        if (request.getResponsiblePerson() != null && !request.getResponsiblePerson().isEmpty()) {
            queryWrapper.eq(Surface::getResponsiblePerson, request.getResponsiblePerson());
        }
        IPage<Surface> ipage = getBaseMapper().selectPage(page, queryWrapper);
        return Page.convert(ipage);
    }

    @Override
    public boolean saveBatch(Collection<Surface> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<Surface> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<Surface> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(Surface entity) {
        return false;
    }

    @Override
    public Surface getOne(Wrapper<Surface> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<Surface> getOneOpt(Wrapper<Surface> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<Surface> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<Surface> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<Surface> getBaseMapper() {
        return surfaceMapper;
    }

    @Override
    public Class<Surface> getEntityClass() {
        return null;
    }
}
