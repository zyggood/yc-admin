package com.yc.admin.system.api.impl;

import com.yc.admin.system.api.dto.AuthRoleDTO;
import com.yc.admin.system.api.RoleApiService;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色API服务实现类
 * 将角色服务的功能暴露给其他模块
 *
 * @author yc
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
public class RoleApiServiceImpl implements RoleApiService {
    
    private final RoleService roleService;
    
    @Override
    public List<Role> findByUserId(Long userId) {
        return roleService.findByUserId(userId);
    }
    
    @Override
    public List<AuthRoleDTO> findAuthRolesByUserId(Long userId) {
        List<Role> roles = roleService.findByUserId(userId);
        return roles.stream()
                .map(this::convertToAuthRoleDTO)
                .collect(Collectors.toList());
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
}