package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.SwitchManualBatch;
import cn.dhbin.isme.geometray.service.SwitchManualBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/switch_manual_batches")
@Tag(name = "道岔人工测量批次")
public class SwitchManualBatchController {

    @Resource
    private SwitchManualBatchService switchManualBatchService;

    @PostMapping
    @Operation(summary = "新增人工测量批次")
    public R<SwitchManualBatch> create(@RequestBody Map<String, Object> payload) {
        SwitchManualBatch saved = switchManualBatchService.createBatch(payload);
        return R.ok(saved);
    }

    @GetMapping
    @Operation(summary = "分页查询人工测量批次")
    public R<Page<SwitchManualBatch>> list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Page<SwitchManualBatch> result = switchManualBatchService.queryPage(page, pageSize, keyword);
        return R.ok(result);
    }
}
