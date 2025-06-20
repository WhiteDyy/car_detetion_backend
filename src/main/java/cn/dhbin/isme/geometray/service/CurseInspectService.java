package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.CurseInspect;
import cn.dhbin.isme.geometray.domain.requeset.CurseInspectRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;


@Service
public interface CurseInspectService extends IService<CurseInspect> {
    Page<CurseInspect> queryPage(CurseInspectRequest request);
}
