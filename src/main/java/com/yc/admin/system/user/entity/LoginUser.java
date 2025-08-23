package com.yc.admin.system.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户信息
 * 实现 Spring Security 的 UserDetails 接口，用于认证和授权
 *
 * @author yc
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户信息
     */
    private User user;

    /**
     * 权限集合
     */
    private Set<String> permissions;

    /**
     * 角色集合
     */
    private Set<String> roles;

    /**
     * 令牌标识
     */
    private String token;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    public LoginUser(User user, Set<String> permissions, Set<String> roles) {
        this.user = user;
        this.permissions = permissions;
        this.roles = roles;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return user != null ? user.getPassword() : null;
    }

    @Override
    public String getUsername() {
        return user != null ? user.getUserName() : null;
    }

    /**
     * 账户是否未过期
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账户是否未锁定
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return user == null || !Objects.equals(user.getStatus(), User.Status.DISABLED);
    }

    /**
     * 凭证是否未过期
     */
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否启用
     */
    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return user != null && Objects.equals(user.getStatus(), User.Status.NORMAL);
    }

    /**
     * 获取用户权限
     */
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    /**
     * 获取用户昵称
     */
    public String getNickname() {
        return user != null ? user.getNickName() : null;
    }

    /**
     * 获取用户邮箱
     */
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

    /**
     * 获取用户手机号
     */
    public String getPhone() {
        return user != null ? user.getPhone() : null;
    }

    /**
     * 获取用户头像
     */
    public String getAvatar() {
        return user != null ? user.getAvatar() : null;
    }

    /**
     * 获取用户状态
     */
    public String getStatus() {
        return user != null ? user.getStatus() : null;
    }

    /**
     * 检查是否拥有指定权限
     * 
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 检查是否拥有指定角色
     * 
     * @param role 角色标识
     * @return 是否拥有角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * 检查是否为超级管理员
     * 
     * @return 是否为超级管理员
     */
    public boolean isAdmin() {
        return user != null && User.ADMIN_USER_ID.equals(user.getId());
    }
}