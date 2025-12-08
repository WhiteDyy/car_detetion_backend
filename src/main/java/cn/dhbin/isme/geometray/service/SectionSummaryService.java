package cn.dhbin.isme.geometray.service;


import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.SectionSummary;
import cn.dhbin.isme.geometray.domain.requeset.SectionSummaryRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

@Service
public interface SectionSummaryService extends IService<SectionSummary> {
    Page<SectionSummary> queryPage(SectionSummaryRequest request);
}
