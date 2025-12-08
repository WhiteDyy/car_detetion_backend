package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.TaqData;
import cn.dhbin.isme.geometray.domain.requeset.TaqRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

@Service
public interface TaqService extends IService<TaqData> {
    Page<TaqData> queryPage(TaqRequest request);
}
