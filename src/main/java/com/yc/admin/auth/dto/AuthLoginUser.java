package com.yc.admin.auth.dto;

import com.yc.admin.system.api.dto.AuthUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
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
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 合并权限和角色
        Set<String> authorities = permissions;
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
    public String getPassword() {
        return user != null ? user.getPassword() : null;
    }
    
    @Override
    public String getUsername() {
        return user != null ? user.getUserName() : null;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return user != null && user.isEnabled();
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
    public String getNickName() {
        return user != null ? user.getNickName() : null;
    }
}