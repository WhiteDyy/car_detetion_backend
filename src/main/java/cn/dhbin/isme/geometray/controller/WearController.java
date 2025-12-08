package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.WearTable;
import cn.dhbin.isme.geometray.domain.requeset.WearRequest;
import cn.dhbin.isme.geometray.service.WearService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wear")
@RequiredArgsConstructor
@Tag(name = "磨耗报表")
public class WearController {
    @Resource
    WearService wearService;

    @PostMapping("/search")
    @Operation(summary = "搜索数据")
    public R<Page<WearTable>> search(@RequestBody WearRequest request) {
//        System.out.println("接收到的参数：" + request);
        Page<WearTable> page = wearService.queryPage(request);
        return R.ok(page);
    }

}
