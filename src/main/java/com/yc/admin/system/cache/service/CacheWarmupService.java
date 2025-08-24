package com.yc.admin.system.cache.service;

import com.yc.admin.system.config.service.ConfigService;
import com.yc.admin.system.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 缓存预热服务
 * 系统启动时自动加载常用数据到缓存
 * @author yc
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Order(100) // 确保在其他组件初始化后执行
public class CacheWarmupService implements ApplicationRunner {

    private final DictService dictService;
    private final ConfigService configService;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始缓存预热...");
        long startTime = System.currentTimeMillis();
        
        try {
            // 并行预热各种缓存
            CompletableFuture<Void> dictFuture = warmupDictCache();
            CompletableFuture<Void> configFuture = warmupConfigCache();
            
            // 等待所有预热任务完成
            CompletableFuture.allOf(dictFuture, configFuture).join();
            
            long endTime = System.currentTimeMillis();
            log.info("缓存预热完成，耗时: {}ms", endTime - startTime);
            
        } catch (Exception e) {
            log.error("缓存预热失败", e);
        } finally {
            // 关闭线程池
            executorService.shutdown();
        }
    }

    /**
     * 预热字典缓存
     */
    private CompletableFuture<Void> warmupDictCache() {
        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("开始预热字典缓存...");
                
                // 加载所有字典数据
                List<?> dictData = dictService.findAll();
                
                // 预热常用字典类型
                String[] commonDictTypes = {
                    "sys_user_sex",     // 用户性别
                    "sys_show_hide",    // 显示状态
                    "sys_normal_disable", // 系统状态
                    "sys_yes_no",       // 是否
                    "sys_notice_type",  // 通知类型
                    "sys_notice_status", // 通知状态
                    "sys_oper_type",    // 操作类型
                    "sys_common_status"  // 通用状态
                };
                
                for (String dictType : commonDictTypes) {
                    try {
                        dictService.type(dictType);
                        log.debug("预热字典类型: {}", dictType);
                    } catch (Exception e) {
                        log.warn("预热字典类型失败: {}", dictType, e);
                    }
                }
                
                log.info("字典缓存预热完成，加载数据量: {}", dictData.size());
                
            } catch (Exception e) {
                log.error("字典缓存预热失败", e);
            }
        }, executorService);
    }

    /**
     * 预热参数配置缓存
     */
    private CompletableFuture<Void> warmupConfigCache() {
        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("开始预热参数配置缓存...");
                
                // 加载所有启用的参数配置
                List<?> configData = configService.findAllEnabled();
                
                // 预热常用系统参数
                String[] commonConfigKeys = {
                    "sys.user.initPassword",    // 用户初始密码
                    "sys.user.passwordPolicy",  // 密码策略
                    "sys.account.captchaEnabled", // 验证码开关
                    "sys.account.registerUser",   // 账户自助注册
                    "sys.upload.maxSize",         // 上传文件大小限制
                    "sys.upload.allowedTypes",    // 允许上传的文件类型
                    "sys.session.timeout",       // 会话超时时间
                    "sys.log.retention.days",    // 日志保留天数
                    "sys.backup.enabled",        // 备份开关
                    "sys.maintenance.mode"       // 维护模式
                };
                
                for (String configKey : commonConfigKeys) {
                    try {
                        configService.getConfigValue(configKey);
                        log.debug("预热参数配置: {}", configKey);
                    } catch (Exception e) {
                        log.warn("预热参数配置失败: {}", configKey, e);
                    }
                }
                
                log.info("参数配置缓存预热完成，加载数据量: {}", configData.size());
                
            } catch (Exception e) {
                log.error("参数配置缓存预热失败", e);
            }
        }, executorService);
    }

    /**
     * 手动触发缓存预热
     * 可以通过API调用此方法进行缓存预热
     */
    public void manualWarmup() {
        log.info("手动触发缓存预热...");
        long startTime = System.currentTimeMillis();
        
        try {
            // 创建临时线程池用于手动预热
            ExecutorService tempExecutor = Executors.newFixedThreadPool(3);
            
            // 并行预热各种缓存
            CompletableFuture<Void> dictFuture = CompletableFuture.runAsync(() -> {
                try {
                    dictService.findAll();
                    log.info("手动预热字典缓存完成");
                } catch (Exception e) {
                    log.error("手动预热字典缓存失败", e);
                }
            }, tempExecutor);
            
            CompletableFuture<Void> configFuture = CompletableFuture.runAsync(() -> {
                try {
                    configService.findAllEnabled();
                    log.info("手动预热参数配置缓存完成");
                } catch (Exception e) {
                    log.error("手动预热参数配置缓存失败", e);
                }
            }, tempExecutor);
            
            // 等待所有预热任务完成
            CompletableFuture.allOf(dictFuture, configFuture).join();
            
            tempExecutor.shutdown();
            
            long endTime = System.currentTimeMillis();
            log.info("手动缓存预热完成，耗时: {}ms", endTime - startTime);
            
        } catch (Exception e) {
            log.error("手动缓存预热失败", e);
        }
    }

    /**
     * 获取预热统计信息
     */
    public WarmupStats getWarmupStats() {
        try {
            int dictCount = dictService.findAll().size();
            int configCount = configService.findAllEnabled().size();
            
            return new WarmupStats(dictCount, configCount, true, "缓存预热正常");
        } catch (Exception e) {
            log.error("获取预热统计信息失败", e);
            return new WarmupStats(0, 0, false, "获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 预热统计信息
     */
    public record WarmupStats(
        int dictCount,
        int configCount,
        boolean healthy,
        String message
    ) {}
}