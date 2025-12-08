package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.CurseInspect;
import cn.dhbin.isme.geometray.domain.requeset.CurseInspectRequest;
import cn.dhbin.isme.geometray.service.CurseInspectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/curse_inspect")
@RequiredArgsConstructor
@Tag(name = "曲线检查记录表")
public class CurseInspectController {
    @Resource
    CurseInspectService curseInspectService;

    @PostMapping("/search")
    @Operation(summary = "数据搜索")
    public R<Page<CurseInspect>> search(@RequestBody CurseInspectRequest request) {
        Page<CurseInspect> curseInspectPage = curseInspectService.queryPage(request);
        return R.ok(curseInspectPage);
    }
}
