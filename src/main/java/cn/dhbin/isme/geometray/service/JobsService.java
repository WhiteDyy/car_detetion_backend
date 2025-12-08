package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.Jobs;
import cn.dhbin.isme.geometray.domain.requeset.JobsPageRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface JobsService extends IService<Jobs> {


    /**
     * 分页查询
     *
     * @param request 请求
     * @return ret
     */
    Page<Jobs> queryPage(JobsPageRequest request);

    byte[] generateReportZip(List<Integer> ids, List<Map<String, String>> forms);
}
