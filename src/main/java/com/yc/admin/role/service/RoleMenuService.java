package com.yc.admin.role.service;

import com.yc.admin.role.entity.RoleMenu;
import com.yc.admin.role.repository.RoleMenuRepository;
import com.yc.admin.common.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色菜单关联服务层
 * 提供角色菜单关联的业务逻辑处理
 *
 * @author YC
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleMenuService {

    private final RoleMenuRepository roleMenuRepository;
    private final PermissionService permissionService;

    /**
     * 根据角色ID查询菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        return roleMenuRepository.findMenuIdsByRoleId(roleId);
    }

    /**
     * 根据菜单ID查询角色ID列表
     *
     * @param menuId 菜单ID
     * @return 角色ID列表
     */
    public List<Long> getRoleIdsByMenuId(Long menuId) {
        if (menuId == null) {
            return List.of();
        }
        return roleMenuRepository.findRoleIdsByMenuId(menuId);
    }

    /**
     * 根据用户ID查询菜单ID列表（通过用户角色关联）
     *
     * @param userId 用户ID
     * @return 菜单ID列表
     */
    public List<Long> getMenuIdsByUserId(Long userId) {
        return permissionService.getMenuIdsByUserId(userId);
    }

    /**
     * 根据权限标识查询菜单ID列表
     *
     * @param permission 权限标识
     * @return 菜单ID列表
     */
    public List<Long> getMenuIdsByPermission(String permission) {
        return permissionService.getMenuIdsByPermission(permission);
    }

    /**
     * 批量根据角色ID查询菜单ID列表
     *
     * @param roleIds 角色ID列表
     * @return 角色菜单关联列表
     */
    public List<RoleMenu> getRoleMenusByRoleIds(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }
        return roleMenuRepository.findByRoleIdIn(roleIds);
    }

    /**
     * 批量根据菜单ID查询角色ID列表
     *
     * @param menuIds 菜单ID列表
     * @return 角色菜单关联列表
     */
    public List<RoleMenu> getRoleMenusByMenuIds(List<Long> menuIds) {
        if (CollectionUtils.isEmpty(menuIds)) {
            return List.of();
        }
        return roleMenuRepository.findByMenuIdIn(menuIds);
    }

    /**
     * 检查角色菜单关联是否存在
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 是否存在
     */
    public boolean existsRoleMenu(Long roleId, Long menuId) {
        if (roleId == null || menuId == null) {
            return false;
        }
        return roleMenuRepository.existsByRoleIdAndMenuId(roleId, menuId);
    }

    /**
     * 统计角色的菜单数量
     *
     * @param roleId 角色ID
     * @return 菜单数量
     */
    public long countMenusByRoleId(Long roleId) {
        if (roleId == null) {
            return 0L;
        }
        return roleMenuRepository.countByRoleId(roleId);
    }

    /**
     * 统计菜单的角色数量
     *
     * @param menuId 菜单ID
     * @return 角色数量
     */
    public long countRolesByMenuId(Long menuId) {
        if (menuId == null) {
            return 0L;
        }
        return roleMenuRepository.countByMenuId(menuId);
    }

    /**
     * 为角色分配菜单权限
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignMenusToRole(Long roleId, List<Long> menuIds) {
        if (roleId == null || CollectionUtils.isEmpty(menuIds)) {
            return;
        }

        // 删除角色现有的菜单权限
        roleMenuRepository.deleteByRoleId(roleId);

        // 创建新的菜单权限关联
        List<RoleMenu> roleMenus = menuIds.stream()
                .distinct()
                .map(menuId -> RoleMenu.of(roleId, menuId))
                .collect(Collectors.toList());

        roleMenuRepository.saveAll(roleMenus);
        log.info("为角色[{}]分配菜单权限[{}]成功", roleId, menuIds);
    }

    /**
     * 为菜单分配角色
     *
     * @param menuId  菜单ID
     * @param roleIds 角色ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToMenu(Long menuId, List<Long> roleIds) {
        if (menuId == null || CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        // 删除菜单现有的角色关联
        roleMenuRepository.deleteByMenuId(menuId);

        // 创建新的角色关联
        List<RoleMenu> roleMenus = roleIds.stream()
                .distinct()
                .map(roleId -> RoleMenu.of(roleId, menuId))
                .collect(Collectors.toList());

        roleMenuRepository.saveAll(roleMenus);
        log.info("为菜单[{}]分配角色[{}]成功", menuId, roleIds);
    }

    /**
     * 添加角色菜单关联
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void addRoleMenu(Long roleId, Long menuId) {
        if (roleId == null || menuId == null) {
            return;
        }

        // 检查关联是否已存在
        if (existsRoleMenu(roleId, menuId)) {
            log.warn("角色[{}]和菜单[{}]的关联已存在", roleId, menuId);
            return;
        }

        RoleMenu roleMenu = RoleMenu.of(roleId, menuId);
        roleMenuRepository.save(roleMenu);
        log.info("添加角色[{}]和菜单[{}]的关联成功", roleId, menuId);
    }

    /**
     * 批量添加角色菜单关联
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void addRoleMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null || CollectionUtils.isEmpty(menuIds)) {
            return;
        }

        // 获取已存在的菜单ID
        Set<Long> existingMenuIds = roleMenuRepository.findMenuIdsByRoleId(roleId)
                .stream().collect(Collectors.toSet());

        // 过滤出需要新增的菜单ID
        List<RoleMenu> newRoleMenus = menuIds.stream()
                .distinct()
                .filter(menuId -> !existingMenuIds.contains(menuId))
                .map(menuId -> RoleMenu.of(roleId, menuId))
                .collect(Collectors.toList());

        if (!newRoleMenus.isEmpty()) {
            roleMenuRepository.saveAll(newRoleMenus);
            log.info("为角色[{}]批量添加菜单关联成功，新增菜单数量: {}", roleId, newRoleMenus.size());
        }
    }

    /**
     * 删除角色菜单关联
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeRoleMenu(Long roleId, Long menuId) {
        if (roleId == null || menuId == null) {
            return;
        }

        roleMenuRepository.deleteByRoleIdAndMenuId(roleId, menuId);
        log.info("删除角色[{}]和菜单[{}]的关联成功", roleId, menuId);
    }

    /**
     * 批量删除角色的菜单关联
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeRoleMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null || CollectionUtils.isEmpty(menuIds)) {
            return;
        }

        roleMenuRepository.deleteByRoleIdAndMenuIdIn(roleId, menuIds);
        log.info("批量删除角色[{}]的菜单关联成功，删除菜单数量: {}", roleId, menuIds.size());
    }

    /**
     * 删除角色的所有菜单关联
     *
     * @param roleId 角色ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAllRoleMenus(Long roleId) {
        if (roleId == null) {
            return;
        }

        long deletedCount = roleMenuRepository.deleteByRoleId(roleId);
        log.info("删除角色[{}]的所有菜单关联成功，删除数量: {}", roleId, deletedCount);
    }

    /**
     * 删除菜单的所有角色关联
     *
     * @param menuId 菜单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAllMenuRoles(Long menuId) {
        if (menuId == null) {
            return;
        }

        long deletedCount = roleMenuRepository.deleteByMenuId(menuId);
        log.info("删除菜单[{}]的所有角色关联成功，删除数量: {}", menuId, deletedCount);
    }

    /**
     * 批量删除角色的菜单关联
     *
     * @param roleIds 角色ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeRoleMenusByRoleIds(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        long deletedCount = roleMenuRepository.deleteByRoleIdIn(roleIds);
        log.info("批量删除角色菜单关联成功，删除数量: {}", deletedCount);
    }

    /**
     * 批量删除菜单的角色关联
     *
     * @param menuIds 菜单ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeRoleMenusByMenuIds(List<Long> menuIds) {
        if (CollectionUtils.isEmpty(menuIds)) {
            return;
        }

        long deletedCount = roleMenuRepository.deleteByMenuIdIn(menuIds);
        log.info("批量删除菜单角色关联成功，删除数量: {}", deletedCount);
    }

    /**
     * 检查用户是否有指定菜单的权限
     *
     * @param userId 用户ID
     * @param menuId 菜单ID
     * @return 是否有权限
     */
    public boolean hasMenuPermission(Long userId, Long menuId) {
        return permissionService.hasMenuPermission(userId, menuId);
    }

    /**
     * 检查用户是否有指定权限标识的权限
     *
     * @param userId     用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    public boolean hasPermission(Long userId, String permission) {
        return permissionService.hasPermission(userId, permission);
    }
}