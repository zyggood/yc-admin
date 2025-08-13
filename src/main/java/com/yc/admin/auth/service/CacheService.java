package com.yc.admin.auth.service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 提供统一的缓存操作接口，支持 Redis 和内存缓存两种实现
 */
public interface CacheService {
    
    /**
     * 设置缓存
     * @param key 键
     * @param value 值
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    void set(String key, Object value, long timeout, TimeUnit unit);
    
    /**
     * 获取缓存
     * @param key 键
     * @return 值
     */
    Object get(String key);
    
    /**
     * 删除缓存
     * @param key 键
     */
    void delete(String key);
    
    /**
     * 检查缓存是否存在
     * @param key 键
     * @return 是否存在
     */
    boolean hasKey(String key);
}