package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.SgTable;
import cn.dhbin.isme.geometray.domain.requeset.SgRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

@Service
public interface SgService extends IService<SgTable> {
    Page<SgTable> queryPage(SgRequest request);
}
