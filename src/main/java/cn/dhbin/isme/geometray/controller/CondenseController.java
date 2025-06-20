package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.CondenseTable;
import cn.dhbin.isme.geometray.domain.requeset.CondenseRequest;
import cn.dhbin.isme.geometray.service.CondenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/condense")
@RequiredArgsConstructor
@Tag(name = "压缩报表")
public class CondenseController {
    @Resource
    CondenseService condenseService;

    @PostMapping("/search")
    @Operation(summary = "数据搜索")
    public R<Page<CondenseTable>> search(@RequestBody CondenseRequest condenseRequest){
        Page<CondenseTable> condenseTablePage = condenseService.queryPage(condenseRequest);
        return R.ok(condenseTablePage);
    }
}
