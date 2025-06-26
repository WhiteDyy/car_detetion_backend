package cn.dhbin.isme.surface.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseBroadcastService {

    private static final Logger logger = LoggerFactory.getLogger(SseBroadcastService.class);

    // 使用线程安全的CopyOnWriteArrayList来存储所有客户端连接
    // 它非常适合“读多写少”的场景，比如广播
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * 添加一个新的SSE Emitter到广播列表
     *
     * @param emitter SseEmitter实例
     */
    public void addEmitter(final SseEmitter emitter) {
        // 为每个连接设置超时和完成事件，以便在断开时从列表中移除
        emitter.onCompletion(() -> {
            logger.info("SSE emitter completed: {}", emitter);
            this.emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            logger.info("SSE emitter timed out: {}", emitter);
            this.emitters.remove(emitter);
        });
        emitter.onError(e -> {
            logger.error("SSE emitter error: {}", emitter, e);
            this.emitters.remove(emitter);
        });

        this.emitters.add(emitter);
        logger.info("New SSE emitter added: {}. Current count: {}", emitter, this.emitters.size());
    }

    /**
     * 向所有连接的客户端广播消息
     *
     * @param data 要发送的数据（这里是图片的URL）
     */
    public void broadcast(String data) {
        logger.info("Broadcasting data to {} clients: {}", this.emitters.size(), data);
        for (SseEmitter emitter : this.emitters) {
            try {
                // 发送一个名为 "image-url" 的事件
                emitter.send(SseEmitter.event().name("image-url").data(data));
            } catch (IOException e) {
                // 如果发送失败（例如客户端关闭了连接），就从列表中移除
                logger.warn("Failed to send to an emitter, removing it.", e);
                this.emitters.remove(emitter);
            }
        }
    }
}