package com.yc.admin.system.cache.controller;

import com.yc.admin.common.core.Result;
import com.yc.admin.common.service.CacheService;
import com.yc.admin.system.cache.service.CacheWarmupService;
import com.yc.admin.system.config.dto.ConfigDto;
import com.yc.admin.system.config.service.ConfigService;
import com.yc.admin.system.dict.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

/**
 * 缓存管理控制器
 * 提供缓存刷新和清除的API接口
 * @author yc
 */
@Slf4j
@Tag(name = "缓存管理", description = "缓存刷新和清除操作")
@RestController
@RequestMapping("/system/cache")
@RequiredArgsConstructor
@Validated
public class CacheController {

    private final CacheService cacheService;
    private final DictService dictService;
    private final ConfigService configService;
    private final CacheWarmupService cacheWarmupService;

    /**
     * 获取缓存统计信息
     */
    @Operation(summary = "获取缓存统计信息", description = "获取当前系统缓存的统计信息")
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('system:cache:list')")
    public Result<CacheService.CacheStats> getCacheStats() {
        return Result.success(cacheService.getCacheStats());
    }

    /**
     * 获取所有缓存名称
     */
    @Operation(summary = "获取缓存名称列表", description = "获取系统中所有的缓存名称")
    @GetMapping("/names")
    @PreAuthorize("hasAuthority('system:cache:list')")
    public Result<Collection<String>> getCacheNames() {
        return Result.success(cacheService.getCacheNames());
    }

    /**
     * 清除指定缓存
     */
    @Operation(summary = "清除指定缓存", description = "清除指定名称的缓存")
    @DeleteMapping("/{cacheName}")
    @PreAuthorize("hasAuthority('system:cache:remove')")
    public Result<String> evictCache(
            @Parameter(description = "缓存名称")
            @PathVariable String cacheName) {
        try {
            cacheService.evictCache(cacheName);
            log.info("清除缓存成功: {}", cacheName);
            return Result.success("清除缓存成功");
        } catch (Exception e) {
            log.error("清除缓存失败: {}", cacheName, e);
            return Result.error("清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除指定缓存的特定key
     */
    @Operation(summary = "清除缓存key", description = "清除指定缓存名称下的特定key")
    @DeleteMapping("/{cacheName}/key/{key}")
    @PreAuthorize("hasAuthority('system:cache:remove')")
    public Result<String> evictCacheKey(
            @Parameter(description = "缓存名称")
            @PathVariable String cacheName,
            @Parameter(description = "缓存key")
            @PathVariable String key) {
        try {
            cacheService.evictCacheKey(cacheName, key);
            log.info("清除缓存key成功: {}:{}", cacheName, key);
            return Result.success("清除缓存key成功");
        } catch (Exception e) {
            log.error("清除缓存key失败: {}:{}", cacheName, key, e);
            return Result.error("清除缓存key失败: " + e.getMessage());
        }
    }

    /**
     * 批量清除缓存
     */
    @Operation(summary = "批量清除缓存", description = "批量清除多个缓存")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:cache:remove')")
    public Result<String> evictCaches(
            @Parameter(description = "缓存名称列表")
            @RequestBody List<String> cacheNames) {
        try {
            cacheService.evictCaches(cacheNames);
            log.info("批量清除缓存成功: {}", cacheNames);
            return Result.success("批量清除缓存成功");
        } catch (Exception e) {
            log.error("批量清除缓存失败: {}", cacheNames, e);
            return Result.error("批量清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除所有缓存
     */
    @Operation(summary = "清除所有缓存", description = "清除系统中的所有缓存")
    @DeleteMapping("/all")
    @PreAuthorize("hasAuthority('system:cache:remove')")
    public Result<String> evictAllCaches() {
        try {
            cacheService.evictAllCaches();
            log.info("清除所有缓存成功");
            return Result.success("清除所有缓存成功");
        } catch (Exception e) {
            log.error("清除所有缓存失败", e);
            return Result.error("清除所有缓存失败: " + e.getMessage());
        }
    }

    /**
     * 根据模式删除Redis缓存
     */
    @Operation(summary = "模式删除缓存", description = "根据key模式删除Redis中的缓存")
    @DeleteMapping("/pattern")
    @PreAuthorize("hasAuthority('system:cache:remove')")
    public Result<String> deleteByPattern(
            @Parameter(description = "key模式，支持通配符")
            @RequestParam String pattern) {
        try {
            cacheService.deleteByPattern(pattern);
            log.info("根据模式删除缓存成功: {}", pattern);
            return Result.success("根据模式删除缓存成功");
        } catch (Exception e) {
            log.error("根据模式删除缓存失败: {}", pattern, e);
            return Result.error("根据模式删除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 刷新字典缓存
     */
    @Operation(summary = "刷新字典缓存", description = "清除并重新加载字典缓存")
    @PostMapping("/refresh/dict")
    @PreAuthorize("hasAuthority('system:cache:refresh')")
    public Result<String> refreshDictCache() {
        try {
            dictService.clearCache();
            // 预热常用字典数据
            dictService.findAll();
            log.info("刷新字典缓存成功");
            return Result.success("刷新字典缓存成功");
        } catch (Exception e) {
            log.error("刷新字典缓存失败", e);
            return Result.error("刷新字典缓存失败: " + e.getMessage());
        }
    }

    /**
     * 刷新参数缓存
     */
    @Operation(summary = "刷新参数缓存", description = "清除并重新加载参数缓存")
    @PostMapping("/refresh/config")
    @PreAuthorize("hasAuthority('system:cache:refresh')")
    public Result<String> refreshConfigCache(
            @Parameter(description = "参数键，为空则刷新全部")
            @RequestParam(required = false) String configKey) {
        try {
            if (configKey != null && !configKey.trim().isEmpty()) {
                configService.clearCache(configKey);
                // 重新加载指定参数
                configService.getConfigValue(configKey);
                log.info("刷新参数缓存成功: {}", configKey);
                return Result.success("刷新参数缓存成功");
            } else {
                // 刷新所有参数缓存
                ConfigDto.CacheRefreshDto refreshDto = new ConfigDto.CacheRefreshDto();
                configService.refreshCache(refreshDto);
                // 预热常用参数数据
                configService.findAllEnabled();
                log.info("刷新所有参数缓存成功");
                return Result.success("刷新所有参数缓存成功");
            }
        } catch (Exception e) {
            log.error("刷新参数缓存失败: {}", configKey, e);
            return Result.error("刷新参数缓存失败: " + e.getMessage());
        }
    }

    /**
     * 刷新所有业务缓存
     */
    @Operation(summary = "刷新所有业务缓存", description = "刷新字典、参数等所有业务缓存")
    @PostMapping("/refresh/all")
    @PreAuthorize("hasAuthority('system:cache:refresh')")
    public Result<String> refreshAllCache() {
        try {
            // 刷新字典缓存
            dictService.clearCache();
            dictService.findAll();
            
            // 刷新参数缓存
            ConfigDto.CacheRefreshDto refreshDto = new ConfigDto.CacheRefreshDto();
            configService.refreshCache(refreshDto);
            configService.findAllEnabled();
            
            log.info("刷新所有业务缓存成功");
            return Result.success("刷新所有业务缓存成功");
        } catch (Exception e) {
            log.error("刷新所有业务缓存失败", e);
            return Result.error("刷新所有业务缓存失败: " + e.getMessage());
        }
    }

    /**
     * 缓存预热
     */
    @Operation(summary = "缓存预热", description = "手动触发系统缓存预热")
    @PostMapping("/warmup")
    @PreAuthorize("hasAuthority('system:cache:refresh')")
    public Result<String> warmupCache() {
        try {
            cacheWarmupService.manualWarmup();
            log.info("手动缓存预热完成");
            return Result.success("缓存预热完成");
        } catch (Exception e) {
            log.error("缓存预热失败", e);
            return Result.error("缓存预热失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存预热统计信息
     */
    @Operation(summary = "获取预热统计", description = "获取缓存预热的统计信息")
    @GetMapping("/warmup/stats")
    @PreAuthorize("hasAuthority('system:cache:list')")
    public Result<CacheWarmupService.WarmupStats> getWarmupStats() {
        try {
            CacheWarmupService.WarmupStats stats = cacheWarmupService.getWarmupStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取预热统计信息失败", e);
            return Result.error("获取预热统计信息失败: " + e.getMessage());
        }
    }
}