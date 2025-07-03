package cn.dhbin.isme.surface.service;

import cn.dhbin.isme.common.sse.SseManager;
import cn.dhbin.isme.surface.controller.SurfaceSseController;
import cn.hutool.json.JSONObject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ImageProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingService.class);

    @Resource
    private SseManager sseManager;

    // 从 application.properties 读取配置
    @Value("${image.storage.path}")
    private String storageDirectoryPath;

    @Value("${image.url.prefix}")
    private String imageUrlPrefix;

    private Path storageDirectory;


//    private final List<String> urlBuffer = new ArrayList<>(); // 存储 URL 的缓冲区
//    private final AtomicLong lastBroadcastTime = new AtomicLong(0); // 上次推送时间
//    private static final long MIN_BROADCAST_INTERVAL = 500; // 推送间隔（2秒）
//    private static final int MAX_BATCH_SIZE = 5; // 最大批量推送数量

    // --- 配置项 ---
    // 当缓冲区达到此数量时，立即推送
    private static final int MAX_BATCH_SIZE = 5; // 建议减小此值，例如 5 或 10
    // 无论数量多少，超过此时间间隔（毫秒）就必须推送一次
    private static final long MIN_BROADCAST_INTERVAL = 500L; // 500毫秒

    // --- 成员变量 ---
    // 使用线程安全的 CopyOnWriteArrayList 存储 URL 缓冲区
    private final List<String> urlBuffer = new CopyOnWriteArrayList<>();
    // 记录上次推送时间的原子长整数
    private final AtomicLong lastBroadcastTime = new AtomicLong(System.currentTimeMillis());


    public void ImageService(Path storageDirectory, String imageUrlPrefix, SseManager sseManager) {
        this.storageDirectory = storageDirectory;
        this.imageUrlPrefix = imageUrlPrefix;
        this.sseManager = sseManager;
    }


    /**
     * 使用 @PostConstruct 注解，在服务初始化时执行此方法
     * 作用：确保图片存储目录存在
     */
    @PostConstruct
    public void init() {
        this.storageDirectory = Paths.get(storageDirectoryPath);
        try {
            Files.createDirectories(this.storageDirectory);
            logger.info("Image storage directory successfully created or already exists at: {}", this.storageDirectory.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not create image storage directory: {}", storageDirectoryPath, e);
            throw new RuntimeException("Could not create image storage directory", e);
        }
    }


//    @Async
//    public void processAndBroadcast(byte[] imageData) {
//        if (imageData == null || imageData.length == 0) {
//            logger.warn("Received empty image data. Skipping.");
//            return;
//        }
//
//        // 生成唯一文件名
//        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
//        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
//        String filename = timestamp + "_" + uniqueId + ".jpg";
//        Path destinationFile = this.storageDirectory.resolve(filename);
//
//        try {
//            // 保存图片
//            Files.createDirectories(this.storageDirectory);
//            Files.write(destinationFile, imageData);
//            logger.info("Successfully saved image to {}", destinationFile.toAbsolutePath());
//
//            // 添加 URL 到缓冲区
//            String imageUrl = imageUrlPrefix + filename;
//            synchronized (urlBuffer) {
//                urlBuffer.add(imageUrl);
//
//                // 检查是否需要推送（时间间隔或数量达到阈值）
//                long currentTime = System.currentTimeMillis();
//                if (urlBuffer.size() >= MAX_BATCH_SIZE || currentTime - lastBroadcastTime.get() >= MIN_BROADCAST_INTERVAL) {
//
//                    // 创建 JSON 对象，包含 URL 列表
//                    JSONObject jsonObject = new JSONObject().set("urls", new ArrayList<>(urlBuffer));
//                    SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event().name("surface_img").data(jsonObject);
//
//                    // 推送
//                    sseManager.broadcast(SurfaceSseController.SURFACE_IMAGE_TOPIC, eventBuilder);
//                    logger.info("Broadcasted {} image URLs", urlBuffer.size());
//
//                    // 清空缓冲区并更新时间
//                    urlBuffer.clear();
//                    lastBroadcastTime.set(currentTime);
//                }
//            }
//        } catch (IOException e) {
//            logger.error("Failed to save image file or broadcast URL.", e);
//        }
//    }


    /**
     * 异步处理图像数据、保存并根据策略广播URL。
     *
     * @param imageData 图像的字节数组
     */
    @Async
    public void processAndBroadcast(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            logger.warn("Received empty image data. Skipping.");
            return;
        }

        // 生成唯一文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String filename = timestamp + "_" + uniqueId + ".jpg";
        Path destinationFile = this.storageDirectory.resolve(filename);

        try {
            // 1. 保存图片到文件系统
            Files.createDirectories(this.storageDirectory);
            Files.write(destinationFile, imageData);
            logger.info("Successfully saved image to {}", destinationFile.toAbsolutePath());

            // 2. 将 URL 添加到缓冲区
            String imageUrl = imageUrlPrefix + filename;
            urlBuffer.add(imageUrl);

            long currentTime = System.currentTimeMillis();

            // 3. 检查是否满足推送条件（核心改动）
            boolean shouldBroadcast = urlBuffer.size() >= MAX_BATCH_SIZE ||
                    currentTime - lastBroadcastTime.get() >= MIN_BROADCAST_INTERVAL;

            if (shouldBroadcast && !urlBuffer.isEmpty()) {
                // 使用 synchronized 确保广播和清空操作的原子性
                synchronized (urlBuffer) {
                    // 再次检查，防止多线程下的重复广播
                    if (!urlBuffer.isEmpty()) {
                        // 创建 JSON 对象，包含 URL 列表
                        JSONObject jsonObject = new JSONObject().set("urls", new ArrayList<>(urlBuffer));
                        SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event()
                                .name("surface_img") // 与前端 addEventListener 名称一致
                                .data(jsonObject); // 推送 JSON 字符串

                        // 推送
                        // 假设 sseManager 和 topic 的定义与您项目中一致
                        sseManager.broadcast(SurfaceSseController.SURFACE_IMAGE_TOPIC, eventBuilder);
                        logger.info("Broadcasted {} image URLs.", urlBuffer.size());

                        // 清空缓冲区并更新时间
                        urlBuffer.clear();
                        lastBroadcastTime.set(currentTime);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to save image file or broadcast URL.", e);
        }
    }

}
