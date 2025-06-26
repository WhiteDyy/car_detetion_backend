package cn.dhbin.isme.surface.controller;


import cn.dhbin.isme.surface.service.SseBroadcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SurfaceSSeController {

    @Autowired
    private SseBroadcastService sseBroadcastService;

    @GetMapping("/sse/stream")
    public SseEmitter streamImages() {
        // 创建一个永不超时的 SseEmitter
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // 将新的 Emitter 添加到广播列表
        sseBroadcastService.addEmitter(emitter);

        return emitter;
    }
}