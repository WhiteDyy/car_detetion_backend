package cn.dhbin.isme.geometray.controller;



import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.Tqi;
import cn.dhbin.isme.geometray.domain.requeset.TqiRequest;
import cn.dhbin.isme.geometray.service.TqiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tqi")
@RequiredArgsConstructor
@Tag(name = "TQI报告")
public class TqiController {
    @Resource
    TqiService tqiService;

    @PostMapping("/search")
    @Operation(summary = "数据搜索")
    public R<Page<Tqi>> search(@RequestBody TqiRequest request) {
        Page<Tqi> tqiPage = tqiService.queryPage(request);
        return R.ok(tqiPage);
    }
}
