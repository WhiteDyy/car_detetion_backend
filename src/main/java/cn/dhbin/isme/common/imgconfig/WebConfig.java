package cn.dhbin.isme.common.imgconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 再次注入配置，确保与ImageProcessingService一致
    @Value("${image.storage.path}")
    private String storageDirectoryPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 当URL路径匹配 /images/** 时
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:src/main/resources/static/images/")
                // 将其映射到本地文件系统的指定目录下
                // "file:" 前缀是必须的，表示这是一个文件系统路径
                .addResourceLocations("file:" + storageDirectoryPath + "/")
                .setCachePeriod(0);
    }
}