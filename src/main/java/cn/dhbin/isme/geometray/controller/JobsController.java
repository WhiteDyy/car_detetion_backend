package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.Jobs;
import cn.dhbin.isme.geometray.domain.requeset.JobsPageRequest;
import cn.dhbin.isme.geometray.service.JobsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "作业任务")
public class JobsController {

    @Resource
    JobsService jobsService;

    @GetMapping("/all_jobs")
    @Operation(summary = "获取所有作业信息")
    public R<Page<Jobs>> findAll(JobsPageRequest request) {
        Page<Jobs> jobsPage = jobsService.queryPage(request);
        return R.ok(jobsPage);
    }
}
