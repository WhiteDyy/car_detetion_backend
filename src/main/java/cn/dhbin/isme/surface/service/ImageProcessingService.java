package cn.dhbin.isme.surface.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ImageProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingService.class);

    @Autowired
    private SseBroadcastService sseBroadcastService;

    // 从 application.properties 读取配置
    @Value("${image.storage.path}")
    private String storageDirectoryPath;

    @Value("${image.url.prefix}")
    private String imageUrlPrefix;

    private Path storageDirectory;

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

    /**
     * 处理接收到的图片数据并广播其URL
     * 使用 @Async 注解，使其在独立的线程中异步执行
     *
     * @param imageData 从ZeroMQ接收到的原始图片字节
     */
    @Async // 关键！让ZMQ监听线程可以快速返回
    public void processAndBroadcast(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            logger.warn("Received empty image data. Skipping.");
            return;
        }

        // 1. 生成唯一的文件名 (例如： a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6.jpg)
//        String filename = UUID.randomUUID().toString() + ".jpg";
//        Path destinationFile = this.storageDirectory.resolve(filename);
//         1. 获取带毫秒的时间戳字符串
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));

// 2. 获取UUID的一部分作为唯一后缀 (取前8位即可有效避免冲突)
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

// 3. 组合成最终文件名
        String filename = timestamp + "_" + uniqueId + ".jpg";
        Path destinationFile = this.storageDirectory.resolve(filename);


        try {
            // 2. 将字节写入文件
            Files.write(destinationFile, imageData);
            logger.info("Successfully saved image to {}", destinationFile.toAbsolutePath());

            // 3. 构建可公开访问的URL
            String imageUrl = imageUrlPrefix + filename;

            // 4. 通过SSE服务广播这个URL
            sseBroadcastService.broadcast(imageUrl);

        } catch (IOException e) {
            logger.error("Failed to save image file or broadcast URL.", e);
        }
    }
}
