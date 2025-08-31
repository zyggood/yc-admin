package com.yc.admin.system.api.impl;

import com.yc.admin.system.api.PermissionApiService;
import com.yc.admin.system.permission.PermissionInheritanceService;
import com.yc.admin.system.permission.PermissionService;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.service.RoleService;
import com.yc.admin.system.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 权限API服务实现类
 * 将权限服务的功能暴露给其他模块
 *
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionApiServiceImpl implements PermissionApiService {
    
    private final PermissionService permissionService;
    private final PermissionInheritanceService permissionInheritanceService;
    private final RoleService roleService;

    @Override
    public Set<String> calculateUserPermissions(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        
        try {
            if (hasInheritanceRole(userId)) {
                // 拥有启用权限继承角色的用户使用权限继承服务获取所有权限
                PermissionInheritanceService.PermissionSet adminPermissions = 
                    permissionInheritanceService.calculateUserPermissions(userId);
                log.debug("启用权限继承的用户 {} 通过权限继承获得权限: {}", userId, adminPermissions.getPermissions());
                return adminPermissions.getPermissions();
            } else {
                // 普通角色用户通过基础权限服务获取权限
                return Set.copyOf(permissionService.getPermissionsByUserId(userId));
            }
        } catch (Exception e) {
            log.warn("计算用户 {} 权限失败: {}", userId, e.getMessage());
            return Set.of();
        }
    }

    @Override
    public boolean hasPermission(Long userId, String permission) {
        if (hasInheritanceRole(userId)) {
            // 拥有启用权限继承角色的用户使用权限继承服务检查权限
            return permissionInheritanceService.hasPermission(userId, permission);
        } else {
            // 普通角色用户使用基础权限服务检查权限
            return permissionService.hasPermission(userId, permission);
        }
    }

    @Override
    public boolean hasMenuPermission(Long userId, Long menuId) {
        if (hasInheritanceRole(userId)) {
            // 拥有启用权限继承角色的用户使用权限继承服务检查菜单权限
            return permissionInheritanceService.hasMenuPermission(userId, menuId);
        } else {
            // 普通角色用户使用基础权限服务检查菜单权限
            return permissionService.hasMenuPermission(userId, menuId);
        }
    }

    /**
     * 获取用户的数据权限范围
     *
     * @param userId 用户ID
     * @return 数据权限范围集合
     */
    @Override
    public Set<PermissionInheritanceService.DataScope> getUserDataScopes(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        
        try {
            PermissionInheritanceService.PermissionSet permissions = 
                permissionInheritanceService.calculateUserPermissions(userId);
            return permissions.getDataScopes();
        } catch (Exception e) {
            log.warn("获取用户 {} 数据权限失败: {}", userId, e.getMessage());
            return Set.of();
        }
    }

    /**
     * 获取用户的数据权限部门ID
     *
     * @param userId 用户ID
     * @return 部门ID集合
     */
    @Override
    public Set<Long> getUserDataDeptIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        
        try {
            PermissionInheritanceService.PermissionSet permissions = 
                permissionInheritanceService.calculateUserPermissions(userId);
            return permissions.getDeptIds();
        } catch (Exception e) {
            log.warn("获取用户 {} 数据权限部门ID失败: {}", userId, e.getMessage());
            return Set.of();
        }
    }

    /**
     * 检查用户是否拥有指定数据权限
     *
     * @param userId 用户ID
     * @param dataScope 数据权限范围
     * @return 是否拥有权限
     */
    @Override
    public boolean hasDataScope(Long userId, PermissionInheritanceService.DataScope dataScope) {
        if (userId == null || dataScope == null) {
            return false;
        }
        
        try {
            PermissionInheritanceService.PermissionSet permissions = 
                permissionInheritanceService.calculateUserPermissions(userId);
            return permissions.hasDataScope(dataScope);
        } catch (Exception e) {
            log.warn("检查用户 {} 数据权限 {} 失败: {}", userId, dataScope, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否拥有指定部门的数据权限
     *
     * @param userId 用户ID
     * @param deptId 部门ID
     * @return 是否拥有权限
     */
    @Override
    public boolean hasDeptDataPermission(Long userId, Long deptId) {
        if (userId == null || deptId == null) {
            return false;
        }
        
        try {
            PermissionInheritanceService.PermissionSet permissions = 
                permissionInheritanceService.calculateUserPermissions(userId);
            return permissions.hasDeptId(deptId);
        } catch (Exception e) {
            log.warn("检查用户 {} 部门 {} 数据权限失败: {}", userId, deptId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否拥有启用权限继承的角色
     *
     * @param userId 用户ID
     * @return 是否拥有启用权限继承的角色
     */
    private boolean hasInheritanceRole(Long userId) {
        if (userId == null) {
            return false;
        }
        
        try {
            List<Role> userRoles = roleService.findByUserId(userId);
            return userRoles.stream().anyMatch(Role::isInheritanceEnabled);
        } catch (Exception e) {
            log.warn("检查用户 {} 是否拥有启用权限继承的角色失败: {}", userId, e.getMessage());
            return false;
        }
    }
}