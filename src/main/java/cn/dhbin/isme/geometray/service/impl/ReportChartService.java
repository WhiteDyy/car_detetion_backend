package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.geometray.domain.entity.Jobs;
import cn.dhbin.isme.geometray.utils.ChartGenerator;
import cn.dhbin.isme.rabbitmqconsumer.GeometryResultEntity;
import cn.dhbin.isme.rabbitmqconsumer.GeometryResultRepository;
import cn.dhbin.isme.rabbitmqconsumer.SensorData;
import cn.dhbin.isme.rabbitmqconsumer.SensorDataRepository;
import cn.dhbin.isme.rabbitmqconsumer.SensorStatus;
import cn.dhbin.isme.rabbitmqconsumer.SensorStatusRepository;
import com.deepoove.poi.XWPFTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportChartService {

    @Autowired
    private GeometryResultRepository geometryResultRepository;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @Autowired
    private SensorStatusRepository sensorStatusRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成带图表的文档 - 合并多个任务的数据到一个文档
     */
    public byte[] generateDocumentWithChartForMultipleJobs(Jobs mainJob, List<Jobs> allJobs, 
                                                          Map<String, String> formData,
                                                          InputStream templateStream) {
        try {
            log.info("========== 开始生成合并报表 ==========");
            log.info("主任务ID: {}, 任务名称: {}", mainJob.getId(), mainJob.getJobName());
            log.info("合并任务数量: {}", allJobs.size());
            log.info("表单数据: {}", formData);
            
            // 准备模板数据（使用主任务的基本信息，但合并所有任务的数据）
            Map<String, Object> data = prepareTemplateDataForMultipleJobs(mainJob, allJobs, formData);
            log.info("准备的模板数据键数量: {}", data.size());
            log.info("模板数据键列表: {}", data.keySet());
            
            // 输出前20个数据项用于调试
            int count = 0;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (count++ < 20) {
                    log.info("数据映射: {} = {}", entry.getKey(), entry.getValue());
                }
            }
            if (data.size() > 20) {
                log.info("... 还有 {} 个数据项未显示", data.size() - 20);
            }
            
            // 预处理模板：将 ${} 格式转换为 {{}} 格式（poi-tl要求的格式）
            log.info("开始预处理模板，将 ${} 格式转换为 {{}} 格式...");
            InputStream processedTemplateStream = convertPlaceholderFormat(templateStream);
            log.info("模板预处理完成");
            
            // 使用模板生成基础文档
            log.info("开始编译模板...");
            XWPFTemplate template = XWPFTemplate.compile(processedTemplateStream);
            
            // 检查模板中识别到的占位符数量
            // 注意：poi-tl使用{{key}}格式，不是${key}格式
            log.info("模板编译完成，开始渲染数据...");
            
            template.render(data);
            log.info("模板渲染完成");

            // 将XWPFTemplate转换为XWPFDocument以便添加图表和表格
            ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
            template.write(templateOut);
            template.close();
            log.info("模板已转换为字节数组，大小: {} bytes", templateOut.size());

            // 创建包含图表和表格的文档（合并所有任务的数据）
            log.info("开始添加图表和表格（合并所有任务数据）...");
            byte[] result = createDocumentWithChartsAndTablesForMultipleJobs(allJobs, templateOut.toByteArray(), formData);
            log.info("报表生成完成，最终大小: {} bytes", result.length);
            log.info("========== 报表生成结束 ==========");
            return result;

        } catch (Exception e) {
            log.error("生成合并报表失败", e);
            e.printStackTrace();
            throw new RuntimeException("生成合并报表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成带图表的文档 - 完整版本
     */
    public byte[] generateDocumentWithChart(Jobs job, Map<String, String> formData,
                                            InputStream templateStream) {
        try {
            log.info("========== 开始生成报表 ==========");
            log.info("任务ID: {}, 任务名称: {}", job.getId(), job.getJobName());
            log.info("表单数据: {}", formData);
            
            // 准备模板数据（包含报告编号、时间等）
            Map<String, Object> data = prepareTemplateData(job, formData);
            log.info("准备的模板数据键数量: {}", data.size());
            log.info("模板数据键列表: {}", data.keySet());
            
            // 输出所有数据用于调试（只输出前20个，避免日志过长）
            int count = 0;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (count++ < 20) {
                    log.info("数据映射: {} = {}", entry.getKey(), entry.getValue());
                }
            }
            if (data.size() > 20) {
                log.info("... 还有 {} 个数据项未显示", data.size() - 20);
            }
            
            // 输出部分关键数据用于调试
            log.info("关键数据检查:");
            log.info("  - jobName: {}", data.get("jobName"));
            log.info("  - inspectionType: {}", data.get("inspectionType"));
            log.info("  - mainEquipment: {}", data.get("mainEquipment"));
            log.info("  - inspectionItem: {}", data.get("inspectionItem"));
            log.info("  - inspectionLocation: {}", data.get("inspectionLocation"));
            log.info("  - inspector: {}", data.get("inspector"));
            log.info("  - reportNumber: {}", data.get("reportNumber"));

            // 预处理模板：将 ${} 格式转换为 {{}} 格式（poi-tl要求的格式）
            log.info("开始预处理模板，将 ${} 格式转换为 {{}} 格式...");
            InputStream processedTemplateStream = convertPlaceholderFormat(templateStream);
            log.info("模板预处理完成");
            
            // 使用模板生成基础文档
            log.info("开始编译模板...");
            XWPFTemplate template = XWPFTemplate.compile(processedTemplateStream);
            log.info("模板编译完成，开始渲染数据...");
            
            template.render(data);
            log.info("模板渲染完成");

            // 将XWPFTemplate转换为XWPFDocument以便添加图表和表格
            ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
            template.write(templateOut);
            template.close();
            log.info("模板已转换为字节数组，大小: {} bytes", templateOut.size());

            // 创建包含图表和表格的文档
            log.info("开始添加图表和表格...");
            byte[] result = createDocumentWithChartsAndTables(job, templateOut.toByteArray(), formData);
            log.info("报表生成完成，最终大小: {} bytes", result.length);
            log.info("========== 报表生成结束 ==========");
            return result;

        } catch (Exception e) {
            log.error("生成带图表的文档失败", e);
            e.printStackTrace();
            throw new RuntimeException("生成带图表的文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 准备模板数据
     * 将任务数据和表单数据合并，用于填充Word模板
     * 注意：表单数据中的字段名需要与模板中的占位符名称匹配
     */
    private Map<String, Object> prepareTemplateData(Jobs job, Map<String, String> formData) {
        Map<String, Object> data = new HashMap<>();

        // ========== 任务基本信息 ==========
        data.put("jobName", job.getJobName() != null ? job.getJobName() : "");
        data.put("lineType", job.getLineType() != null ? job.getLineType() : "");
        data.put("direction", job.getDirection() != null ? job.getDirection() : "");
        data.put("deviceId", job.getDeviceId() != null ? job.getDeviceId() : "");
        data.put("operator", job.getOperator() != null ? job.getOperator() : "");
        data.put("description", job.getDescription() != null ? job.getDescription() : "");
        data.put("speed", job.getSpeed() != null ? job.getSpeed() : "");

        // ========== 日期格式化 ==========
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        
        // 检测日期（使用开始时间，如果没有则使用当前日期）
        LocalDate inspectionDate = job.getStartTime() != null ? job.getStartTime().toLocalDate() : today;
        data.put("inspectionDate", inspectionDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        data.put("inspectionDateYYYYMMDD", inspectionDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        
        // 开始时间
        if (job.getStartTime() != null) {
            data.put("startTime", job.getStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
            data.put("startTimeDetail", job.getStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
            data.put("startTimeYYYYMMDD", job.getStartTime().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        } else {
            data.put("startTime", "");
            data.put("startTimeDetail", "");
            data.put("startTimeYYYYMMDD", today.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        }

        // 结束时间
        if (job.getEndTime() != null) {
            data.put("endTime", job.getEndTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
            data.put("endTimeDetail", job.getEndTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
        } else {
            data.put("endTime", "");
            data.put("endTimeDetail", "");
        }

        // 创建时间
        if (job.getCreatedAt() != null) {
            data.put("createdAt", job.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        } else {
            data.put("createdAt", "");
        }

        // ========== 表单数据（统一表单，应用到所有任务）==========
        if (formData != null) {
            log.info("处理表单数据，表单数据项数: {}", formData.size());
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                if (!"jobId".equals(entry.getKey())) {
                    String value = entry.getValue() != null ? entry.getValue() : "";
                    data.put(entry.getKey(), value);
                    log.info("添加表单数据到模板: {} = {}", entry.getKey(), value);
                }
            }
            
            // 确保所有表单字段都被设置（即使值为空）
            if (!data.containsKey("inspectionType")) {
                data.put("inspectionType", formData.getOrDefault("inspectionType", ""));
            }
            if (!data.containsKey("mainEquipment")) {
                data.put("mainEquipment", formData.getOrDefault("mainEquipment", ""));
            }
            if (!data.containsKey("inspectionItem")) {
                data.put("inspectionItem", formData.getOrDefault("inspectionItem", ""));
            }
            if (!data.containsKey("inspectionLocation")) {
                data.put("inspectionLocation", formData.getOrDefault("inspectionLocation", ""));
            }
            if (!data.containsKey("inspector")) {
                data.put("inspector", formData.getOrDefault("inspector", ""));
            }
            if (!data.containsKey("clientAddress")) {
                data.put("clientAddress", formData.getOrDefault("clientAddress", ""));
            }
        } else {
            log.warn("表单数据为空，将无法填充表单字段");
            // 即使表单数据为空，也设置默认空值，避免模板占位符显示错误
            data.put("inspectionType", "");
            data.put("mainEquipment", "");
            data.put("inspectionItem", "");
            data.put("inspectionLocation", "");
            data.put("inspector", "");
            data.put("clientAddress", "");
        }

        // ========== 系统生成数据 ==========
        // 报表生成日期（当前日期）
        String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        data.put("createDate", formattedDate);
        data.put("reportGenerateDate", formattedDate);
        
        // 报表生成时间（详细时间）
        String formattedDateTime = now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"));
        data.put("reportGenerateDateTime", formattedDateTime);
        
        // 报告签发日期（年、月、日分别提供）
        data.put("reportSignYear", String.valueOf(today.getYear()));
        data.put("reportSignMonth", String.valueOf(today.getMonthValue()));
        data.put("reportSignDay", String.valueOf(today.getDayOfMonth()));

        // ========== 报告编号生成 ==========
        // 主报告编号格式：TLK-XCXJY-2.1-20250101-线路名称-1
        String reportNumber = generateMainReportNumber(job, inspectionDate);
        data.put("reportNumber", reportNumber);
        
        // 线路名称（从任务名称或线路类型中提取）
        String lineName = extractLineName(job);
        data.put("lineName", lineName);
        data.put("lineNameForReport", lineName.isEmpty() ? "线路" : lineName);
        
        // 报告序号（用于报告编号）
        String reportSequence = String.valueOf(job.getId() != null ? job.getId() : 1);
        data.put("reportSequence", reportSequence);
        
        // 生成各个附录的报告编号
        // 格式：1-20250101-线路名称-1-5Km/h
        String speedStr = job.getSpeed() != null ? job.getSpeed() + "Km/h" : "5Km/h";
        data.put("reportNumber1", String.format("1-%s-%s-%s-%s", 
                inspectionDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                lineName.isEmpty() ? "线路" : lineName,
                reportSequence,
                speedStr));
        data.put("reportNumber2", String.format("2-%s", reportSequence));
        data.put("reportNumber3", String.format("3-%s", reportSequence));
        data.put("reportNumber4", String.format("4-%s", reportSequence));
        data.put("reportNumber5", String.format("5-%s", reportSequence));

        // ========== 机构信息 ==========
        data.put("companyName", "成都铁联科科技有限公司");
        data.put("department", "实验检测部");
        data.put("unitName", "成都铁联科科技有限公司试验检测工程中心");
        data.put("unitAddress", formData != null && formData.containsKey("clientAddress") && 
                 formData.get("clientAddress") != null && !formData.get("clientAddress").isEmpty() 
                 ? formData.get("clientAddress") : "成都高新区益州大道中段588号");
        data.put("phone", "13880454258");
        data.put("postalCode", "610095");
        data.put("email", "fdingjianming@126.com");
        data.put("productName", "线岔一体化测量轨道巡检仪");
        data.put("equipmentName", "线岔一体化测量轨道巡检仪");
        data.put("equipmentModel", "TLK-XCXJY-2.1");
        data.put("equipmentCount", "1台");
        data.put("equipmentStatus", "正常");
        data.put("standardNumber", "TB/T 3147-2020");
        data.put("standardName", "TB/T 3147-2020《轨道几何状态检测》");
        data.put("clientCompany", "成都铁联科科技有限公司");
        data.put("sampleName", "轨道");

        // ========== 图表占位符（防止poi-tl清除）==========
        // 为图表占位符提供特殊占位符值，这样poi-tl不会清除它们
        // 后续会在replaceChartPlaceholders方法中替换为实际图表
        // 使用不同的标记来区分不同的图表类型
        data.put("chart_gauge", "[CHART_GAUGE]");
        data.put("chart_level", "[CHART_LEVEL]");
        data.put("chart_height_left", "[CHART_HEIGHT_LEFT]");
        data.put("chart_height_right", "[CHART_HEIGHT_RIGHT]");
        data.put("chart_height", "[CHART_HEIGHT]");
        data.put("chart_direction_left", "[CHART_DIRECTION_LEFT]");
        data.put("chart_direction_right", "[CHART_DIRECTION_RIGHT]");
        data.put("chart_direction", "[CHART_DIRECTION]");
        data.put("chart_height_4_1", "[CHART_HEIGHT_LEFT]");
        data.put("chart_height_4_2", "[CHART_HEIGHT_RIGHT]");
        data.put("chart_direction_5_1", "[CHART_DIRECTION_LEFT]");
        data.put("chart_direction_5_2", "[CHART_DIRECTION_RIGHT]");

        return data;
    }

    /**
     * 准备模板数据（多任务版本）
     * 包含所有任务的基本信息和选择的任务名称
     */
    private Map<String, Object> prepareTemplateDataForMultipleJobs(Jobs mainJob, List<Jobs> allJobs, Map<String, String> formData) {
        // 先使用主任务准备基础数据
        Map<String, Object> data = prepareTemplateData(mainJob, formData);
        
        // 添加选择的任务名称（最多3个）
        if (allJobs != null && !allJobs.isEmpty()) {
            for (int i = 0; i < Math.min(allJobs.size(), 3); i++) {
                Jobs job = allJobs.get(i);
                String jobName = job.getJobName() != null ? job.getJobName() : "";
                data.put("selectData" + (i + 1), jobName);
                log.info("添加选择的任务名称: selectData{} = {}", (i + 1), jobName);
            }
            
            // 如果任务数量少于3个，将剩余的设置为空字符串
            for (int i = allJobs.size(); i < 3; i++) {
                data.put("selectData" + (i + 1), "");
            }
        } else {
            // 如果没有任务，全部设置为空字符串
            data.put("selectData1", "");
            data.put("selectData2", "");
            data.put("selectData3", "");
        }
        
        return data;
    }

    /**
     * 生成主报告编号
     * 格式：TLK-XCXJY-2.1-20250101-线路名称-1
     * 注意：这里生成的是实际值，不是占位符
     */
    private String generateMainReportNumber(Jobs job, LocalDate inspectionDate) {
        String dateStr = inspectionDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String lineName = extractLineName(job);
        String lineNamePart = lineName.isEmpty() ? "线路" : lineName;
        // 序号暂时使用任务ID，实际应该根据业务规则生成
        String sequence = String.valueOf(job.getId() != null ? job.getId() : 1);
        // 生成完整的报告编号，不包含占位符
        return String.format("TLK-XCXJY-2.1-%s-%s-%s", dateStr, lineNamePart, sequence);
    }

    /**
     * 从任务中提取线路名称
     */
    private String extractLineName(Jobs job) {
        if (job.getJobName() != null && !job.getJobName().isEmpty()) {
            // 尝试从任务名称中提取线路名称
            return job.getJobName();
        }
        if (job.getLineType() != null && !job.getLineType().isEmpty()) {
            return job.getLineType();
        }
        return "";
    }

    /**
     * 创建包含图表和表格的文档 - 合并多个任务的数据
     */
    private byte[] createDocumentWithChartsAndTablesForMultipleJobs(List<Jobs> jobs, byte[] templateBytes, Map<String, String> formData) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(templateBytes);
             XWPFDocument document = new XWPFDocument(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            log.info("开始处理合并文档，任务数量: {}", jobs.size());
            
            // 创建超限率存储（线程安全，每次生成文档都是新的实例）
            Map<String, Double> overLimitRates = new HashMap<>();

            // 合并所有任务的几何数据
            List<GeometryResultEntity> allGeometryResults = new ArrayList<>();
            List<SensorData> allSensorDataList = new ArrayList<>();
            List<SensorStatus> allSensorStatusList = new ArrayList<>();

            for (Jobs job : jobs) {
                log.info("查询任务 {} 的数据...", job.getId());
                
                List<GeometryResultEntity> geometryResults = geometryResultRepository.findByJobId(job.getId());
                if (geometryResults != null) {
                    allGeometryResults.addAll(geometryResults);
                    log.info("任务 {} 的几何数据: {} 条", job.getId(), geometryResults.size());
                }
                
                List<SensorData> sensorDataList = sensorDataRepository.findByJobId(job.getId());
                if (sensorDataList != null) {
                    allSensorDataList.addAll(sensorDataList);
                    log.info("任务 {} 的传感器数据: {} 条", job.getId(), sensorDataList.size());
                }
                
                List<SensorStatus> sensorStatusList = sensorStatusRepository.findByJobId(job.getId());
                if (sensorStatusList != null) {
                    allSensorStatusList.addAll(sensorStatusList);
                    log.info("任务 {} 的传感器状态数据: {} 条", job.getId(), sensorStatusList.size());
                }
            }

            log.info("合并后的数据统计:");
            log.info("  - 几何数据总数: {}", allGeometryResults.size());
            log.info("  - 传感器数据总数: {}", allSensorDataList.size());
            log.info("  - 传感器状态数据总数: {}", allSensorStatusList.size());

            // 使用第一个任务作为主任务（用于生成图表标题等）
            Jobs mainJob = jobs.get(0);

            // 生成散点图（合并所有任务的数据）- 替换模板中的占位符
            log.info("开始生成散点图（合并数据）...");
            try {
                replaceChartPlaceholdersForMultipleJobs(document, jobs, allGeometryResults, overLimitRates);
                log.info("散点图生成完成");
            } catch (Exception e) {
                log.error("生成散点图失败", e);
                // 继续执行，不中断流程
            }

            // 填充原始数据完整性表格
            log.info("开始填充原始数据完整性表格...");
            try {
                fillDataIntegrityTable(document, mainJob, allSensorStatusList, allSensorDataList);
                log.info("原始数据完整性表格填充完成");
            } catch (Exception e) {
                log.error("填充原始数据完整性表格失败", e);
                // 继续执行，不中断流程
            }

            // 填充重复性检测结果表格
            log.info("开始填充重复性检测结果表格...");
            try {
                fillRepeatabilityTables(document, mainJob, allGeometryResults, formData);
                log.info("重复性检测结果表格填充完成");
            } catch (Exception e) {
                log.error("填充重复性检测结果表格失败", e);
                // 继续执行，不中断流程
            }

            // 填充超限率到文档
            log.info("开始填充超限率到文档...");
            try {
                fillOverLimitRatesToDocument(document, overLimitRates);
                log.info("超限率填充完成");
            } catch (Exception e) {
                log.error("填充超限率失败", e);
                // 继续执行，不中断流程
            }

            // 保存文档
            log.info("保存文档...");
            document.write(outputStream);
            byte[] result = outputStream.toByteArray();
            log.info("文档保存完成，大小: {} bytes", result.length);
            return result;

        } catch (Exception e) {
            log.error("创建合并文档失败", e);
            throw new RuntimeException("创建合并文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建包含图表和表格的文档
     */
    private byte[] createDocumentWithChartsAndTables(Jobs job, byte[] templateBytes, Map<String, String> formData) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(templateBytes);
             XWPFDocument document = new XWPFDocument(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

           
            
            // 创建超限率存储（线程安全，每次生成文档都是新的实例）
            Map<String, Double> overLimitRates = new HashMap<>();

            // 查询任务相关的数据
           
            List<GeometryResultEntity> geometryResults = geometryResultRepository.findByJobId(job.getId());
            
            List<SensorData> sensorDataList = sensorDataRepository.findByJobId(job.getId());
            
            List<SensorStatus> sensorStatusList = sensorStatusRepository.findByJobId(job.getId());

            // 生成散点图（轨距、水平、高低、轨向等）- 替换模板中的占位符
            try {
                replaceChartPlaceholders(document, job, geometryResults, overLimitRates);
            } catch (Exception e) {
                log.error("生成散点图失败", e);
                // 继续执行，不中断流程
            }

            // 填充原始数据完整性表格
            try {
                fillDataIntegrityTable(document, job, sensorStatusList, sensorDataList);
            } catch (Exception e) {
                log.error("填充原始数据完整性表格失败", e);
                // 继续执行，不中断流程
            }

            // 填充重复性检测结果表格
            try {
                fillRepeatabilityTables(document, job, geometryResults, formData);
            } catch (Exception e) {
                log.error("填充重复性检测结果表格失败", e);
                // 继续执行，不中断流程
            }

            // 填充超限率到文档
            try {
                fillOverLimitRatesToDocument(document, overLimitRates);
            } catch (Exception e) {
                log.error("填充超限率失败", e);
                // 继续执行，不中断流程
            }

            // 保存文档
            document.write(outputStream);
            byte[] result = outputStream.toByteArray();
            return result;

        } catch (Exception e) {
            log.error("创建包含图表和表格的文档失败", e);
            throw new RuntimeException("创建包含图表和表格的文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 替换模板中的图表占位符（多任务版本）
     * 查找模板中的占位符（如 {{chart_gauge}}, {{chart_level}} 等），并在该位置插入对应的图表
     * 每个图表包含6条线：3条任务数据线、1条均值线、1条上限线、1条下限线
     */
    private void replaceChartPlaceholdersForMultipleJobs(XWPFDocument document, List<Jobs> jobs, 
                                                        List<GeometryResultEntity> allGeometryResults,
                                                        Map<String, Double> overLimitRates) {
        if (jobs == null || jobs.isEmpty() || allGeometryResults == null || allGeometryResults.isEmpty()) {
            log.warn("任务或几何数据为空，无法生成图表");
            return;
        }

        try {
            // 按任务分组数据
            Map<Long, List<GeometryResultEntity>> dataByJob = allGeometryResults.stream()
                    .collect(Collectors.groupingBy(GeometryResultEntity::getJobId));

            // 调用原有的单任务方法（保持兼容性）
            Jobs mainJob = jobs.get(0);
            replaceChartPlaceholders(document, mainJob, allGeometryResults, jobs, dataByJob, overLimitRates);
        } catch (Exception e) {
            log.error("替换图表占位符失败（多任务）", e);
            e.printStackTrace();
        }
    }

    /**
     * 替换模板中的图表占位符
     * 查找模板中的占位符（如 {{chart_gauge}}, {{chart_level}} 等），并在该位置插入对应的图表
     */
    private void replaceChartPlaceholders(XWPFDocument document, Jobs job, List<GeometryResultEntity> geometryResults,
                                         Map<String, Double> overLimitRates) {
        replaceChartPlaceholders(document, job, geometryResults, null, null, overLimitRates);
    }

    /**
     * 替换模板中的图表占位符（完整版本，支持多任务）
     */
    private void replaceChartPlaceholders(XWPFDocument document, Jobs job, List<GeometryResultEntity> geometryResults, 
                                         List<Jobs> jobs, Map<Long, List<GeometryResultEntity>> dataByJob,
                                         Map<String, Double> overLimitRates) {
        if (geometryResults == null || geometryResults.isEmpty()) {
            log.warn("几何数据为空，无法生成图表");
            return;
        }

        try {
            // 创建数据提取器函数（用于从 trackGeometry JSON 中提取数据）
            // trackGeometry 格式: [mileage, gd0(左高低), gd1(右高低), zs0(左轨向), zs1(右轨向)]
            java.util.function.Function<GeometryResultEntity, Double> leftHeightExtractor = result -> {
                if (result.getTrackGeometry() == null || result.getTrackGeometry().isEmpty()) {
                    return null;
                }
                try {
                    JsonNode trackNode = objectMapper.readTree(result.getTrackGeometry());
                    if (trackNode.isArray() && trackNode.size() >= 2) {
                        return trackNode.get(1).asDouble(); // gd0 (左高低)
                    }
                } catch (Exception e) {
                    // 解析失败，返回null
                }
                return null;
            };

            java.util.function.Function<GeometryResultEntity, Double> rightHeightExtractor = result -> {
                if (result.getTrackGeometry() == null || result.getTrackGeometry().isEmpty()) {
                    return null;
                }
                try {
                    JsonNode trackNode = objectMapper.readTree(result.getTrackGeometry());
                    if (trackNode.isArray() && trackNode.size() >= 3) {
                        return trackNode.get(2).asDouble(); // gd1 (右高低)
                    }
                } catch (Exception e) {
                    // 解析失败，返回null
                }
                return null;
            };

            java.util.function.Function<GeometryResultEntity, Double> leftDirectionExtractor = result -> {
                if (result.getTrackGeometry() == null || result.getTrackGeometry().isEmpty()) {
                    return null;
                }
                try {
                    JsonNode trackNode = objectMapper.readTree(result.getTrackGeometry());
                    if (trackNode.isArray() && trackNode.size() >= 4) {
                        return trackNode.get(3).asDouble(); // zs0 (左轨向)
                    }
                } catch (Exception e) {
                    // 解析失败，返回null
                }
                return null;
            };

            java.util.function.Function<GeometryResultEntity, Double> rightDirectionExtractor = result -> {
                if (result.getTrackGeometry() == null || result.getTrackGeometry().isEmpty()) {
                    return null;
                }
                try {
                    JsonNode trackNode = objectMapper.readTree(result.getTrackGeometry());
                    if (trackNode.isArray() && trackNode.size() >= 5) {
                        return trackNode.get(4).asDouble(); // zs1 (右轨向)
                    }
                } catch (Exception e) {
                    // 解析失败，返回null
                }
                return null;
            };
            // 提取各种数据
            List<Double> gaugeData = geometryResults.stream()
                    .filter(r -> r.getTdf01Gauge() != null)
                    .map(GeometryResultEntity::getTdf01Gauge)
                    .collect(Collectors.toList());

            List<Double> levelData = geometryResults.stream()
                    .filter(r -> r.getLsf01Level() != null)
                    .map(GeometryResultEntity::getLsf01Level)
                    .collect(Collectors.toList());

            // 提取轨道几何数据（高低、轨向等）
            List<Double> leftHeightData = new ArrayList<>();
            List<Double> rightHeightData = new ArrayList<>();
            List<Double> leftDirectionData = new ArrayList<>();
            List<Double> rightDirectionData = new ArrayList<>();
            List<String> mileageLabels = new ArrayList<>();

            for (GeometryResultEntity result : geometryResults) {
                if (result.getTrackGeometry() != null && !result.getTrackGeometry().isEmpty()) {
                    try {
                        JsonNode trackNode = objectMapper.readTree(result.getTrackGeometry());
                        if (trackNode.isArray() && trackNode.size() >= 5) {
                            double mileage = trackNode.get(0).asDouble();
                            double gd0 = trackNode.get(1).asDouble(); // 左高低
                            double gd1 = trackNode.get(2).asDouble(); // 右高低
                            double zs0 = trackNode.get(3).asDouble(); // 左轨向
                            double zs1 = trackNode.get(4).asDouble(); // 右轨向

                            mileageLabels.add(String.format("%.2f", mileage));
                            leftHeightData.add(gd0);
                            rightHeightData.add(gd1);
                            leftDirectionData.add(zs0);
                            rightDirectionData.add(zs1);
                        }
                    } catch (Exception e) {
                        // 解析失败，跳过
                    }
                }
            }

            // 查找并替换段落中的图表占位符
            // 匹配原始占位符格式和poi-tl替换后的标记
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text == null) continue;
                
                // 匹配轨距图表占位符
                if (text.contains("${chart_gauge}") || text.contains("{{chart_gauge}}") || text.contains("[CHART_GAUGE]")) {
                    String placeholder = text.contains("${chart_gauge}") ? "${chart_gauge}" : 
                                        text.contains("{{chart_gauge}}") ? "{{chart_gauge}}" : "[CHART_GAUGE]";
                    replacePlaceholderWithChartUsingEncoder(paragraph, placeholder,
                            "轨距重复性检测", "里程", "轨距(mm)", 
                            jobs, dataByJob, GeometryResultEntity::getTdf01Gauge, overLimitRates);
                    log.info("已替换轨距图表占位符: {}", text);
                    continue;
                }

                // 匹配水平图表占位符
                if (text.contains("${chart_level}") || text.contains("{{chart_level}}") || text.contains("[CHART_LEVEL]")) {
                    String placeholder = text.contains("${chart_level}") ? "${chart_level}" : 
                                        text.contains("{{chart_level}}") ? "{{chart_level}}" : "[CHART_LEVEL]";
                    replacePlaceholderWithChartUsingEncoder(paragraph, placeholder,
                            "水平重复性检测", "里程", "水平(mm)", 
                            jobs, dataByJob, GeometryResultEntity::getLsf01Level, overLimitRates);
                    continue;
                }

                // 匹配左高低图表占位符
                if (text.contains("${chart_height_left}") || text.contains("{{chart_height_left}}") || text.contains("[CHART_HEIGHT_LEFT]")) {
                    String placeholder = text.contains("${chart_height_left}") ? "${chart_height_left}" : 
                                        text.contains("{{chart_height_left}}") ? "{{chart_height_left}}" : "[CHART_HEIGHT_LEFT]";
                    replacePlaceholderWithChartUsingEncoder(paragraph, placeholder,
                            "4-1左高低重复性", "里程", "高低(mm)", 
                            jobs, dataByJob, leftHeightExtractor, overLimitRates);
                    continue;
                }

                // 匹配右高低图表占位符
                if (text.contains("${chart_height_right}") || text.contains("{{chart_height_right}}") || text.contains("[CHART_HEIGHT_RIGHT]")) {
                    String placeholder = text.contains("${chart_height_right}") ? "${chart_height_right}" : 
                                        text.contains("{{chart_height_right}}") ? "{{chart_height_right}}" : "[CHART_HEIGHT_RIGHT]";
                    replacePlaceholderWithChartUsingEncoder(paragraph, placeholder,
                            "4-2右高低重复性", "里程", "高低(mm)", 
                            jobs, dataByJob, rightHeightExtractor, overLimitRates);
                    continue;
                }

                // 匹配左轨向图表占位符
                if (text.contains("${chart_direction_left}") || text.contains("{{chart_direction_left}}") || text.contains("[CHART_DIRECTION_LEFT]")) {
                    String placeholder = text.contains("${chart_direction_left}") ? "${chart_direction_left}" : 
                                        text.contains("{{chart_direction_left}}") ? "{{chart_direction_left}}" : "[CHART_DIRECTION_LEFT]";
                    replacePlaceholderWithChartUsingEncoder(paragraph, placeholder,
                            "5-1左轨向(正矢)重复性", "里程", "轨向(mm)", 
                            jobs, dataByJob, leftDirectionExtractor, overLimitRates);
                    continue;
                }

                // 匹配右轨向图表占位符
                if (text.contains("${chart_direction_right}") || text.contains("{{chart_direction_right}}") || text.contains("[CHART_DIRECTION_RIGHT]")) {
                    String placeholder = text.contains("${chart_direction_right}") ? "${chart_direction_right}" : 
                                        text.contains("{{chart_direction_right}}") ? "{{chart_direction_right}}" : "[CHART_DIRECTION_RIGHT]";
                    replacePlaceholderWithChartUsingEncoder(paragraph, placeholder,
                            "5-2右轨向(正矢)重复性", "里程", "轨向(mm)", 
                            jobs, dataByJob, rightDirectionExtractor, overLimitRates);
                    continue;
                }
            }

            // 查找并替换表格单元格中的图表占位符
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            String text = paragraph.getText();
                            if (text == null) continue;
                            
                            // 匹配原始占位符格式和poi-tl替换后的标记
                            if (text.contains("${chart_gauge}") || text.contains("{{chart_gauge}}") || text.contains("[CHART_GAUGE]")) {
                                String placeholder = text.contains("${chart_gauge}") ? "${chart_gauge}" : 
                                                    text.contains("{{chart_gauge}}") ? "{{chart_gauge}}" : "[CHART_GAUGE]";
                                replacePlaceholderWithChartUsingEncoder(paragraph, placeholder,
                                        "轨距重复性检测", "里程", "轨距(mm)", 
                                        jobs, dataByJob, GeometryResultEntity::getTdf01Gauge, overLimitRates);
                                continue;
                            }

                            if (text.contains("${chart_level}") || text.contains("{{chart_level}}") || text.contains("[CHART_LEVEL]")) {
                                String placeholder = text.contains("${chart_level}") ? "${chart_level}" : 
                                                    text.contains("{{chart_level}}") ? "{{chart_level}}" : "[CHART_LEVEL]";
                                replacePlaceholderWithChartUsingEncoder(paragraph, placeholder,
                                        "水平重复性检测", "里程", "水平(mm)", 
                                        jobs, dataByJob, GeometryResultEntity::getLsf01Level, overLimitRates);
                                continue;
                            }

                            if (text.contains("${chart_height_left}") || text.contains("{{chart_height_left}}") || text.contains("[CHART_HEIGHT_LEFT]")) {
                                String placeholder = text.contains("${chart_height_left}") ? "${chart_height_left}" : 
                                                    text.contains("{{chart_height_left}}") ? "{{chart_height_left}}" : "[CHART_HEIGHT_LEFT]";
                                replacePlaceholderWithChartUsingEncoder(paragraph, placeholder,
                                        "4-1左高低重复性", "里程", "高低(mm)", 
                                        jobs, dataByJob, leftHeightExtractor, overLimitRates);
                                continue;
                            }

                            if (text.contains("${chart_height_right}") || text.contains("{{chart_height_right}}") || text.contains("[CHART_HEIGHT_RIGHT]")) {
                                String placeholder = text.contains("${chart_height_right}") ? "${chart_height_right}" : 
                                                    text.contains("{{chart_height_right}}") ? "{{chart_height_right}}" : "[CHART_HEIGHT_RIGHT]";
                                replacePlaceholderWithChartUsingEncoder(paragraph, placeholder,
                                        "4-2右高低重复性", "里程", "高低(mm)", 
                                        jobs, dataByJob, rightHeightExtractor, overLimitRates);
                                continue;
                            }

                            if (text.contains("${chart_direction_left}") || text.contains("{{chart_direction_left}}") || text.contains("[CHART_DIRECTION_LEFT]")) {
                                String placeholder = text.contains("${chart_direction_left}") ? "${chart_direction_left}" : 
                                                    text.contains("{{chart_direction_left}}") ? "{{chart_direction_left}}" : "[CHART_DIRECTION_LEFT]";
                                replacePlaceholderWithChartUsingEncoder(paragraph, placeholder,
                                        "5-1左轨向(正矢)重复性", "里程", "轨向(mm)", 
                                        jobs, dataByJob, leftDirectionExtractor, overLimitRates);
                                continue;
                            }

                            if (text.contains("${chart_direction_right}") || text.contains("{{chart_direction_right}}") || text.contains("[CHART_DIRECTION_RIGHT]")) {
                                String placeholder = text.contains("${chart_direction_right}") ? "${chart_direction_right}" : 
                                                    text.contains("{{chart_direction_right}}") ? "{{chart_direction_right}}" : "[CHART_DIRECTION_RIGHT]";
                                replacePlaceholderWithChartUsingEncoder(paragraph, placeholder,
                                        "5-2右轨向(正矢)重复性", "里程", "轨向(mm)", 
                                        jobs, dataByJob, rightDirectionExtractor, overLimitRates);
                                continue;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("替换图表占位符失败", e);
            e.printStackTrace();
        }
    }

    /**
     * 在指定段落位置替换占位符为图表（支持多任务数据，6条线）
     * 使用XmlCursor在占位符位置插入图表，保持原有格式，不添加额外段落
     * @param jobs 任务列表（用于生成多条数据线）
     * @param dataByJob 按任务ID分组的数据
     */
    private void replacePlaceholderWithChart(XWPFParagraph paragraph, String placeholder, 
                                           String chartTitle, String xAxisTitle, String yAxisTitle,
                                           List<String> xLabels, List<Double> data,
                                           List<Jobs> jobs, Map<Long, List<GeometryResultEntity>> dataByJob,
                                           java.util.function.Function<GeometryResultEntity, Double> dataExtractor) {
        // 如果有多任务数据，使用多任务版本
        if (jobs != null && !jobs.isEmpty() && dataByJob != null && !dataByJob.isEmpty() && dataExtractor != null) {
            replacePlaceholderWithMultiSeriesChart(paragraph, placeholder, chartTitle, xAxisTitle, yAxisTitle,
                    xLabels, jobs, dataByJob, dataExtractor);
            return;
        }
        
        // 否则使用单任务版本（保持兼容性）
        replacePlaceholderWithSingleSeriesChart(paragraph, placeholder, chartTitle, xAxisTitle, yAxisTitle,
                xLabels, data);
    }

    /**
     * 使用encoder作为横坐标替换占位符为多系列图表（6条线：3条任务数据线、1条均值线、1条上限线、1条下限线）
     * 横坐标为encoder值，按encoder对齐多个任务的数据
     */
    private void replacePlaceholderWithChartUsingEncoder(XWPFParagraph paragraph, String placeholder,
                                                        String chartTitle, String xAxisTitle, String yAxisTitle,
                                                        List<Jobs> jobs, Map<Long, List<GeometryResultEntity>> dataByJob,
                                                        java.util.function.Function<GeometryResultEntity, Double> dataExtractor,
                                                        Map<String, Double> overLimitRates) {
        if (!checkAndClearPlaceholder(paragraph, placeholder)) {
            return;
        }

        XWPFDocument document = paragraph.getDocument();
        XWPFChart chart = createChartAtPlaceholder(document, paragraph, chartTitle, xAxisTitle, yAxisTitle);
        
        if (chart == null) {
            return;
        }

        // 创建坐标轴 - 对于散点图，X轴和Y轴都应该是数值轴
        org.apache.poi.xddf.usermodel.chart.XDDFValueAxis bottomAxis = 
                chart.createValueAxis(org.apache.poi.xddf.usermodel.chart.AxisPosition.BOTTOM);
        org.apache.poi.xddf.usermodel.chart.XDDFValueAxis leftAxis = 
                chart.createValueAxis(org.apache.poi.xddf.usermodel.chart.AxisPosition.LEFT);
        leftAxis.setCrosses(org.apache.poi.xddf.usermodel.chart.AxisCrosses.AUTO_ZERO);

        // 收集所有任务的 (encoder, value) 数据对
        Map<Integer, List<Double>> dataByEncoder = new TreeMap<>(); // 使用TreeMap自动按encoder排序
        List<Map<Integer, Double>> taskDataMaps = new ArrayList<>(); // 每个任务的数据映射
        
        // 判断是否为高低或轨向图表（使用不同的上下限计算方式）
        boolean isHeightOrDirection = chartTitle.contains("高低") || chartTitle.contains("轨向");
        
        // 1. 为每个任务收集数据（最多3个任务）
        int taskCount = Math.min(jobs != null ? jobs.size() : 0, 3);
        for (int i = 0; i < taskCount; i++) {
            Jobs job = jobs.get(i);
            List<GeometryResultEntity> jobData = dataByJob != null ? dataByJob.get(job.getId()) : null;
            if (jobData != null && !jobData.isEmpty()) {
                Map<Integer, Double> taskMap = new HashMap<>();
                
                for (GeometryResultEntity result : jobData) {
                    if (result.getEncoder() != null) {
                        Double value = dataExtractor != null ? dataExtractor.apply(result) : null;
                        if (value != null) {
                            Integer encoder = result.getEncoder();
                            taskMap.put(encoder, value);
                            
                            // 收集到按encoder分组的数据中
                            dataByEncoder.computeIfAbsent(encoder, k -> new ArrayList<>()).add(value);
                        }
                    }
                }
                
                if (!taskMap.isEmpty()) {
                    taskDataMaps.add(taskMap);
                }
            }
        }

        // 2. 获取所有encoder值（已排序）- 保留完整数据用于计算
        List<Integer> allEncoderList = new ArrayList<>(dataByEncoder.keySet());
        if (allEncoderList.isEmpty()) {
            log.warn("没有找到有效的encoder数据，无法生成图表: {}", chartTitle);
            return;
        }

        // 3. 采样encoder值用于显示（如果数据点太多，只显示部分点）
        // 目标：最多显示200个点，保持趋势可见
        List<Integer> displayEncoderList;
        int maxDisplayPoints = 200;
        if (allEncoderList.size() <= maxDisplayPoints) {
            displayEncoderList = allEncoderList;
        } else {
            // 均匀采样
            displayEncoderList = new ArrayList<>();
            int step = allEncoderList.size() / maxDisplayPoints;
            for (int i = 0; i < allEncoderList.size(); i += step) {
                displayEncoderList.add(allEncoderList.get(i));
            }
            // 确保包含最后一个点
            if (!displayEncoderList.contains(allEncoderList.get(allEncoderList.size() - 1))) {
                displayEncoderList.add(allEncoderList.get(allEncoderList.size() - 1));
            }
        }

        // 4. 准备X轴数据（采样后的encoder值，转换为Double数组用于数值轴）
        Double[] encoderValues = displayEncoderList.stream()
                .map(Integer::doubleValue)
                .toArray(Double[]::new);
        org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource<Double> xDataSource = 
                org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromArray(encoderValues);

        // 创建散点图数据
        org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData scatterData = 
                (org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData) chart.createData(
                        org.apache.poi.xddf.usermodel.chart.ChartTypes.SCATTER, bottomAxis, leftAxis);

        // 5. 为每个任务添加数据系列（使用采样后的encoder）
        for (int i = 0; i < taskDataMaps.size(); i++) {
            Map<Integer, Double> taskMap = taskDataMaps.get(i);
            List<Double> taskValues = new ArrayList<>();
            
            // 按采样后的encoder顺序提取值，如果某个encoder没有数据则使用null（但Apache POI不支持null，所以用0）
            for (Integer encoder : displayEncoderList) {
                Double value = taskMap.get(encoder);
                taskValues.add(value != null ? value : 0.0);
            }
            
            if (!taskValues.isEmpty()) {
                org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource<Double> taskDataSource = 
                        org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromArray(
                                taskValues.toArray(new Double[0]));
                
                org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series taskSeries = 
                        (org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series) scatterData.addSeries(
                                xDataSource, taskDataSource);
                taskSeries.setTitle("任务" + (i + 1), null);
                // 设置线条宽度（单位：磅，1磅约等于0.35毫米）
                setLineWidth(taskSeries, 1.0); // 设置为1磅，可以根据需要调整
            }
        }

        // 6. 计算均值、上限、下限（按采样后的encoder，但使用完整数据计算）
        List<Double> meanValues = new ArrayList<>();
        List<Double> upperLimitValues = new ArrayList<>();
        List<Double> lowerLimitValues = new ArrayList<>();
        
        // 计算超限率（使用完整数据）
        int totalEncoders = allEncoderList.size(); // 总里程数 a
        int exceededEncoders = 0; // 超限里程数 b
        Set<Integer> exceededEncoderSet = new HashSet<>(); // 用于去重，确保同一个里程不重复计算

        for (Integer encoder : displayEncoderList) {
            List<Double> pointValues = dataByEncoder.get(encoder);
            if (pointValues != null && !pointValues.isEmpty()) {
                // 计算均值
                double mean = pointValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                meanValues.add(mean);
                
                // 计算上限和下限
                double upperLimit, lowerLimit;
                if (isHeightOrDirection) {
                    // 高低和轨向：上限 = 均值 + 0.5，下限 = 均值 - 0.5
                    upperLimit = mean + 0.5;
                    lowerLimit = mean - 0.5;
                } else {
                    // 轨距和水平：上限 = 均值 + 0.225，下限 = 均值 - 0.225
                    upperLimit = mean + 0.225;
                    lowerLimit = mean - 0.225;
                }
                upperLimitValues.add(upperLimit);
                lowerLimitValues.add(lowerLimit);
                
                // 检查该encoder是否超限（使用完整数据）
                if (!exceededEncoderSet.contains(encoder)) {
                    boolean exceeded = false;
                    for (Double value : pointValues) {
                        if (value > upperLimit || value < lowerLimit) {
                            exceeded = true;
                            break;
                        }
                    }
                    if (exceeded) {
                        exceededEncoderSet.add(encoder);
                        exceededEncoders++;
                    }
                }
            } else {
                meanValues.add(0.0);
                upperLimitValues.add(0.0);
                lowerLimitValues.add(0.0);
            }
        }
        
        // 计算所有encoder的超限情况（使用完整数据）
        for (Integer encoder : allEncoderList) {
            if (!exceededEncoderSet.contains(encoder)) {
                List<Double> pointValues = dataByEncoder.get(encoder);
                if (pointValues != null && !pointValues.isEmpty()) {
                    double mean = pointValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    double upperLimit, lowerLimit;
                    if (isHeightOrDirection) {
                        upperLimit = mean + 0.5;
                        lowerLimit = mean - 0.5;
                    } else {
                        upperLimit = mean + 0.225;
                        lowerLimit = mean - 0.225;
                    }
                    
                    // 检查是否超限
                    boolean exceeded = false;
                    for (Double value : pointValues) {
                        if (value > upperLimit || value < lowerLimit) {
                            exceeded = true;
                            break;
                        }
                    }
                    if (exceeded) {
                        exceededEncoderSet.add(encoder);
                        exceededEncoders++;
                    }
                }
            }
        }
        
        // 计算多个阈值的超限率（使用完整数据）
        // 根据图表类型确定需要计算的阈值列表
        List<Double> thresholds = getThresholdsForChart(chartTitle);
        
        for (Double threshold : thresholds) {
            int exceededCount = 0;
            Set<Integer> exceededEncoderSetForThreshold = new HashSet<>();
            
            // 计算该阈值下的超限率
            for (Integer encoder : allEncoderList) {
                if (!exceededEncoderSetForThreshold.contains(encoder)) {
                    List<Double> pointValues = dataByEncoder.get(encoder);
                    if (pointValues != null && !pointValues.isEmpty()) {
                        double mean = pointValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                        double upperLimit = mean + threshold;
                        double lowerLimit = mean - threshold;
                        
                        // 检查是否超限
                        boolean exceeded = false;
                        for (Double value : pointValues) {
                            if (value > upperLimit || value < lowerLimit) {
                                exceeded = true;
                                break;
                            }
                        }
                        if (exceeded) {
                            exceededEncoderSetForThreshold.add(encoder);
                            exceededCount++;
                        }
                    }
                }
            }
            
            // 计算超限率：b/a*100%
            double exceedRate = totalEncoders > 0 ? (exceededCount * 100.0 / totalEncoders) : 0.0;
            
            // 保存超限率到Map中
            String key = getOverLimitRateKey(chartTitle, threshold);
            if (key != null) {
                overLimitRates.put(key, exceedRate);
                log.info("图表 {} - 阈值 {} - 总里程数: {}, 超限里程数: {}, 超限率: {:.2f}%", 
                        chartTitle, threshold, totalEncoders, exceededCount, exceedRate);
            }
        }

        // 6. 添加均值线
        if (!meanValues.isEmpty()) {
            org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource<Double> meanDataSource = 
                    org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromArray(
                            meanValues.toArray(new Double[0]));
            org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series meanSeries = 
                    (org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series) scatterData.addSeries(
                            xDataSource, meanDataSource);
            meanSeries.setTitle("均值", null);
            setLineWidth(meanSeries, 1.0); // 设置线条宽度
        }

        // 7. 添加上限线
        if (!upperLimitValues.isEmpty()) {
            org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource<Double> upperDataSource = 
                    org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromArray(
                            upperLimitValues.toArray(new Double[0]));
            org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series upperSeries = 
                    (org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series) scatterData.addSeries(
                            xDataSource, upperDataSource);
            upperSeries.setTitle("上限", null);
            setLineWidth(upperSeries, 1.0); // 设置线条宽度
        }

        // 8. 添加下限线
        if (!lowerLimitValues.isEmpty()) {
            org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource<Double> lowerDataSource = 
                    org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromArray(
                            lowerLimitValues.toArray(new Double[0]));
            org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series lowerSeries = 
                    (org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series) scatterData.addSeries(
                            xDataSource, lowerDataSource);
            lowerSeries.setTitle("下限", null);
            setLineWidth(lowerSeries, 1.0); // 设置线条宽度
        }

        // 绘制图表
        chart.plot(scatterData);

        // 设置坐标轴标题
        bottomAxis.setTitle(xAxisTitle);
        leftAxis.setTitle(yAxisTitle);

        // 移动图表到占位符位置
        moveChartToPlaceholder(document, paragraph, chartTitle);
    }

    /**
     * 在指定段落位置替换占位符为多系列图表（6条线：3条任务数据线、1条均值线、1条上限线、1条下限线）
     */
    private void replacePlaceholderWithMultiSeriesChart(XWPFParagraph paragraph, String placeholder,
                                                        String chartTitle, String xAxisTitle, String yAxisTitle,
                                                        List<String> xLabels, List<Jobs> jobs,
                                                        Map<Long, List<GeometryResultEntity>> dataByJob,
                                                        java.util.function.Function<GeometryResultEntity, Double> dataExtractor) {
        if (!checkAndClearPlaceholder(paragraph, placeholder)) {
            return;
        }

        XWPFDocument document = paragraph.getDocument();
        XWPFChart chart = createChartAtPlaceholder(document, paragraph, chartTitle, xAxisTitle, yAxisTitle);
        
        if (chart == null) {
            return;
        }

        // 创建坐标轴 - 对于散点图，X轴和Y轴都应该是数值轴
        org.apache.poi.xddf.usermodel.chart.XDDFValueAxis bottomAxis = 
                chart.createValueAxis(org.apache.poi.xddf.usermodel.chart.AxisPosition.BOTTOM);
        org.apache.poi.xddf.usermodel.chart.XDDFValueAxis leftAxis = 
                chart.createValueAxis(org.apache.poi.xddf.usermodel.chart.AxisPosition.LEFT);
        leftAxis.setCrosses(org.apache.poi.xddf.usermodel.chart.AxisCrosses.AUTO_ZERO);

        // 准备X轴数据（里程标签）
        org.apache.poi.xddf.usermodel.chart.XDDFDataSource<String> catDataSource = 
                org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromArray(
                        xLabels.toArray(new String[0]));

        // 创建散点图数据
        org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData scatterData = 
                (org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData) chart.createData(
                        org.apache.poi.xddf.usermodel.chart.ChartTypes.SCATTER, bottomAxis, leftAxis);

        // 收集所有任务的数据，用于计算均值、上限、下限
        List<List<Double>> allTaskData = new ArrayList<>();
        
        // 判断是否为高低或轨向图表（使用不同的上下限计算方式）
        boolean isHeightOrDirection = chartTitle.contains("高低") || chartTitle.contains("轨向");
        
        // 1. 为每个任务添加数据系列（最多3个任务）
        int taskCount = Math.min(jobs.size(), 3);
        for (int i = 0; i < taskCount; i++) {
            Jobs job = jobs.get(i);
            List<GeometryResultEntity> jobData = dataByJob.get(job.getId());
            if (jobData != null && !jobData.isEmpty()) {
                List<Double> taskValues = new ArrayList<>();
                for (GeometryResultEntity result : jobData) {
                    Double value = dataExtractor.apply(result);
                    if (value != null) {
                        taskValues.add(value);
                    }
                }
                
                // 如果任务数据点数量与xLabels不一致，需要调整
                // 如果任务数据点更多，截取前xLabels.size()个
                // 如果任务数据点更少，用最后一个值填充
                if (!taskValues.isEmpty()) {
                    while (taskValues.size() < xLabels.size()) {
                        taskValues.add(taskValues.isEmpty() ? 0.0 : taskValues.get(taskValues.size() - 1));
                    }
                    if (taskValues.size() > xLabels.size()) {
                        taskValues = taskValues.subList(0, xLabels.size());
                    }
                    
                    allTaskData.add(taskValues);
                    
                    org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource<Double> taskDataSource = 
                            org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromArray(
                                    taskValues.toArray(new Double[0]));
                    
                    org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series taskSeries = 
                            (org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series) scatterData.addSeries(
                                    catDataSource, taskDataSource);
                    taskSeries.setTitle("任务" + (i + 1), null);
                    setLineWidth(taskSeries, 1.0); // 设置线条宽度
                }
            }
        }

        // 2. 计算均值、上限、下限
        if (!allTaskData.isEmpty() && !xLabels.isEmpty()) {
            int dataPointCount = xLabels.size();
            List<Double> meanValues = new ArrayList<>();
            List<Double> upperLimitValues = new ArrayList<>();
            List<Double> lowerLimitValues = new ArrayList<>();

            for (int i = 0; i < dataPointCount; i++) {
                List<Double> pointValues = new ArrayList<>();
                for (List<Double> taskData : allTaskData) {
                    if (i < taskData.size()) {
                        pointValues.add(taskData.get(i));
                    }
                }
                
                if (!pointValues.isEmpty()) {
                    // 计算均值
                    double mean = pointValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    meanValues.add(mean);
                    
                    // 计算上限和下限
                    if (isHeightOrDirection) {
                        // 高低和轨向：上限 = 均值 + 0.5，下限 = 均值 - 0.5
                        upperLimitValues.add(mean + 0.5);
                        lowerLimitValues.add(mean - 0.5);
                    } else {
                        // 轨距和水平：上限 = 均值 + 2*标准差，下限 = 均值 - 2*标准差
                        double variance = pointValues.stream()
                                .mapToDouble(v -> Math.pow(v - mean, 2))
                                .average().orElse(0.0);
                        double stdDev = Math.sqrt(variance);
                        upperLimitValues.add(mean + 2 * stdDev);
                        lowerLimitValues.add(mean - 2 * stdDev);
                    }
                } else {
                    meanValues.add(0.0);
                    upperLimitValues.add(0.0);
                    lowerLimitValues.add(0.0);
                }
            }

            // 3. 添加均值线
            if (!meanValues.isEmpty()) {
                org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource<Double> meanDataSource = 
                        org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromArray(
                                meanValues.toArray(new Double[0]));
                org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series meanSeries = 
                        (org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series) scatterData.addSeries(
                                catDataSource, meanDataSource);
                meanSeries.setTitle("均值", null);
                setLineWidth(meanSeries, 1.0); // 设置线条宽度
            }

            // 4. 添加上限线
            if (!upperLimitValues.isEmpty()) {
                org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource<Double> upperDataSource = 
                        org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromArray(
                                upperLimitValues.toArray(new Double[0]));
                org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series upperSeries = 
                        (org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series) scatterData.addSeries(
                                catDataSource, upperDataSource);
                upperSeries.setTitle("上限", null);
                setLineWidth(upperSeries, 1.0); // 设置线条宽度
            }

            // 5. 添加下限线
            if (!lowerLimitValues.isEmpty()) {
                org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource<Double> lowerDataSource = 
                        org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromArray(
                                lowerLimitValues.toArray(new Double[0]));
                org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series lowerSeries = 
                        (org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series) scatterData.addSeries(
                                catDataSource, lowerDataSource);
                lowerSeries.setTitle("下限", null);
                setLineWidth(lowerSeries, 1.0); // 设置线条宽度
            }
        }

        // 绘制图表
        chart.plot(scatterData);

        // 设置坐标轴标题
        bottomAxis.setTitle(xAxisTitle);
        leftAxis.setTitle(yAxisTitle);

        // 移动图表到占位符位置
        moveChartToPlaceholder(document, paragraph, chartTitle);
    }

    /**
     * 在指定段落位置替换占位符为单系列图表（兼容旧版本）
     */
    private void replacePlaceholderWithSingleSeriesChart(XWPFParagraph paragraph, String placeholder, 
                                           String chartTitle, String xAxisTitle, String yAxisTitle,
                                           List<String> xLabels, List<Double> data) {
        try {
            String text = paragraph.getText();
            if (text == null) {
                return;
            }
            
            // 检查段落是否包含占位符（支持 {{}} 和 ${} 两种格式）
            // 也检查是否包含特殊占位符标记 [CHART_PLACEHOLDER]（poi-tl替换后的结果）
            boolean containsPlaceholder = text.contains(placeholder) || text.contains("[CHART_PLACEHOLDER]");
            if (!containsPlaceholder) {
                return;
            }

            // 检查段落是否只包含占位符（允许前后有空白）
            String trimmedText = text.trim();
            String placeholderVariants = placeholder;
            // 支持 {{}} 和 ${} 两种格式，以及特殊占位符标记 [CHART_PLACEHOLDER]
            if (placeholder.contains("{{")) {
                placeholderVariants = placeholder + "|" + placeholder.replace("{{", "${").replace("}}", "}") + "|[CHART_PLACEHOLDER]";
            } else if (placeholder.contains("${")) {
                placeholderVariants = placeholder + "|" + placeholder.replace("${", "{{").replace("}", "}}") + "|[CHART_PLACEHOLDER]";
            } else {
                placeholderVariants = placeholder + "|[CHART_PLACEHOLDER]";
            }
            
            boolean isPlaceholderOnly = false;
            for (String variant : placeholderVariants.split("\\|")) {
                if (trimmedText.equals(variant) || trimmedText.equals(variant.trim()) || trimmedText.contains("[CHART_PLACEHOLDER]")) {
                    isPlaceholderOnly = true;
                    break;
                }
            }

            if (isPlaceholderOnly) {
                // 清除段落中的所有运行（文本内容）
                List<XWPFRun> runs = paragraph.getRuns();
                for (int i = runs.size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }

                // 使用XmlCursor在段落位置插入图表（参考ChartGenerator.createSimpleBarChart的实现）
                org.apache.xmlbeans.XmlCursor cursor = paragraph.getCTP().newCursor();
                
                // 创建图表（会在文档末尾创建新段落，我们需要将其移动到当前段落）
                XWPFDocument document = paragraph.getDocument();
                
                // 记录当前段落在文档中的位置和段落总数
                int targetParagraphIndex = document.getParagraphs().indexOf(paragraph);
                int paragraphCountBefore = document.getParagraphs().size();
                
                log.info("准备创建图表: {}, 当前段落索引: {}, 段落总数: {}", chartTitle, targetParagraphIndex, paragraphCountBefore);
                
            // 创建图表（会在文档末尾创建新段落）
            // 优化图表尺寸：宽度 10*500000，高度 10*250000（增加高度让折线图更大更清晰）
            XWPFChart chart = document.createChart(10 * 500000, 7 * 260000);
            
            // 不设置图表标题（根据用户要求）

                // 创建坐标轴
                org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis categoryAxis = 
                        chart.createCategoryAxis(org.apache.poi.xddf.usermodel.chart.AxisPosition.BOTTOM);
                org.apache.poi.xddf.usermodel.chart.XDDFValueAxis valueAxis = 
                        chart.createValueAxis(org.apache.poi.xddf.usermodel.chart.AxisPosition.LEFT);
                valueAxis.setCrosses(org.apache.poi.xddf.usermodel.chart.AxisCrosses.AUTO_ZERO);

                // 准备数据
                org.apache.poi.xddf.usermodel.chart.XDDFDataSource<String> catDataSource = 
                        org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromArray(
                                xLabels.toArray(new String[0]));
                org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource<Double> valDataSource = 
                        org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromArray(
                                data.toArray(new Double[0]));

                // 创建散点图数据
                org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData scatterData = 
                        (org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData) chart.createData(
                                org.apache.poi.xddf.usermodel.chart.ChartTypes.SCATTER, categoryAxis, valueAxis);

                // 添加数据系列
                org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series series = 
                        (org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData.Series) scatterData.addSeries(
                                catDataSource, valDataSource);
                series.setTitle("检测值", null);

                // 绘制图表
                chart.plot(scatterData);

                // 设置坐标轴标题
                categoryAxis.setTitle(xAxisTitle);
                valueAxis.setTitle(yAxisTitle);

                // 找到图表所在的段落（应该是文档的最后一个段落）
                List<XWPFParagraph> allParagraphs = document.getParagraphs();
                int paragraphCountAfter = allParagraphs.size();
                log.info("图表创建后，段落总数: {}", paragraphCountAfter);
                
                if (paragraphCountAfter > paragraphCountBefore) {
                    // 找到了新创建的图表段落
                    XWPFParagraph chartParagraph = allParagraphs.get(allParagraphs.size() - 1);
                    
                    // 将图表段落的XML内容复制到当前段落
                    try {
                        // 复制图表段落的CTP（段落XML）内容到当前段落
                        org.apache.xmlbeans.XmlObject chartParaXml = chartParagraph.getCTP().copy();
                        paragraph.getCTP().set(chartParaXml);
                        
                        log.info("成功复制图表段落内容到占位符位置");
                        
                        // 删除文档末尾的图表段落（因为我们已经复制了内容）
                        try {
                            // 获取body元素列表
                            List<org.apache.poi.xwpf.usermodel.IBodyElement> bodyElements = document.getBodyElements();
                            int bodyElementIndex = -1;
                            
                            // 从后往前遍历body元素，找到图表段落（应该是最后一个段落）
                            for (int i = bodyElements.size() - 1; i >= 0; i--) {
                                org.apache.poi.xwpf.usermodel.IBodyElement element = bodyElements.get(i);
                                if (element instanceof XWPFParagraph) {
                                    XWPFParagraph para = (XWPFParagraph) element;
                                    // 检查是否是图表段落（通过比较段落对象或位置）
                                    if (para.equals(chartParagraph)) {
                                        bodyElementIndex = i;
                                        break;
                                    }
                                }
                            }
                            
                            // 如果找到了，删除它
                            if (bodyElementIndex >= 0 && bodyElementIndex < bodyElements.size()) {
                                // 使用正确的方法删除body元素
                                try {
                                    // 尝试使用removeBodyElement方法
                                    document.removeBodyElement(bodyElementIndex);
                                } catch (NoSuchMethodError e1) {
                                    // 注意：由于Apache POI API限制，无法直接删除文档末尾的段落
                                    // 但图表已经在占位符位置了，文档末尾的副本不影响主要功能
                                    log.info("图表已插入到占位符位置，文档末尾的副本将被保留（不影响使用）");
                                } catch (Exception e1) {
                                    log.warn("删除图表段落时发生错误: {}", e1.getMessage());
                                    // 继续执行，即使删除失败也不影响主要功能
                                }
                            } else {
                                log.warn("未找到图表段落在body中的位置。bodyElementIndex: {}, bodyElements.size: {}", 
                                        bodyElementIndex, bodyElements.size());
                            }
                        } catch (Exception e) {
                            log.warn("无法删除文档末尾的图表段落: {}", e.getMessage());
                            // 继续执行，即使删除失败也不影响主要功能（图表已经在占位符位置了）
                        }
                        
                        log.info("成功将图表插入到占位符位置: {}", chartTitle);
                    } catch (Exception e) {
                        log.error("无法移动图表段落到占位符位置: {}", e.getMessage(), e);
                        e.printStackTrace();
                        // 如果移动失败，至少图表已经在文档末尾创建了
                    }
                } else {
                    log.warn("图表可能未正确创建，段落数量未增加。段落总数: {}, 之前: {}", 
                            paragraphCountAfter, paragraphCountBefore);
                }
                
                cursor.dispose();
            } else {
                // 如果占位符前后有其他文本，记录日志但不处理（保持原有文本）
                log.warn("占位符 {} 前后有其他文本，无法直接替换为图表。段落内容: {}", placeholder, text);
            }

        } catch (Exception e) {
            log.error("替换占位符为图表失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查并清除占位符
     */
    private boolean checkAndClearPlaceholder(XWPFParagraph paragraph, String placeholder) {
        try {
            String text = paragraph.getText();
            if (text == null) {
                return false;
            }
            
            // 检查段落是否包含占位符（支持 {{}} 和 ${} 两种格式，以及poi-tl替换后的标记）
            // 支持新的标记格式：[CHART_GAUGE], [CHART_LEVEL], [CHART_HEIGHT_LEFT] 等
            boolean containsPlaceholder = text.contains(placeholder);
            if (!containsPlaceholder) {
                // 检查是否是poi-tl替换后的标记格式
                if (placeholder.contains("chart_gauge") && text.contains("[CHART_GAUGE]")) {
                    containsPlaceholder = true;
                } else if (placeholder.contains("chart_level") && text.contains("[CHART_LEVEL]")) {
                    containsPlaceholder = true;
                } else if (placeholder.contains("chart_height_left") && text.contains("[CHART_HEIGHT_LEFT]")) {
                    containsPlaceholder = true;
                } else if (placeholder.contains("chart_height_right") && text.contains("[CHART_HEIGHT_RIGHT]")) {
                    containsPlaceholder = true;
                } else if (placeholder.contains("chart_direction_left") && text.contains("[CHART_DIRECTION_LEFT]")) {
                    containsPlaceholder = true;
                } else if (placeholder.contains("chart_direction_right") && text.contains("[CHART_DIRECTION_RIGHT]")) {
                    containsPlaceholder = true;
                }
            }
            if (!containsPlaceholder) {
                return false;
            }

            // 检查段落是否只包含占位符（允许前后有空白）
            String trimmedText = text.trim();
            String placeholderVariants = placeholder;
            // 支持 {{}} 和 ${} 两种格式，以及poi-tl替换后的标记
            if (placeholder.contains("{{")) {
                String altFormat = placeholder.replace("{{", "${").replace("}}", "}");
                String chartMarker = getChartMarker(placeholder);
                placeholderVariants = placeholder + "|" + altFormat + (chartMarker != null ? "|" + chartMarker : "");
            } else if (placeholder.contains("${")) {
                String altFormat = placeholder.replace("${", "{{").replace("}", "}}");
                String chartMarker = getChartMarker(placeholder);
                placeholderVariants = placeholder + "|" + altFormat + (chartMarker != null ? "|" + chartMarker : "");
            } else if (placeholder.startsWith("[CHART_")) {
                // 如果占位符本身就是标记格式，直接使用
                placeholderVariants = placeholder;
            } else {
                String chartMarker = getChartMarker(placeholder);
                placeholderVariants = placeholder + (chartMarker != null ? "|" + chartMarker : "");
            }
            
            boolean isPlaceholderOnly = false;
            for (String variant : placeholderVariants.split("\\|")) {
                if (trimmedText.equals(variant) || trimmedText.equals(variant.trim())) {
                    isPlaceholderOnly = true;
                    break;
                }
            }

            if (isPlaceholderOnly) {
                // 清除段落中的所有运行（文本内容）
                List<XWPFRun> runs = paragraph.getRuns();
                for (int i = runs.size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("检查占位符失败", e);
            return false;
        }
    }

    /**
     * 根据占位符获取对应的图表标记
     */
    private String getChartMarker(String placeholder) {
        if (placeholder.contains("chart_gauge")) {
            return "[CHART_GAUGE]";
        } else if (placeholder.contains("chart_level")) {
            return "[CHART_LEVEL]";
        } else if (placeholder.contains("chart_height_left")) {
            return "[CHART_HEIGHT_LEFT]";
        } else if (placeholder.contains("chart_height_right")) {
            return "[CHART_HEIGHT_RIGHT]";
        } else if (placeholder.contains("chart_direction_left")) {
            return "[CHART_DIRECTION_LEFT]";
        } else if (placeholder.contains("chart_direction_right")) {
            return "[CHART_DIRECTION_RIGHT]";
        }
        return null;
    }

    /**
     * 设置图表系列的线条宽度
     * @param series 图表系列
     * @param widthInPoints 线条宽度（单位：磅，1磅约等于0.35毫米）
     */
    private void setLineWidth(org.apache.poi.xddf.usermodel.chart.XDDFChartData.Series series, double widthInPoints) {
        try {
            org.apache.poi.xddf.usermodel.XDDFLineProperties lineProperties = new org.apache.poi.xddf.usermodel.XDDFLineProperties();
            lineProperties.setWidth(widthInPoints);
            series.setLineProperties(lineProperties);
        } catch (Exception e) {
            log.warn("设置线条宽度失败: {}", e.getMessage());
            // 线条宽度设置失败不影响主要功能
        }
    }

    /**
     * 在占位符位置创建图表
     */
    private XWPFChart createChartAtPlaceholder(XWPFDocument document, XWPFParagraph paragraph, 
                                              String chartTitle, String xAxisTitle, String yAxisTitle) {
        try {
            // 记录当前段落在文档中的位置和段落总数
            int targetParagraphIndex = document.getParagraphs().indexOf(paragraph);
            int paragraphCountBefore = document.getParagraphs().size();
            
            log.info("准备创建图表: {}, 当前段落索引: {}, 段落总数: {}", chartTitle, targetParagraphIndex, paragraphCountBefore);
            
            // 创建图表（会在文档末尾创建新段落）
            // 增大图表尺寸，特别是高度，让折线图更清晰：宽度 10*500000，高度 9*500000
            XWPFChart chart = document.createChart(10 * 500000, 7 * 260000);
            
            // 不设置图表标题（根据用户要求）
            
            // 优化图表外观，让折线图更大更直观
            optimizeChartAppearance(chart);
            
            return chart;
        } catch (Exception e) {
            log.error("创建图表失败", e);
            return null;
        }
    }
    
    /**
     * 优化图表外观，让折线图在有限尺寸内更大更直观
     * 
     * 优化策略：
     * 1. 将图例放置在底部，释放更多纵向空间给折线图
     * 2. 图例不覆盖图表内容，保持数据清晰
     * 3. 配合增大的图表高度（9*500000），让折线图绘图区域显著增大
     * 
     * 整体效果：图表更大、更紧凑、数据趋势更明显
     * 
     * 注意：由于Apache POI底层API的兼容性问题，此方法使用高级API实现
     * 如需更精细控制（如字体大小、边距等），建议在Word模板中预设图表样式
     */
    private void optimizeChartAppearance(XWPFChart chart) {
        try {
            // 使用Apache POI的XDDFChart API设置图例位置
            // 将图例放置在底部，腾出更多纵向空间，让折线图占据更大的显示区域
            org.apache.poi.xddf.usermodel.chart.XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(org.apache.poi.xddf.usermodel.chart.LegendPosition.BOTTOM);
            legend.setOverlay(false); // 图例不覆盖图表内容，保持数据可读性
            
            log.debug("图表外观优化完成：图例位置=底部，尺寸=10x9单位，整体更紧凑");
        } catch (Exception e) {
            log.warn("优化图表外观失败: {}", e.getMessage());
            // 优化失败不影响主要功能，图表仍然可以正常显示
        }
    }

    /**
     * 将图表移动到占位符位置
     */
    private void moveChartToPlaceholder(XWPFDocument document, XWPFParagraph paragraph, String chartTitle) {
        try {
            // 找到图表所在的段落（应该是文档的最后一个段落）
            List<XWPFParagraph> allParagraphs = document.getParagraphs();
            int paragraphCountAfter = allParagraphs.size();
            log.info("图表创建后，段落总数: {}", paragraphCountAfter);
            
            int targetParagraphIndex = document.getParagraphs().indexOf(paragraph);
            int paragraphCountBefore = paragraphCountAfter - 1; // 图表创建前应该有 paragraphCountAfter - 1 个段落
            
            if (paragraphCountAfter > paragraphCountBefore) {
                // 找到了新创建的图表段落
                XWPFParagraph chartParagraph = allParagraphs.get(allParagraphs.size() - 1);
                
                // 将图表段落的XML内容复制到当前段落
                try {
                    // 复制图表段落的CTP（段落XML）内容到当前段落
                    org.apache.xmlbeans.XmlObject chartParaXml = chartParagraph.getCTP().copy();
                    paragraph.getCTP().set(chartParaXml);
                    
                    log.info("成功复制图表段落内容到占位符位置");
                    
                    // 删除文档末尾的图表段落（因为我们已经复制了内容）
                    try {
                        // 获取body元素列表
                        List<org.apache.poi.xwpf.usermodel.IBodyElement> bodyElements = document.getBodyElements();
                        int bodyElementIndex = -1;
                        
                        // 从后往前遍历body元素，找到图表段落（应该是最后一个段落）
                        for (int i = bodyElements.size() - 1; i >= 0; i--) {
                            org.apache.poi.xwpf.usermodel.IBodyElement element = bodyElements.get(i);
                            if (element instanceof XWPFParagraph) {
                                XWPFParagraph para = (XWPFParagraph) element;
                                // 检查是否是图表段落（通过比较段落对象或位置）
                                if (para.equals(chartParagraph)) {
                                    bodyElementIndex = i;
                                    break;
                                }
                            }
                        }
                        
                        // 如果找到了，删除它
                        if (bodyElementIndex >= 0 && bodyElementIndex < bodyElements.size()) {
                            // 使用正确的方法删除body元素
                            try {
                                // 尝试使用removeBodyElement方法
                                document.removeBodyElement(bodyElementIndex);
                                log.info("成功删除文档末尾的图表段落，索引: {}", bodyElementIndex);
                            } catch (NoSuchMethodError e1) {
                                // 如果removeBodyElement不存在，跳过删除操作
                                log.warn("removeBodyElement方法不可用，跳过删除操作: {}", e1.getMessage());
                                // 注意：图表已经在占位符位置了，文档末尾可能还会有一个图表副本
                                // 但这不影响主要功能，图表会正确显示在占位符位置
                                log.info("图表已插入到占位符位置，文档末尾的副本将被保留（不影响使用）");
                            } catch (Exception e1) {
                                log.warn("删除图表段落时发生错误: {}", e1.getMessage());
                                // 继续执行，即使删除失败也不影响主要功能
                            }
                        } else {
                            log.warn("未找到图表段落在body中的位置。bodyElementIndex: {}, bodyElements.size: {}", 
                                    bodyElementIndex, bodyElements.size());
                        }
                    } catch (Exception e) {
                        log.warn("无法删除文档末尾的图表段落: {}", e.getMessage());
                        // 继续执行，即使删除失败也不影响主要功能（图表已经在占位符位置了）
                    }
                    
                    log.info("成功将图表插入到占位符位置: {}", chartTitle);
                } catch (Exception e) {
                    log.error("无法移动图表段落到占位符位置: {}", e.getMessage(), e);
                    e.printStackTrace();
                    // 如果移动失败，至少图表已经在文档末尾创建了
                }
            } else {
                log.warn("图表可能未正确创建，段落数量未增加。段落总数: {}, 之前: {}", 
                        paragraphCountAfter, paragraphCountBefore);
            }
        } catch (Exception e) {
            log.error("移动图表到占位符位置失败", e);
        }
    }

    /**
     * 生成散点图（轨距、水平、高低、轨向等）
     * 根据模板要求，需要生成包含曲线1、曲线2、曲线3、平均值(红色)、均值上限、均值下限的散点图
     * 注意：此方法已废弃，请使用 replaceChartPlaceholders 方法
     */
    @Deprecated
    private void generateScatterCharts(XWPFDocument document, Jobs job, List<GeometryResultEntity> geometryResults) {
        if (geometryResults == null || geometryResults.isEmpty()) {
            return;
        }

        try {
            // 提取轨距数据
            List<Double> gaugeData = geometryResults.stream()
                    .filter(r -> r.getTdf01Gauge() != null)
                    .map(GeometryResultEntity::getTdf01Gauge)
                    .collect(Collectors.toList());

            // 提取水平数据
            List<Double> levelData = geometryResults.stream()
                    .filter(r -> r.getLsf01Level() != null)
                    .map(GeometryResultEntity::getLsf01Level)
                    .collect(Collectors.toList());

            // 提取轨道几何数据（高低、轨向等）
            // trackGeometry格式: [里程, gd0, gd1, zs0, zs1]
            // gd0/gd1: 高低数据（左/右）
            // zs0/zs1: 轨向数据（左/右）
            List<Double> leftHeightData = new ArrayList<>();
            List<Double> rightHeightData = new ArrayList<>();
            List<Double> leftDirectionData = new ArrayList<>();
            List<Double> rightDirectionData = new ArrayList<>();
            List<String> mileageLabels = new ArrayList<>();

            for (GeometryResultEntity result : geometryResults) {
                if (result.getTrackGeometry() != null && !result.getTrackGeometry().isEmpty()) {
                    try {
                        JsonNode trackNode = objectMapper.readTree(result.getTrackGeometry());
                        if (trackNode.isArray() && trackNode.size() >= 5) {
                            double mileage = trackNode.get(0).asDouble();
                            double gd0 = trackNode.get(1).asDouble(); // 左高低
                            double gd1 = trackNode.get(2).asDouble(); // 右高低
                            double zs0 = trackNode.get(3).asDouble(); // 左轨向
                            double zs1 = trackNode.get(4).asDouble(); // 右轨向

                            mileageLabels.add(String.format("%.2f", mileage));
                            leftHeightData.add(gd0);
                            rightHeightData.add(gd1);
                            leftDirectionData.add(zs0);
                            rightDirectionData.add(zs1);
                        }
                    } catch (Exception e) {
                        // 解析失败，跳过
                    }
                }
            }

            // 生成轨距散点图（重复性检测：3次检测的曲线 + 平均值）
            if (!gaugeData.isEmpty()) {
                generateRepeatabilityChart(document, "轨距重复性检测", "里程", "轨距(mm)", 
                        generateMileageLabels(gaugeData.size()), gaugeData);
            }

            // 生成水平散点图
            if (!levelData.isEmpty()) {
                generateRepeatabilityChart(document, "水平重复性检测", "里程", "水平(mm)", 
                        generateMileageLabels(levelData.size()), levelData);
            }

            // 生成左高低散点图
            if (!leftHeightData.isEmpty()) {
                generateRepeatabilityChart(document, "左高低重复性检测", "里程", "高低(mm)", 
                        mileageLabels, leftHeightData);
            }

            // 生成右高低散点图
            if (!rightHeightData.isEmpty()) {
                generateRepeatabilityChart(document, "右高低重复性检测", "里程", "高低(mm)", 
                        mileageLabels, rightHeightData);
            }

            // 生成左轨向散点图（正矢）
            if (!leftDirectionData.isEmpty()) {
                generateRepeatabilityChart(document, "5-1左轨向(正矢)重复性", "里程", "轨向(mm)", 
                        mileageLabels, leftDirectionData);
            }

            // 生成右轨向散点图（正矢）
            if (!rightDirectionData.isEmpty()) {
                generateRepeatabilityChart(document, "5-2右轨向(正矢)重复性", "里程", "轨向(mm)", 
                        mileageLabels, rightDirectionData);
            }

        } catch (Exception e) {
            System.err.println("生成散点图失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 生成重复性检测散点图
     * 根据模板要求，需要生成3次检测曲线、平均值(红色)、均值上限、均值下限
     * 如果只有一次检测数据，则生成单条曲线和平均值
     */
    private void generateRepeatabilityChart(XWPFDocument document, String chartTitle,
                                          String xAxisTitle, String yAxisTitle,
                                          List<String> xLabels, List<Double> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        // 计算平均值
        double average = data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        List<Double> averageList = new ArrayList<>();
        List<Double> upperLimit = new ArrayList<>();
        List<Double> lowerLimit = new ArrayList();

        for (int i = 0; i < data.size(); i++) {
            averageList.add(average);
            upperLimit.add(average + 0.225);
            lowerLimit.add(average - 0.225);
        }

        // 如果有足够的数据，可以分成3次检测
        // 这里简化处理，将数据分成3部分作为3次检测
        int size = data.size();
        int partSize = size / 3;
        
        List<Double> curve1 = new ArrayList<>();
        List<Double> curve2 = new ArrayList<>();
        List<Double> curve3 = new ArrayList<>();
        List<String> labels1 = new ArrayList<>();
        List<String> labels2 = new ArrayList<>();
        List<String> labels3 = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            if (i < partSize) {
                curve1.add(data.get(i));
                labels1.add(xLabels.get(i));
            } else if (i < partSize * 2) {
                curve2.add(data.get(i));
                labels2.add(xLabels.get(i));
            } else {
                curve3.add(data.get(i));
                labels3.add(xLabels.get(i));
            }
        }

        // 如果数据不足，使用单条曲线
        if (curve1.isEmpty() || curve2.isEmpty() || curve3.isEmpty()) {
            // 使用单条曲线
            ChartGenerator.createScatterChart(document, chartTitle, xAxisTitle, yAxisTitle, xLabels, data);
        } else {
            // 使用多系列散点图
            // 注意：需要确保所有系列的长度一致，这里简化处理
            int maxSize = Math.max(Math.max(curve1.size(), curve2.size()), curve3.size());
            while (curve1.size() < maxSize) curve1.add(curve1.isEmpty() ? 0.0 : curve1.get(curve1.size() - 1));
            while (curve2.size() < maxSize) curve2.add(curve2.isEmpty() ? 0.0 : curve2.get(curve2.size() - 1));
            while (curve3.size() < maxSize) curve3.add(curve3.isEmpty() ? 0.0 : curve3.get(curve3.size() - 1));
            while (labels1.size() < maxSize) labels1.add(labels1.isEmpty() ? "0" : labels1.get(labels1.size() - 1));
            while (labels2.size() < maxSize) labels2.add(labels2.isEmpty() ? "0" : labels2.get(labels2.size() - 1));
            while (labels3.size() < maxSize) labels3.add(labels3.isEmpty() ? "0" : labels3.get(labels3.size() - 1));

            // 使用统一的标签（使用最长的）
            List<String> unifiedLabels = xLabels.size() >= maxSize ? 
                    xLabels.subList(0, maxSize) : xLabels;
            List<Double> avgList = averageList.size() >= maxSize ? 
                    averageList.subList(0, maxSize) : averageList;
            List<Double> upperList = upperLimit.size() >= maxSize ? 
                    upperLimit.subList(0, maxSize) : upperLimit;
            List<Double> lowerList = lowerLimit.size() >= maxSize ? 
                    lowerLimit.subList(0, maxSize) : lowerLimit;

            ChartGenerator.createMultiSeriesScatterChart(document, chartTitle, xAxisTitle, yAxisTitle,
                    unifiedLabels, curve1, curve2, curve3, avgList, upperList, lowerList);
        }
    }

    /**
     * 生成里程标签
     */
    private List<String> generateMileageLabels(int count) {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            labels.add(String.valueOf(i));
        }
        return labels;
    }

    /**
     * 填充原始数据完整性表格
     */
    private void fillDataIntegrityTable(XWPFDocument document, Jobs job, 
                                       List<SensorStatus> sensorStatusList, 
                                       List<SensorData> sensorDataList) {
        // 获取传感器状态（通常只有一条记录）
        SensorStatus sensorStatus = sensorStatusList != null && !sensorStatusList.isEmpty() 
                ? sensorStatusList.get(0) : null;

        if (sensorStatus == null) {
            return;
        }

        // 这里需要根据模板中的表格位置来填充数据
        // 由于Word表格操作比较复杂，这里先记录逻辑，实际实现需要根据模板结构来调整
        // 表格内容：
        // - 陀螺仪L/R: sensorStatus.getGyroA()/getGyroB() == 1 ? "O" : "X"
        // - 里程L/R: sensorStatus.getEncoderA()/getEncoderB() == 1 ? "O" : "X"
        // - 倾角计: sensorStatus.getDipmeter() == 1 ? "O" : "X"
        // - 点激光L/R: sensorStatus.getPointLaserA()/getPointLaserB() == 1 ? "O" : "X"
        // - 超声L/R: sensorStatus.getUltrasonicA()/getUltrasonicB() == 1 ? "O" : "X"
        // - IMU: sensorStatus.getImu() == 1 ? "O" : "X"
        // - INS: sensorStatus.getIns() == 1 ? "O" : "X"
        // - 电子标签: sensorStatus.getRfid() == 1 ? "O" : "X"
    }

    /**
     * 填充重复性检测结果表格
     * 包括：轨距、水平、高低、轨向（正矢）的重复性检测结果
     */
    private void fillRepeatabilityTables(XWPFDocument document, Jobs job, 
                                       List<GeometryResultEntity> geometryResults,
                                       Map<String, String> formData) {
        if (geometryResults == null || geometryResults.isEmpty()) {
            log.warn("几何数据为空，无法填充重复性检测结果表格");
            return;
        }

        try {
            // 提取各种数据
            List<Double> gaugeData = geometryResults.stream()
                    .filter(r -> r.getTdf01Gauge() != null)
                    .map(GeometryResultEntity::getTdf01Gauge)
                    .collect(Collectors.toList());

            List<Double> levelData = geometryResults.stream()
                    .filter(r -> r.getLsf01Level() != null)
                    .map(GeometryResultEntity::getLsf01Level)
                    .collect(Collectors.toList());

            List<Double> leftHeightData = new ArrayList<>();
            List<Double> rightHeightData = new ArrayList<>();
            List<Double> leftDirectionData = new ArrayList<>();
            List<Double> rightDirectionData = new ArrayList<>();

            for (GeometryResultEntity result : geometryResults) {
                if (result.getTrackGeometry() != null && !result.getTrackGeometry().isEmpty()) {
                    try {
                        JsonNode trackNode = objectMapper.readTree(result.getTrackGeometry());
                        if (trackNode.isArray() && trackNode.size() >= 5) {
                            leftHeightData.add(trackNode.get(1).asDouble()); // gd0
                            rightHeightData.add(trackNode.get(2).asDouble()); // gd1
                            leftDirectionData.add(trackNode.get(3).asDouble()); // zs0
                            rightDirectionData.add(trackNode.get(4).asDouble()); // zs1
                        }
                    } catch (Exception e) {
                        // 解析失败，跳过
                    }
                }
            }

            // 计算重复性（标准差）
            double gaugeRepeatability = calculateRepeatability(gaugeData);
            double levelRepeatability = calculateRepeatability(levelData);
            double leftHeightRepeatability = calculateRepeatability(leftHeightData);
            double rightHeightRepeatability = calculateRepeatability(rightHeightData);
            double leftDirectionRepeatability = calculateRepeatability(leftDirectionData);
            double rightDirectionRepeatability = calculateRepeatability(rightDirectionData);

            // 查找并填充表格
            // 注意：这里需要根据模板中的表格结构来定位
            // 由于Word表格操作复杂，这里提供一个通用的填充方法
            // 实际使用时，可能需要根据模板的具体结构来调整

            log.info("重复性检测结果:");
            log.info("  轨距重复性: {} mm", gaugeRepeatability);
            log.info("  水平重复性: {} mm", levelRepeatability);
            log.info("  左高低重复性: {} mm", leftHeightRepeatability);
            log.info("  右高低重复性: {} mm", rightHeightRepeatability);
            log.info("  左轨向(正矢)重复性: {} mm", leftDirectionRepeatability);
            log.info("  右轨向(正矢)重复性: {} mm", rightDirectionRepeatability);

            // 遍历文档中的所有表格，查找包含"重复性"或"轨向"关键词的表格
            List<XWPFTable> tables = document.getTables();
            for (XWPFTable table : tables) {
                // 检查表格是否包含"轨向"或"重复性"关键词
                String tableText = table.getText().toLowerCase();
                if (tableText.contains("轨向") || tableText.contains("重复性")) {
                    fillDirectionRepeatabilityTable(table, leftDirectionRepeatability, rightDirectionRepeatability);
                }
            }

        } catch (Exception e) {
            log.error("填充重复性检测结果表格失败", e);
            // 不抛出异常，继续执行
        }
    }

    /**
     * 填充轨向（正矢）重复性检测结果表格
     */
    private void fillDirectionRepeatabilityTable(XWPFTable table, 
                                                 double leftRepeatability, double rightRepeatability) {
        try {
            // 轨向（正矢）的标准值
            double level0Standard10m = 0.5;
            double level0Standard30m = 0.5;
            double level0Standard300m = 2.25;
            double level1Standard10m = 0.75;
            double allowedError10m = 0.7;
            double allowedError10m2 = 1.0;

            // 遍历表格行，查找需要填充的位置
            List<XWPFTableRow> rows = table.getRows();
            for (int i = 0; i < rows.size(); i++) {
                XWPFTableRow row = rows.get(i);
                // XWPFTableRow 没有 getText() 方法，需要手动获取单元格文本
                StringBuilder rowTextBuilder = new StringBuilder();
                for (XWPFTableCell cell : row.getTableCells()) {
                    rowTextBuilder.append(cell.getText()).append(" ");
                }
                String rowText = rowTextBuilder.toString().toLowerCase();

                // 查找左轨向重复性行
                if (rowText.contains("左轨向") || rowText.contains("5-1")) {
                    fillRepeatabilityRow(row, leftRepeatability, level0Standard10m, level0Standard30m,
                                        level0Standard300m, level1Standard10m, allowedError10m, allowedError10m2);
                }

                // 查找右轨向重复性行
                if (rowText.contains("右轨向") || rowText.contains("5-2")) {
                    fillRepeatabilityRow(row, rightRepeatability, level0Standard10m, level0Standard30m,
                                        level0Standard300m, level1Standard10m, allowedError10m, allowedError10m2);
                }
            }
        } catch (Exception e) {
            log.error("填充轨向重复性表格失败", e);
        }
    }

    /**
     * 填充重复性检测行数据
     */
    private void fillRepeatabilityRow(XWPFTableRow row,
                                      double repeatability,
                                      double level0Standard10m, double level0Standard30m, double level0Standard300m,
                                      double level1Standard10m,
                                      double allowedError10m, double allowedError10m2) {
        try {
            List<XWPFTableCell> cells = row.getTableCells();
            
            // 根据表格结构填充数据
            // 假设表格结构为：重复性(mm) | 0级标准(10m) | 0级标准(30m) | 0级标准(300m) | 1级标准(10m) | 超限率 | 允许误差(10m) | 允许误差(10m) | 超限率
            if (cells.size() >= 9) {
                // 重复性(mm)
                setCellText(cells.get(1), String.format("%.2f", repeatability));
                
                // 0级标准
                setCellText(cells.get(2), String.format("%.2f", level0Standard10m));
                setCellText(cells.get(3), String.format("%.2f", level0Standard30m));
                setCellText(cells.get(4), String.format("%.2f", level0Standard300m));
                
                // 1级标准
                setCellText(cells.get(5), String.format("%.2f", level1Standard10m));
                
                // 超限率（计算）
                double exceedRate0 = calculateExceedRate(repeatability, level0Standard10m);
                setCellText(cells.get(6), String.format("%.2f%%", exceedRate0));
                
                // 允许误差
                setCellText(cells.get(7), String.format("%.2f", allowedError10m));
                setCellText(cells.get(8), String.format("%.2f", allowedError10m2));
                
                // 超限率（允许误差）
                double exceedRate1 = calculateExceedRate(repeatability, allowedError10m);
                if (cells.size() > 9) {
                    setCellText(cells.get(9), String.format("%.2f%%", exceedRate1));
                }
            }
        } catch (Exception e) {
            log.error("填充重复性行失败", e);
        }
    }

    /**
     * 设置单元格文本
     */
    private void setCellText(XWPFTableCell cell, String text) {
        if (cell != null) {
            try {
                // 清除现有内容
                int paragraphCount = cell.getParagraphs().size();
                for (int i = paragraphCount - 1; i >= 0; i--) {
                    cell.removeParagraph(i);
                }
                // 添加新段落和文本
                XWPFParagraph paragraph = cell.addParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(text);
                run.setFontFamily("宋体");
                run.setFontSize(10);
            } catch (Exception e) {
                log.warn("设置单元格文本失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 计算重复性（标准差）
     */
    private double calculateRepeatability(List<Double> data) {
        if (data == null || data.isEmpty()) {
            return 0.0;
        }
        
        // 计算平均值
        double mean = data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        // 计算标准差
        double variance = data.stream()
                .mapToDouble(x -> Math.pow(x - mean, 2))
                .average()
                .orElse(0.0);
        
        return Math.sqrt(variance);
    }

    /**
     * 根据图表标题获取需要计算的阈值列表
     */
    private List<Double> getThresholdsForChart(String chartTitle) {
        List<Double> thresholds = new ArrayList<>();
        
        if (chartTitle.contains("轨距")) {
            // 轨距：0.15, 0.225, 0.25, 0.375
            thresholds.add(0.15);
            thresholds.add(0.225);
            thresholds.add(0.25);
            thresholds.add(0.375);
        } else if (chartTitle.contains("水平")) {
            // 水平：0.15, 0.225, 0.25, 0.375
            thresholds.add(0.15);
            thresholds.add(0.225);
            thresholds.add(0.25);
            thresholds.add(0.375);
        } else if (chartTitle.contains("左高低") || chartTitle.equals("4-1左高低重复性")) {
            // 左高低：0.5, 2.25, 0.75
            thresholds.add(0.5);
            thresholds.add(2.25);
            thresholds.add(0.75);
        } else if (chartTitle.contains("右高低") || chartTitle.equals("4-2右高低重复性")) {
            // 右高低：0.5, 2.25, 0.75
            thresholds.add(0.5);
            thresholds.add(2.25);
            thresholds.add(0.75);
        } else if (chartTitle.contains("左轨向") || chartTitle.contains("5-1左轨向")) {
            // 左轨向：0.5, 2.25, 0.75
            thresholds.add(0.5);
            thresholds.add(2.25);
            thresholds.add(0.75);
        } else if (chartTitle.contains("右轨向") || chartTitle.contains("5-2右轨向")) {
            // 右轨向：0.5, 2.25, 0.75
            thresholds.add(0.5);
            thresholds.add(2.25);
            thresholds.add(0.75);
        }
        
        return thresholds;
    }

    /**
     * 根据图表标题和阈值生成超限率占位符key
     */
    private String getOverLimitRateKey(String chartTitle, Double threshold) {
        String prefix = null;
        
        // 根据图表标题确定前缀
        if (chartTitle.contains("轨距")) {
            prefix = "Tdfs";
        } else if (chartTitle.contains("水平")) {
            prefix = "level";
        } else if (chartTitle.contains("左高低") || chartTitle.equals("4-1左高低重复性")) {
            prefix = "heightL";
        } else if (chartTitle.contains("右高低") || chartTitle.equals("4-2右高低重复性")) {
            prefix = "heightR";
        } else if (chartTitle.contains("左轨向") || chartTitle.contains("5-1左轨向")) {
            prefix = "directL";
        } else if (chartTitle.contains("右轨向") || chartTitle.contains("5-2右轨向")) {
            prefix = "directR";
        }
        
        if (prefix != null) {
            // 格式化阈值，去除小数点后多余的0
            String thresholdStr = threshold.toString();
            if (thresholdStr.endsWith(".0")) {
                thresholdStr = thresholdStr.substring(0, thresholdStr.length() - 2);
            }
            return prefix + "-" + thresholdStr;
        }
        
        return null;
    }

    /**
     * 保存图表超限率（已废弃，现在使用新的多阈值计算方法）
     * @deprecated 请使用新的多阈值计算方法，该方法保留用于兼容性
     */
    @Deprecated
    private void saveOverLimitRate(String chartTitle, double exceedRate, Map<String, Double> overLimitRates) {
        // 保留原有逻辑，但不再使用
        String key = null;
        
        // 根据图表标题确定存储的key
        if (chartTitle.contains("轨距")) {
            key = "Tdfs-0.225";
        } else if (chartTitle.contains("水平")) {
            key = "level-0.225";
        } else if (chartTitle.contains("左高低") || chartTitle.equals("4-1左高低重复性")) {
            key = "heightL-0.5";
        } else if (chartTitle.contains("右高低") || chartTitle.equals("4-2右高低重复性")) {
            key = "heightR-0.5";
        } else if (chartTitle.contains("左轨向") || chartTitle.contains("5-1左轨向")) {
            key = "directL-0.5";
        } else if (chartTitle.contains("右轨向") || chartTitle.contains("5-2右轨向")) {
            key = "directR-0.5";
        }
        
        if (key != null) {
            overLimitRates.put(key, exceedRate);
            log.info("保存超限率: {} = {:.2f}%", key, exceedRate);
        }
    }
    
    /**
     * 将超限率填充到文档占位符
     * 占位符格式：
     * 轨距：${Tdfs-0.15}, ${Tdfs-0.225}, ${Tdfs-0.25}, ${Tdfs-0.375}
     * 水平：${level-0.15}, ${level-0.225}, ${level-0.25}, ${level-0.375}
     * 左高低：${heightL-0.5}, ${heightL-2.25}, ${heightL-0.75}
     * 右高低：${heightR-0.5}, ${heightR-2.25}, ${heightR-0.75}
     * 左轨向：${directionL-0.5}, ${directionL-2.25}, ${directionL-0.75}
     * 右轨向：${directR-0.5}, ${directR-2.25}, ${directR-0.75}
     */
    private void fillOverLimitRatesToDocument(XWPFDocument document, Map<String, Double> overLimitRates) {
        try {
            log.info("开始填充超限率到文档，当前超限率数量: {}", overLimitRates.size());
            
            // 遍历所有段落
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replaceOverLimitRateInParagraph(paragraph, overLimitRates);
            }
            
            // 遍历所有表格中的段落
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            replaceOverLimitRateInParagraph(paragraph, overLimitRates);
                        }
                    }
                }
            }
            
            log.info("超限率填充完成");
        } catch (Exception e) {
            log.error("填充超限率失败", e);
        }
    }
    
    /**
     * 在段落中替换超限率占位符
     * 改进版本：保留原有文本格式，只替换占位符部分
     * 支持 ${} 和 {{}} 两种格式（容错处理）
     */
    private void replaceOverLimitRateInParagraph(XWPFParagraph paragraph, Map<String, Double> overLimitRates) {
        if (paragraph.getRuns() == null || paragraph.getRuns().isEmpty()) {
            return;
        }
        
        try {
            // 遍历所有run，查找并替换占位符
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.getText(0);
                if (text == null || text.isEmpty()) {
                    continue;
                }
                
                // 检查是否包含超限率占位符（支持 ${} 和 {{}} 两种格式）
                boolean needsReplace = false;
                for (String key : overLimitRates.keySet()) {
                    if (text.contains("${" + key + "}") || text.contains("{{" + key + "}}")) {
                        needsReplace = true;
                        break;
                    }
                }
                
                if (needsReplace) {
                    // 替换占位符，保留原有格式
                    String newText = text;
                    for (Map.Entry<String, Double> entry : overLimitRates.entrySet()) {
                        String placeholder1 = "${" + entry.getKey() + "}";  // ${} 格式
                        String placeholder2 = "{{" + entry.getKey() + "}}"; // {{}} 格式（容错）
                        String value = String.format("%.2f%%", entry.getValue());
                        
                        // 替换两种格式的占位符
                        newText = newText.replace(placeholder1, value);
                        newText = newText.replace(placeholder2, value);
                    }
                    
                    if (!newText.equals(text)) {
                        run.setText(newText, 0);
                        log.debug("超限率占位符替换: {} -> {}", text, newText);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("替换段落中的超限率失败: {}", e.getMessage());
        }
    }
    
    /**
     * 计算超限率
     */
    private double calculateExceedRate(double value, double standard) {
        if (standard == 0) {
            return 0.0;
        }
        // 如果值超过标准，计算超限率
        if (value > standard) {
            return ((value - standard) / standard) * 100.0;
        }
        return 0.0;
    }

    /**
     * 添加分页符
     * 注意：此方法会添加新段落，可能影响模板格式，请谨慎使用
     */
    @Deprecated
    private void addPageBreak(XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.addBreak(org.apache.poi.xwpf.usermodel.BreakType.PAGE);
    }

    /**
     * 添加小节标题
     * 注意：此方法会添加新段落，可能影响模板格式，请谨慎使用
     */
    @Deprecated
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

    /**
     * 将模板中的 ${key} 格式占位符转换为 {{key}} 格式（poi-tl要求的格式）
     * 这个方法会遍历Word文档中的所有段落和表格单元格，替换占位符格式
     */
    private InputStream convertPlaceholderFormat(InputStream templateStream) {
        try {
            // 先将输入流读取为字节数组，以便可以多次使用
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = templateStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] templateBytes = buffer.toByteArray();
            
            // 从字节数组创建新的输入流来读取文档
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(templateBytes);
            XWPFDocument document = new XWPFDocument(byteArrayInputStream);
            
            // 正则表达式：匹配 ${...} 格式的占位符
            // 支持 ${key}、${key.subkey}、${key,default} 等格式
            Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
            
            int replaceCount = 0;
            
            // 替换所有段落中的占位符
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                if (replacePlaceholdersInParagraph(paragraph, pattern)) {
                    replaceCount++;
                }
            }
            
            // 替换所有表格单元格中的占位符
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            if (replacePlaceholdersInParagraph(paragraph, pattern)) {
                                replaceCount++;
                            }
                        }
                    }
                }
            }
            
            // 将修改后的文档转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            document.close();
            byteArrayInputStream.close();
            
            log.info("占位符格式转换完成，已将 ${} 格式转换为 {{}} 格式，共替换 {} 处", replaceCount);
            return new ByteArrayInputStream(outputStream.toByteArray());
            
        } catch (Exception e) {
            log.error("转换占位符格式失败，将使用原始模板: {}", e.getMessage(), e);
            // 如果转换失败，尝试重新读取原始流
            try {
                // 重新创建输入流
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int nRead;
                while ((nRead = templateStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                return new ByteArrayInputStream(buffer.toByteArray());
            } catch (IOException ex) {
                log.error("无法读取模板流", ex);
                throw new RuntimeException("无法处理模板文件", ex);
            }
        }
    }

    /**
     * 在段落中替换占位符格式
     * 注意：超限率占位符（${Tdfs-0.225}, ${level-0.225}等）不应该被转换，因为它们需要在后续步骤中填充
     * @return 是否进行了替换
     */
    private boolean replacePlaceholdersInParagraph(XWPFParagraph paragraph, Pattern pattern) {
        String text = paragraph.getText();
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // 检查是否包含 ${} 格式的占位符
        if (!text.contains("${")) {
            return false;
        }
        
        // 定义超限率占位符列表（这些占位符应该保持 ${} 格式，不转换为 {{}}）
        String[] overLimitPlaceholders = {
            "Tdfs-0.225", "level-0.225", 
            "heightR-0.5", "heightL-0.5", 
            "directR-0.5", "directL-0.5"
        };
        
        // 使用正则表达式替换 ${key} 为 {{key}}，但排除超限率占位符
        String replacedText = text;
        
        // 先找到所有匹配的占位符
        java.util.regex.Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String placeholderContent = matcher.group(1); // 获取 ${} 中间的内容
            
            // 检查是否是超限率占位符
            boolean isOverLimitPlaceholder = false;
            for (String overLimitKey : overLimitPlaceholders) {
                if (placeholderContent.equals(overLimitKey)) {
                    isOverLimitPlaceholder = true;
                    break;
                }
            }
            
            // 如果是超限率占位符，保持原样；否则转换为 {{}}
            if (isOverLimitPlaceholder) {
                matcher.appendReplacement(sb, "\\${" + placeholderContent + "}"); // 保持 ${} 格式
                log.debug("保留超限率占位符: ${{{}}}", placeholderContent);
            } else {
                matcher.appendReplacement(sb, "{{" + placeholderContent + "}}"); // 转换为 {{}} 格式
            }
        }
        matcher.appendTail(sb);
        replacedText = sb.toString();
        
        // 如果文本被修改，更新段落内容
        if (!replacedText.equals(text)) {
            // 保存原有格式信息
            List<XWPFRun> runs = paragraph.getRuns();
            boolean isBold = false;
            boolean isItalic = false;
            int fontSize = -1;
            String fontFamily = null;
            String color = null;
            
            if (!runs.isEmpty()) {
                XWPFRun originalRun = runs.get(0);
                isBold = originalRun.isBold();
                isItalic = originalRun.isItalic();
                fontSize = originalRun.getFontSize();
                fontFamily = originalRun.getFontFamily();
                color = originalRun.getColor();
            }
            
            // 清除段落中的所有运行（runs）
            for (int i = runs.size() - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }
            
            // 添加新的运行，包含替换后的文本
            XWPFRun run = paragraph.createRun();
            run.setText(replacedText);
            
            // 恢复原有格式
            if (fontSize > 0) {
                run.setFontSize(fontSize);
            }
            if (fontFamily != null) {
                run.setFontFamily(fontFamily);
            }
            if (color != null) {
                run.setColor(color);
            }
            run.setBold(isBold);
            run.setItalic(isItalic);
            
            log.debug("已替换段落中的占位符: {} -> {}", text, replacedText);
            return true;
        }
        
        return false;
    }
}