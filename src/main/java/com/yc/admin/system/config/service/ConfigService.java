package com.yc.admin.system.config.service;

import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.system.config.dto.ConfigDto;
import com.yc.admin.system.config.dto.ConfigDtoConverter;
import com.yc.admin.system.config.entity.Config;
import com.yc.admin.system.config.repository.ConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统参数配置服务类
 *
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigRepository configRepository;

    /**
     * 分页查询系统参数
     *
     * @param queryDto    查询条件
     * @param pageRequest 分页参数
     * @return 分页结果
     */
    public Page<Config> page(ConfigDto.QueryDto queryDto, PageRequest pageRequest) {
        return configRepository.findByQueryDto(queryDto, pageRequest);
    }

    /**
     * 根据参数键获取参数值
     *
     * @param configKey 参数键
     * @return 参数值
     */
    @Cacheable(value = "config", key = "#configKey", unless = "#result == null")
    public String getConfigValue(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return null;
        }

        return configRepository.findEnabledByConfigKey(configKey)
                .map(Config::getConfigValue)
                .orElse(null);
    }

    /**
     * 根据参数键获取参数值，如果不存在则返回默认值
     *
     * @param configKey    参数键
     * @param defaultValue 默认值
     * @return 参数值
     */
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    /**
     * 根据参数键获取布尔值
     *
     * @param configKey 参数键
     * @return 布尔值
     */
    public Boolean getConfigBooleanValue(String configKey) {
        String value = getConfigValue(configKey);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "Y".equalsIgnoreCase(value);
    }

    /**
     * 根据参数键获取整数值
     *
     * @param configKey 参数键
     * @return 整数值
     */
    public Integer getConfigIntValue(String configKey) {
        String value = getConfigValue(configKey);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("参数值转换为整数失败: configKey={}, value={}", configKey, value);
            return null;
        }
    }

    /**
     * 批量获取参数值
     *
     * @param configKeys 参数键列表
     * @return 参数键值对
     */
    public Map<String, String> getConfigValues(List<String> configKeys) {
        if (configKeys == null || configKeys.isEmpty()) {
            return Map.of();
        }

        List<Config> configs = configRepository.findEnabledByConfigKeys(configKeys);
        return configs.stream()
                .collect(Collectors.toMap(Config::getConfigKey, Config::getConfigValue));
    }

    /**
     * 根据ID查询参数
     *
     * @param id 参数ID
     * @return 参数配置
     */
    public Config findById(Long id) {
        return configRepository.findById(id)
                .orElseThrow(() -> new BusinessException("参数配置不存在"));
    }

    /**
     * 根据参数键查询参数
     *
     * @param configKey 参数键
     * @return 参数配置
     */
    @Cacheable(value = "config:entity", key = "#configKey", unless = "#result == null")
    public Config findByConfigKey(String configKey) {
        return configRepository.findByConfigKey(configKey).orElse(null);
    }

    /**
     * 创建参数配置
     *
     * @param createDto 创建参数
     * @return 参数配置
     */
    @Transactional
    @CacheEvict(value = {"config", "config:entity"}, key = "#createDto.configKey")
    public Config create(ConfigDto.CreateDto createDto) {
        // 检查参数键是否已存在
        if (configRepository.existsByConfigKey(createDto.getConfigKey())) {
            throw new BusinessException("参数键已存在: " + createDto.getConfigKey());
        }

        Config config = ConfigDtoConverter.toEntity(createDto);
        
        // 设置默认值
        if (config.getConfigType() == null) {
            config.setConfigType("N");
        }
        if (config.getStatus() == null) {
            config.setStatus(0);
        }

        Config savedConfig = configRepository.save(config);
        log.info("创建系统参数: configKey={}, configName={}", savedConfig.getConfigKey(), savedConfig.getConfigName());
        
        return savedConfig;
    }

    /**
     * 更新参数配置
     *
     * @param updateDto 更新参数
     * @return 参数配置
     */
    @Transactional
    @CacheEvict(value = {"config", "config:entity"}, key = "#updateDto.configKey")
    public Config update(ConfigDto.UpdateDto updateDto) {
        Config existingConfig = findById(updateDto.getId());
        
        // 检查参数键是否被其他记录使用
        if (!existingConfig.getConfigKey().equals(updateDto.getConfigKey()) &&
            configRepository.existsByConfigKeyAndIdNot(updateDto.getConfigKey(), updateDto.getId())) {
            throw new BusinessException("参数键已存在: " + updateDto.getConfigKey());
        }

        // 系统内置参数不允许修改参数键和类型
        if (existingConfig.isSystemConfig()) {
            updateDto.setConfigKey(existingConfig.getConfigKey());
            updateDto.setConfigType(existingConfig.getConfigType());
        }

        Config config = ConfigDtoConverter.toEntity(updateDto);
        Config savedConfig = configRepository.save(config);
        
        log.info("更新系统参数: configKey={}, configName={}", savedConfig.getConfigKey(), savedConfig.getConfigName());
        
        return savedConfig;
    }

    /**
     * 删除参数配置
     *
     * @param id 参数ID
     */
    @Transactional
    public void delete(Long id) {
        Config config = findById(id);
        
        // 系统内置参数不允许删除
        if (config.isSystemConfig()) {
            throw new BusinessException("系统内置参数不允许删除");
        }

        // 逻辑删除
        config.markDeleted();
        configRepository.save(config);
        
        // 清除缓存
        clearCache(config.getConfigKey());
        
        log.info("删除系统参数: configKey={}, configName={}", config.getConfigKey(), config.getConfigName());
    }

    /**
     * 批量删除参数配置
     *
     * @param ids 参数ID列表
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<Config> configs = configRepository.findAllById(ids);
        
        for (Config config : configs) {
            if (config.isSystemConfig()) {
                throw new BusinessException("系统内置参数不允许删除: " + config.getConfigKey());
            }
            
            config.markDeleted();
            clearCache(config.getConfigKey());
        }
        
        configRepository.saveAll(configs);
        
        log.info("批量删除系统参数: count={}", configs.size());
    }

    /**
     * 刷新参数缓存
     *
     * @param refreshDto 刷新参数
     */
    @CacheEvict(value = {"config", "config:entity"}, allEntries = true)
    public void refreshCache(ConfigDto.CacheRefreshDto refreshDto) {
        if (StringUtils.hasText(refreshDto.getConfigKey())) {
            clearCache(refreshDto.getConfigKey());
            log.info("刷新参数缓存: configKey={}", refreshDto.getConfigKey());
        } else {
            log.info("刷新全部参数缓存");
        }
    }

    /**
     * 清除指定参数的缓存
     *
     * @param configKey 参数键
     */
    @CacheEvict(value = {"config", "config:entity"}, key = "#configKey")
    public void clearCache(String configKey) {
        log.debug("清除参数缓存: configKey={}", configKey);
    }

    /**
     * 获取所有启用的参数
     *
     * @return 参数列表
     */
    public List<Config> findAllEnabled() {
        return configRepository.findAllEnabled();
    }

    /**
     * 根据参数类型查询参数列表
     *
     * @param configType 参数类型
     * @return 参数列表
     */
    public List<Config> findByConfigType(String configType) {
        return configRepository.findByConfigTypeAndDelFlag(configType, 0);
    }
}