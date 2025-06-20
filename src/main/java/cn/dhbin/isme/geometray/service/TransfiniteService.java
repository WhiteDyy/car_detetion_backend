package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.TransfiniteTable;
import cn.dhbin.isme.geometray.domain.requeset.TransfiniteRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

@Service
public interface TransfiniteService extends IService<TransfiniteTable> {
    Page<TransfiniteTable> queryPage(TransfiniteRequest request);
}
