package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.CondenseTable;
import cn.dhbin.isme.geometray.domain.requeset.CondenseRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

@Service
public interface CondenseService extends IService<CondenseTable> {
    Page<CondenseTable> queryPage(CondenseRequest request);
}
