package com.yc.admin.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yc.admin.system.api.dto.AuthUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证登录用户
 * 实现Spring Security的UserDetails接口，用于auth模块内部的用户认证
 *
 * @author yc
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginUser implements UserDetails {
    
    /**
     * 用户信息
     */
    private AuthUserDTO user;
    
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
    
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 合并权限和角色
        Set<String> authorities = new HashSet<>();
        if (permissions != null) {
            authorities.addAll(permissions);
        }
        if (roles != null) {
            // 角色添加ROLE_前缀
            Set<String> roleAuthorities = roles.stream()
                    .map(role -> "ROLE_" + role)
                    .collect(Collectors.toSet());
            authorities.addAll(roleAuthorities);
        }
        
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
    
    @Override
    @JsonIgnore
    public String getPassword() {
        return user != null ? user.getPassword() : null;
    }
    
    @Override
    @JsonIgnore
    public String getUsername() {
        return user != null ? user.getUserName() : null;
    }
    
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true; //TODO: 
    }
    
    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return user != null && user.isEnabled();
    }
    
    /**
     * 获取用户ID
     */
    @JsonIgnore
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    /**
     * 获取用户昵称
     */
    @JsonIgnore
    public String getNickName() {
        return user != null ? user.getNickName() : null;
    }
    
    /**
     * 获取用户邮箱
     */
    @JsonIgnore
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }
    
    /**
     * 获取用户手机号
     */
    @JsonIgnore
    public String getPhone() {
        return user != null ? user.getPhone() : null;
    }
    
    /**
     * 获取用户头像
     */
    @JsonIgnore
    public String getAvatar() {
        return user != null ? user.getAvatar() : null;
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
    @JsonIgnore
    public boolean isAdmin() {
        return user != null && user.getId() != null && user.getId().equals(1L);
    }
}