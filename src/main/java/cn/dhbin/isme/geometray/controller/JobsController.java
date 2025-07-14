package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.auth.RoleType;
import cn.dhbin.isme.common.auth.Roles;
import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.JobRecord;
import cn.dhbin.isme.geometray.domain.entity.Jobs;
import cn.dhbin.isme.geometray.domain.requeset.EndJobRequest;
import cn.dhbin.isme.geometray.domain.requeset.JobsPageRequest;
import cn.dhbin.isme.geometray.domain.requeset.StartJobRequest;
import cn.dhbin.isme.geometray.service.JobsService;
import cn.dhbin.isme.pms.domain.request.CreateRoleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/start_job")
    @Operation(summary = "开始作业任务")
    public R<JobRecord> startJob(@RequestBody StartJobRequest request) {
        JobRecord record = jobsService.startJob(request);
        return R.ok(record);
    }

    @PostMapping("/end_job")
    @Operation(summary = "结束作业任务")
    public R<JobRecord> endJob(
            @RequestBody EndJobRequest request
    ) {
        JobRecord record = jobsService.endJob(request);
        return R.ok(record);
    }


}
