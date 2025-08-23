package com.yc.admin.system.config.dto;

import com.yc.admin.system.config.entity.Config;
import lombok.experimental.UtilityClass;

/**
 * 系统参数配置 DTO 转换器
 *
 * @author YC
 * @since 1.0.0
 */
@UtilityClass
public class ConfigDtoConverter {

    /**
     * DTO 转实体
     *
     * @param configDto DTO 对象
     * @return 实体对象
     */
    public static Config toEntity(ConfigDto configDto) {
        if (configDto == null) {
            return null;
        }

        Config config = new Config();
        config.setConfigName(configDto.getConfigName());
        config.setConfigKey(configDto.getConfigKey());
        config.setConfigValue(configDto.getConfigValue());
        config.setConfigType(configDto.getConfigType());
        config.setRemark(configDto.getRemark());
        config.setStatus(configDto.getStatus());

        // 如果是更新 DTO，设置 ID
        if (configDto instanceof ConfigDto.UpdateDto updateDto) {
            config.setId(updateDto.getId());
        }

        return config;
    }

    /**
     * 实体转 DTO
     *
     * @param config 实体对象
     * @return DTO 对象
     */
    public static ConfigDto toDto(Config config) {
        if (config == null) {
            return null;
        }

        ConfigDto dto = new ConfigDto();
        dto.setConfigName(config.getConfigName());
        dto.setConfigKey(config.getConfigKey());
        dto.setConfigValue(config.getConfigValue());
        dto.setConfigType(config.getConfigType());
        dto.setRemark(config.getRemark());
        dto.setStatus(config.getStatus());

        return dto;
    }

    /**
     * 实体转参数值 DTO
     *
     * @param config 实体对象
     * @return 参数值 DTO
     */
    public static ConfigDto.ValueDto toValueDto(Config config) {
        if (config == null) {
            return null;
        }

        ConfigDto.ValueDto valueDto = new ConfigDto.ValueDto();
        valueDto.setConfigKey(config.getConfigKey());
        valueDto.setConfigValue(config.getConfigValue());
        valueDto.setConfigType(config.getConfigType());
        valueDto.setStatus(config.getStatus());

        return valueDto;
    }

    /**
     * 实体转更新 DTO
     *
     * @param config 实体对象
     * @return 更新 DTO
     */
    public static ConfigDto.UpdateDto toUpdateDto(Config config) {
        if (config == null) {
            return null;
        }

        ConfigDto.UpdateDto updateDto = new ConfigDto.UpdateDto();
        updateDto.setId(config.getId());
        updateDto.setConfigName(config.getConfigName());
        updateDto.setConfigKey(config.getConfigKey());
        updateDto.setConfigValue(config.getConfigValue());
        updateDto.setConfigType(config.getConfigType());
        updateDto.setRemark(config.getRemark());
        updateDto.setStatus(config.getStatus());

        return updateDto;
    }
}