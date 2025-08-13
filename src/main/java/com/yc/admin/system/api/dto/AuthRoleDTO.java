package com.yc.admin.system.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证角色DTO
 * 用于auth模块内部的角色信息传递，避免直接依赖system模块的Role entity
 *
 * @author yc
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRoleDTO {
    
    /**
     * 角色ID
     */
    private Long id;
    
    /**
     * 角色标识
     */
    private String roleKey;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 角色状态
     */
    private String status;
}