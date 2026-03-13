package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.Jobs;
import cn.dhbin.isme.geometray.domain.requeset.JobsPageRequest;
import cn.dhbin.isme.geometray.mapper.JobsMapper;
import cn.dhbin.isme.geometray.service.JobsService;
import cn.dhbin.isme.geometray.service.impl.ReportChartService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deepoove.poi.XWPFTemplate;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobsServiceImpl implements JobsService {
    @Resource
    private JobsMapper jobsMapper;

    @Resource
    private ReportChartService reportChartService;

    // 模板地址
    @Value("${generate-report.template-url}")
    private String templateUrl;
    // 报表输出地址
    @Value("${generate-report.output-url}")
    private String outputUrl;

    @Override
    public Page<Jobs> queryPage(JobsPageRequest request) {
        // 转换为MyBatis-Plus的分页对象
        IPage<Jobs> page = request.toPage();

        // 创建查询条件
        LambdaQueryWrapper<Jobs> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件，注意判空
        queryWrapper
                .eq(StrUtil.isNotBlank(request.getLineType()), Jobs::getLineType, request.getLineType())
                .eq(StrUtil.isNotBlank(request.getDirection()), Jobs::getDirection, request.getDirection())
                .eq(StrUtil.isNotBlank(request.getJobName()), Jobs::getJobName, request.getJobName())
                .eq(request.getStartTime() != null, Jobs::getStartTime, request.getStartTime())
                .eq(request.getEndTime() != null, Jobs::getEndTime, request.getEndTime())
                .eq(StrUtil.isNotBlank(request.getDeviceId()), Jobs::getDeviceId, request.getDeviceId())
                .eq(StrUtil.isNotBlank(request.getOperator()), Jobs::getOperator, request.getOperator())
                .like(StrUtil.isNotBlank(request.getDescription()), Jobs::getDescription, request.getDescription())
                .eq(request.getCreatedAt() != null, Jobs::getCreatedAt, request.getCreatedAt())
                // 按 id 倒序排列，确保 id 越大的任务排在越前面
                .orderByDesc(Jobs::getId);

        // 执行分页查询
        IPage<Jobs> jobsPage = getBaseMapper().selectPage(page, queryWrapper);

        // 转换结果并返回
        return Page.convert(jobsPage);
    }

    @Override
    public Jobs saveJob(Jobs job) {
        if (job.getId() == null) {
            // 新增 - 使用 insertSelective 可以避免插入 null 值
            // 但 MyBatis-Plus 的 insert 方法也会正确处理 null 值
            jobsMapper.insert(job);
        } else {
            // 更新 - 使用 updateById 会更新所有字段，包括 null 值
            // 如果需要只更新非 null 字段，可以使用 updateByIdSelective，但 MyBatis-Plus 默认没有这个方法
            jobsMapper.updateById(job);
        }
        return job;
    }

    @Override
    public byte[] generateReportZip(List<Integer> ids, List<Map<String, String>> forms) {
        List<Jobs> jobs = jobsMapper.selectBatchIds(ids);

        if (jobs.isEmpty()) {
            throw new RuntimeException("没有找到选中的任务");
        }

        // 获取第一个任务的表单数据（统一表单，应用到所有任务）
        Map<String, String> formData = null;
        if (forms != null && !forms.isEmpty()) {
            // 使用第一个表单数据作为统一表单
            formData = forms.get(0);
            // 移除jobId字段，因为这是统一表单
            if (formData != null) {
                formData = new HashMap<>(formData);
                formData.remove("jobId");
            }
        }

        InputStream templateStream = null;
        try {
            // 加载模板文件
            templateStream = new FileInputStream(templateUrl);

            if (templateStream == null) {
                throw new RuntimeException("模板文件未找到: " + templateUrl);
            }

            // 使用第一个任务作为主任务（用于基本信息），但合并所有任务的数据
            Jobs mainJob = jobs.get(0);
            
            // 使用新的服务生成带图表的文档（合并所有任务的数据）
            byte[] documentBytes = reportChartService.generateDocumentWithChartForMultipleJobs(
                    mainJob, jobs, formData, templateStream);

            return documentBytes;

        } catch (Exception e) {
            System.err.println("生成报表失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("生成报表失败: " + e.getMessage(), e);
        } finally {
            // 确保流被关闭
            if (templateStream != null) {
                try {
                    templateStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 创建ZIP文件
     */
    private byte[] createZipFile(List<String> files) {
        String zipFilePath = outputUrl + "reports_" + System.currentTimeMillis() + ".zip";
        try {
            java.util.zip.ZipOutputStream zipOut = new java.util.zip.ZipOutputStream(
                    Files.newOutputStream(Paths.get(zipFilePath))
            );
            byte[] buffer = new byte[1024];

            for (String filePath : files) {
                String fileName = Paths.get(filePath).getFileName().toString();
                java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(fileName);
                zipOut.putNextEntry(zipEntry);

                try (FileInputStream fis = new FileInputStream(filePath)) {
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zipOut.write(buffer, 0, length);
                    }
                }
                zipOut.closeEntry();
            }
            zipOut.close();

            // 读取zip文件并返回字节数组
            byte[] zipBytes = Files.readAllBytes(Paths.get(zipFilePath));

            // 清理临时文件
            cleanupTempFiles(files, zipFilePath);

            return zipBytes;
        } catch (Exception e) {
            throw new RuntimeException("创建zip文件失败", e);
        }
    }

    /**
     * 清理临时文件
     */
    private void cleanupTempFiles(List<String> docFiles, String zipFilePath) {
        try {
            // 删除生成的docx文件
            for (String filePath : docFiles) {
                Files.deleteIfExists(Paths.get(filePath));
            }
            // 删除zip文件
            Files.deleteIfExists(Paths.get(zipFilePath));
        } catch (Exception e) {
            // 清理失败不影响主流程，只记录日志
            System.err.println("清理临时文件失败: " + e.getMessage());
        }
    }

    // 其他方法保持不变...
    /**
     * @param entityList
     * @param batchSize
     * @return
     */
    @Override
    public boolean saveBatch(Collection<Jobs> entityList, int batchSize) {
        return false;
    }

    /**
     * @param entityList
     * @param batchSize
     * @return
     */
    @Override
    public boolean saveOrUpdateBatch(Collection<Jobs> entityList, int batchSize) {
        return false;
    }

    /**
     * @param entityList
     * @param batchSize
     * @return
     */
    @Override
    public boolean updateBatchById(Collection<Jobs> entityList, int batchSize) {
        return false;
    }

    /**
     * @param entity
     * @return
     */
    @Override
    public boolean saveOrUpdate(Jobs entity) {
        return false;
    }

    /**
     * @param queryWrapper
     * @param throwEx
     * @return
     */
    @Override
    public Jobs getOne(Wrapper<Jobs> queryWrapper, boolean throwEx) {
        return null;
    }

    /**
     * @param queryWrapper
     * @param throwEx
     * @return
     */
    @Override
    public Optional<Jobs> getOneOpt(Wrapper<Jobs> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    /**
     * @param queryWrapper
     * @return
     */
    @Override
    public Map<String, Object> getMap(Wrapper<Jobs> queryWrapper) {
        return Map.of();
    }

    /**
     * @param queryWrapper
     * @param mapper
     * @param <V>
     * @return
     */
    @Override
    public <V> V getObj(Wrapper<Jobs> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public BaseMapper<Jobs> getBaseMapper() {
        return jobsMapper;
    }

    /**
     * @return
     */
    @Override
    public Class<Jobs> getEntityClass() {
        return null;
    }
}