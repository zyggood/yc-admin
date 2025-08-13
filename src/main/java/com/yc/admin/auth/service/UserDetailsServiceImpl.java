package com.yc.admin.auth.service;

import com.yc.admin.system.api.MenuApiService;
import com.yc.admin.system.api.RoleApiService;
import com.yc.admin.system.api.UserApiService;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.user.entity.LoginUser;
import com.yc.admin.system.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security 用户详情服务实现
 * 负责根据用户名加载用户信息、角色和权限
 *
 * @author yc
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserApiService userApiService;
    private final RoleApiService roleApiService;
    private final MenuApiService menuApiService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("加载用户信息: {}", username);
        
        // 查询用户信息
        User user = userApiService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        // 检查用户状态
        if (user.getStatus() == User.Status.DISABLED) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        
        // 构建登录用户信息
        return createLoginUser(user);
    }

    /**
     * 创建登录用户信息
     * 
     * @param user 用户实体
     * @return 登录用户信息
     */
    private LoginUser createLoginUser(User user) {
        // 获取用户权限
        Set<String> permissions = getUserPermissions(user.getId());
        
        // 获取用户角色
        Set<String> roles = getUserRoles(user.getId());
        
        // 创建登录用户对象
        LoginUser loginUser = new LoginUser();
        loginUser.setUser(user);
        loginUser.setPermissions(permissions);
        loginUser.setRoles(roles);
        
        log.debug("用户 {} 权限加载完成，权限数量: {}, 角色数量: {}", 
                user.getUserName(), permissions.size(), roles.size());
        
        return loginUser;
    }

    /**
     * 获取用户权限
     * 
     * @param userId 用户ID
     * @return 权限集合
     */
    private Set<String> getUserPermissions(Long userId) {
        Set<String> permissions = new HashSet<>();
        
        try {
            // 通过用户ID获取菜单权限
            List<String> menuPermissions = menuApiService.findPermissionsByUserId(userId);
            if (!CollectionUtils.isEmpty(menuPermissions)) {
                permissions.addAll(menuPermissions);
            }
            
            log.debug("用户 {} 菜单权限: {}", userId, menuPermissions);
            
        } catch (Exception e) {
            log.warn("获取用户 {} 权限失败: {}", userId, e.getMessage());
        }
        
        return permissions;
    }

    /**
     * 获取用户角色
     * 
     * @param userId 用户ID
     * @return 角色集合
     */
    private Set<String> getUserRoles(Long userId) {
        Set<String> roles = new HashSet<>();
        
        try {
            // 通过用户ID获取角色
            List<Role> userRoleList = roleApiService.findByUserId(userId);
            if (!CollectionUtils.isEmpty(userRoleList)) {
                List<String> userRoles = userRoleList.stream()
                    .map(Role::getRoleKey)
                    .collect(Collectors.toList());
                roles.addAll(userRoles);
            }
            
            log.debug("用户 {} 角色: {}", userId, roles);
            
        } catch (Exception e) {
            log.warn("获取用户 {} 角色失败: {}", userId, e.getMessage());
        }
        
        return roles;
    }

    /**
     * 根据用户ID重新加载用户信息
     * 
     * @param userId 用户ID
     * @return 登录用户信息
     */
    public LoginUser loadUserByUserId(Long userId) {
        log.debug("根据用户ID加载用户信息: {}", userId);
        
        // 查询用户信息
        User user = userApiService.findEntityById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + userId));
        
        // 检查用户状态
        if (user.getStatus() == User.Status.DISABLED) {
            throw new UsernameNotFoundException("用户已被禁用: " + userId);
        }
        
        // 构建登录用户信息
        return createLoginUser(user);
    }
}