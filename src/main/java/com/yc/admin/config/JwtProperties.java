package com.yc.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性
 * @author yc
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT密钥
     */
    private String secret = "mySecretKey123456789012345678901234567890";

    /**
     * JWT过期时间（毫秒）
     */
    private Long expiration = 86400000L; // 24小时

    /**
     * JWT刷新令牌过期时间（毫秒）
     */
    private Long refreshExpiration = 604800000L; // 7天

    /**
     * JWT令牌前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * JWT请求头名称
     */
    private String headerName = "Authorization";
}