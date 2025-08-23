package com.yc.admin.system.config.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统参数配置 DTO
 *
 * @author YC
 * @since 1.0.0
 */
@Data
public class ConfigDto {

    @Schema(description = "参数名称")
    private String configName;

    @Schema(description = "参数键名")
    private String configKey;

    @Schema(description = "参数键值")
    private String configValue;

    @Schema(description = "参数类型")
    private String configType;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态")
    private Integer status;

    /**
     * 查询参数 DTO
     */
    @Data
    @Schema(description = "系统参数查询参数")
    public static class QueryDto {

        @Schema(description = "参数名称")
        private String configName;

        @Schema(description = "参数键名")
        private String configKey;

        @Schema(description = "参数类型")
        private String configType;

        @Schema(description = "状态")
        private Integer status;
    }

    /**
     * 创建参数 DTO
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "系统参数创建参数")
    public static class CreateDto extends ConfigDto {
        // 继承父类所有字段
    }

    /**
     * 更新参数 DTO
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "系统参数更新参数")
    public static class UpdateDto extends ConfigDto {

        @Schema(description = "参数ID")
        private Long id;
    }

    /**
     * 参数值查询 DTO
     */
    @Data
    @Schema(description = "参数值查询结果")
    public static class ValueDto {

        @Schema(description = "参数键名")
        private String configKey;

        @Schema(description = "参数键值")
        private String configValue;

        @Schema(description = "参数类型")
        private String configType;

        @Schema(description = "状态")
        private Integer status;
    }

    /**
     * 缓存刷新 DTO
     */
    @Data
    @Schema(description = "缓存刷新参数")
    public static class CacheRefreshDto {

        @Schema(description = "参数键名（为空则刷新全部）")
        private String configKey;

        @Schema(description = "是否强制刷新")
        private Boolean force = false;
    }
}