package cn.dhbin.isme.geometray.controller;

import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.geometray.domain.dto.FolderInfoDto;
import cn.dhbin.isme.geometray.domain.dto.FrameDataDto;
import cn.dhbin.isme.geometray.service.PointCloudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 点云数据可视化控制器
 */
@Slf4j
@RestController
@RequestMapping("/point-cloud")
@RequiredArgsConstructor
@Tag(name = "点云数据可视化", description = "点云数据可视化相关接口")
public class PointCloudController {

    private final PointCloudService pointCloudService;

    @GetMapping("/folder/load")
    @Operation(summary = "加载数据文件夹", description = "加载指定路径的数据文件夹，返回所有可用的帧号列表")
    public R<FolderInfoDto> loadDataFolder(@RequestParam String folderPath) {
        log.info("加载数据文件夹请求: {}", folderPath);
        FolderInfoDto folderInfo = pointCloudService.loadDataFolder(folderPath);
        return R.ok(folderInfo);
    }

    @GetMapping("/frame")
    @Operation(summary = "获取帧数据", description = "获取指定文件夹和帧号的点云数据")
    public R<FrameDataDto> getFrameData(
            @RequestParam String folderPath,
            @RequestParam Integer frameNumber) {
        log.info("获取帧数据请求: folderPath={}, frameNumber={}", folderPath, frameNumber);
        FrameDataDto frameData = pointCloudService.getFrameData(folderPath, frameNumber);
        return R.ok(frameData);
    }
}

