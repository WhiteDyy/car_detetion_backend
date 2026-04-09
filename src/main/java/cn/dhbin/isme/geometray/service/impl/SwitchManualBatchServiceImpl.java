package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.SwitchManualBatch;
import cn.dhbin.isme.geometray.mapper.SwitchManualBatchMapper;
import cn.dhbin.isme.geometray.service.SwitchManualBatchService;
import cn.dhbin.isme.rabbitmqconsumer.RabbitMQControlProducer;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SwitchManualBatchServiceImpl implements SwitchManualBatchService {

    @Resource
    private SwitchManualBatchMapper switchManualBatchMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private RabbitMQControlProducer rabbitMQControlProducer;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public SwitchManualBatch createBatch(Map<String, Object> payload) {
        ensureTableIfNeeded();

        String batchName = toStr(payload.get("batchName"));
        String turnoutName = toStr(payload.get("turnoutName"));
        String remark = toStr(payload.get("remark"));
        LocalDateTime recordedAt = parseRecordedAt(payload.get("recordedAt"));

        List<Map<String, Object>> nodes = new ArrayList<>();
        Object nodesObj = payload.get("nodes");
        if (nodesObj instanceof List<?>) {
            for (Object item : (List<?>) nodesObj) {
                if (item instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nodeMap = (Map<String, Object>) item;
                    nodes.add(nodeMap);
                }
            }
        }

        try {
            String nodesJson = objectMapper.writeValueAsString(nodes);

            SwitchManualBatch entity = new SwitchManualBatch();
            entity.setBatchName(batchName);
            entity.setTurnoutName(turnoutName);
            entity.setRecordedAt(recordedAt);
            entity.setRemark(remark);
            entity.setNodesJson(nodesJson);
            entity.setCreatedAt(LocalDateTime.now());

            switchManualBatchMapper.insert(entity);
            entity.setNodes(nodes);

            boolean sent = rabbitMQControlProducer.sendTurnoutConfigCommand(
                    turnoutName,
                    batchName,
                    recordedAt,
                    remark,
                    nodes
            );
            if (!sent) {
                log.warn("人工测量批次已落库，但发送MQ同步消息失败: batchId={}, turnoutName={}", entity.getId(), turnoutName);
            }

            return entity;
        } catch (Exception e) {
            throw new RuntimeException("保存人工测量批次失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<SwitchManualBatch> queryPage(Integer page, Integer pageSize, String keyword) {
        ensureTableIfNeeded();

        int pageNum = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;

        LambdaQueryWrapper<SwitchManualBatch> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SwitchManualBatch::getBatchName, kw)
                    .or()
                    .like(SwitchManualBatch::getTurnoutName, kw));
        }
        wrapper.orderByDesc(SwitchManualBatch::getRecordedAt)
                .orderByDesc(SwitchManualBatch::getId);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SwitchManualBatch> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, size);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SwitchManualBatch> result =
                switchManualBatchMapper.selectPage(mpPage, wrapper);

        for (SwitchManualBatch row : result.getRecords()) {
            row.setNodes(parseNodes(row.getNodesJson()));
        }

        Page<SwitchManualBatch> response = new Page<>();
        response.setPageData(result.getRecords());
        response.setTotal(result.getTotal());
        return response;
    }

    private void ensureTableIfNeeded() {
        String ddl = "CREATE TABLE IF NOT EXISTS switch_manual_batch ("
                + "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                + "batch_name VARCHAR(128) NOT NULL,"
                + "turnout_name VARCHAR(128) NOT NULL,"
                + "recorded_at DATETIME NOT NULL,"
                + "remark TEXT NULL,"
                + "nodes_json LONGTEXT NOT NULL,"
                + "created_at DATETIME NOT NULL"
                + ")";
        jdbcTemplate.execute(ddl);
    }

    private List<Map<String, Object>> parseNodes(String nodesJson) {
        if (nodesJson == null || nodesJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(nodesJson, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            log.warn("解析nodes_json失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String toStr(Object v) {
        return v == null ? null : String.valueOf(v).trim();
    }

    private LocalDateTime parseRecordedAt(Object value) {
        if (value == null) {
            return LocalDateTime.now();
        }
        try {
            if (value instanceof Number) {
                long millis = ((Number) value).longValue();
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
            }
            String s = String.valueOf(value).trim();
            if (s.matches("^\\d{13}$")) {
                long millis = Long.parseLong(s);
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
            }
            return LocalDateTime.parse(s.replace("Z", ""));
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
