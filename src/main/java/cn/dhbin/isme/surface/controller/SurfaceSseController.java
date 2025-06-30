package cn.dhbin.isme.surface.controller;


import cn.dhbin.isme.common.sse.SseManager; // 引入新的 SseManager
import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SurfaceSseController {

    @Resource
    private SseManager sseManager;

    // 定义主题名称为常量，便于复用和维护
    public static final String SURFACE_IMAGE_TOPIC = "surfaceImage";

    @GetMapping("/surface_images")
    public SseEmitter streamImages() {
        // 创建一个超时时间较长的 Emitter
        SseEmitter emitter = new SseEmitter(3600_000L); // 1小时超时

        // 将 Emitter 注册到 "surfaceImage" 主题
        sseManager.register(SURFACE_IMAGE_TOPIC, emitter);

        return emitter;
    }
}