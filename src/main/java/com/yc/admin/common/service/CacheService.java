package com.yc.admin.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务类
 * 提供通用的缓存操作方法
 * @author yc
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 清除指定缓存名称的所有缓存
     * @param cacheName 缓存名称
     */
    public void evictCache(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("清除缓存成功: {}", cacheName);
            } else {
                log.warn("缓存不存在: {}", cacheName);
            }
        } catch (Exception e) {
            log.error("清除缓存失败: {}", cacheName, e);
            throw new RuntimeException("清除缓存失败: " + cacheName, e);
        }
    }

    /**
     * 清除指定缓存名称下的特定key
     * @param cacheName 缓存名称
     * @param key 缓存key
     */
    public void evictCacheKey(String cacheName, Object key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                log.info("清除缓存key成功: {}:{}", cacheName, key);
            } else {
                log.warn("缓存不存在: {}", cacheName);
            }
        } catch (Exception e) {
            log.error("清除缓存key失败: {}:{}", cacheName, key, e);
            throw new RuntimeException("清除缓存key失败: " + cacheName + ":" + key, e);
        }
    }

    /**
     * 批量清除多个缓存
     * @param cacheNames 缓存名称集合
     */
    public void evictCaches(Collection<String> cacheNames) {
        if (cacheNames == null || cacheNames.isEmpty()) {
            log.warn("缓存名称集合为空，无需清除");
            return;
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (String cacheName : cacheNames) {
            try {
                evictCache(cacheName);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("批量清除缓存失败: {}", cacheName, e);
            }
        }
        
        log.info("批量清除缓存完成，成功: {}, 失败: {}", successCount, failCount);
    }

    /**
     * 清除所有缓存
     */
    public void evictAllCaches() {
        try {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            evictCaches(cacheNames);
            log.info("清除所有缓存完成");
        } catch (Exception e) {
            log.error("清除所有缓存失败", e);
            throw new RuntimeException("清除所有缓存失败", e);
        }
    }

    /**
     * 根据模式删除Redis中的key
     * @param pattern key模式，支持通配符
     */
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("根据模式删除缓存成功: {}, 删除数量: {}", pattern, keys.size());
            } else {
                log.info("根据模式未找到匹配的缓存: {}", pattern);
            }
        } catch (Exception e) {
            log.error("根据模式删除缓存失败: {}", pattern, e);
            throw new RuntimeException("根据模式删除缓存失败: " + pattern, e);
        }
    }

    /**
     * 设置缓存值
     * @param key 缓存key
     * @param value 缓存值
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
            log.debug("设置缓存成功: {}", key);
        } catch (Exception e) {
            log.error("设置缓存失败: {}", key, e);
            throw new RuntimeException("设置缓存失败: " + key, e);
        }
    }

    /**
     * 获取缓存值
     * @param key 缓存key
     * @return 缓存值
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取缓存失败: {}", key, e);
            return null;
        }
    }

    /**
     * 检查key是否存在
     * @param key 缓存key
     * @return 是否存在
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查缓存key存在性失败: {}", key, e);
            return false;
        }
    }

    /**
     * 获取所有缓存名称
     * @return 缓存名称集合
     */
    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }

    /**
     * 获取缓存统计信息
     * @return 缓存统计信息
     */
    public CacheStats getCacheStats() {
        try {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            int cacheCount = cacheNames.size();
            
            // 获取Redis中的key数量（近似值）
            Set<String> allKeys = redisTemplate.keys("*");
            int keyCount = allKeys != null ? allKeys.size() : 0;
            
            return new CacheStats(cacheCount, keyCount, cacheNames);
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return new CacheStats(0, 0, Set.of());
        }
    }

    /**
     * 缓存统计信息
     */
    public record CacheStats(
            int cacheCount,
            int keyCount,
            Collection<String> cacheNames
    ) {}
}