package com.yc.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Modulith 后台管理系统主启动类
 * 
 * @author YC
 * @since 2024
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.yc.admin.**.repository")
@MapperScan(basePackages = "com.yc.admin.**.mapper")
@EnableScheduling
@EnableAsync
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

}
