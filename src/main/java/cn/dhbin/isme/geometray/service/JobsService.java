package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.Jobs;
import cn.dhbin.isme.geometray.domain.requeset.JobsPageRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

@Service
public interface JobsService extends IService<Jobs> {


    /**
     * 分页查询
     *
     * @param request 请求
     * @return ret
     */
    Page<Jobs> queryPage(JobsPageRequest request);
}
