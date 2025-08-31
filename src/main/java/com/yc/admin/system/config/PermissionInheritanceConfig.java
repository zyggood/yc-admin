package com.yc.admin.system.config;

import com.yc.admin.system.permission.PermissionInheritanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 权限继承配置类
 * 配置权限继承策略和相关参数
 *
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class PermissionInheritanceConfig {

    /**
     * 权限继承配置属性
     */
    @ConfigurationProperties(prefix = "yc.admin.permission.inheritance")
    public static class PermissionInheritanceProperties {
        
        /**
         * 默认继承策略
         */
        private PermissionInheritanceService.InheritanceStrategy defaultStrategy = 
            PermissionInheritanceService.InheritanceStrategy.ADDITIVE;
        
        /**
         * 默认合并策略
         */
        private PermissionInheritanceService.MergeStrategy defaultMergeStrategy = 
            PermissionInheritanceService.MergeStrategy.UNION;
        
        /**
         * 是否启用权限继承
         */
        private boolean enabled = true;
        
        /**
         * 超级管理员是否自动获得所有权限
         */
        private boolean adminAutoInheritAll = true;
        
        /**
         * 权限缓存时间（秒）
         */
        private long cacheTimeoutSeconds = 300;
        
        // Getters and Setters
        public PermissionInheritanceService.InheritanceStrategy getDefaultStrategy() {
            return defaultStrategy;
        }
        
        public void setDefaultStrategy(PermissionInheritanceService.InheritanceStrategy defaultStrategy) {
            this.defaultStrategy = defaultStrategy;
        }
        
        public PermissionInheritanceService.MergeStrategy getDefaultMergeStrategy() {
            return defaultMergeStrategy;
        }
        
        public void setDefaultMergeStrategy(PermissionInheritanceService.MergeStrategy defaultMergeStrategy) {
            this.defaultMergeStrategy = defaultMergeStrategy;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public boolean isAdminAutoInheritAll() {
            return adminAutoInheritAll;
        }
        
        public void setAdminAutoInheritAll(boolean adminAutoInheritAll) {
            this.adminAutoInheritAll = adminAutoInheritAll;
        }
        
        public long getCacheTimeoutSeconds() {
            return cacheTimeoutSeconds;
        }
        
        public void setCacheTimeoutSeconds(long cacheTimeoutSeconds) {
            this.cacheTimeoutSeconds = cacheTimeoutSeconds;
        }
    }
    
    @Bean
    @ConfigurationProperties(prefix = "yc.admin.permission.inheritance")
    public PermissionInheritanceProperties permissionInheritanceProperties() {
        return new PermissionInheritanceProperties();
    }
}