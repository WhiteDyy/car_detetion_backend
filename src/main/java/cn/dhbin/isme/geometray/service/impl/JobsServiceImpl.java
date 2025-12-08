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
                .eq(request.getCreatedAt() != null, Jobs::getCreatedAt, request.getCreatedAt());

        // 执行分页查询
        IPage<Jobs> jobsPage = getBaseMapper().selectPage(page, queryWrapper);

        // 转换结果并返回
        return Page.convert(jobsPage);
    }

    @Override
    public byte[] generateReportZip(List<Integer> ids, List<Map<String, String>> forms) {
        // 创建一个列表，用于存储生成的文件路径
        List<String> files = new ArrayList<>();
        List<Jobs> jobs = jobsMapper.selectBatchIds(ids);

        // 将forms转换为以jobId为键的Map，便于快速查找
        Map<String, Map<String, String>> formMap = forms.stream()
                .collect(Collectors.toMap(
                        form -> String.valueOf(form.get("jobId")),
                        form -> form
                ));

        for (Jobs job : jobs) {
            InputStream templateStream = null;
            try {
                // 加载模板文件
                templateStream = new FileInputStream(templateUrl);

                if (templateStream == null) {
                    System.err.println("模板文件未找到");
                    continue;
                }

                // 获取对应的表单数据
                Map<String, String> formData = formMap.get(String.valueOf(job.getId()));

                // 使用新的服务生成带图表的文档
                byte[] documentBytes = reportChartService.generateDocumentWithChart(job, formData, templateStream);

                // 输出文件，生成报表
                String outputPath = outputUrl + "report_" + job.getId() + "_" + System.currentTimeMillis() + ".docx";
                Files.write(Paths.get(outputPath), documentBytes);
                files.add(outputPath);

            } catch (Exception e) {
                System.err.println("生成任务 " + job.getId() + " 的报表失败: " + e.getMessage());
                e.printStackTrace();
                // 继续处理其他任务
                continue;
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

        if (files.isEmpty()) {
            throw new RuntimeException("没有成功生成任何报表文件");
        }

        // 将所有生成的docx文件打包成zip并返回字节数组
        return createZipFile(files);
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