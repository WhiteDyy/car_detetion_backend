package cn.dhbin.isme.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * 文件下载配置类
 * 用于优化大文件传输
 */
@Configuration
public class FileDownloadConfig implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        if (factory instanceof TomcatServletWebServerFactory) {
            TomcatServletWebServerFactory tomcatFactory = (TomcatServletWebServerFactory) factory;
            // 禁用传输限制
            tomcatFactory.addConnectorCustomizers(connector -> {
                connector.setProperty("maxSwallowSize", "-1");
                connector.setProperty("maxHttpFormPostSize", "-1");
            });
        }
    }
}