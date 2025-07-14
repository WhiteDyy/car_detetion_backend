package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.JobRecord;
import cn.dhbin.isme.geometray.domain.entity.Jobs;
import cn.dhbin.isme.geometray.domain.requeset.EndJobRequest;
import cn.dhbin.isme.geometray.domain.requeset.JobsPageRequest;
import cn.dhbin.isme.geometray.domain.requeset.StartJobRequest;
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

    /**
     * 开始任务
     *
     * @param request 任务信息
     * @return 返回操作结果
     */
    JobRecord startJob(StartJobRequest request);


    /**
     * 结束任务
     *
     * @param request 任务信息
     * @return 返回操作结果
     */
    JobRecord endJob(EndJobRequest request);
}
