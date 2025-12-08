package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.SgTable;
import cn.dhbin.isme.geometray.domain.requeset.SgRequest;
import cn.dhbin.isme.geometray.service.SgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sg")
@RequiredArgsConstructor
@Tag(name = "道岔道尺报表")
public class SgController {
    @Resource
    SgService sgService;

    @PostMapping("/search")
    @Operation(summary = "搜索数据")
    public R<Page<SgTable>> search(@RequestBody SgRequest request) {
        Page<SgTable> page = sgService.queryPage(request);
        return R.ok(page);
    }
}
