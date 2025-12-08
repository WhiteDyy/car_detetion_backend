package cn.dhbin.isme.geometray.controller;


import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.TransfiniteTable;
import cn.dhbin.isme.geometray.domain.requeset.TransfiniteRequest;
import cn.dhbin.isme.geometray.service.TransfiniteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfinite")
@RequiredArgsConstructor
@Tag(name = "超限报表")
public class TransfiniteController {
    @Resource
    TransfiniteService transfiniteService;

    @PostMapping("/search")
    @Operation(summary = "数据搜索")
    public R<Page<TransfiniteTable>> search(@RequestBody TransfiniteRequest transfiniteRequest) {
        Page<TransfiniteTable> transfiniteTablePage = transfiniteService.queryPage(transfiniteRequest);
        return R.ok(transfiniteTablePage);
    }
}
