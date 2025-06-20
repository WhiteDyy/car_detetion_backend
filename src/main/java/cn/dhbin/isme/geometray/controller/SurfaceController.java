package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.entity.Surface;
import cn.dhbin.isme.geometray.domain.requeset.SurfaceRequest;
import cn.dhbin.isme.geometray.service.SurfaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/surface")
@RequiredArgsConstructor
@Tag(name = "图像报表")
public class SurfaceController {
    @Resource
    SurfaceService surfaceService;

    @PostMapping("/search")
    @Operation(summary = "搜索数据")
    public R<Page<Surface>> search(@RequestBody SurfaceRequest request) {
        Page<Surface> surfacePage = surfaceService.querPage(request);
        return R.ok(surfacePage);
    }
}
