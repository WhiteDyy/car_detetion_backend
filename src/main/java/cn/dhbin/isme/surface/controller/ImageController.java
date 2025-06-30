package cn.dhbin.isme.surface.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/surface_disease")
public class ImageController {


    // 定义图片存储目录（可配置为 application.properties 或外部目录）
    private final Path storageDirectory = Paths.get("src/main/resources/static/images");

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        // 构建文件路径
        Path filePath = storageDirectory.resolve(filename).normalize();

        // 确保文件存在
        if (!filePath.toFile().exists()) {
            return ResponseEntity.notFound().build();
        }

        // 创建文件资源
        Resource resource = new FileSystemResource(filePath);

        // 设置响应头，指定图片的 Content-Type
        String contentType = determineContentType(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);

    }

    // 根据文件名后缀判断 Content-Type
    private String determineContentType(String filename) {
        String lowercaseFilename = filename.toLowerCase();
        if (lowercaseFilename.endsWith(".jpg") || lowercaseFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowercaseFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowercaseFilename.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "application/octet-stream"; // 默认类型
        }
    }
}
