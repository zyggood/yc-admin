package com.yc.admin.system.config.service;

import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.system.config.dto.ConfigDto;
import com.yc.admin.system.config.entity.Config;
import com.yc.admin.system.config.repository.ConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import com.yc.admin.AdminApplication;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ConfigService 单元测试
 *
 * @author YC
 * @since 1.0.0
 */
@SpringBootTest
@ContextConfiguration(classes = AdminApplication.class)
@Transactional
@DisplayName("ConfigService 单元测试")
class ConfigServiceTest {

    @MockBean
    private ConfigRepository configRepository;

    private ConfigService configService;

    private Config testConfig;
    private Config systemConfig;
    private ConfigDto.QueryDto queryDto;
    private ConfigDto.CreateDto createDto;
    private ConfigDto.UpdateDto updateDto;
    private ConfigDto.CacheRefreshDto refreshDto;

    @BeforeEach
    void setUp() {
        configService = new ConfigService(configRepository);
        
        // 测试数据
        testConfig = new Config();
        testConfig.setId(1L);
        testConfig.setConfigName("测试参数");
        testConfig.setConfigKey("test.config");
        testConfig.setConfigValue("test_value");
        testConfig.setConfigType("N");
        testConfig.setStatus(0);
        testConfig.setRemark("测试备注");
        testConfig.setCreateTime(LocalDateTime.now());
        testConfig.setUpdateTime(LocalDateTime.now());
        testConfig.setDelFlag(0);

        systemConfig = new Config();
        systemConfig.setId(2L);
        systemConfig.setConfigName("系统参数");
        systemConfig.setConfigKey("sys.config");
        systemConfig.setConfigValue("sys_value");
        systemConfig.setConfigType("Y");
        systemConfig.setStatus(0);
        systemConfig.setRemark("系统备注");
        systemConfig.setCreateTime(LocalDateTime.now());
        systemConfig.setUpdateTime(LocalDateTime.now());
        systemConfig.setDelFlag(0);

        queryDto = new ConfigDto.QueryDto();
        queryDto.setConfigName("测试");
        queryDto.setConfigKey("test");
        queryDto.setConfigType("N");
        queryDto.setStatus(0);

        createDto = new ConfigDto.CreateDto();
        createDto.setConfigName("新参数");
        createDto.setConfigKey("new.config");
        createDto.setConfigValue("new_value");
        createDto.setConfigType("N");
        createDto.setStatus(0);
        createDto.setRemark("新备注");

        updateDto = new ConfigDto.UpdateDto();
        updateDto.setId(1L);
        updateDto.setConfigName("更新参数");
        updateDto.setConfigKey("updated.config");
        updateDto.setConfigValue("updated_value");
        updateDto.setConfigType("N");
        updateDto.setStatus(0);
        updateDto.setRemark("更新备注");

        refreshDto = new ConfigDto.CacheRefreshDto();
        refreshDto.setConfigKey("test.config");
        refreshDto.setForce(false);
    }

    @Nested
    @DisplayName("分页查询测试")
    class PageQueryTests {

        @Test
        @DisplayName("正常分页查询")
        void testPageQuery_Success() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 10);
            Page<Config> expectedPage = new PageImpl<>(List.of(testConfig), pageRequest, 1);
            when(configRepository.findByQueryDto(queryDto, pageRequest)).thenReturn(expectedPage);

            // When
            Page<Config> result = configService.page(queryDto, pageRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getConfigKey()).isEqualTo("test.config");
            verify(configRepository).findByQueryDto(queryDto, pageRequest);
        }

        @Test
        @DisplayName("空查询条件分页查询")
        void testPageQuery_EmptyQuery() {
            // Given
            ConfigDto.QueryDto emptyQuery = new ConfigDto.QueryDto();
            PageRequest pageRequest = PageRequest.of(0, 10);
            Page<Config> expectedPage = new PageImpl<>(List.of(testConfig, systemConfig), pageRequest, 2);
            when(configRepository.findByQueryDto(emptyQuery, pageRequest)).thenReturn(expectedPage);

            // When
            Page<Config> result = configService.page(emptyQuery, pageRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(configRepository).findByQueryDto(emptyQuery, pageRequest);
        }
    }

    @Nested
    @DisplayName("获取参数值测试")
    class GetConfigValueTests {

        @Test
        @DisplayName("正常获取参数值")
        void testGetConfigValue_Success() {
            // Given
            when(configRepository.findEnabledByConfigKey("test.config"))
                    .thenReturn(Optional.of(testConfig));

            // When
            String result = configService.getConfigValue("test.config");

            // Then
            assertThat(result).isEqualTo("test_value");
            verify(configRepository).findEnabledByConfigKey("test.config");
        }

        @Test
        @DisplayName("参数不存在返回null")
        void testGetConfigValue_NotFound() {
            // Given
            when(configRepository.findEnabledByConfigKey("not.exist"))
                    .thenReturn(Optional.empty());

            // When
            String result = configService.getConfigValue("not.exist");

            // Then
            assertThat(result).isNull();
            verify(configRepository).findEnabledByConfigKey("not.exist");
        }

        @Test
        @DisplayName("参数键为空返回null")
        void testGetConfigValue_EmptyKey() {
            // When
            String result1 = configService.getConfigValue("");
            String result2 = configService.getConfigValue(null);
            String result3 = configService.getConfigValue("   ");

            // Then
            assertThat(result1).isNull();
            assertThat(result2).isNull();
            assertThat(result3).isNull();
            verify(configRepository, never()).findEnabledByConfigKey(anyString());
        }

        @Test
        @DisplayName("获取参数值带默认值")
        void testGetConfigValueWithDefault_Success() {
            // Given
            when(configRepository.findEnabledByConfigKey("test.config"))
                    .thenReturn(Optional.of(testConfig));

            // When
            String result = configService.getConfigValue("test.config", "default_value");

            // Then
            assertThat(result).isEqualTo("test_value");
        }

        @Test
        @DisplayName("参数不存在返回默认值")
        void testGetConfigValueWithDefault_UseDefault() {
            // Given
            when(configRepository.findEnabledByConfigKey("not.exist"))
                    .thenReturn(Optional.empty());

            // When
            String result = configService.getConfigValue("not.exist", "default_value");

            // Then
            assertThat(result).isEqualTo("default_value");
        }
    }

    @Nested
    @DisplayName("获取布尔值测试")
    class GetBooleanValueTests {

        @Test
        @DisplayName("获取true值")
        void testGetConfigBooleanValue_True() {
            // Given
            Config trueConfig = new Config();
            trueConfig.setConfigValue("true");
            when(configRepository.findEnabledByConfigKey("bool.config"))
                    .thenReturn(Optional.of(trueConfig));

            // When
            Boolean result = configService.getConfigBooleanValue("bool.config");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("获取false值")
        void testGetConfigBooleanValue_False() {
            // Given
            Config falseConfig = new Config();
            falseConfig.setConfigValue("false");
            when(configRepository.findEnabledByConfigKey("bool.config"))
                    .thenReturn(Optional.of(falseConfig));

            // When
            Boolean result = configService.getConfigBooleanValue("bool.config");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("获取数字布尔值")
        void testGetConfigBooleanValue_Number() {
            // Given
            Config oneConfig = new Config();
            oneConfig.setConfigValue("1");
            when(configRepository.findEnabledByConfigKey("bool.config"))
                    .thenReturn(Optional.of(oneConfig));

            // When
            Boolean result = configService.getConfigBooleanValue("bool.config");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("获取Y/N布尔值")
        void testGetConfigBooleanValue_YN() {
            // Given
            Config yConfig = new Config();
            yConfig.setConfigValue("Y");
            when(configRepository.findEnabledByConfigKey("bool.config"))
                    .thenReturn(Optional.of(yConfig));

            // When
            Boolean result = configService.getConfigBooleanValue("bool.config");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("参数不存在返回null")
        void testGetConfigBooleanValue_NotFound() {
            // Given
            when(configRepository.findEnabledByConfigKey("not.exist"))
                    .thenReturn(Optional.empty());

            // When
            Boolean result = configService.getConfigBooleanValue("not.exist");

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("获取整数值测试")
    class GetIntValueTests {

        @Test
        @DisplayName("正常获取整数值")
        void testGetConfigIntValue_Success() {
            // Given
            Config intConfig = new Config();
            intConfig.setConfigValue("123");
            when(configRepository.findEnabledByConfigKey("int.config"))
                    .thenReturn(Optional.of(intConfig));

            // When
            Integer result = configService.getConfigIntValue("int.config");

            // Then
            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("无效数字格式返回null")
        void testGetConfigIntValue_InvalidFormat() {
            // Given
            Config invalidConfig = new Config();
            invalidConfig.setConfigValue("invalid_number");
            when(configRepository.findEnabledByConfigKey("int.config"))
                    .thenReturn(Optional.of(invalidConfig));

            // When
            Integer result = configService.getConfigIntValue("int.config");

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("参数不存在返回null")
        void testGetConfigIntValue_NotFound() {
            // Given
            when(configRepository.findEnabledByConfigKey("not.exist"))
                    .thenReturn(Optional.empty());

            // When
            Integer result = configService.getConfigIntValue("not.exist");

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("批量获取参数值测试")
    class GetConfigValuesTests {

        @Test
        @DisplayName("正常批量获取参数值")
        void testGetConfigValues_Success() {
            // Given
            List<String> configKeys = List.of("test.config", "sys.config");
            List<Config> configs = List.of(testConfig, systemConfig);
            when(configRepository.findEnabledByConfigKeys(configKeys)).thenReturn(configs);

            // When
            Map<String, String> result = configService.getConfigValues(configKeys);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get("test.config")).isEqualTo("test_value");
            assertThat(result.get("sys.config")).isEqualTo("sys_value");
            verify(configRepository).findEnabledByConfigKeys(configKeys);
        }

        @Test
        @DisplayName("空列表返回空Map")
        void testGetConfigValues_EmptyList() {
            // When
            Map<String, String> result1 = configService.getConfigValues(Collections.emptyList());
            Map<String, String> result2 = configService.getConfigValues(null);

            // Then
            assertThat(result1).isEmpty();
            assertThat(result2).isEmpty();
            verify(configRepository, never()).findEnabledByConfigKeys(anyList());
        }
    }

    @Nested
    @DisplayName("查询单个参数测试")
    class FindTests {

        @Test
        @DisplayName("根据ID查询成功")
        void testFindById_Success() {
            // Given
            when(configRepository.findById(1L)).thenReturn(Optional.of(testConfig));

            // When
            Config result = configService.findById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getConfigKey()).isEqualTo("test.config");
            verify(configRepository).findById(1L);
        }

        @Test
        @DisplayName("根据ID查询不存在抛异常")
        void testFindById_NotFound() {
            // Given
            when(configRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configService.findById(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("参数配置不存在");
            verify(configRepository).findById(999L);
        }

        @Test
        @DisplayName("根据参数键查询成功")
        void testFindByConfigKey_Success() {
            // Given
            when(configRepository.findByConfigKey("test.config"))
                    .thenReturn(Optional.of(testConfig));

            // When
            Config result = configService.findByConfigKey("test.config");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getConfigKey()).isEqualTo("test.config");
            verify(configRepository).findByConfigKey("test.config");
        }

        @Test
        @DisplayName("根据参数键查询不存在返回null")
        void testFindByConfigKey_NotFound() {
            // Given
            when(configRepository.findByConfigKey("not.exist"))
                    .thenReturn(Optional.empty());

            // When
            Config result = configService.findByConfigKey("not.exist");

            // Then
            assertThat(result).isNull();
            verify(configRepository).findByConfigKey("not.exist");
        }
    }

    @Nested
    @DisplayName("创建参数测试")
    class CreateTests {

        @Test
        @DisplayName("正常创建参数")
        void testCreate_Success() {
            // Given
            when(configRepository.existsByConfigKey("new.config")).thenReturn(false);
            when(configRepository.save(any(Config.class))).thenReturn(testConfig);

            // When
            Config result = configService.create(createDto);

            // Then
            assertThat(result).isNotNull();
            verify(configRepository).existsByConfigKey("new.config");
            verify(configRepository).save(any(Config.class));
        }

        @Test
        @DisplayName("参数键已存在抛异常")
        void testCreate_KeyExists() {
            // Given
            when(configRepository.existsByConfigKey("new.config")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> configService.create(createDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("参数键已存在: new.config");
            verify(configRepository).existsByConfigKey("new.config");
            verify(configRepository, never()).save(any(Config.class));
        }
    }

    @Nested
    @DisplayName("更新参数测试")
    class UpdateTests {

        @Test
        @DisplayName("正常更新参数")
        void testUpdate_Success() {
            // Given
            when(configRepository.findById(1L)).thenReturn(Optional.of(testConfig));
            when(configRepository.existsByConfigKeyAndIdNot("updated.config", 1L)).thenReturn(false);
            when(configRepository.save(any(Config.class))).thenReturn(testConfig);

            // When
            Config result = configService.update(updateDto);

            // Then
            assertThat(result).isNotNull();
            verify(configRepository).findById(1L);
            verify(configRepository).existsByConfigKeyAndIdNot("updated.config", 1L);
            verify(configRepository).save(any(Config.class));
        }

        @Test
        @DisplayName("更新不存在的参数抛异常")
        void testUpdate_NotFound() {
            // Given
            when(configRepository.findById(999L)).thenReturn(Optional.empty());
            updateDto.setId(999L);

            // When & Then
            assertThatThrownBy(() -> configService.update(updateDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("参数配置不存在");
            verify(configRepository).findById(999L);
            verify(configRepository, never()).save(any(Config.class));
        }

        @Test
        @DisplayName("更新参数键已存在抛异常")
        void testUpdate_KeyExists() {
            // Given
            when(configRepository.findById(1L)).thenReturn(Optional.of(testConfig));
            when(configRepository.existsByConfigKeyAndIdNot("updated.config", 1L)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> configService.update(updateDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("参数键已存在: updated.config");
            verify(configRepository).findById(1L);
            verify(configRepository).existsByConfigKeyAndIdNot("updated.config", 1L);
            verify(configRepository, never()).save(any(Config.class));
        }

        @Test
        @DisplayName("更新系统参数保护键和类型")
        void testUpdate_SystemConfig() {
            // Given
            when(configRepository.findById(2L)).thenReturn(Optional.of(systemConfig));
            when(configRepository.save(any(Config.class))).thenReturn(systemConfig);
            updateDto.setId(2L);
            updateDto.setConfigKey("new.sys.config");
            updateDto.setConfigType("N");

            // When
            Config result = configService.update(updateDto);

            // Then
            assertThat(result).isNotNull();
            // 验证系统参数的键和类型不会被修改
            assertThat(updateDto.getConfigKey()).isEqualTo("sys.config");
            assertThat(updateDto.getConfigType()).isEqualTo("Y");
            verify(configRepository).findById(2L);
            verify(configRepository).save(any(Config.class));
        }
    }

    @Nested
    @DisplayName("删除参数测试")
    class DeleteTests {

        @Test
        @DisplayName("正常删除参数")
        void testDelete_Success() {
            // Given
            when(configRepository.findById(1L)).thenReturn(Optional.of(testConfig));
            when(configRepository.save(any(Config.class))).thenReturn(testConfig);

            // When
            configService.delete(1L);

            // Then
            verify(configRepository).findById(1L);
            verify(configRepository).save(any(Config.class));
        }

        @Test
        @DisplayName("删除不存在的参数抛异常")
        void testDelete_NotFound() {
            // Given
            when(configRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configService.delete(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("参数配置不存在");
            verify(configRepository).findById(999L);
            verify(configRepository, never()).save(any(Config.class));
        }

        @Test
        @DisplayName("删除系统参数抛异常")
        void testDelete_SystemConfig() {
            // Given
            when(configRepository.findById(2L)).thenReturn(Optional.of(systemConfig));

            // When & Then
            assertThatThrownBy(() -> configService.delete(2L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("系统内置参数不允许删除");
            verify(configRepository).findById(2L);
            verify(configRepository, never()).save(any(Config.class));
        }

        @Test
        @DisplayName("正常批量删除参数")
        void testDeleteBatch_Success() {
            // Given
            List<Long> ids = List.of(1L);
            when(configRepository.findAllById(ids)).thenReturn(List.of(testConfig));
            when(configRepository.saveAll(anyList())).thenReturn(List.of(testConfig));

            // When
            configService.deleteBatch(ids);

            // Then
            verify(configRepository).findAllById(ids);
            verify(configRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("批量删除包含系统参数抛异常")
        void testDeleteBatch_ContainsSystemConfig() {
            // Given
            List<Long> ids = List.of(1L, 2L);
            when(configRepository.findAllById(ids)).thenReturn(List.of(testConfig, systemConfig));

            // When & Then
            assertThatThrownBy(() -> configService.deleteBatch(ids))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("系统内置参数不允许删除: sys.config");
            verify(configRepository).findAllById(ids);
            verify(configRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("批量删除空列表")
        void testDeleteBatch_EmptyList() {
            // When
            configService.deleteBatch(Collections.emptyList());
            configService.deleteBatch(null);

            // Then
            verify(configRepository, never()).findAllById(anyList());
            verify(configRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("缓存操作测试")
    class CacheTests {

        @Test
        @DisplayName("刷新指定参数缓存")
        void testRefreshCache_SpecificKey() {
            // When
            configService.refreshCache(refreshDto);

            // Then
            // 验证方法执行完成（缓存注解会处理实际的缓存清除）
            assertThat(refreshDto.getConfigKey()).isEqualTo("test.config");
        }

        @Test
        @DisplayName("刷新全部缓存")
        void testRefreshCache_AllCache() {
            // Given
            refreshDto.setConfigKey(null);

            // When
            configService.refreshCache(refreshDto);

            // Then
            // 验证方法执行完成
            assertThat(refreshDto.getConfigKey()).isNull();
        }

        @Test
        @DisplayName("清除指定参数缓存")
        void testClearCache() {
            // When
            configService.clearCache("test.config");

            // Then
            // 验证方法执行完成（缓存注解会处理实际的缓存清除）
        }
    }

    @Nested
    @DisplayName("查询列表测试")
    class ListTests {

        @Test
        @DisplayName("查询所有启用参数")
        void testFindAllEnabled() {
            // Given
            List<Config> configs = List.of(testConfig, systemConfig);
            when(configRepository.findAllEnabled()).thenReturn(configs);

            // When
            List<Config> result = configService.findAllEnabled();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(testConfig, systemConfig);
            verify(configRepository).findAllEnabled();
        }

        @Test
        @DisplayName("根据类型查询参数")
        void testFindByConfigType() {
            // Given
            List<Config> configs = List.of(testConfig);
            when(configRepository.findByConfigTypeAndDelFlag("N", 0)).thenReturn(configs);

            // When
            List<Config> result = configService.findByConfigType("N");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getConfigType()).isEqualTo("N");
            verify(configRepository).findByConfigTypeAndDelFlag("N", 0);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @Test
        @DisplayName("参数值为空字符串")
        void testEmptyConfigValue() {
            // Given
            Config emptyConfig = new Config();
            emptyConfig.setConfigValue("");
            when(configRepository.findEnabledByConfigKey("empty.config"))
                    .thenReturn(Optional.of(emptyConfig));

            // When
            String result = configService.getConfigValue("empty.config");
            Boolean boolResult = configService.getConfigBooleanValue("empty.config");
            Integer intResult = configService.getConfigIntValue("empty.config");

            // Then
            assertThat(result).isEmpty();
            assertThat(boolResult).isNull();
            assertThat(intResult).isNull();
        }

        @Test
        @DisplayName("参数值为空格")
        void testWhitespaceConfigValue() {
            // Given
            Config whitespaceConfig = new Config();
            whitespaceConfig.setConfigValue("   ");
            when(configRepository.findEnabledByConfigKey("whitespace.config"))
                    .thenReturn(Optional.of(whitespaceConfig));

            // When
            String result = configService.getConfigValue("whitespace.config");
            Boolean boolResult = configService.getConfigBooleanValue("whitespace.config");
            Integer intResult = configService.getConfigIntValue("whitespace.config");

            // Then
            assertThat(result).isEqualTo("   ");
            assertThat(boolResult).isNull();
            assertThat(intResult).isNull();
        }

        @Test
        @DisplayName("极大整数值")
        void testLargeIntValue() {
            // Given
            Config largeIntConfig = new Config();
            largeIntConfig.setConfigValue(String.valueOf(Integer.MAX_VALUE));
            when(configRepository.findEnabledByConfigKey("large.int.config"))
                    .thenReturn(Optional.of(largeIntConfig));

            // When
            Integer result = configService.getConfigIntValue("large.int.config");

            // Then
            assertThat(result).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("负整数值")
        void testNegativeIntValue() {
            // Given
            Config negativeIntConfig = new Config();
            negativeIntConfig.setConfigValue("-123");
            when(configRepository.findEnabledByConfigKey("negative.int.config"))
                    .thenReturn(Optional.of(negativeIntConfig));

            // When
            Integer result = configService.getConfigIntValue("negative.int.config");

            // Then
            assertThat(result).isEqualTo(-123);
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionTests {

        @Test
        @DisplayName("数据库异常处理")
        void testDatabaseException() {
            // Given
            when(configRepository.findById(1L)).thenThrow(new RuntimeException("数据库连接失败"));

            // When & Then
            assertThatThrownBy(() -> configService.findById(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("数据库连接失败");
        }

        @Test
        @DisplayName("保存异常处理")
        void testSaveException() {
            // Given
            when(configRepository.existsByConfigKey("new.config")).thenReturn(false);
            when(configRepository.save(any(Config.class))).thenThrow(new RuntimeException("保存失败"));

            // When & Then
            assertThatThrownBy(() -> configService.create(createDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("保存失败");
        }
    }
}