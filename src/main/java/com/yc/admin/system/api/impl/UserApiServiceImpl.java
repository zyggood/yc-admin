package com.yc.admin.system.api.impl;

import com.yc.admin.system.api.dto.AuthRoleDTO;
import com.yc.admin.system.api.dto.AuthUserDTO;
import com.yc.admin.system.api.UserApiService;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.service.RoleService;
import com.yc.admin.system.user.dto.UserDTO;
import com.yc.admin.system.user.entity.User;
import com.yc.admin.system.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户API服务实现类
 * 将用户服务的功能暴露给其他模块
 *
 * @author yc
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
public class UserApiServiceImpl implements UserApiService {
    
    private final UserService userService;
    private final RoleService roleService;
    
    @Override
    public UserDTO findById(Long userId) {
        return userService.findById(userId);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return userService.findByUsername(username);
    }


    @Override
    public Optional<User> findEntityById(Long userId) {
        return userService.findEntityById(userId);
    }
    
    @Override
    public Optional<AuthUserDTO> findAuthUserByUsername(String username) {
        return userService.findByUsername(username)
                .map(this::convertToAuthUserDTO);
    }
    
    @Override
    public Optional<AuthUserDTO> findAuthUserById(Long userId) {
        return userService.findEntityById(userId)
                .map(this::convertToAuthUserDTO);
    }
    
    @Override
    public List<AuthRoleDTO> findAuthRolesByUserId(Long userId) {
        List<Role> roles = roleService.findByUserId(userId);
        return roles.stream()
                .map(this::convertToAuthRoleDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将User实体转换为AuthUserDTO
     */
    private AuthUserDTO convertToAuthUserDTO(User user) {
        return AuthUserDTO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .password(user.getPassword())
                .status(user.getStatus())
                .nickName(user.getNickName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .build();
    }
    
    /**
     * 将Role实体转换为AuthRoleDTO
     */
    private AuthRoleDTO convertToAuthRoleDTO(Role role) {
        return AuthRoleDTO.builder()
                .id(role.getId())
                .roleKey(role.getRoleKey())
                .roleName(role.getRoleName())
                .status(role.getStatus())
                .build();
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userService.findByEmail(email);
    }
    
    @Override
    public Optional<User> findByUserName(String userName) {
        return userService.findByUsername(userName);
    }
}