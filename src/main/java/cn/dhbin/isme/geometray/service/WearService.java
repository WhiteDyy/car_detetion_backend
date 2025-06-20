package cn.dhbin.isme.geometray.service;


import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.WearTable;
import cn.dhbin.isme.geometray.domain.requeset.WearRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

@Service
public interface WearService extends IService<WearTable> {
    Page<WearTable> queryPage(WearRequest request);
}
