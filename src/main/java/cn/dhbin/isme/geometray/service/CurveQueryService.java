package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.CurveQuery;

import cn.dhbin.isme.geometray.domain.requeset.CurveQueryRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

@Service
public interface CurveQueryService extends IService<CurveQuery> {
    Page<CurveQuery> queryPage(CurveQueryRequest request);
}
