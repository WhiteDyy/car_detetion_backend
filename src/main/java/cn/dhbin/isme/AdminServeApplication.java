package cn.dhbin.isme;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application
 *
 * @author dhb
 */
@SpringBootApplication
//@EnableScheduling
public class AdminServeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServeApplication.class, args);
    }

}