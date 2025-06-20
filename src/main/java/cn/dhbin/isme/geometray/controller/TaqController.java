package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.TaqData;
import cn.dhbin.isme.geometray.domain.requeset.TaqRequest;
import cn.dhbin.isme.geometray.service.TaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/taq")
@RequiredArgsConstructor
@Tag(name = "道岔尺寸报表")
public class TaqController {
    @Resource
    TaqService taqService;

    @PostMapping("/search")
    @Operation(summary = "搜索数据")
    public R<Page<TaqData>> search(@RequestBody TaqRequest taqRequest) {
        Page<TaqData> page = taqService.queryPage(taqRequest);
        return R.ok(page);
    }
}
