package com.yc.admin.system.permission;

import com.yc.admin.system.user.repository.UserRoleRepository;
import com.yc.admin.system.role.repository.RoleRepository;
import com.yc.admin.system.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 权限服务层
 * 提供跨模块的权限查询功能
 *
 * @author YC
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;

    /**
     * 根据用户ID查询菜单ID列表（通过用户角色关联）
     *
     * @param userId 用户ID
     * @return 菜单ID列表
     */
    public List<Long> getMenuIdsByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return userRoleRepository.findMenuIdsByUserId(userId);
    }

    /**
     * 根据用户ID列表查询菜单ID列表（通过用户角色关联）
     *
     * @param userIds 用户ID列表
     * @return 菜单ID列表
     */
    public List<Long> getMenuIdsByUserIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return List.of();
        }
        return userRoleRepository.findMenuIdsByUserIdIn(userIds);
    }

    /**
     * 根据角色权限字符串查询菜单ID列表
     *
     * @param roleKeys 角色权限字符串列表
     * @return 菜单ID列表
     */
    public List<Long> getMenuIdsByRoleKeys(List<String> roleKeys) {
        if (CollectionUtils.isEmpty(roleKeys)) {
            return List.of();
        }
        return roleRepository.findMenuIdsByRoleKeys(roleKeys);
    }

    /**
     * 根据权限标识查询菜单ID列表
     *
     * @param permission 权限标识
     * @return 菜单ID列表
     */
    public List<Long> getMenuIdsByPermission(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return List.of();
        }
        return menuRepository.findMenuIdsByPermission(permission.trim());
    }

    /**
     * 检查用户是否有指定菜单的权限
     *
     * @param userId 用户ID
     * @param menuId 菜单ID
     * @return 是否有权限
     */
    public boolean hasMenuPermission(Long userId, Long menuId) {
        if (userId == null || menuId == null) {
            return false;
        }
        List<Long> userMenuIds = getMenuIdsByUserId(userId);
        return userMenuIds.contains(menuId);
    }

    /**
     * 检查用户是否有指定权限标识的权限
     *
     * @param userId     用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    public boolean hasPermission(Long userId, String permission) {
        if (userId == null || permission == null || permission.trim().isEmpty()) {
            return false;
        }
        List<Long> userMenuIds = getMenuIdsByUserId(userId);
        List<Long> permissionMenuIds = getMenuIdsByPermission(permission.trim());
        return userMenuIds.stream().anyMatch(permissionMenuIds::contains);
    }
    
    /**
     * 根据用户ID查询权限标识列表
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    public List<String> getPermissionsByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return menuRepository.findByUserId(userId).stream()
                .map(menu -> menu.getPerms())
                .filter(perms -> perms != null && !perms.trim().isEmpty())
                .toList();
    }
    
    /**
     * 根据用户ID查询角色权限字符串列表
     *
     * @param userId 用户ID
     * @return 角色权限字符串列表
     */
    public List<String> getRolesByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return roleRepository.findByUserId(userId).stream()
                .map(role -> role.getRoleKey())
                .filter(roleKey -> roleKey != null && !roleKey.trim().isEmpty())
                .toList();
    }
}