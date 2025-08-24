package com.yc.admin.system.cache.service;

import com.yc.admin.system.config.service.ConfigService;
import com.yc.admin.system.dict.service.DictService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheWarmupService 单元测试")
class CacheWarmupServiceTest {

    @Mock
    private DictService dictService;

    @Mock
    private ConfigService configService;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private CacheWarmupService cacheWarmupService;

    @BeforeEach
    void setUp() {
        // Mock setup for common operations
        when(dictService.findAll()).thenReturn(Collections.emptyList());
        when(configService.findAllEnabled()).thenReturn(Collections.emptyList());
    }

    @Nested
    @DisplayName("应用启动预热测试")
    class ApplicationStartupWarmupTests {

        @Test
        @DisplayName("应用启动时预热缓存 - 成功")
        void run_Success() throws Exception {
            // Given
            when(dictService.findAll()).thenReturn(Collections.emptyList());
            when(configService.findAllEnabled()).thenReturn(Collections.emptyList());
            when(dictService.type(anyString())).thenReturn(Collections.emptyList());
            when(configService.getConfigValue(anyString())).thenReturn("test-value");

            // When
            assertDoesNotThrow(() -> cacheWarmupService.run(applicationArguments));

            // Then
            verify(dictService, atLeastOnce()).findAll();
            verify(configService, atLeastOnce()).findAllEnabled();
            verify(dictService, atLeast(1)).type(anyString());
            verify(configService, atLeast(1)).getConfigValue(anyString());
        }

        @Test
        @DisplayName("应用启动时预热缓存 - DictService异常")
        void run_DictServiceException() throws Exception {
            // Given
            when(dictService.findAll()).thenThrow(new RuntimeException("Dict cache error"));
            when(configService.findAllEnabled()).thenReturn(Collections.emptyList());
            when(configService.getConfigValue(anyString())).thenReturn("test-value");

            // When
            assertDoesNotThrow(() -> cacheWarmupService.run(applicationArguments));

            // Then
            verify(dictService).findAll();
            verify(configService).findAllEnabled(); // Should still be called
        }

        @Test
        @DisplayName("应用启动时预热缓存 - ConfigService异常")
        void run_ConfigServiceException() throws Exception {
            // Given
            when(dictService.findAll()).thenReturn(Collections.emptyList());
            when(dictService.type(anyString())).thenReturn(Collections.emptyList());
            when(configService.findAllEnabled()).thenThrow(new RuntimeException("Config cache error"));

            // When
            assertDoesNotThrow(() -> cacheWarmupService.run(applicationArguments));

            // Then
            verify(dictService).findAll();
            verify(configService).findAllEnabled();
        }

        @Test
        @DisplayName("应用启动时预热缓存 - 所有服务异常")
        void run_AllServicesException() throws Exception {
            // Given
            when(dictService.findAll()).thenThrow(new RuntimeException("Dict cache error"));
            when(configService.findAllEnabled()).thenThrow(new RuntimeException("Config cache error"));

            // When
            assertDoesNotThrow(() -> cacheWarmupService.run(applicationArguments));

            // Then
            verify(dictService).findAll();
            verify(configService).findAllEnabled();
        }
    }

    @Nested
    @DisplayName("手动预热测试")
    class ManualWarmupTests {

        @Test
        @DisplayName("手动预热缓存 - 成功")
        void manualWarmup_Success() {
            // Given
            when(dictService.findAll()).thenReturn(Collections.emptyList());
            when(configService.findAllEnabled()).thenReturn(Collections.emptyList());

            // When
            assertDoesNotThrow(() -> cacheWarmupService.manualWarmup());

            // Then
            verify(dictService, atLeastOnce()).findAll();
            verify(configService, atLeastOnce()).findAllEnabled();
        }

        @Test
        @DisplayName("手动预热缓存 - DictService异常")
        void manualWarmup_DictServiceException() {
            // Given
            when(dictService.findAll()).thenThrow(new RuntimeException("Dict cache error"));
            when(configService.findAllEnabled()).thenReturn(Collections.emptyList());

            // When
            assertDoesNotThrow(() -> cacheWarmupService.manualWarmup());

            // Then
            verify(dictService).findAll();
            verify(configService).findAllEnabled(); // Should still be called
        }

        @Test
        @DisplayName("手动预热缓存 - ConfigService异常")
        void manualWarmup_ConfigServiceException() {
            // Given
            when(dictService.findAll()).thenReturn(Collections.emptyList());
            when(configService.findAllEnabled()).thenThrow(new RuntimeException("Config cache error"));

            // When
            assertDoesNotThrow(() -> cacheWarmupService.manualWarmup());

            // Then
            verify(dictService).findAll();
            verify(configService).findAllEnabled();
        }

        @Test
        @DisplayName("手动预热缓存 - 所有服务异常")
        void manualWarmup_AllServicesException() {
            // Given
            when(dictService.findAll()).thenThrow(new RuntimeException("Dict cache error"));
            when(configService.findAllEnabled()).thenThrow(new RuntimeException("Config cache error"));

            // When
            assertDoesNotThrow(() -> cacheWarmupService.manualWarmup());

            // Then
            verify(dictService).findAll();
            verify(configService).findAllEnabled();
        }
    }

    @Nested
    @DisplayName("字典类型预热测试")
    class DictTypeWarmupTests {

        @Test
        @DisplayName("预热常用字典类型 - 成功")
        void warmupDictTypes_Success() throws Exception {
            // Given
            when(dictService.findAll()).thenReturn(Collections.emptyList());
            when(dictService.type(anyString())).thenReturn(Collections.emptyList());
            when(configService.findAllEnabled()).thenReturn(Collections.emptyList());
            when(configService.getConfigValue(anyString())).thenReturn("test-value");

            // When
            assertDoesNotThrow(() -> cacheWarmupService.run(applicationArguments));

            // Then
            verify(dictService, atLeast(8)).type(anyString()); // 8 common dict types
            verify(dictService).type("sys_user_sex");
            verify(dictService).type("sys_show_hide");
            verify(dictService).type("sys_normal_disable");
            verify(dictService).type("sys_yes_no");
        }

        @Test
        @DisplayName("预热字典类型 - 部分失败")
        void warmupDictTypes_PartialFailure() throws Exception {
            // Given
            when(dictService.findAll()).thenReturn(Collections.emptyList());
            lenient().when(dictService.type("sys_user_sex")).thenReturn(Collections.emptyList());
            lenient().when(dictService.type("sys_show_hide")).thenThrow(new RuntimeException("Dict type error"));
            when(dictService.type(anyString())).thenReturn(Collections.emptyList());
            when(configService.findAllEnabled()).thenReturn(Collections.emptyList());
            when(configService.getConfigValue(anyString())).thenReturn("test-value");

            // When
            assertDoesNotThrow(() -> cacheWarmupService.run(applicationArguments));

            // Then
            verify(dictService).type("sys_user_sex");
            verify(dictService).type("sys_show_hide");
            // Other types should still be called despite one failure
            verify(dictService, atLeast(8)).type(anyString());
        }
    }

    @Nested
    @DisplayName("配置参数预热测试")
    class ConfigWarmupTests {

        @Test
        @DisplayName("预热常用配置参数 - 成功")
        void warmupConfigKeys_Success() throws Exception {
            // Given
            when(dictService.findAll()).thenReturn(Collections.emptyList());
            when(dictService.type(anyString())).thenReturn(Collections.emptyList());
            when(configService.findAllEnabled()).thenReturn(Collections.emptyList());
            when(configService.getConfigValue(anyString())).thenReturn("test-value");

            // When
            assertDoesNotThrow(() -> cacheWarmupService.run(applicationArguments));

            // Then
            verify(configService, atLeast(10)).getConfigValue(anyString()); // 10 common config keys
            verify(configService).getConfigValue("sys.user.initPassword");
            verify(configService).getConfigValue("sys.user.passwordPolicy");
            verify(configService).getConfigValue("sys.account.captchaEnabled");
            verify(configService).getConfigValue("sys.session.timeout");
        }

        @Test
        @DisplayName("预热配置参数 - 部分失败")
        void warmupConfigKeys_PartialFailure() throws Exception {
            // Given
            when(dictService.findAll()).thenReturn(Collections.emptyList());
            when(dictService.type(anyString())).thenReturn(Collections.emptyList());
            when(configService.findAllEnabled()).thenReturn(Collections.emptyList());
            lenient().when(configService.getConfigValue("sys.user.initPassword")).thenReturn("123456");
            lenient().when(configService.getConfigValue("sys.user.passwordPolicy")).thenThrow(new RuntimeException("Config error"));
            when(configService.getConfigValue(anyString())).thenReturn("test-value");

            // When
            assertDoesNotThrow(() -> cacheWarmupService.run(applicationArguments));

            // Then
            verify(configService).getConfigValue("sys.user.initPassword");
            verify(configService).getConfigValue("sys.user.passwordPolicy");
            // Other config keys should still be called despite one failure
            verify(configService, atLeast(10)).getConfigValue(anyString());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("空数据预热")
        void warmup_EmptyData() throws Exception {
            // Given
            when(dictService.findAll()).thenReturn(Collections.emptyList());
            when(dictService.type(anyString())).thenReturn(Collections.emptyList());
            when(configService.findAllEnabled()).thenReturn(Collections.emptyList());
            when(configService.getConfigValue(anyString())).thenReturn(null);

            // When
            assertDoesNotThrow(() -> cacheWarmupService.run(applicationArguments));

            // Then
            verify(dictService).findAll();
            verify(configService).findAllEnabled();
        }

        @Test
        @DisplayName("大量数据预热")
        void warmup_LargeData() throws Exception {
            // Given
            when(dictService.findAll()).thenReturn(Collections.emptyList());
            when(dictService.type(anyString())).thenReturn(Collections.emptyList());
            when(configService.findAllEnabled()).thenReturn(Collections.emptyList());
            when(configService.getConfigValue(anyString())).thenReturn("test-value");

            // When
            assertDoesNotThrow(() -> cacheWarmupService.run(applicationArguments));

            // Then
            verify(dictService).findAll();
            verify(configService).findAllEnabled();
        }

        @Test
        @DisplayName("并发预热测试")
        void warmup_ConcurrentAccess() throws InterruptedException {
            // Given
            when(dictService.findAll()).thenReturn(Collections.emptyList());
            when(configService.findAllEnabled()).thenReturn(Collections.emptyList());

            // When
            Thread thread1 = new Thread(() -> cacheWarmupService.manualWarmup());
            Thread thread2 = new Thread(() -> cacheWarmupService.manualWarmup());

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            // Then
            verify(dictService, atLeast(2)).findAll();
            verify(configService, atLeast(2)).findAllEnabled();
        }

        @Test
        @DisplayName("null参数处理")
        void run_NullArguments() throws Exception {
            // Given
            when(dictService.findAll()).thenReturn(Collections.emptyList());
            when(dictService.type(anyString())).thenReturn(Collections.emptyList());
            when(configService.findAllEnabled()).thenReturn(Collections.emptyList());
            when(configService.getConfigValue(anyString())).thenReturn("test-value");

            // When
            assertDoesNotThrow(() -> cacheWarmupService.run(null));

            // Then
            verify(dictService).findAll();
            verify(configService).findAllEnabled();
        }
    }
}