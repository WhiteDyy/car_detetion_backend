package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.geometray.domain.entity.Jobs;
import cn.dhbin.isme.geometray.utils.ChartGenerator;
import com.deepoove.poi.XWPFTemplate;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportChartService {

    /**
     * 生成带图表的文档 - 修复版本
     */
    public byte[] generateDocumentWithChart(Jobs job, Map<String, String> formData,
                                            InputStream templateStream) {
        try {
            // 准备模板数据
            Map<String, Object> data = prepareTemplateData(job, formData);

            // 使用模板生成基础文档
            XWPFTemplate template = XWPFTemplate.compile(templateStream).render(data);

            // 将XWPFTemplate转换为XWPFDocument以便添加图表
            ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
            template.write(templateOut);
            template.close();

            // 创建包含图表的文档
            return createDocumentWithCharts(job, templateOut.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("生成带图表的文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 准备模板数据
     */
    private Map<String, Object> prepareTemplateData(Jobs job, Map<String, String> formData) {
        Map<String, Object> data = new HashMap<>();

        // 添加任务数据
        data.put("jobName", job.getJobName() != null ? job.getJobName() : "");
        data.put("lineType", job.getLineType() != null ? job.getLineType() : "");
        data.put("direction", job.getDirection() != null ? job.getDirection() : "");
        data.put("deviceId", job.getDeviceId() != null ? job.getDeviceId() : "");
        data.put("operator", job.getOperator() != null ? job.getOperator() : "");
        data.put("description", job.getDescription() != null ? job.getDescription() : "");

        // 格式化日期
        if (job.getStartTime() != null) {
            data.put("startTime", job.getStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        } else {
            data.put("startTime", "");
        }

        if (job.getEndTime() != null) {
            data.put("endTime", job.getEndTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        } else {
            data.put("endTime", "");
        }

        if (job.getCreatedAt() != null) {
            data.put("createdAt", job.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        } else {
            data.put("createdAt", "");
        }

        // 添加表单数据
        if (formData != null) {
            data.putAll(formData);
        }

        // 添加创建日期
        LocalDate createDate = LocalDate.now();
        String formattedDate = createDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        data.put("createDate", formattedDate);

        return data;
    }

    /**
     * 创建包含图表的文档
     */
    private byte[] createDocumentWithCharts(Jobs job, byte[] templateBytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(templateBytes);
             XWPFDocument document = new XWPFDocument(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 在文档末尾添加分页符
            addPageBreak(document);

            // 添加图表部分标题
            addSectionTitle(document, "检验统计图表");

            // 生成检验统计图表
            ChartGenerator.createInspectionChart(document,
                    job.getJobName() != null ? job.getJobName() : "未命名任务");

            // 保存文档
            document.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("创建包含图表的文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加分页符
     */
    private void addPageBreak(XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.addBreak(org.apache.poi.xwpf.usermodel.BreakType.PAGE);
    }

    /**
     * 添加小节标题
     */
    private void addSectionTitle(XWPFDocument document, String title) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setPageBreak(true); // 确保在新页面开始

        XWPFRun run = paragraph.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(16);
        run.setFontFamily("宋体");
        run.addBreak();

        // 添加空行
        document.createParagraph();
    }
}