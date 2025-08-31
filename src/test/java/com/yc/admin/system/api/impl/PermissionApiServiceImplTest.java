package com.yc.admin.system.api.impl;

import com.yc.admin.system.permission.PermissionInheritanceService;
import com.yc.admin.system.permission.PermissionService;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * PermissionApiServiceImpl 测试类
 * 测试基于角色的权限继承判断逻辑
 *
 * @author YC
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("权限API服务实现测试")
class PermissionApiServiceImplTest {

    @Mock
    private PermissionService permissionService;

    @Mock
    private PermissionInheritanceService permissionInheritanceService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private PermissionApiServiceImpl permissionApiService;

    private Long adminUserId;
    private Long normalUserId;
    private Role adminRole;
    private Role normalRole;

    @BeforeEach
    void setUp() {
        adminUserId = 1L;
        normalUserId = 2L;

        // 创建启用权限继承的角色
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleKey("admin");
        adminRole.setRoleName("超级管理员");
        adminRole.enableInheritance(); // 启用权限继承

        // 创建普通角色（不启用权限继承）
        normalRole = new Role();
        normalRole.setId(2L);
        normalRole.setRoleKey("user");
        normalRole.setRoleName("普通用户");
        normalRole.disableInheritance(); // 不启用权限继承
    }

    @Test
    @DisplayName("计算用户权限 - 启用权限继承的角色使用权限继承服务")
    void testCalculateUserPermissions_InheritanceEnabledRole() {
        // Given
        when(roleService.findByUserId(adminUserId)).thenReturn(List.of(adminRole));
        
        PermissionInheritanceService.PermissionSet mockPermissionSet = 
            mock(PermissionInheritanceService.PermissionSet.class);
        when(mockPermissionSet.getPermissions()).thenReturn(Set.of("system:user:list", "system:role:list"));
        when(permissionInheritanceService.calculateUserPermissions(adminUserId))
            .thenReturn(mockPermissionSet);

        // When
        Set<String> permissions = permissionApiService.calculateUserPermissions(adminUserId);

        // Then
        assertThat(permissions).containsExactlyInAnyOrder("system:user:list", "system:role:list");
        verify(roleService).findByUserId(adminUserId);
        verify(permissionInheritanceService).calculateUserPermissions(adminUserId);
        verify(permissionService, never()).getPermissionsByUserId(adminUserId);
    }

    @Test
    @DisplayName("计算用户权限 - 未启用权限继承的角色使用基础权限服务")
    void testCalculateUserPermissions_InheritanceDisabledRole() {
        // Given
        when(roleService.findByUserId(normalUserId)).thenReturn(List.of(normalRole));
        when(permissionService.getPermissionsByUserId(normalUserId))
            .thenReturn(List.of("system:user:query"));

        // When
        Set<String> permissions = permissionApiService.calculateUserPermissions(normalUserId);

        // Then
        assertThat(permissions).containsExactly("system:user:query");
        verify(roleService).findByUserId(normalUserId);
        verify(permissionService).getPermissionsByUserId(normalUserId);
        verify(permissionInheritanceService, never()).calculateUserPermissions(normalUserId);
    }

    @Test
    @DisplayName("检查权限 - 启用权限继承的角色使用权限继承服务")
    void testHasPermission_InheritanceEnabledRole() {
        // Given
        String permission = "system:user:create";
        when(roleService.findByUserId(adminUserId)).thenReturn(List.of(adminRole));
        when(permissionInheritanceService.hasPermission(adminUserId, permission)).thenReturn(true);

        // When
        boolean hasPermission = permissionApiService.hasPermission(adminUserId, permission);

        // Then
        assertThat(hasPermission).isTrue();
        verify(roleService).findByUserId(adminUserId);
        verify(permissionInheritanceService).hasPermission(adminUserId, permission);
        verify(permissionService, never()).hasPermission(adminUserId, permission);
    }

    @Test
    @DisplayName("检查权限 - 未启用权限继承的角色使用基础权限服务")
    void testHasPermission_InheritanceDisabledRole() {
        // Given
        String permission = "system:user:query";
        when(roleService.findByUserId(normalUserId)).thenReturn(List.of(normalRole));
        when(permissionService.hasPermission(normalUserId, permission)).thenReturn(true);

        // When
        boolean hasPermission = permissionApiService.hasPermission(normalUserId, permission);

        // Then
        assertThat(hasPermission).isTrue();
        verify(roleService).findByUserId(normalUserId);
        verify(permissionService).hasPermission(normalUserId, permission);
        verify(permissionInheritanceService, never()).hasPermission(normalUserId, permission);
    }

    @Test
    @DisplayName("检查菜单权限 - 启用权限继承的角色使用权限继承服务")
    void testHasMenuPermission_InheritanceEnabledRole() {
        // Given
        Long menuId = 1L;
        when(roleService.findByUserId(adminUserId)).thenReturn(List.of(adminRole));
        when(permissionInheritanceService.hasMenuPermission(adminUserId, menuId)).thenReturn(true);

        // When
        boolean hasMenuPermission = permissionApiService.hasMenuPermission(adminUserId, menuId);

        // Then
        assertThat(hasMenuPermission).isTrue();
        verify(roleService).findByUserId(adminUserId);
        verify(permissionInheritanceService).hasMenuPermission(adminUserId, menuId);
        verify(permissionService, never()).hasMenuPermission(adminUserId, menuId);
    }

    @Test
    @DisplayName("检查菜单权限 - 未启用权限继承的角色使用基础权限服务")
    void testHasMenuPermission_InheritanceDisabledRole() {
        // Given
        Long menuId = 1L;
        when(roleService.findByUserId(normalUserId)).thenReturn(List.of(normalRole));
        when(permissionService.hasMenuPermission(normalUserId, menuId)).thenReturn(true);

        // When
        boolean hasMenuPermission = permissionApiService.hasMenuPermission(normalUserId, menuId);

        // Then
        assertThat(hasMenuPermission).isTrue();
        verify(roleService).findByUserId(normalUserId);
        verify(permissionService).hasMenuPermission(normalUserId, menuId);
        verify(permissionInheritanceService, never()).hasMenuPermission(normalUserId, menuId);
    }

    @Test
    @DisplayName("用户拥有多个角色包含启用权限继承的角色")
    void testMultipleRoles_WithInheritanceEnabledRole() {
        // Given
        when(roleService.findByUserId(adminUserId)).thenReturn(List.of(adminRole, normalRole));
        
        PermissionInheritanceService.PermissionSet mockPermissionSet = 
            mock(PermissionInheritanceService.PermissionSet.class);
        when(mockPermissionSet.getPermissions()).thenReturn(Set.of("system:admin:all"));
        when(permissionInheritanceService.calculateUserPermissions(adminUserId))
            .thenReturn(mockPermissionSet);

        // When
        Set<String> permissions = permissionApiService.calculateUserPermissions(adminUserId);

        // Then
        assertThat(permissions).containsExactly("system:admin:all");
        verify(roleService).findByUserId(adminUserId);
        verify(permissionInheritanceService).calculateUserPermissions(adminUserId);
        verify(permissionService, never()).getPermissionsByUserId(adminUserId);
    }

    @Test
    @DisplayName("用户ID为null时返回空权限集合")
    void testCalculateUserPermissions_NullUserId() {
        // When
        Set<String> permissions = permissionApiService.calculateUserPermissions(null);

        // Then
        assertThat(permissions).isEmpty();
        verify(roleService, never()).findByUserId(any());
        verify(permissionService, never()).getPermissionsByUserId(any());
        verify(permissionInheritanceService, never()).calculateUserPermissions(any());
    }

    @Test
    @DisplayName("角色服务异常时使用基础权限服务")
    void testCalculateUserPermissions_RoleServiceException() {
        // Given
        when(roleService.findByUserId(normalUserId)).thenThrow(new RuntimeException("角色查询失败"));
        when(permissionService.getPermissionsByUserId(normalUserId))
            .thenReturn(List.of("system:user:query"));

        // When
        Set<String> permissions = permissionApiService.calculateUserPermissions(normalUserId);

        // Then
        assertThat(permissions).containsExactly("system:user:query");
        verify(roleService).findByUserId(normalUserId);
        verify(permissionService).getPermissionsByUserId(normalUserId);
    }

    @Test
    @DisplayName("混合角色场景 - 部分角色启用权限继承")
    void testMixedRoles_PartialInheritanceEnabled() {
        // Given
        Role inheritanceRole = new Role();
        inheritanceRole.setId(3L);
        inheritanceRole.setRoleKey("manager");
        inheritanceRole.setRoleName("管理员");
        inheritanceRole.enableInheritance(); // 启用权限继承
        
        Long mixedUserId = 3L;
        when(roleService.findByUserId(mixedUserId)).thenReturn(List.of(inheritanceRole, normalRole));
        
        PermissionInheritanceService.PermissionSet mockPermissionSet = 
            mock(PermissionInheritanceService.PermissionSet.class);
        when(mockPermissionSet.getPermissions()).thenReturn(Set.of("system:manager:all"));
        when(permissionInheritanceService.calculateUserPermissions(mixedUserId))
            .thenReturn(mockPermissionSet);

        // When
        Set<String> permissions = permissionApiService.calculateUserPermissions(mixedUserId);

        // Then
        assertThat(permissions).containsExactly("system:manager:all");
        verify(roleService).findByUserId(mixedUserId);
        verify(permissionInheritanceService).calculateUserPermissions(mixedUserId);
        verify(permissionService, never()).getPermissionsByUserId(mixedUserId);
    }
}