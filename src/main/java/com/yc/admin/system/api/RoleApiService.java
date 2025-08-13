package com.yc.admin.system.api;

import com.yc.admin.system.role.entity.Role;

import java.util.List;

/**
 * 角色 API 服务接口
 * 提供给其他模块使用的角色相关功能
 * 
 * @author YC
 * @since 1.0.0
 */
public interface RoleApiService {

    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> findByUserId(Long userId);
}