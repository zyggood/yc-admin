package com.yc.admin.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Redis 条件化配置
 * 只有在 redis.enabled=true 时才启用 Redis 配置
 */
@Configuration
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = false)
@Import(RedisConfig.class)
public class RedisConditionalConfig {
    // Redis 配置只有在 redis.enabled=true 时才会生效
}