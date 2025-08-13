package com.yc.admin.system.api;

import java.util.List;

/**
 * 菜单 API 服务接口
 * 提供给其他模块使用的菜单相关功能
 * 
 * @author YC
 * @since 1.0.0
 */
public interface MenuApiService {

    /**
     * 根据用户ID查询权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> findPermissionsByUserId(Long userId);
}