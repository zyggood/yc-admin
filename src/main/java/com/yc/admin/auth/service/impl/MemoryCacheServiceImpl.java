package com.yc.admin.auth.service.impl;

import com.yc.admin.auth.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 内存缓存服务实现
 * 当 Redis 不可用时使用内存缓存作为备选方案
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "false", matchIfMissing = true)
public class MemoryCacheServiceImpl implements CacheService {
    
    private final ConcurrentHashMap<String, CacheItem> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public MemoryCacheServiceImpl() {
        // 每分钟清理一次过期的缓存项
        scheduler.scheduleAtFixedRate(this::cleanExpiredItems, 1, 1, TimeUnit.MINUTES);
        log.warn("使用内存缓存替代 Redis，重启应用后缓存数据将丢失");
    }
    
    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        long expireTime = System.currentTimeMillis() + unit.toMillis(timeout);
        cache.put(key, new CacheItem(value, expireTime));
    }
    
    @Override
    public Object get(String key) {
        CacheItem item = cache.get(key);
        if (item == null) {
            return null;
        }
        
        if (item.isExpired()) {
            cache.remove(key);
            return null;
        }
        
        return item.getValue();
    }
    
    @Override
    public void delete(String key) {
        cache.remove(key);
    }
    
    @Override
    public boolean hasKey(String key) {
        CacheItem item = cache.get(key);
        if (item == null) {
            return false;
        }
        
        if (item.isExpired()) {
            cache.remove(key);
            return false;
        }
        
        return true;
    }
    
    /**
     * 清理过期的缓存项
     */
    private void cleanExpiredItems() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * 缓存项
     */
    private static class CacheItem {
        private final Object value;
        private final long expireTime;
        
        public CacheItem(Object value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }
        
        public Object getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
}