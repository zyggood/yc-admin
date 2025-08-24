package com.yc.admin.system.role.service;

import com.yc.admin.system.role.entity.RoleMenu;
import com.yc.admin.system.role.repository.RoleMenuRepository;
import com.yc.admin.system.permission.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RoleMenuService 单元测试
 *
 * @author yc
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleMenuService 单元测试")
class RoleMenuServiceTest {

    @Mock
    private RoleMenuRepository roleMenuRepository;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private RoleMenuService roleMenuService;

    private RoleMenu roleMenu1;
    private RoleMenu roleMenu2;
    private RoleMenu roleMenu3;

    @BeforeEach
    void setUp() {
        roleMenu1 = new RoleMenu(1L, 101L);
        roleMenu2 = new RoleMenu(1L, 102L);
        roleMenu3 = new RoleMenu(2L, 101L);
    }

    @Nested
    @DisplayName("查询角色菜单ID列表测试")
    class GetMenuIdsByRoleIdTests {

        @Test
        @DisplayName("正常查询角色菜单ID列表")
        void testGetMenuIdsByRoleId_Success() {
            // Given
            Long roleId = 1L;
            List<Long> expectedMenuIds = Arrays.asList(101L, 102L);
            when(roleMenuRepository.findMenuIdsByRoleId(roleId)).thenReturn(expectedMenuIds);

            // When
            List<Long> result = roleMenuService.getMenuIdsByRoleId(roleId);

            // Then
            assertThat(result).isEqualTo(expectedMenuIds);
            verify(roleMenuRepository).findMenuIdsByRoleId(roleId);
        }

        @Test
        @DisplayName("角色ID为null时返回空列表")
        void testGetMenuIdsByRoleId_NullRoleId() {
            // When
            List<Long> result = roleMenuService.getMenuIdsByRoleId(null);

            // Then
            assertThat(result).isEmpty();
            verify(roleMenuRepository, never()).findMenuIdsByRoleId(any());
        }
    }

    @Nested
    @DisplayName("查询菜单角色ID列表测试")
    class GetRoleIdsByMenuIdTests {

        @Test
        @DisplayName("正常查询菜单角色ID列表")
        void testGetRoleIdsByMenuId_Success() {
            // Given
            Long menuId = 101L;
            List<Long> expectedRoleIds = Arrays.asList(1L, 2L);
            when(roleMenuRepository.findRoleIdsByMenuId(menuId)).thenReturn(expectedRoleIds);

            // When
            List<Long> result = roleMenuService.getRoleIdsByMenuId(menuId);

            // Then
            assertThat(result).isEqualTo(expectedRoleIds);
            verify(roleMenuRepository).findRoleIdsByMenuId(menuId);
        }

        @Test
        @DisplayName("菜单ID为null时返回空列表")
        void testGetRoleIdsByMenuId_NullMenuId() {
            // When
            List<Long> result = roleMenuService.getRoleIdsByMenuId(null);

            // Then
            assertThat(result).isEmpty();
            verify(roleMenuRepository, never()).findRoleIdsByMenuId(any());
        }
    }

    @Nested
    @DisplayName("通过用户ID查询菜单ID列表测试")
    class GetMenuIdsByUserIdTests {

        @Test
        @DisplayName("正常查询用户菜单ID列表")
        void testGetMenuIdsByUserId_Success() {
            // Given
            Long userId = 1L;
            List<Long> expectedMenuIds = Arrays.asList(101L, 102L);
            when(permissionService.getMenuIdsByUserId(userId)).thenReturn(expectedMenuIds);

            // When
            List<Long> result = roleMenuService.getMenuIdsByUserId(userId);

            // Then
            assertThat(result).isEqualTo(expectedMenuIds);
            verify(permissionService).getMenuIdsByUserId(userId);
        }
    }

    @Nested
    @DisplayName("通过权限标识查询菜单ID列表测试")
    class GetMenuIdsByPermissionTests {

        @Test
        @DisplayName("正常查询权限菜单ID列表")
        void testGetMenuIdsByPermission_Success() {
            // Given
            String permission = "system:user:query";
            List<Long> expectedMenuIds = Arrays.asList(101L, 102L);
            when(permissionService.getMenuIdsByPermission(permission)).thenReturn(expectedMenuIds);

            // When
            List<Long> result = roleMenuService.getMenuIdsByPermission(permission);

            // Then
            assertThat(result).isEqualTo(expectedMenuIds);
            verify(permissionService).getMenuIdsByPermission(permission);
        }
    }

    @Nested
    @DisplayName("批量查询角色菜单关联测试")
    class GetRoleMenusByRoleIdsTests {

        @Test
        @DisplayName("正常批量查询角色菜单关联")
        void testGetRoleMenusByRoleIds_Success() {
            // Given
            List<Long> roleIds = Arrays.asList(1L, 2L);
            List<RoleMenu> expectedRoleMenus = Arrays.asList(roleMenu1, roleMenu2, roleMenu3);
            when(roleMenuRepository.findByRoleIdIn(roleIds)).thenReturn(expectedRoleMenus);

            // When
            List<RoleMenu> result = roleMenuService.getRoleMenusByRoleIds(roleIds);

            // Then
            assertThat(result).isEqualTo(expectedRoleMenus);
            verify(roleMenuRepository).findByRoleIdIn(roleIds);
        }

        @Test
        @DisplayName("角色ID列表为空时返回空列表")
        void testGetRoleMenusByRoleIds_EmptyList() {
            // When
            List<RoleMenu> result = roleMenuService.getRoleMenusByRoleIds(Collections.emptyList());

            // Then
            assertThat(result).isEmpty();
            verify(roleMenuRepository, never()).findByRoleIdIn(any());
        }

        @Test
        @DisplayName("角色ID列表为null时返回空列表")
        void testGetRoleMenusByRoleIds_NullList() {
            // When
            List<RoleMenu> result = roleMenuService.getRoleMenusByRoleIds(null);

            // Then
            assertThat(result).isEmpty();
            verify(roleMenuRepository, never()).findByRoleIdIn(any());
        }
    }

    @Nested
    @DisplayName("批量查询菜单角色关联测试")
    class GetRoleMenusByMenuIdsTests {

        @Test
        @DisplayName("正常批量查询菜单角色关联")
        void testGetRoleMenusByMenuIds_Success() {
            // Given
            List<Long> menuIds = Arrays.asList(101L, 102L);
            List<RoleMenu> expectedRoleMenus = Arrays.asList(roleMenu1, roleMenu2, roleMenu3);
            when(roleMenuRepository.findByMenuIdIn(menuIds)).thenReturn(expectedRoleMenus);

            // When
            List<RoleMenu> result = roleMenuService.getRoleMenusByMenuIds(menuIds);

            // Then
            assertThat(result).isEqualTo(expectedRoleMenus);
            verify(roleMenuRepository).findByMenuIdIn(menuIds);
        }

        @Test
        @DisplayName("菜单ID列表为空时返回空列表")
        void testGetRoleMenusByMenuIds_EmptyList() {
            // When
            List<RoleMenu> result = roleMenuService.getRoleMenusByMenuIds(Collections.emptyList());

            // Then
            assertThat(result).isEmpty();
            verify(roleMenuRepository, never()).findByMenuIdIn(any());
        }

        @Test
        @DisplayName("菜单ID列表为null时返回空列表")
        void testGetRoleMenusByMenuIds_NullList() {
            // When
            List<RoleMenu> result = roleMenuService.getRoleMenusByMenuIds(null);

            // Then
            assertThat(result).isEmpty();
            verify(roleMenuRepository, never()).findByMenuIdIn(any());
        }
    }

    @Nested
    @DisplayName("角色菜单关联存在性检查测试")
    class ExistsRoleMenuTests {

        @Test
        @DisplayName("角色菜单关联存在")
        void testExistsRoleMenu_Exists() {
            // Given
            Long roleId = 1L;
            Long menuId = 101L;
            when(roleMenuRepository.existsByRoleIdAndMenuId(roleId, menuId)).thenReturn(true);

            // When
            boolean result = roleMenuService.existsRoleMenu(roleId, menuId);

            // Then
            assertThat(result).isTrue();
            verify(roleMenuRepository).existsByRoleIdAndMenuId(roleId, menuId);
        }

        @Test
        @DisplayName("角色菜单关联不存在")
        void testExistsRoleMenu_NotExists() {
            // Given
            Long roleId = 1L;
            Long menuId = 101L;
            when(roleMenuRepository.existsByRoleIdAndMenuId(roleId, menuId)).thenReturn(false);

            // When
            boolean result = roleMenuService.existsRoleMenu(roleId, menuId);

            // Then
            assertThat(result).isFalse();
            verify(roleMenuRepository).existsByRoleIdAndMenuId(roleId, menuId);
        }

        @Test
        @DisplayName("角色ID为null时返回false")
        void testExistsRoleMenu_NullRoleId() {
            // When
            boolean result = roleMenuService.existsRoleMenu(null, 101L);

            // Then
            assertThat(result).isFalse();
            verify(roleMenuRepository, never()).existsByRoleIdAndMenuId(any(), any());
        }

        @Test
        @DisplayName("菜单ID为null时返回false")
        void testExistsRoleMenu_NullMenuId() {
            // When
            boolean result = roleMenuService.existsRoleMenu(1L, null);

            // Then
            assertThat(result).isFalse();
            verify(roleMenuRepository, never()).existsByRoleIdAndMenuId(any(), any());
        }
    }

    @Nested
    @DisplayName("统计角色菜单数量测试")
    class CountMenusByRoleIdTests {

        @Test
        @DisplayName("正常统计角色菜单数量")
        void testCountMenusByRoleId_Success() {
            // Given
            Long roleId = 1L;
            long expectedCount = 5L;
            when(roleMenuRepository.countByRoleId(roleId)).thenReturn(expectedCount);

            // When
            long result = roleMenuService.countMenusByRoleId(roleId);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(roleMenuRepository).countByRoleId(roleId);
        }

        @Test
        @DisplayName("角色ID为null时返回0")
        void testCountMenusByRoleId_NullRoleId() {
            // When
            long result = roleMenuService.countMenusByRoleId(null);

            // Then
            assertThat(result).isZero();
            verify(roleMenuRepository, never()).countByRoleId(any());
        }
    }

    @Nested
    @DisplayName("统计菜单角色数量测试")
    class CountRolesByMenuIdTests {

        @Test
        @DisplayName("正常统计菜单角色数量")
        void testCountRolesByMenuId_Success() {
            // Given
            Long menuId = 101L;
            long expectedCount = 3L;
            when(roleMenuRepository.countByMenuId(menuId)).thenReturn(expectedCount);

            // When
            long result = roleMenuService.countRolesByMenuId(menuId);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(roleMenuRepository).countByMenuId(menuId);
        }

        @Test
        @DisplayName("菜单ID为null时返回0")
        void testCountRolesByMenuId_NullMenuId() {
            // When
            long result = roleMenuService.countRolesByMenuId(null);

            // Then
            assertThat(result).isZero();
            verify(roleMenuRepository, never()).countByMenuId(any());
        }
    }

    @Nested
    @DisplayName("为角色分配菜单权限测试")
    class AssignMenusToRoleTests {

        @Test
        @DisplayName("正常为角色分配菜单权限")
        void testAssignMenusToRole_Success() {
            // Given
            Long roleId = 1L;
            List<Long> menuIds = Arrays.asList(101L, 102L, 103L);

            // When
            roleMenuService.assignMenusToRole(roleId, menuIds);

            // Then
            verify(roleMenuRepository).deleteByRoleId(roleId);
            verify(roleMenuRepository).saveAll(argThat(roleMenus -> {
                List<RoleMenu> list = (List<RoleMenu>) roleMenus;
                return list.size() == 3 &&
                       list.stream().allMatch(rm -> rm.getRoleId().equals(roleId)) &&
                       list.stream().map(RoleMenu::getMenuId).allMatch(menuIds::contains);
            }));
        }

        @Test
        @DisplayName("角色ID为null时不执行操作")
        void testAssignMenusToRole_NullRoleId() {
            // Given
            List<Long> menuIds = Arrays.asList(101L, 102L);

            // When
            roleMenuService.assignMenusToRole(null, menuIds);

            // Then
            verify(roleMenuRepository, never()).deleteByRoleId(any());
            verify(roleMenuRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("菜单ID列表为空时不执行操作")
        void testAssignMenusToRole_EmptyMenuIds() {
            // Given
            Long roleId = 1L;

            // When
            roleMenuService.assignMenusToRole(roleId, Collections.emptyList());

            // Then
            verify(roleMenuRepository, never()).deleteByRoleId(any());
            verify(roleMenuRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("菜单ID列表包含重复项时去重")
        void testAssignMenusToRole_DuplicateMenuIds() {
            // Given
            Long roleId = 1L;
            List<Long> menuIds = Arrays.asList(101L, 102L, 101L, 103L);

            // When
            roleMenuService.assignMenusToRole(roleId, menuIds);

            // Then
            verify(roleMenuRepository).deleteByRoleId(roleId);
            verify(roleMenuRepository).saveAll(argThat(roleMenus -> {
                List<RoleMenu> list = (List<RoleMenu>) roleMenus;
                return list.size() == 3; // 去重后应该只有3个
            }));
        }
    }

    @Nested
    @DisplayName("为菜单分配角色测试")
    class AssignRolesToMenuTests {

        @Test
        @DisplayName("正常为菜单分配角色")
        void testAssignRolesToMenu_Success() {
            // Given
            Long menuId = 101L;
            List<Long> roleIds = Arrays.asList(1L, 2L, 3L);

            // When
            roleMenuService.assignRolesToMenu(menuId, roleIds);

            // Then
            verify(roleMenuRepository).deleteByMenuId(menuId);
            verify(roleMenuRepository).saveAll(argThat(roleMenus -> {
                List<RoleMenu> list = (List<RoleMenu>) roleMenus;
                return list.size() == 3 &&
                       list.stream().allMatch(rm -> rm.getMenuId().equals(menuId)) &&
                       list.stream().map(RoleMenu::getRoleId).allMatch(roleIds::contains);
            }));
        }

        @Test
        @DisplayName("菜单ID为null时不执行操作")
        void testAssignRolesToMenu_NullMenuId() {
            // Given
            List<Long> roleIds = Arrays.asList(1L, 2L);

            // When
            roleMenuService.assignRolesToMenu(null, roleIds);

            // Then
            verify(roleMenuRepository, never()).deleteByMenuId(any());
            verify(roleMenuRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("角色ID列表为空时不执行操作")
        void testAssignRolesToMenu_EmptyRoleIds() {
            // Given
            Long menuId = 101L;

            // When
            roleMenuService.assignRolesToMenu(menuId, Collections.emptyList());

            // Then
            verify(roleMenuRepository, never()).deleteByMenuId(any());
            verify(roleMenuRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("添加角色菜单关联测试")
    class AddRoleMenuTests {

        @Test
        @DisplayName("正常添加角色菜单关联")
        void testAddRoleMenu_Success() {
            // Given
            Long roleId = 1L;
            Long menuId = 101L;
            when(roleMenuRepository.existsByRoleIdAndMenuId(roleId, menuId)).thenReturn(false);

            // When
            roleMenuService.addRoleMenu(roleId, menuId);

            // Then
            verify(roleMenuRepository).existsByRoleIdAndMenuId(roleId, menuId);
            verify(roleMenuRepository).save(argThat(roleMenu -> 
                roleMenu.getRoleId().equals(roleId) && roleMenu.getMenuId().equals(menuId)
            ));
        }

        @Test
        @DisplayName("关联已存在时不重复添加")
        void testAddRoleMenu_AlreadyExists() {
            // Given
            Long roleId = 1L;
            Long menuId = 101L;
            when(roleMenuRepository.existsByRoleIdAndMenuId(roleId, menuId)).thenReturn(true);

            // When
            roleMenuService.addRoleMenu(roleId, menuId);

            // Then
            verify(roleMenuRepository).existsByRoleIdAndMenuId(roleId, menuId);
            verify(roleMenuRepository, never()).save(any());
        }

        @Test
        @DisplayName("角色ID为null时不执行操作")
        void testAddRoleMenu_NullRoleId() {
            // When
            roleMenuService.addRoleMenu(null, 101L);

            // Then
            verify(roleMenuRepository, never()).existsByRoleIdAndMenuId(any(), any());
            verify(roleMenuRepository, never()).save(any());
        }

        @Test
        @DisplayName("菜单ID为null时不执行操作")
        void testAddRoleMenu_NullMenuId() {
            // When
            roleMenuService.addRoleMenu(1L, null);

            // Then
            verify(roleMenuRepository, never()).existsByRoleIdAndMenuId(any(), any());
            verify(roleMenuRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("批量添加角色菜单关联测试")
    class AddRoleMenusTests {

        @Test
        @DisplayName("正常批量添加角色菜单关联")
        void testAddRoleMenus_Success() {
            // Given
            Long roleId = 1L;
            List<Long> menuIds = Arrays.asList(101L, 102L, 103L);
            List<Long> existingMenuIds = Arrays.asList(101L); // 101已存在
            when(roleMenuRepository.findMenuIdsByRoleId(roleId)).thenReturn(existingMenuIds);

            // When
            roleMenuService.addRoleMenus(roleId, menuIds);

            // Then
            verify(roleMenuRepository).findMenuIdsByRoleId(roleId);
            verify(roleMenuRepository).saveAll(argThat(roleMenus -> {
                List<RoleMenu> list = (List<RoleMenu>) roleMenus;
                return list.size() == 2 && // 只添加102和103
                       list.stream().allMatch(rm -> rm.getRoleId().equals(roleId)) &&
                       list.stream().map(RoleMenu::getMenuId).noneMatch(id -> id.equals(101L));
            }));
        }

        @Test
        @DisplayName("所有菜单都已存在时不添加")
        void testAddRoleMenus_AllExist() {
            // Given
            Long roleId = 1L;
            List<Long> menuIds = Arrays.asList(101L, 102L);
            List<Long> existingMenuIds = Arrays.asList(101L, 102L); // 都已存在
            when(roleMenuRepository.findMenuIdsByRoleId(roleId)).thenReturn(existingMenuIds);

            // When
            roleMenuService.addRoleMenus(roleId, menuIds);

            // Then
            verify(roleMenuRepository).findMenuIdsByRoleId(roleId);
            verify(roleMenuRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("角色ID为null时不执行操作")
        void testAddRoleMenus_NullRoleId() {
            // Given
            List<Long> menuIds = Arrays.asList(101L, 102L);

            // When
            roleMenuService.addRoleMenus(null, menuIds);

            // Then
            verify(roleMenuRepository, never()).findMenuIdsByRoleId(any());
            verify(roleMenuRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("菜单ID列表为空时不执行操作")
        void testAddRoleMenus_EmptyMenuIds() {
            // Given
            Long roleId = 1L;

            // When
            roleMenuService.addRoleMenus(roleId, Collections.emptyList());

            // Then
            verify(roleMenuRepository, never()).findMenuIdsByRoleId(any());
            verify(roleMenuRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("删除角色菜单关联测试")
    class RemoveRoleMenuTests {

        @Test
        @DisplayName("正常删除角色菜单关联")
        void testRemoveRoleMenu_Success() {
            // Given
            Long roleId = 1L;
            Long menuId = 101L;

            // When
            roleMenuService.removeRoleMenu(roleId, menuId);

            // Then
            verify(roleMenuRepository).deleteByRoleIdAndMenuId(roleId, menuId);
        }

        @Test
        @DisplayName("角色ID为null时不执行操作")
        void testRemoveRoleMenu_NullRoleId() {
            // When
            roleMenuService.removeRoleMenu(null, 101L);

            // Then
            verify(roleMenuRepository, never()).deleteByRoleIdAndMenuId(any(), any());
        }

        @Test
        @DisplayName("菜单ID为null时不执行操作")
        void testRemoveRoleMenu_NullMenuId() {
            // When
            roleMenuService.removeRoleMenu(1L, null);

            // Then
            verify(roleMenuRepository, never()).deleteByRoleIdAndMenuId(any(), any());
        }
    }

    @Nested
    @DisplayName("批量删除角色菜单关联测试")
    class RemoveRoleMenusTests {

        @Test
        @DisplayName("正常批量删除角色菜单关联")
        void testRemoveRoleMenus_Success() {
            // Given
            Long roleId = 1L;
            List<Long> menuIds = Arrays.asList(101L, 102L);

            // When
            roleMenuService.removeRoleMenus(roleId, menuIds);

            // Then
            verify(roleMenuRepository).deleteByRoleIdAndMenuIdIn(roleId, menuIds);
        }

        @Test
        @DisplayName("角色ID为null时不执行操作")
        void testRemoveRoleMenus_NullRoleId() {
            // Given
            List<Long> menuIds = Arrays.asList(101L, 102L);

            // When
            roleMenuService.removeRoleMenus(null, menuIds);

            // Then
            verify(roleMenuRepository, never()).deleteByRoleIdAndMenuIdIn(any(), any());
        }

        @Test
        @DisplayName("菜单ID列表为空时不执行操作")
        void testRemoveRoleMenus_EmptyMenuIds() {
            // Given
            Long roleId = 1L;

            // When
            roleMenuService.removeRoleMenus(roleId, Collections.emptyList());

            // Then
            verify(roleMenuRepository, never()).deleteByRoleIdAndMenuIdIn(any(), any());
        }
    }

    @Nested
    @DisplayName("删除角色所有菜单关联测试")
    class RemoveAllRoleMenusTests {

        @Test
        @DisplayName("正常删除角色所有菜单关联")
        void testRemoveAllRoleMenus_Success() {
            // Given
            Long roleId = 1L;
            when(roleMenuRepository.deleteByRoleId(roleId)).thenReturn(5);

            // When
            roleMenuService.removeAllRoleMenus(roleId);

            // Then
            verify(roleMenuRepository).deleteByRoleId(roleId);
        }

        @Test
        @DisplayName("角色ID为null时不执行操作")
        void testRemoveAllRoleMenus_NullRoleId() {
            // When
            roleMenuService.removeAllRoleMenus(null);

            // Then
            verify(roleMenuRepository, never()).deleteByRoleId(any());
        }
    }

    @Nested
    @DisplayName("删除菜单所有角色关联测试")
    class RemoveAllMenuRolesTests {

        @Test
        @DisplayName("正常删除菜单所有角色关联")
        void testRemoveAllMenuRoles_Success() {
            // Given
            Long menuId = 101L;
            when(roleMenuRepository.deleteByMenuId(menuId)).thenReturn(3);

            // When
            roleMenuService.removeAllMenuRoles(menuId);

            // Then
            verify(roleMenuRepository).deleteByMenuId(menuId);
        }

        @Test
        @DisplayName("菜单ID为null时不执行操作")
        void testRemoveAllMenuRoles_NullMenuId() {
            // When
            roleMenuService.removeAllMenuRoles(null);

            // Then
            verify(roleMenuRepository, never()).deleteByMenuId(any());
        }
    }

    @Nested
    @DisplayName("批量删除角色菜单关联测试")
    class RemoveRoleMenusByRoleIdsTests {

        @Test
        @DisplayName("正常批量删除角色菜单关联")
        void testRemoveRoleMenusByRoleIds_Success() {
            // Given
            List<Long> roleIds = Arrays.asList(1L, 2L);
            when(roleMenuRepository.deleteByRoleIdIn(roleIds)).thenReturn(10);

            // When
            roleMenuService.removeRoleMenusByRoleIds(roleIds);

            // Then
            verify(roleMenuRepository).deleteByRoleIdIn(roleIds);
        }

        @Test
        @DisplayName("角色ID列表为空时不执行操作")
        void testRemoveRoleMenusByRoleIds_EmptyList() {
            // When
            roleMenuService.removeRoleMenusByRoleIds(Collections.emptyList());

            // Then
            verify(roleMenuRepository, never()).deleteByRoleIdIn(any());
        }

        @Test
        @DisplayName("角色ID列表为null时不执行操作")
        void testRemoveRoleMenusByRoleIds_NullList() {
            // When
            roleMenuService.removeRoleMenusByRoleIds(null);

            // Then
            verify(roleMenuRepository, never()).deleteByRoleIdIn(any());
        }
    }

    @Nested
    @DisplayName("批量删除菜单角色关联测试")
    class RemoveRoleMenusByMenuIdsTests {

        @Test
        @DisplayName("正常批量删除菜单角色关联")
        void testRemoveRoleMenusByMenuIds_Success() {
            // Given
            List<Long> menuIds = Arrays.asList(101L, 102L);
            when(roleMenuRepository.deleteByMenuIdIn(menuIds)).thenReturn(8);

            // When
            roleMenuService.removeRoleMenusByMenuIds(menuIds);

            // Then
            verify(roleMenuRepository).deleteByMenuIdIn(menuIds);
        }

        @Test
        @DisplayName("菜单ID列表为空时不执行操作")
        void testRemoveRoleMenusByMenuIds_EmptyList() {
            // When
            roleMenuService.removeRoleMenusByMenuIds(Collections.emptyList());

            // Then
            verify(roleMenuRepository, never()).deleteByMenuIdIn(any());
        }

        @Test
        @DisplayName("菜单ID列表为null时不执行操作")
        void testRemoveRoleMenusByMenuIds_NullList() {
            // When
            roleMenuService.removeRoleMenusByMenuIds(null);

            // Then
            verify(roleMenuRepository, never()).deleteByMenuIdIn(any());
        }
    }

    @Nested
    @DisplayName("权限检查测试")
    class PermissionCheckTests {

        @Test
        @DisplayName("检查用户菜单权限")
        void testHasMenuPermission() {
            // Given
            Long userId = 1L;
            Long menuId = 101L;
            when(permissionService.hasMenuPermission(userId, menuId)).thenReturn(true);

            // When
            boolean result = roleMenuService.hasMenuPermission(userId, menuId);

            // Then
            assertThat(result).isTrue();
            verify(permissionService).hasMenuPermission(userId, menuId);
        }

        @Test
        @DisplayName("检查用户权限标识")
        void testHasPermission() {
            // Given
            Long userId = 1L;
            String permission = "system:user:query";
            when(permissionService.hasPermission(userId, permission)).thenReturn(true);

            // When
            boolean result = roleMenuService.hasPermission(userId, permission);

            // Then
            assertThat(result).isTrue();
            verify(permissionService).hasPermission(userId, permission);
        }
    }
}