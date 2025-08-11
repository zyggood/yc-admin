package com.yc.admin.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA 审计配置
 * 启用 JPA 审计功能，自动填充创建人、更新人、创建时间、更新时间
 * 
 * @author YC
 * @since 1.0.0
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditConfig {

    /**
     * 审计人员提供者
     * 从 Spring Security 上下文中获取当前用户信息
     * 
     * @return AuditorAware<String>
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }

    /**
     * Spring Security 审计人员感知实现
     */
    public static class SpringSecurityAuditorAware implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                // 系统操作使用特殊前缀，避免与真实用户名 "system" 冲突
                return Optional.of("[SYSTEM]");
            }
            
            // 匿名用户使用特殊前缀
            if ("anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("[ANONYMOUS]");
            }
            
            // 返回当前认证用户的用户名
            return Optional.of(authentication.getName());
        }
    }
}