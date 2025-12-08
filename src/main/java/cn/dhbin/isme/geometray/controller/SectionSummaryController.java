package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.SectionSummary;
import cn.dhbin.isme.geometray.domain.requeset.SectionSummaryRequest;
import cn.dhbin.isme.geometray.service.SectionSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/section_summary")
@RequiredArgsConstructor
@Tag(name = "区段小结报表")
public class SectionSummaryController {
    @Resource
    SectionSummaryService sectionSummaryService;

    @PostMapping("/search")
    @Operation(summary = "搜索数据")
    public R<Page<SectionSummary>> search(@RequestBody SectionSummaryRequest request) {
        Page<SectionSummary> page = sectionSummaryService.queryPage(request);
        return R.ok(page);
    }
}
