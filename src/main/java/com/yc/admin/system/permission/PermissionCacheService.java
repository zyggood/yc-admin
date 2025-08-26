package com.yc.admin.system.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限缓存服务
 * 提供权限信息的缓存管理功能
 *
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PermissionService permissionService;

    // 缓存键前缀
    private static final String USER_PERMISSIONS_KEY = "user:permissions:";
    private static final String USER_ROLES_KEY = "user:roles:";
    private static final String USER_MENUS_KEY = "user:menus:";
    private static final String ROLE_PERMISSIONS_KEY = "role:permissions:";
    private static final String ROLE_MENUS_KEY = "role:menus:";
    
    // 缓存过期时间（秒）
    private static final long CACHE_EXPIRE_TIME = 3600; // 1小时

    /**
     * 获取用户权限列表（带缓存）
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    @Cacheable(value = "userPermissions", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public Set<String> getUserPermissions(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }

        String cacheKey = USER_PERMISSIONS_KEY + userId;
        
        // 先从Redis缓存获取
        @SuppressWarnings("unchecked")
        Set<String> cachedPermissions = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPermissions != null) {
            log.debug("从缓存获取用户权限: userId={}, permissions={}", userId, cachedPermissions);
            return cachedPermissions;
        }

        // 缓存未命中，从数据库查询
        Set<String> permissions = loadUserPermissionsFromDb(userId);
        
        // 存入缓存
        if (!CollectionUtils.isEmpty(permissions)) {
            redisTemplate.opsForValue().set(cacheKey, permissions, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            log.debug("缓存用户权限: userId={}, permissions={}", userId, permissions);
        }
        
        return permissions;
    }

    /**
     * 获取用户角色列表（带缓存）
     *
     * @param userId 用户ID
     * @return 角色权限字符串列表
     */
    @Cacheable(value = "userRoles", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public Set<String> getUserRoles(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }

        String cacheKey = USER_ROLES_KEY + userId;
        
        // 先从Redis缓存获取
        @SuppressWarnings("unchecked")
        Set<String> cachedRoles = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedRoles != null) {
            log.debug("从缓存获取用户角色: userId={}, roles={}", userId, cachedRoles);
            return cachedRoles;
        }

        // 缓存未命中，从数据库查询
        Set<String> roles = loadUserRolesFromDb(userId);
        
        // 存入缓存
        if (!CollectionUtils.isEmpty(roles)) {
            redisTemplate.opsForValue().set(cacheKey, roles, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            log.debug("缓存用户角色: userId={}, roles={}", userId, roles);
        }
        
        return roles;
    }

    /**
     * 获取用户菜单ID列表（带缓存）
     *
     * @param userId 用户ID
     * @return 菜单ID列表
     */
    @Cacheable(value = "userMenus", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public Set<Long> getUserMenuIds(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }

        String cacheKey = USER_MENUS_KEY + userId;
        
        // 先从Redis缓存获取
        @SuppressWarnings("unchecked")
        Set<Long> cachedMenuIds = (Set<Long>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedMenuIds != null) {
            log.debug("从缓存获取用户菜单: userId={}, menuIds={}", userId, cachedMenuIds);
            return cachedMenuIds;
        }

        // 缓存未命中，从数据库查询
        List<Long> menuIds = permissionService.getMenuIdsByUserId(userId);
        Set<Long> menuIdSet = new HashSet<>(menuIds);
        
        // 存入缓存
        if (!CollectionUtils.isEmpty(menuIdSet)) {
            redisTemplate.opsForValue().set(cacheKey, menuIdSet, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            log.debug("缓存用户菜单: userId={}, menuIds={}", userId, menuIdSet);
        }
        
        return menuIdSet;
    }

    /**
     * 清除用户权限缓存
     *
     * @param userId 用户ID
     */
    @CacheEvict(value = {"userPermissions", "userRoles", "userMenus"}, key = "#userId")
    public void evictUserCache(Long userId) {
        if (userId == null) {
            return;
        }
        
        // 清除Redis缓存
        redisTemplate.delete(USER_PERMISSIONS_KEY + userId);
        redisTemplate.delete(USER_ROLES_KEY + userId);
        redisTemplate.delete(USER_MENUS_KEY + userId);
        
        log.info("清除用户权限缓存: userId={}", userId);
    }

    /**
     * 清除角色权限缓存
     *
     * @param roleId 角色ID
     */
    public void evictRoleCache(Long roleId) {
        if (roleId == null) {
            return;
        }
        
        // 清除Redis缓存
        redisTemplate.delete(ROLE_PERMISSIONS_KEY + roleId);
        redisTemplate.delete(ROLE_MENUS_KEY + roleId);
        
        log.info("清除角色权限缓存: roleId={}", roleId);
    }

    /**
     * 清除所有权限缓存
     */
    @CacheEvict(value = {"userPermissions", "userRoles", "userMenus"}, allEntries = true)
    public void evictAllCache() {
        // 清除Redis中的权限相关缓存
        Set<String> keys = redisTemplate.keys(USER_PERMISSIONS_KEY + "*");
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
        
        keys = redisTemplate.keys(USER_ROLES_KEY + "*");
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
        
        keys = redisTemplate.keys(USER_MENUS_KEY + "*");
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
        
        keys = redisTemplate.keys(ROLE_PERMISSIONS_KEY + "*");
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
        
        keys = redisTemplate.keys(ROLE_MENUS_KEY + "*");
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
        
        log.info("清除所有权限缓存");
    }

    /**
     * 检查用户是否有指定权限（带缓存）
     *
     * @param userId     用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    public boolean hasPermission(Long userId, String permission) {
        if (userId == null || permission == null || permission.trim().isEmpty()) {
            return false;
        }
        
        Set<String> userPermissions = getUserPermissions(userId);
        return userPermissions.contains(permission.trim());
    }

    /**
     * 检查用户是否有指定角色（带缓存）
     *
     * @param userId  用户ID
     * @param roleKey 角色权限字符串
     * @return 是否有角色
     */
    public boolean hasRole(Long userId, String roleKey) {
        if (userId == null || roleKey == null || roleKey.trim().isEmpty()) {
            return false;
        }
        
        Set<String> userRoles = getUserRoles(userId);
        return userRoles.contains(roleKey.trim());
    }

    /**
     * 检查用户是否有指定菜单权限（带缓存）
     *
     * @param userId 用户ID
     * @param menuId 菜单ID
     * @return 是否有权限
     */
    public boolean hasMenuPermission(Long userId, Long menuId) {
        if (userId == null || menuId == null) {
            return false;
        }
        
        Set<Long> userMenuIds = getUserMenuIds(userId);
        return userMenuIds.contains(menuId);
    }

    /**
     * 从数据库加载用户权限
     *
     * @param userId 用户ID
     * @return 权限标识集合
     */
    private Set<String> loadUserPermissionsFromDb(Long userId) {
        // 通过用户角色关联查询所有权限标识
        List<String> permissions = permissionService.getPermissionsByUserId(userId);
        return permissions.stream()
                .filter(perm -> perm != null && !perm.trim().isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * 从数据库加载用户角色
     *
     * @param userId 用户ID
     * @return 角色权限字符串集合
     */
    private Set<String> loadUserRolesFromDb(Long userId) {
        // 从数据库查询用户角色
        List<String> roles = permissionService.getRolesByUserId(userId);
        return roles.stream()
                .filter(role -> role != null && !role.trim().isEmpty())
                .collect(Collectors.toSet());
    }
}