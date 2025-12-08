package cn.dhbin.isme.geometray.controller;


import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.CurveQuery;
import cn.dhbin.isme.geometray.domain.requeset.CurveQueryRequest;
import cn.dhbin.isme.geometray.service.CurveQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/curve")
@RequiredArgsConstructor
@Tag(name = "曲线查询表")
public class CurveQueryController {

    @Resource
    CurveQueryService curveQueryService;

    @PostMapping("/search")
    @Operation(summary = "查询数据")
    public R<Page<CurveQuery>> search(@RequestBody CurveQueryRequest request) {
        Page<CurveQuery> curveQueryPage = curveQueryService.queryPage(request);
        return R.ok(curveQueryPage);
    }
}
