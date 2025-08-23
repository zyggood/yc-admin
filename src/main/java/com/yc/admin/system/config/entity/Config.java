package com.yc.admin.system.config.entity;

import com.yc.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 系统参数配置实体类
 *
 * @author YC
 * @since 1.0.0
 */
@Entity
@Table(name = "sys_config", indexes = {
        @Index(name = "idx_sys_config_key", columnList = "config_key", unique = true),
        @Index(name = "idx_sys_config_type", columnList = "config_type")
})
@Getter
@Setter
@ToString(callSuper = true)
@AttributeOverride(name = "id", column = @Column(name = "config_id"))
public class Config extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 参数名称
     */
    @Column(name = "config_name", nullable = false, length = 100)
    private String configName;

    /**
     * 参数键名
     */
    @Column(name = "config_key", nullable = false, length = 100, unique = true)
    private String configKey;

    /**
     * 参数键值
     */
    @Column(name = "config_value", nullable = false, length = 500)
    private String configValue;

    /**
     * 参数类型（Y系统内置 N非系统内置）
     */
    @Column(name = "config_type", nullable = false, length = 1)
    private String configType = "N";

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 状态（0正常 1停用）
     */
    @Column(name = "status", nullable = false, length = 1)
    private Integer status = 0;

    /**
     * 是否系统内置参数
     * @return true：系统内置，false：非系统内置
     */
    public boolean isSystemConfig() {
        return "Y".equals(configType);
    }

    /**
     * 是否启用
     * @return true：启用，false：停用
     */
    public boolean isEnabled() {
        return status != null && status == 0;
    }

    /**
     * 标记为系统内置
     */
    public void markAsSystemConfig() {
        this.configType = "Y";
    }

    /**
     * 标记为非系统内置
     */
    public void markAsUserConfig() {
        this.configType = "N";
    }

    /**
     * 启用参数
     */
    public void enable() {
        this.status = 0;
    }

    /**
     * 停用参数
     */
    public void disable() {
        this.status = 1;
    }
}