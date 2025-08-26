package com.yc.admin.system.permission;

import com.yc.admin.system.menu.entity.Menu;
import com.yc.admin.system.menu.repository.MenuRepository;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.repository.RoleRepository;
import com.yc.admin.system.user.entity.User;
import com.yc.admin.system.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 权限继承服务测试类
 *
 * @author YC
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class PermissionInheritanceServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private PermissionInheritanceService permissionInheritanceService;

    private User testUser;
    private Role testRole1;
    private Role testRole2;
    private Menu testMenu1;
    private Menu testMenu2;
    private Menu testMenu3;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testuser");
        testUser.setStatus("0"); // 正常状态
        testUser.setDelFlag(0); // 未删除
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());

        // 创建测试角色1
        testRole1 = new Role();
        testRole1.setId(1L);
        testRole1.setRoleName("管理员");
        testRole1.setRoleKey("admin");
        testRole1.setStatus("0"); // 正常状态
        testRole1.setDelFlag(0); // 未删除
        testRole1.setCreateTime(LocalDateTime.now());
        testRole1.setUpdateTime(LocalDateTime.now());

        // 创建测试角色2
        testRole2 = new Role();
        testRole2.setId(2L);
        testRole2.setRoleName("普通用户");
        testRole2.setRoleKey("user");
        testRole2.setStatus("0"); // 正常状态
        testRole2.setDelFlag(0); // 未删除
        testRole2.setCreateTime(LocalDateTime.now());
        testRole2.setUpdateTime(LocalDateTime.now());

        // 创建测试菜单1
        testMenu1 = new Menu();
        testMenu1.setId(1L);
        testMenu1.setMenuName("系统管理");
        testMenu1.setPerms("system:manage");
        testMenu1.setMenuType("M"); // 目录
        testMenu1.setStatus(0); // 正常状态
        testMenu1.setDelFlag(0); // 未删除
        testMenu1.setCreateTime(LocalDateTime.now());
        testMenu1.setUpdateTime(LocalDateTime.now());

        // 创建测试菜单2
        testMenu2 = new Menu();
        testMenu2.setId(2L);
        testMenu2.setMenuName("用户管理");
        testMenu2.setPerms("system:user:list");
        testMenu2.setMenuType("C"); // 菜单
        testMenu2.setParentId(1L); // 父菜单为系统管理
        testMenu2.setStatus(0); // 正常状态
        testMenu2.setDelFlag(0); // 未删除
        testMenu2.setCreateTime(LocalDateTime.now());
        testMenu2.setUpdateTime(LocalDateTime.now());

        // 创建测试菜单3
        testMenu3 = new Menu();
        testMenu3.setId(3L);
        testMenu3.setMenuName("添加用户");
        testMenu3.setPerms("system:user:add");
        testMenu3.setMenuType("F"); // 按钮
        testMenu3.setParentId(2L); // 父菜单为用户管理
        testMenu3.setStatus(0); // 正常状态
        testMenu3.setDelFlag(0); // 未删除
        testMenu3.setCreateTime(LocalDateTime.now());
        testMenu3.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void testCalculateUserPermissions_ValidUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByUserId(1L)).thenReturn(List.of(testRole1, testRole2));
        when(menuRepository.findByRoleId(1L)).thenReturn(List.of(testMenu1, testMenu2, testMenu3));
        when(menuRepository.findByRoleId(2L)).thenReturn(List.of(testMenu2));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole1));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(testRole2));

        // When
        PermissionInheritanceService.PermissionSet result = permissionInheritanceService.calculateUserPermissions(1L);

        assertThat(result.getMenuIds()).as("Result menuIds should not be empty").isNotEmpty();
        
    }

    @Test
    void testCalculateUserPermissions_UserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        PermissionInheritanceService.PermissionSet result = permissionInheritanceService.calculateUserPermissions(999L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void testCalculateUserPermissions_InactiveUser() {
        // Given
        testUser.setStatus("1"); // 停用状态
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        PermissionInheritanceService.PermissionSet result = permissionInheritanceService.calculateUserPermissions(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void testCalculateUserPermissions_DeletedUser() {
        // Given
        testUser.setDelFlag(1); // 已删除
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        PermissionInheritanceService.PermissionSet result = permissionInheritanceService.calculateUserPermissions(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void testCalculateRolePermissions_ValidRole() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole1));
        when(menuRepository.findByRoleId(1L)).thenReturn(List.of(testMenu1, testMenu2));

        // When
        PermissionInheritanceService.PermissionSet result = permissionInheritanceService.calculateRolePermissions(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMenuIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(result.getPermissions()).containsExactlyInAnyOrder("system:manage", "system:user:list");
    }

    @Test
    void testCalculateRolePermissions_RoleNotFound() {
        // Given
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        PermissionInheritanceService.PermissionSet result = permissionInheritanceService.calculateRolePermissions(999L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void testHasPermission_UserHasPermission() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByUserId(1L)).thenReturn(List.of(testRole1));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole1));
        when(menuRepository.findByRoleId(1L)).thenReturn(List.of(testMenu1, testMenu2));

        // When
        boolean result = permissionInheritanceService.hasPermission(1L, "system:user:list");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testHasPermission_UserDoesNotHavePermission() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByUserId(1L)).thenReturn(List.of(testRole1));

        // When
        boolean result = permissionInheritanceService.hasPermission(1L, "system:user:add");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testHasMenuPermission_UserHasMenuPermission() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByUserId(1L)).thenReturn(List.of(testRole1));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole1));
        when(menuRepository.findByRoleId(1L)).thenReturn(List.of(testMenu1, testMenu2));

        // When
        boolean result = permissionInheritanceService.hasMenuPermission(1L, 2L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testHasMenuPermission_UserDoesNotHaveMenuPermission() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByUserId(1L)).thenReturn(List.of(testRole1));

        // When
        boolean result = permissionInheritanceService.hasMenuPermission(1L, 3L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testHasAnyPermission_UserHasOnePermission() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByUserId(1L)).thenReturn(List.of(testRole1));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole1));
        when(menuRepository.findByRoleId(1L)).thenReturn(List.of(testMenu1, testMenu2));

        // When
        boolean result = permissionInheritanceService.hasAnyPermission(1L, "system:user:add", "system:user:list");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testHasAnyPermission_UserHasNoPermissions() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByUserId(1L)).thenReturn(List.of(testRole1));

        // When
        boolean result = permissionInheritanceService.hasAnyPermission(1L, "system:user:add", "system:user:edit");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testHasAllPermissions_UserHasAllPermissions() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByUserId(1L)).thenReturn(List.of(testRole1));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole1));
        when(menuRepository.findByRoleId(1L)).thenReturn(List.of(testMenu1, testMenu2, testMenu3));

        // When
        boolean result = permissionInheritanceService.hasAllPermissions(1L, "system:manage", "system:user:list");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testHasAllPermissions_UserMissingOnePermission() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByUserId(1L)).thenReturn(List.of(testRole1));

        // When
        boolean result = permissionInheritanceService.hasAllPermissions(1L, "system:manage", "system:user:add");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testMergeRolePermissions_UnionStrategy() {
        // Given
        List<Role> roles = List.of(testRole1, testRole2);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole1));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(testRole2));
        when(menuRepository.findByRoleId(1L)).thenReturn(List.of(testMenu1, testMenu2));
        when(menuRepository.findByRoleId(2L)).thenReturn(List.of(testMenu2, testMenu3));

        // When
        PermissionInheritanceService.PermissionSet result = permissionInheritanceService.mergeRolePermissions(
                roles, PermissionInheritanceService.MergeStrategy.UNION
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMenuIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
        assertThat(result.getPermissions()).containsExactlyInAnyOrder(
                "system:manage", "system:user:list", "system:user:add"
        );
    }

    @Test
    void testPermissionSet_FromMenus() {
        // Given
        List<Menu> menus = List.of(testMenu1, testMenu2, testMenu3);

        // When
        PermissionInheritanceService.PermissionSet result = PermissionInheritanceService.PermissionSet.fromMenus(menus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMenuIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
        assertThat(result.getPermissions()).containsExactlyInAnyOrder(
                "system:manage", "system:user:list", "system:user:add"
        );
    }

    @Test
    void testPermissionSet_Empty() {
        // When
        PermissionInheritanceService.PermissionSet result = PermissionInheritanceService.PermissionSet.empty();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getMenuIds()).isEmpty();
        assertThat(result.getPermissions()).isEmpty();
    }

    @Test
    void testPermissionSet_HasMenuId() {
        // Given
        List<Menu> menus = List.of(testMenu1, testMenu2);
        PermissionInheritanceService.PermissionSet permissionSet = PermissionInheritanceService.PermissionSet.fromMenus(menus);

        // When & Then
        assertThat(permissionSet.hasMenuId(1L)).isTrue();
        assertThat(permissionSet.hasMenuId(2L)).isTrue();
        assertThat(permissionSet.hasMenuId(3L)).isFalse();
    }

    @Test
    void testPermissionSet_HasPermission() {
        // Given
        List<Menu> menus = List.of(testMenu1, testMenu2);
        PermissionInheritanceService.PermissionSet permissionSet = PermissionInheritanceService.PermissionSet.fromMenus(menus);

        // When & Then
        assertThat(permissionSet.hasPermission("system:manage")).isTrue();
        assertThat(permissionSet.hasPermission("system:user:list")).isTrue();
        assertThat(permissionSet.hasPermission("system:user:add")).isFalse();
    }
}