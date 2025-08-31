package com.yc.admin.system.api;

import com.yc.admin.system.permission.PermissionInheritanceService;
import java.util.Set;

/**
 * 权限 API 服务接口
 * 提供给其他模块使用的权限相关功能
 * 
 * @author YC
 * @since 1.0.0
 */
public interface PermissionApiService {

    /**
     * 计算用户的完整权限集合（包含权限继承）
     * @param userId 用户ID
     * @return 权限集合
     */
    Set<String> calculateUserPermissions(Long userId);

    /**
     * 检查用户是否有指定权限
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 检查用户是否有指定菜单权限
     * @param userId 用户ID
     * @param menuId 菜单ID
     * @return 是否有权限
     */
    boolean hasMenuPermission(Long userId, Long menuId);

    /**
     * 获取用户的数据权限范围
     * @param userId 用户ID
     * @return 数据权限范围集合
     */
    Set<PermissionInheritanceService.DataScope> getUserDataScopes(Long userId);

    /**
     * 获取用户的数据权限部门ID
     * @param userId 用户ID
     * @return 部门ID集合
     */
    Set<Long> getUserDataDeptIds(Long userId);

    /**
     * 检查用户是否拥有指定数据权限
     * @param userId 用户ID
     * @param dataScope 数据权限范围
     * @return 是否拥有权限
     */
    boolean hasDataScope(Long userId, PermissionInheritanceService.DataScope dataScope);

    /**
     * 检查用户是否拥有指定部门的数据权限
     * @param userId 用户ID
     * @param deptId 部门ID
     * @return 是否拥有权限
     */
    boolean hasDeptDataPermission(Long userId, Long deptId);
}