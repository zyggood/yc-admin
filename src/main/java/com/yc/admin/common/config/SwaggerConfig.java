package com.yc.admin.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger配置类
 *
 * @author yc
 */
@Configuration
public class SwaggerConfig {

    /**
     * 创建OpenAPI配置
     *
     * @return OpenAPI配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("YC Admin API")
                        .description("YC Admin 管理系统 API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("YC")
                                .email("admin@yc.com")
                                .url("https://github.com/zyggood/yc-admin"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("开发环境"),
                        new Server().url("http://localhost:8081").description("测试环境")
                ));
    }
}