package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.Olw;
import cn.dhbin.isme.geometray.domain.requeset.OlwRequest;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OlwService extends IService<Olw> {
    Page<Olw> queryPage(OlwRequest request);
}
