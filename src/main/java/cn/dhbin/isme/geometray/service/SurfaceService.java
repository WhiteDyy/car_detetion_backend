package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.Surface;
import cn.dhbin.isme.geometray.domain.requeset.SurfaceRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

@Service
public interface SurfaceService extends IService<Surface> {
    Page<Surface> querPage(SurfaceRequest request);
}
