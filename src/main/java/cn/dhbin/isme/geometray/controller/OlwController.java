package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.Olw;
import cn.dhbin.isme.geometray.domain.requeset.OlwRequest;
import cn.dhbin.isme.geometray.service.OlwService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/olw")
@RequiredArgsConstructor
@Tag(name = "输出长波报表")
public class OlwController {
    @Resource
    OlwService olwService;

    @PostMapping("/search")
    @Operation(summary = "搜索数据")
    public R<Page<Olw>> search(@RequestBody OlwRequest olwRequest) {
        Page<Olw> page = olwService.queryPage(olwRequest);
        return R.ok(page);
    }
}
