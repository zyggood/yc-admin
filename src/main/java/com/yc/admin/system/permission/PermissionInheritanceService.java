package com.yc.admin.system.permission;

import com.yc.admin.system.menu.entity.Menu;
import com.yc.admin.system.menu.repository.MenuRepository;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.repository.RoleRepository;
import com.yc.admin.system.role.repository.RoleDeptRepository;
import com.yc.admin.system.user.entity.User;
import com.yc.admin.system.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限继承和合并服务
 * 提供权限继承、合并、计算等功能
 *
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionInheritanceService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleDeptRepository roleDeptRepository;
    private final MenuRepository menuRepository;
    private final PermissionService permissionService;

    /**
     * 权限继承策略
     */
    public enum InheritanceStrategy {
        /** 累加继承：子权限包含父权限的所有权限 */
        ADDITIVE,
        /** 覆盖继承：子权限覆盖父权限 */
        OVERRIDE,
        /** 交集继承：子权限只包含与父权限的交集 */
        INTERSECTION
    }

    /**
     * 权限合并策略
     */
    public enum MergeStrategy {
        /** 并集合并：合并所有权限 */
        UNION,
        /** 交集合并：只保留共同权限 */
        INTERSECTION,
        /** 差集合并：排除指定权限 */
        DIFFERENCE
    }

    /**
     * 权限范围
     */
    public enum PermissionScope {
        /** 全部权限 */
        ALL,
        /** 菜单权限 */
        MENU_ONLY,
        /** 按钮权限 */
        BUTTON_ONLY,
        /** 数据权限 */
        DATA_ONLY
    }

    /**
     * 数据权限范围
     */
    public enum DataScope {
        /** 全部数据权限 */
        ALL("1"),
        /** 自定数据权限 */
        CUSTOM("2"),
        /** 本部门数据权限 */
        DEPT("3"),
        /** 本部门及以下数据权限 */
        DEPT_AND_CHILD("4"),
        /** 仅本人数据权限 */
        SELF("5");

        private final String value;

        DataScope(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static DataScope fromValue(String value) {
            for (DataScope scope : values()) {
                if (scope.value.equals(value)) {
                    return scope;
                }
            }
            return DEPT; // 默认返回部门权限
        }
    }

    /**
     * 计算用户的完整权限集合（包含继承和合并）
     *
     * @param userId 用户ID
     * @return 完整权限集合
     */
    public PermissionSet calculateUserPermissions(Long userId) {
        if (userId == null) {
            return PermissionSet.empty();
        }

        // 获取用户信息
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty() || !userOpt.get().isNormal()) {
            log.warn("用户不存在或已停用: userId={}", userId);
            return PermissionSet.empty();
        }

        User user = userOpt.get();
        log.debug("计算用户权限: userId={}, userName={}", userId, user.getUserName());

        // 获取用户的所有角色
        List<Role> userRoles = getUserRoles(userId);
        if (CollectionUtils.isEmpty(userRoles)) {
            log.debug("用户没有分配角色: userId={}", userId);
            return PermissionSet.empty();
        }

        // 合并所有角色的权限
        PermissionSet mergedPermissions = mergeRolePermissions(userRoles, MergeStrategy.UNION);
        
        // 应用权限继承
        PermissionSet inheritedPermissions = applyPermissionInheritance(mergedPermissions, InheritanceStrategy.ADDITIVE);
        
        log.debug("用户权限计算完成: userId={}, permissions={}", userId, inheritedPermissions);
        return inheritedPermissions;
    }

    /**
     * 计算角色的完整权限集合（包含继承）
     *
     * @param roleId 角色ID
     * @return 完整权限集合
     */
    public PermissionSet calculateRolePermissions(Long roleId) {
        return calculateRolePermissions(roleId, PermissionScope.ALL);
    }

    /**
     * 计算角色的权限集合
     *
     * @param roleId 角色ID
     * @param scope 权限范围
     * @return 权限集合
     */
    @Cacheable(value = "rolePermissions", key = "#roleId + '_' + #scope")
    public PermissionSet calculateRolePermissions(Long roleId, PermissionScope scope) {
        if (roleId == null) {
            return PermissionSet.empty();
        }

        // 获取角色信息
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty() || !roleOpt.get().isNormal()) {
            log.warn("角色不存在或已停用: roleId={}", roleId);
            return PermissionSet.empty();
        }

        Role role = roleOpt.get();
        log.debug("计算角色权限: roleId={}, roleName={}, dataScope={}", roleId, role.getRoleName(), role.getDataScope());

        // 获取角色的菜单权限
        List<Menu> roleMenus = getRoleMenus(roleId);
        PermissionSet rolePermissions = PermissionSet.fromMenus(roleMenus);
        
        // 添加角色的数据权限
        Set<DataScope> dataScopes = new HashSet<>();
        Set<Long> deptIds = new HashSet<>();
        
        if (role.getDataScope() != null) {
            DataScope dataScope = DataScope.fromValue(role.getDataScope());
            if (dataScope != null) {
                dataScopes.add(dataScope);
                
                // 如果是自定义数据权限，需要获取关联的部门ID
                if (dataScope == DataScope.CUSTOM) {
                    try {
                        List<Long> roleDeptIds = roleDeptRepository.findDeptIdsByRoleId(roleId);
                        deptIds.addAll(roleDeptIds);
                        log.debug("角色{}的自定义数据权限包含部门: {}", roleId, roleDeptIds);
                    } catch (Exception e) {
                        log.warn("获取角色{}的自定义数据权限部门失败: {}", roleId, e.getMessage());
                    }
                }
            }
        }
        
        // 创建包含数据权限的权限集合
        PermissionSet rolePermissionsWithData = new PermissionSet(
            rolePermissions.getMenuIds(),
            rolePermissions.getPermissions(),
            dataScopes,
            deptIds
        );
        
        // 应用权限继承
        PermissionSet inheritedPermissions = applyPermissionInheritance(rolePermissionsWithData, InheritanceStrategy.ADDITIVE);
        
        log.debug("角色权限计算完成: roleId={}, permissions={}", roleId, inheritedPermissions);
        return inheritedPermissions;
    }

    /**
     * 合并多个角色的权限
     *
     * @param roles    角色列表
     * @param strategy 合并策略
     * @return 合并后的权限集合
     */
    public PermissionSet mergeRolePermissions(List<Role> roles, MergeStrategy strategy) {
        return mergeRolePermissions(roles, strategy, PermissionScope.ALL);
    }

    /**
     * 合并多个角色的权限
     *
     * @param roles 角色列表
     * @param strategy 合并策略
     * @param scope 权限范围
     * @return 合并后的权限集合
     */
    public PermissionSet mergeRolePermissions(List<Role> roles, MergeStrategy strategy, PermissionScope scope) {
        if (CollectionUtils.isEmpty(roles)) {
            return PermissionSet.empty();
        }

        List<PermissionSet> permissionSets = roles.stream()
                .filter(Role::isNormal)
                .map(role -> calculateRolePermissions(role.getId(), scope))
                .filter(result -> !result.isEmpty())
                .collect(Collectors.toList());

        return mergePermissionSets(permissionSets, strategy);
    }

    /**
     * 合并多个权限集合
     *
     * @param permissionSets 权限集合列表
     * @param strategy       合并策略
     * @return 合并后的权限集合
     */
    public PermissionSet mergePermissionSets(List<PermissionSet> permissionSets, MergeStrategy strategy) {
        if (CollectionUtils.isEmpty(permissionSets)) {
            return PermissionSet.empty();
        }

        if (permissionSets.size() == 1) {
            return permissionSets.get(0);
        }

        PermissionSet result = permissionSets.get(0);
        for (int i = 1; i < permissionSets.size(); i++) {
            result = mergeTwo(result, permissionSets.get(i), strategy);
        }

        return result;
    }

    /**
     * 应用权限继承
     *
     * @param permissions 原始权限集合
     * @param strategy    继承策略
     * @return 应用继承后的权限集合
     */
    public PermissionSet applyPermissionInheritance(PermissionSet permissions, InheritanceStrategy strategy) {
        if (permissions.isEmpty()) {
            return permissions;
        }

        Set<Long> inheritedMenuIds = new HashSet<>(permissions.getMenuIds());
        Set<String> inheritedPermissions = new HashSet<>(permissions.getPermissions());

        // 对每个菜单应用继承策略
        for (Long menuId : permissions.getMenuIds()) {
            Set<Long> parentMenuIds = getParentMenuIds(menuId);
            Set<Long> childMenuIds = getChildMenuIds(menuId);

            switch (strategy) {
                case ADDITIVE:
                    // 累加继承：包含父菜单和子菜单的权限
                    inheritedMenuIds.addAll(parentMenuIds);
                    inheritedMenuIds.addAll(childMenuIds);
                    break;
                case OVERRIDE:
                    // 覆盖继承：只保留当前菜单权限
                    break;
                case INTERSECTION:
                    // 交集继承：只保留与父菜单的交集
                    inheritedMenuIds.retainAll(parentMenuIds);
                    break;
            }
        }

        // 根据菜单ID获取对应的权限标识
        Set<String> menuPermissions = getPermissionsByMenuIds(inheritedMenuIds);
        inheritedPermissions.addAll(menuPermissions);

        return new PermissionSet(inheritedMenuIds, inheritedPermissions);
    }

    /**
     * 获取用户的所有角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    private List<Role> getUserRoles(Long userId) {
        return roleRepository.findByUserId(userId).stream()
                .filter(Role::isNormal)
                .collect(Collectors.toList());
    }

    /**
     * 获取角色的所有菜单
     *
     * @param roleId 角色ID
     * @return 菜单列表
     */
    private List<Menu> getRoleMenus(Long roleId) {
        return menuRepository.findByRoleId(roleId).stream()
                .filter(menu -> menu.getDelFlag() == 0 && Integer.valueOf(0).equals(menu.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 验证用户是否拥有指定权限
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    public boolean hasPermission(Long userId, String permission) {
        if (userId == null || !StringUtils.hasText(permission)) {
            return false;
        }
        
        PermissionSet userPermissions = calculateUserPermissions(userId);
        return userPermissions.hasPermission(permission);
    }

    /**
     * 验证用户是否拥有指定菜单权限
     *
     * @param userId 用户ID
     * @param menuId 菜单ID
     * @return 是否拥有菜单权限
     */
    public boolean hasMenuPermission(Long userId, Long menuId) {
        if (userId == null || menuId == null) {
            return false;
        }
        
        PermissionSet userPermissions = calculateUserPermissions(userId);
        return userPermissions.hasMenuId(menuId);
    }

    /**
     * 验证用户是否拥有任一权限
     *
     * @param userId 用户ID
     * @param permissions 权限标识列表
     * @return 是否拥有任一权限
     */
    public boolean hasAnyPermission(Long userId, String... permissions) {
        if (userId == null || permissions == null || permissions.length == 0) {
            return false;
        }
        
        PermissionSet userPermissions = calculateUserPermissions(userId);
        return Arrays.stream(permissions)
                .anyMatch(userPermissions::hasPermission);
    }

    /**
     * 验证用户是否拥有所有权限
     *
     * @param userId 用户ID
     * @param permissions 权限标识列表
     * @return 是否拥有所有权限
     */
    public boolean hasAllPermissions(Long userId, String... permissions) {
        if (userId == null || permissions == null || permissions.length == 0) {
            return false;
        }
        
        PermissionSet userPermissions = calculateUserPermissions(userId);
        return Arrays.stream(permissions)
                .allMatch(userPermissions::hasPermission);
    }

    /**
     * 获取父菜单ID集合
     *
     * @param menuId 菜单ID
     * @return 父菜单ID集合
     */
    private Set<Long> getParentMenuIds(Long menuId) {
        Set<Long> parentIds = new HashSet<>();
        Optional<Menu> menuOpt = menuRepository.findById(menuId);
        
        if (menuOpt.isPresent()) {
            Menu menu = menuOpt.get();
            Long parentId = menu.getParentId();
            
            while (parentId != null && parentId != 0) {
                parentIds.add(parentId);
                Optional<Menu> parentMenuOpt = menuRepository.findById(parentId);
                if (parentMenuOpt.isPresent()) {
                    parentId = parentMenuOpt.get().getParentId();
                } else {
                    break;
                }
            }
        }
        
        return parentIds;
    }

    /**
     * 获取角色的所有子角色ID（递归）
     * 注意：当前角色实体不支持层级结构，此方法返回空集合
     */
    private Set<Long> getChildRoleIds(Long roleId) {
        // 当前角色实体不支持父子关系，返回空集合
        return new HashSet<>();
    }
    
    /**
     * 获取子菜单ID集合
     *
     * @param menuId 菜单ID
     * @return 子菜单ID集合
     */
    private Set<Long> getChildMenuIds(Long menuId) {
        Set<Long> childIds = new HashSet<>();
        List<Menu> children = menuRepository.findByParentIdAndDelFlagOrderByOrderNumAsc(menuId, 0);
        
        for (Menu child : children) {
            childIds.add(child.getId());
            // 递归获取子菜单的子菜单
            childIds.addAll(getChildMenuIds(child.getId()));
        }
        
        return childIds;
    }

    /**
     * 根据菜单ID集合获取权限标识集合
     *
     * @param menuIds 菜单ID集合
     * @return 权限标识集合
     */
    private Set<String> getPermissionsByMenuIds(Set<Long> menuIds) {
        if (CollectionUtils.isEmpty(menuIds)) {
            return Collections.emptySet();
        }
        
        List<Menu> menus = menuRepository.findAllById(menuIds);
        return menus.stream()
                .filter(menu -> menu.getPerms() != null && !menu.getPerms().trim().isEmpty())
                .map(Menu::getPerms)
                .collect(Collectors.toSet());
    }

    /**
     * 合并两个权限集合
     *
     * @param set1     权限集合1
     * @param set2     权限集合2
     * @param strategy 合并策略
     * @return 合并后的权限集合
     */
    private PermissionSet mergeTwo(PermissionSet set1, PermissionSet set2, MergeStrategy strategy) {
        Set<Long> menuIds = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        Set<DataScope> dataScopes = new HashSet<>();
        Set<Long> deptIds = new HashSet<>();

        switch (strategy) {
            case UNION:
                // 并集合并
                menuIds.addAll(set1.getMenuIds());
                menuIds.addAll(set2.getMenuIds());
                permissions.addAll(set1.getPermissions());
                permissions.addAll(set2.getPermissions());
                dataScopes.addAll(set1.getDataScopes());
                dataScopes.addAll(set2.getDataScopes());
                deptIds.addAll(set1.getDeptIds());
                deptIds.addAll(set2.getDeptIds());
                break;
            case INTERSECTION:
                // 交集合并
                menuIds.addAll(set1.getMenuIds());
                menuIds.retainAll(set2.getMenuIds());
                permissions.addAll(set1.getPermissions());
                permissions.retainAll(set2.getPermissions());
                dataScopes.addAll(set1.getDataScopes());
                dataScopes.retainAll(set2.getDataScopes());
                deptIds.addAll(set1.getDeptIds());
                deptIds.retainAll(set2.getDeptIds());
                break;
            case DIFFERENCE:
                // 差集合并（set1 - set2）
                menuIds.addAll(set1.getMenuIds());
                menuIds.removeAll(set2.getMenuIds());
                permissions.addAll(set1.getPermissions());
                permissions.removeAll(set2.getPermissions());
                dataScopes.addAll(set1.getDataScopes());
                dataScopes.removeAll(set2.getDataScopes());
                deptIds.addAll(set1.getDeptIds());
                deptIds.removeAll(set2.getDeptIds());
                break;
        }

        return new PermissionSet(menuIds, permissions, dataScopes, deptIds);
    }

    /**
     * 权限集合类
     */
    public static class PermissionSet {
        private final Set<Long> menuIds;
        private final Set<String> permissions;
        private final Set<DataScope> dataScopes;
        private final Set<Long> deptIds; // 自定义数据权限的部门ID

        public PermissionSet(Set<Long> menuIds, Set<String> permissions) {
            this(menuIds, permissions, new HashSet<>(), new HashSet<>());
        }

        public PermissionSet(Set<Long> menuIds, Set<String> permissions, Set<DataScope> dataScopes, Set<Long> deptIds) {
            this.menuIds = menuIds != null ? new HashSet<>(menuIds) : new HashSet<>();
            this.permissions = permissions != null ? new HashSet<>(permissions) : new HashSet<>();
            this.dataScopes = dataScopes != null ? new HashSet<>(dataScopes) : new HashSet<>();
            this.deptIds = deptIds != null ? new HashSet<>(deptIds) : new HashSet<>();
        }

        public static PermissionSet empty() {
            return new PermissionSet(Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        }

        public static PermissionSet fromMenus(List<Menu> menus) {
            if (CollectionUtils.isEmpty(menus)) {
                return empty();
            }

            Set<Long> menuIds = menus.stream()
                    .map(Menu::getId)
                    .collect(Collectors.toSet());

            Set<String> permissions = menus.stream()
                    .filter(menu -> menu.getPerms() != null && !menu.getPerms().trim().isEmpty())
                    .map(Menu::getPerms)
                    .collect(Collectors.toSet());

            return new PermissionSet(menuIds, permissions);
        }

        public Set<Long> getMenuIds() {
            return new HashSet<>(menuIds);
        }

        public Set<String> getPermissions() {
            return new HashSet<>(permissions);
        }

        public Set<DataScope> getDataScopes() {
            return new HashSet<>(dataScopes);
        }

        public Set<Long> getDeptIds() {
            return new HashSet<>(deptIds);
        }

        public boolean isEmpty() {
            return menuIds.isEmpty() && permissions.isEmpty() && dataScopes.isEmpty() && deptIds.isEmpty();
        }

        public boolean hasMenuId(Long menuId) {
            return menuIds.contains(menuId);
        }

        public boolean hasPermission(String permission) {
            return permissions.contains(permission);
        }

        public boolean hasDataScope(DataScope dataScope) {
            return dataScopes.contains(dataScope);
        }

        public boolean hasDeptId(Long deptId) {
            return deptIds.contains(deptId);
        }

        @Override
        public String toString() {
            return "PermissionSet{" +
                    "menuIds=" + menuIds +
                    ", permissions=" + permissions +
                    ", dataScopes=" + dataScopes +
                    ", deptIds=" + deptIds +
                    '}';
        }
    }
}