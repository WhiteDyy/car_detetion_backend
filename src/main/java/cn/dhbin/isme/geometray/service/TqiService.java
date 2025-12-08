package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.Tqi;
import cn.dhbin.isme.geometray.domain.requeset.TqiRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

@Service
public interface TqiService extends IService<Tqi> {
    Page<Tqi> queryPage(TqiRequest request);
}
