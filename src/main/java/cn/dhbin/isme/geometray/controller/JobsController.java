package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.response.BizResponseCode;
import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.Jobs;
import cn.dhbin.isme.geometray.domain.requeset.JobsPageRequest;
import cn.dhbin.isme.geometray.service.JobsService;
import cn.dhbin.isme.rabbitmqconsumer.RabbitMQControlProducer;
import cn.dhbin.isme.rabbitmqconsumer.JobIdManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "作业任务")
public class JobsController {

    @Resource
    JobsService jobsService;
    
    @Resource
    RabbitMQControlProducer controlProducer;
    
    @Resource
    JobIdManager jobIdManager;

    @GetMapping("/all_jobs")
    @Operation(summary = "获取所有作业信息")
    public R<Page<Jobs>> findAll(JobsPageRequest request) {
        Page<Jobs> jobsPage = jobsService.queryPage(request);
        return R.ok(jobsPage);
    }

    @PostMapping("/generate_report")
    @Operation(summary = "生成选定作业数据报表")
    public ResponseEntity<byte[]> generateReport(@RequestBody Map<String, Object> request) {
        List<Integer> ids = (List<Integer>) request.get("ids");
        List<Map<String, String>> forms = (List<Map<String, String>>) request.get("forms");
        byte[] docxFile = jobsService.generateReportZip(ids, forms);
        
        // 生成文件名（使用当前日期和第一个任务ID）
        String fileName = "report_" + java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".docx";
        if (ids != null && !ids.isEmpty()) {
            fileName = "report_" + ids.get(0) + "_" + 
                java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".docx";
        }
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .body(docxFile);
    }
    
    @PostMapping("/start_job")
    @Operation(summary = "开始采集作业")
    public R<Jobs> startJob(@RequestBody Map<String, Object> request) {
        try {
            // 1. 获取或创建作业记录
            Jobs job = null;
            Long jobId = null;
            
            // 优先使用id字段，如果没有则使用jobId
            if (request.get("id") != null) {
                jobId = Long.valueOf(request.get("id").toString());
            } else if (request.get("jobId") != null) {
                jobId = Long.valueOf(request.get("jobId").toString());
            }
            
            // 如果提供了ID，尝试从数据库获取现有任务
            if (jobId != null) {
                job = jobsService.getById(jobId);
            }
            
            // 如果任务不存在，创建新任务
            if (job == null) {
                job = new Jobs();
                if (jobId != null) {
                    job.setId(jobId);
                }
            }
            
            // 检查任务是否已开始
            if (job.getStartTime() != null && job.getEndTime() == null) {
                R<Jobs> r = new R<>();
                r.setCode(BizResponseCode.ERR_400.getCode());
                r.setMessage("该任务已开始，不能重复开始");
                r.setData(null);
                return r;
            }
            
            // 检查任务是否已结束
            if (job.getEndTime() != null) {
                R<Jobs> r = new R<>();
                r.setCode(BizResponseCode.ERR_400.getCode());
                r.setMessage("该任务已结束，不能再次开始");
                r.setData(null);
                return r;
            }
            
            // 更新任务信息（如果提供了新值）
            if (request.get("jobName") != null) {
                job.setJobName(request.get("jobName").toString());
            }
            if (request.get("operator") != null) {
                job.setOperator(request.get("operator").toString());
            }
            if (request.get("deviceId") != null) {
                job.setDeviceId(request.get("deviceId").toString());
            }
            if (request.get("lineType") != null) {
                job.setLineType(request.get("lineType").toString());
            }
            if (request.get("direction") != null) {
                job.setDirection(request.get("direction").toString());
            }
            if (request.get("speed") != null) {
                job.setSpeed(request.get("speed").toString());
            }
            
            // 设置开始时间为当前时间（点击开始检测时的时间）
            if (request.get("startTime") != null) {
                try {
                    String startTimeStr = request.get("startTime").toString();
                    // 处理ISO格式：YYYY-MM-DDTHH:mm:ss
                    if (startTimeStr.contains("T")) {
                        startTimeStr = startTimeStr.replace("Z", "");
                        // 如果包含时区信息，先移除
                        if (startTimeStr.contains("+") || (startTimeStr.lastIndexOf("-") > startTimeStr.indexOf("T"))) {
                            int tIndex = startTimeStr.indexOf("T");
                            int zoneIndex = startTimeStr.indexOf("+");
                            if (zoneIndex == -1) {
                                // 查找最后一个-（可能是时区）
                                int lastDash = startTimeStr.lastIndexOf("-");
                                if (lastDash > tIndex + 5) { // 确保不是日期部分的-
                                    zoneIndex = lastDash;
                                }
                            }
                            if (zoneIndex > tIndex) {
                                startTimeStr = startTimeStr.substring(0, zoneIndex);
                            }
                        }
                        job.setStartTime(java.time.LocalDateTime.parse(startTimeStr));
                    } else {
                        job.setStartTime(java.time.LocalDateTime.parse(startTimeStr));
                    }
                } catch (Exception e) {
                    // 如果解析失败，使用当前时间
                    job.setStartTime(java.time.LocalDateTime.now());
                }
            } else {
                // 如果没有提供开始时间，使用当前时间
                job.setStartTime(java.time.LocalDateTime.now());
            }
            
            // 如果是新任务，设置创建时间
            if (job.getCreatedAt() == null) {
                job.setCreatedAt(java.time.LocalDateTime.now());
            }
            
            // 保存或更新作业记录
            Jobs savedJob = jobsService.saveJob(job);
            
            // 2. 设置当前任务ID
            jobIdManager.setCurrentJobId(savedJob.getId());
            
            // 3. 发送开始采集命令到RabbitMQ（传递任务ID）
            boolean sent = controlProducer.sendStartCommand(savedJob.getId());
            if (!sent) {
                R<Jobs> r = new R<>();
                r.setCode(BizResponseCode.ERR_400.getCode());
                r.setMessage("发送开始采集命令失败");
                r.setData(null);
                return r;
            }
            
            return R.ok(savedJob);
        } catch (Exception e) {
            R<Jobs> r = new R<>();
            r.setCode(BizResponseCode.ERR_400.getCode());
            r.setMessage("开始采集作业失败: " + e.getMessage());
            r.setData(null);
            return r;
        }
    }
    
    @PostMapping("/create")
    @Operation(summary = "创建新任务")
    public R<Jobs> createJob(@RequestBody Map<String, Object> request) {
        try {
            Jobs job = new Jobs();
            if (request.get("jobName") != null) {
                job.setJobName(request.get("jobName").toString());
            }
            if (request.get("operator") != null) {
                job.setOperator(request.get("operator").toString());
            }
            if (request.get("deviceId") != null) {
                job.setDeviceId(request.get("deviceId").toString());
            }
            if (request.get("lineType") != null) {
                job.setLineType(request.get("lineType").toString());
            }
            if (request.get("direction") != null) {
                job.setDirection(request.get("direction").toString());
            }
            if (request.get("speed") != null) {
                job.setSpeed(request.get("speed").toString());
            }
            if (request.get("description") != null) {
                job.setDescription(request.get("description").toString());
            }
            if (request.get("lineId") != null) {
                job.setLineId(Long.valueOf(request.get("lineId").toString()));
            }
            if (request.get("startTime") != null) {
                try {
                    String startTimeStr = request.get("startTime").toString();
                    // 处理ISO格式：YYYY-MM-DDTHH:mm:ss 或 YYYY-MM-DDTHH:mm:ssZ
                    if (startTimeStr.contains("T")) {
                        // 移除Z后缀（如果有）
                        startTimeStr = startTimeStr.replace("Z", "");
                        // 如果包含时区信息，先移除
                        if (startTimeStr.contains("+") || startTimeStr.contains("-")) {
                            int tIndex = startTimeStr.indexOf("T");
                            int zoneIndex = startTimeStr.indexOf("+");
                            if (zoneIndex == -1) zoneIndex = startTimeStr.indexOf("-", tIndex + 1);
                            if (zoneIndex > tIndex) {
                                startTimeStr = startTimeStr.substring(0, zoneIndex);
                            }
                        }
                        job.setStartTime(java.time.LocalDateTime.parse(startTimeStr));
                    } else {
                        // 尝试解析其他格式
                        job.setStartTime(java.time.LocalDateTime.parse(startTimeStr));
                    }
                } catch (Exception e) {
                    job.setStartTime(java.time.LocalDateTime.now());
                }
            }
            if (request.get("endTime") != null && !request.get("endTime").toString().trim().isEmpty()) {
                try {
                    String endTimeStr = request.get("endTime").toString();
                    if (endTimeStr.contains("T")) {
                        // 移除Z后缀（如果有）
                        endTimeStr = endTimeStr.replace("Z", "");
                        // 如果包含时区信息，先移除
                        if (endTimeStr.contains("+") || endTimeStr.contains("-")) {
                            int tIndex = endTimeStr.indexOf("T");
                            int zoneIndex = endTimeStr.indexOf("+");
                            if (zoneIndex == -1) zoneIndex = endTimeStr.indexOf("-", tIndex + 1);
                            if (zoneIndex > tIndex) {
                                endTimeStr = endTimeStr.substring(0, zoneIndex);
                            }
                        }
                        job.setEndTime(java.time.LocalDateTime.parse(endTimeStr));
                    } else {
                        job.setEndTime(java.time.LocalDateTime.parse(endTimeStr));
                    }
                } catch (Exception e) {
                    // 结束时间解析失败，可以为空
                }
            }
            job.setCreatedAt(java.time.LocalDateTime.now());
            
            // 保存作业记录
            Jobs savedJob = jobsService.saveJob(job);
            return R.ok(savedJob);
        } catch (Exception e) {
            R<Jobs> r = new R<>();
            r.setCode(BizResponseCode.ERR_400.getCode());
            r.setMessage("创建任务失败: " + e.getMessage());
            r.setData(null);
            return r;
        }
    }
    
    @PostMapping("/end_job")
    @Operation(summary = "结束采集作业")
    public R<Void> endJob(@RequestBody Map<String, Object> request) {
        try {
            // 1. 获取当前任务ID
            Long currentJobId = jobIdManager.getCurrentJobId();
            
            // 2. 更新作业记录（优先使用请求中的ID，否则使用当前任务ID）
            Long jobId = null;
            if (request.get("id") != null) {
                jobId = Long.valueOf(request.get("id").toString());
            } else if (currentJobId != null) {
                jobId = currentJobId;
            }
            
            if (jobId != null) {
                Jobs job = jobsService.getById(jobId);
                if (job != null) {
                    job.setEndTime(java.time.LocalDateTime.now());
                    jobsService.updateById(job);
                }
            }
            
            // 3. 清除当前任务ID
            jobIdManager.clearCurrentJobId();
            
            // 4. 发送结束采集命令到RabbitMQ
            boolean sent = controlProducer.sendStopCommand();
            if (!sent) {
                R<Void> r = new R<>();
                r.setCode(BizResponseCode.ERR_400.getCode());
                r.setMessage("发送结束采集命令失败");
                r.setData(null);
                return r;
            }
            
            return R.ok(null);
        } catch (Exception e) {
            R<Void> r = new R<>();
            r.setCode(BizResponseCode.ERR_400.getCode());
            r.setMessage("结束采集作业失败: " + e.getMessage());
            r.setData(null);
            return r;
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除作业任务")
    public R<Void> deleteJob(@PathVariable Long id) {
        try {
            boolean deleted = jobsService.removeById(id);
            if (deleted) {
                return R.ok(null);
            } else {
                R<Void> r = new R<>();
                r.setCode(BizResponseCode.ERR_400.getCode());
                r.setMessage("删除失败，任务不存在");
                r.setData(null);
                return r;
            }
        } catch (Exception e) {
            R<Void> r = new R<>();
            r.setCode(BizResponseCode.ERR_400.getCode());
            r.setMessage("删除任务失败: " + e.getMessage());
            r.setData(null);
            return r;
        }
    }
}