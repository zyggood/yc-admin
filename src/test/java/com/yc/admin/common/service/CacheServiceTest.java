package com.yc.admin.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheService 单元测试")
class CacheServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private Cache cache;

    @InjectMocks
    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("缓存清除测试")
    class EvictCacheTests {

        @Test
        @DisplayName("清除指定缓存 - 成功")
        void evictCache_Success() {
            // Given
            String cacheName = "testCache";
            when(cacheManager.getCache(cacheName)).thenReturn(cache);

            // When
            assertDoesNotThrow(() -> cacheService.evictCache(cacheName));

            // Then
            verify(cacheManager).getCache(cacheName);
            verify(cache).clear();
        }

        @Test
        @DisplayName("清除指定缓存 - 缓存不存在")
        void evictCache_CacheNotFound() {
            // Given
            String cacheName = "nonExistentCache";
            when(cacheManager.getCache(cacheName)).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> cacheService.evictCache(cacheName));
            verify(cacheManager).getCache(cacheName);
            verify(cache, never()).clear();
        }

        @Test
        @DisplayName("清除指定缓存 - 异常处理")
        void evictCache_Exception() {
            // Given
            String cacheName = "testCache";
            when(cacheManager.getCache(cacheName)).thenThrow(new RuntimeException("Cache error"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> cacheService.evictCache(cacheName));
            assertTrue(exception.getMessage().contains("清除缓存失败"));
        }

        @Test
        @DisplayName("清除指定缓存键 - 成功")
        void evictCacheKey_Success() {
            // Given
            String cacheName = "testCache";
            String key = "testKey";
            when(cacheManager.getCache(cacheName)).thenReturn(cache);

            // When
            assertDoesNotThrow(() -> cacheService.evictCacheKey(cacheName, key));

            // Then
            verify(cacheManager).getCache(cacheName);
            verify(cache).evict(key);
        }

        @Test
        @DisplayName("清除指定缓存键 - 缓存不存在")
        void evictCacheKey_CacheNotFound() {
            // Given
            String cacheName = "nonExistentCache";
            String key = "testKey";
            when(cacheManager.getCache(cacheName)).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> cacheService.evictCacheKey(cacheName, key));
            verify(cacheManager).getCache(cacheName);
            verify(cache, never()).evict(any());
        }

        @Test
        @DisplayName("批量清除缓存 - 成功")
        void evictCaches_Success() {
            // Given
            Collection<String> cacheNames = Arrays.asList("cache1", "cache2");
            when(cacheManager.getCache("cache1")).thenReturn(cache);
            when(cacheManager.getCache("cache2")).thenReturn(cache);

            // When
            assertDoesNotThrow(() -> cacheService.evictCaches(cacheNames));

            // Then
            verify(cacheManager).getCache("cache1");
            verify(cacheManager).getCache("cache2");
            verify(cache, times(2)).clear();
        }

        @Test
        @DisplayName("清除所有缓存 - 成功")
        void evictAllCaches_Success() {
            // Given
            Collection<String> cacheNames = Arrays.asList("cache1", "cache2");
            when(cacheManager.getCacheNames()).thenReturn(cacheNames);
            when(cacheManager.getCache("cache1")).thenReturn(cache);
            when(cacheManager.getCache("cache2")).thenReturn(cache);

            // When
            assertDoesNotThrow(() -> cacheService.evictAllCaches());

            // Then
            verify(cacheManager).getCacheNames();
            verify(cache, times(2)).clear();
        }
    }

    @Nested
    @DisplayName("Redis操作测试")
    class RedisOperationTests {

        @Test
        @DisplayName("根据模式删除缓存 - 成功")
        void deleteByPattern_Success() {
            // Given
            String pattern = "test:*";
            Set<String> keys = Set.of("test:key1", "test:key2");
            when(redisTemplate.keys(pattern)).thenReturn(keys);

            // When
            assertDoesNotThrow(() -> cacheService.deleteByPattern(pattern));

            // Then
            verify(redisTemplate).keys(pattern);
            verify(redisTemplate).delete(keys);
        }

        @Test
        @DisplayName("根据模式删除缓存 - 无匹配键")
        void deleteByPattern_NoMatchingKeys() {
            // Given
            String pattern = "test:*";
            when(redisTemplate.keys(pattern)).thenReturn(Collections.emptySet());

            // When
            assertDoesNotThrow(() -> cacheService.deleteByPattern(pattern));

            // Then
            verify(redisTemplate).keys(pattern);
            verify(redisTemplate, never()).delete(any(Collection.class));
        }

        @Test
        @DisplayName("设置缓存值 - 成功")
        void set_Success() {
            // Given
            String key = "testKey";
            Object value = "testValue";
            long timeout = 60;
            TimeUnit timeUnit = TimeUnit.SECONDS;

            // When
            assertDoesNotThrow(() -> cacheService.set(key, value, timeout, timeUnit));

            // Then
            verify(valueOperations).set(key, value, timeout, timeUnit);
        }

        @Test
        @DisplayName("获取缓存值 - 成功")
        void get_Success() {
            // Given
            String key = "testKey";
            Object expectedValue = "testValue";
            when(valueOperations.get(key)).thenReturn(expectedValue);

            // When
            Object result = cacheService.get(key);

            // Then
            assertEquals(expectedValue, result);
            verify(valueOperations).get(key);
        }

        @Test
        @DisplayName("获取缓存值 - 异常处理")
        void get_Exception() {
            // Given
            String key = "testKey";
            when(valueOperations.get(key)).thenThrow(new RuntimeException("Redis error"));

            // When
            Object result = cacheService.get(key);

            // Then
            assertNull(result);
            verify(valueOperations).get(key);
        }

        @Test
        @DisplayName("检查键是否存在 - 存在")
        void exists_KeyExists() {
            // Given
            String key = "testKey";
            when(redisTemplate.hasKey(key)).thenReturn(true);

            // When
            boolean result = cacheService.exists(key);

            // Then
            assertTrue(result);
            verify(redisTemplate).hasKey(key);
        }

        @Test
        @DisplayName("检查键是否存在 - 不存在")
        void exists_KeyNotExists() {
            // Given
            String key = "testKey";
            when(redisTemplate.hasKey(key)).thenReturn(false);

            // When
            boolean result = cacheService.exists(key);

            // Then
            assertFalse(result);
            verify(redisTemplate).hasKey(key);
        }

        @Test
        @DisplayName("检查键是否存在 - 异常处理")
        void exists_Exception() {
            // Given
            String key = "testKey";
            when(redisTemplate.hasKey(key)).thenThrow(new RuntimeException("Redis error"));

            // When
            boolean result = cacheService.exists(key);

            // Then
            assertFalse(result);
            verify(redisTemplate).hasKey(key);
        }
    }

    @Nested
    @DisplayName("缓存统计测试")
    class CacheStatsTests {

        @Test
        @DisplayName("获取缓存名称 - 成功")
        void getCacheNames_Success() {
            // Given
            Collection<String> expectedNames = Arrays.asList("cache1", "cache2");
            when(cacheManager.getCacheNames()).thenReturn(expectedNames);

            // When
            Collection<String> result = cacheService.getCacheNames();

            // Then
            assertEquals(expectedNames, result);
            verify(cacheManager).getCacheNames();
        }

        @Test
        @DisplayName("获取缓存统计信息 - 成功")
        void getCacheStats_Success() {
            // Given
            Collection<String> cacheNames = Arrays.asList("cache1", "cache2");
            Set<String> allKeys = Set.of("key1", "key2", "key3");
            when(cacheManager.getCacheNames()).thenReturn(cacheNames);
            when(redisTemplate.keys("*")).thenReturn(allKeys);

            // When
            CacheService.CacheStats result = cacheService.getCacheStats();

            // Then
            assertNotNull(result);
            assertEquals(2, result.cacheCount());
            assertEquals(3, result.keyCount());
            assertEquals(cacheNames, result.cacheNames());
        }

        @Test
        @DisplayName("获取缓存统计信息 - 异常处理")
        void getCacheStats_Exception() {
            // Given
            when(cacheManager.getCacheNames()).thenThrow(new RuntimeException("Cache error"));

            // When
            CacheService.CacheStats result = cacheService.getCacheStats();

            // Then
            assertNotNull(result);
            assertEquals(0, result.cacheCount());
            assertEquals(0, result.keyCount());
            assertTrue(result.cacheNames().isEmpty());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("空缓存名称")
        void evictCache_EmptyName() {
            // Given
            String cacheName = "";
            when(cacheManager.getCache(cacheName)).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> cacheService.evictCache(cacheName));
        }

        @Test
        @DisplayName("null缓存名称")
        void evictCache_NullName() {
            // Given
            String cacheName = null;
            when(cacheManager.getCache(cacheName)).thenReturn(null);

            // When & Then
            assertDoesNotThrow(() -> cacheService.evictCache(cacheName));
        }

        @Test
        @DisplayName("空集合批量清除")
        void evictCaches_EmptyCollection() {
            // Given
            Collection<String> cacheNames = Collections.emptyList();

            // When & Then
            assertDoesNotThrow(() -> cacheService.evictCaches(cacheNames));
            verify(cacheManager, never()).getCache(any());
        }

        @Test
        @DisplayName("null集合批量清除")
        void evictCaches_NullCollection() {
            // Given
            Collection<String> cacheNames = null;

            // When & Then
            assertDoesNotThrow(() -> cacheService.evictCaches(cacheNames));
            verify(cacheManager, never()).getCache(any());
        }

        @Test
        @DisplayName("Redis返回null键集合")
        void deleteByPattern_NullKeys() {
            // Given
            String pattern = "test:*";
            when(redisTemplate.keys(pattern)).thenReturn(null);

            // When
            assertDoesNotThrow(() -> cacheService.deleteByPattern(pattern));

            // Then
            verify(redisTemplate).keys(pattern);
            verify(redisTemplate, never()).delete(any(Collection.class));
        }
    }
}