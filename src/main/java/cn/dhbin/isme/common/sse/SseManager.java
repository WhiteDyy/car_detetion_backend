package cn.dhbin.isme.common.sse;

import cn.hutool.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 通用的SSE连接管理器服务.
 * 负责管理不同主题(Topic)的SSE连接、广播消息和心跳维持.
 */
@Service
public class SseManager {

    private static final Logger logger = LoggerFactory.getLogger(SseManager.class);

    // 使用 ConcurrentHashMap 来存储不同主题的 Emitter 列表
    // Key: 主题名称 (e.g., "sensor", "surfaceImage")
    // Value: 该主题下的所有 SseEmitter 连接
    private final Map<String, List<SseEmitter>> topicEmitters = new ConcurrentHashMap<>();

    // 单独的线程池用于发送心跳，与业务线程分离
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    public SseManager() {
        // 启动一个定时任务，每15秒向所有活跃的连接发送心跳
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeatToAll, 15, 15, TimeUnit.SECONDS);
    }

    /**
     * 为指定主题注册一个新的 SseEmitter.
     *
     * @param topic   主题名称
     * @param emitter 要注册的 SseEmitter
     */
    public void register(String topic, SseEmitter emitter) {
        // getOrDefault + computeIfAbsent 的线程安全组合
        List<SseEmitter> emitters = topicEmitters.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        logger.info("New client registered for topic '{}'. Total clients for this topic: {}", topic, emitters.size());

        // 设置 Emitter 的完成、超时和错误回调，以便在连接断开时自动清理
        emitter.onCompletion(() -> {
            logger.info("Emitter completed for topic '{}'. Removing...", topic);
            removeEmitter(topic, emitter);
        });
        emitter.onTimeout(() -> {
            logger.info("Emitter timed out for topic '{}'. Removing...", topic);
            removeEmitter(topic, emitter);
        });
        emitter.onError(e -> {
            logger.error("Emitter error for topic '{}'. Removing...", topic, e);
            removeEmitter(topic, emitter);
        });

        JSONObject message = new JSONObject().set("message", "Successfully connected to topic: " + topic);

        // 注册成功后，立即发送一次连接确认事件
        try {
            emitter.send(SseEmitter.event().name("connected").data(message));
        } catch (IOException e) {
            logger.warn("Failed to send initial connection event to client for topic '{}'", topic, e);
        }
    }

    /**
     * 向指定主题的所有客户端广播事件.
     *
     * @param topic 主题名称
     * @param event 要广播的 SseEventBuilder 对象
     */
    public void broadcast(String topic, SseEmitter.SseEventBuilder event) {
        List<SseEmitter> emitters = topicEmitters.get(topic);
        if (emitters == null || emitters.isEmpty()) {
            // logger.trace("No clients connected to topic '{}', skipping broadcast.", topic);
            return;
        }

        logger.info("Broadcasting to {} clients on topic '{}'", emitters.size(), topic);

        // 遍历并发送消息
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(event);
            } catch (Exception e) {
                // 如果发送失败，触发 onError 回调，最终会调用 removeEmitter
                logger.warn("Failed to send message to a client on topic '{}'. It will be removed.", topic, e);
            }
        }
    }

    /**
     * 从指定主题中移除一个 Emitter.
     *
     * @param topic   主题名称
     * @param emitter 要移除的 SseEmitter
     */
    private void removeEmitter(String topic, SseEmitter emitter) {
        List<SseEmitter> emitters = topicEmitters.get(topic);
        if (emitters != null) {
            emitters.remove(emitter);
            logger.info("Client removed from topic '{}'. Remaining clients: {}", topic, emitters.size());
            // 如果一个主题下没有任何连接了，可以从Map中移除这个主题以释放内存
            if (emitters.isEmpty()) {
                topicEmitters.remove(topic);
                logger.info("Topic '{}' is now empty and has been removed.", topic);
            }
        }
    }

    /**
     * 向所有主题的所有客户端发送心跳.
     */
    private void sendHeartbeatToAll() {
        if (topicEmitters.isEmpty()) {
            return;
        }
        logger.debug("Sending heartbeat to all clients across {} topics.", topicEmitters.size());
        SseEmitter.SseEventBuilder heartbeatEvent = SseEmitter.event().name("heartbeat").comment("keep-alive");

        topicEmitters.forEach((topic, emitters) -> {
            // logger.debug("Sending heartbeat to {} clients on topic '{}'", emitters.size(), topic);
            broadcast(topic, heartbeatEvent);
        });
    }

    /**
     * 在应用关闭时，优雅地关闭线程池.
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down SSE heartbeat executor...");
        heartbeatExecutor.shutdown();
        try {
            if (!heartbeatExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                heartbeatExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            heartbeatExecutor.shutdownNow();
        }
        logger.info("SSE heartbeat executor has been shut down.");
    }
}