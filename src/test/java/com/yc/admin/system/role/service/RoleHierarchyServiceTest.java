package com.yc.admin.system.role.service;

import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.system.role.dto.RoleDTO;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 角色层级功能测试类
 * 
 * @author YC
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("角色层级功能测试")
class RoleHierarchyServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role rootRole;
    private Role parentRole;
    private Role childRole;
    private Role grandChildRole;

    @BeforeEach
    void setUp() {
        // 创建测试角色层级：rootRole -> parentRole -> childRole -> grandChildRole
        rootRole = createTestRole(1L, "根角色", "root", null);
        parentRole = createTestRole(2L, "父角色", "parent", 1L);
        childRole = createTestRole(3L, "子角色", "child", 2L);
        grandChildRole = createTestRole(4L, "孙子角色", "grandchild", 3L);
    }

    private Role createTestRole(Long id, String roleName, String roleKey, Long parentId) {
        Role role = new Role();
        role.setId(id);
        role.setRoleName(roleName);
        role.setRoleKey(roleKey);
        role.setParentId(parentId);
        role.setRoleSort(1);
        role.setStatus("0");
        role.setDelFlag(0);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        return role;
    }

    @Nested
    @DisplayName("查询子角色测试")
    class FindChildRolesTest {

        @Test
        @DisplayName("查询直接子角色 - 成功")
        void testFindChildRoles_Success() {
            // Given
            when(roleRepository.findByParentIdAndDelFlag(1L, 0))
                    .thenReturn(List.of(parentRole));

            // When
            List<RoleDTO> result = roleService.findChildRoles(1L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRoleName()).isEqualTo("父角色");
            verify(roleRepository).findByParentIdAndDelFlag(1L, 0);
        }

        @Test
        @DisplayName("查询子角色 - 父角色ID为null")
        void testFindChildRoles_NullParentId() {
            // When
            List<RoleDTO> result = roleService.findChildRoles(null);

            // Then
            assertThat(result).isEmpty();
            verify(roleRepository, never()).findByParentIdAndDelFlag(any(), any());
        }

        @Test
        @DisplayName("查询所有子角色（递归） - 成功")
        void testFindAllChildRoles_Success() {
            // Given
            when(roleRepository.findAllChildRoles(1L, 0))
                    .thenReturn(List.of(parentRole, childRole, grandChildRole));

            // When
            List<RoleDTO> result = roleService.findAllChildRoles(1L);

            // Then
            assertThat(result).hasSize(3);
            verify(roleRepository).findAllChildRoles(1L, 0);
        }
    }

    @Nested
    @DisplayName("查询根角色测试")
    class FindRootRolesTest {

        @Test
        @DisplayName("查询根角色 - 成功")
        void testFindRootRoles_Success() {
            // Given
            when(roleRepository.findByParentIdIsNullAndDelFlag(0))
                    .thenReturn(List.of(rootRole));

            // When
            List<RoleDTO> result = roleService.findRootRoles();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRoleName()).isEqualTo("根角色");
            verify(roleRepository).findByParentIdIsNullAndDelFlag(0);
        }
    }

    @Nested
    @DisplayName("查询父角色测试")
    class FindParentRoleTest {

        @Test
        @DisplayName("查询父角色 - 成功")
        void testFindParentRole_Success() {
            // Given
            when(roleRepository.findById(2L)).thenReturn(Optional.of(parentRole));
            when(roleRepository.findById(1L)).thenReturn(Optional.of(rootRole));

            // When
            Optional<RoleDTO> result = roleService.findParentRole(2L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getRoleName()).isEqualTo("根角色");
        }

        @Test
        @DisplayName("查询父角色 - 角色ID为null")
        void testFindParentRole_NullRoleId() {
            // When
            Optional<RoleDTO> result = roleService.findParentRole(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("查询父角色 - 角色不存在")
        void testFindParentRole_RoleNotFound() {
            // Given
            when(roleRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<RoleDTO> result = roleService.findParentRole(999L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("查询祖先角色测试")
    class FindAncestorRolesTest {

        @Test
        @DisplayName("查询祖先角色 - 成功")
        void testFindAncestorRoles_Success() {
            // Given
            when(roleRepository.findAncestorRoles(4L, 0))
                    .thenReturn(List.of(childRole, parentRole, rootRole));

            // When
            List<RoleDTO> result = roleService.findAncestorRoles(4L);

            // Then
            assertThat(result).hasSize(3);
            verify(roleRepository).findAncestorRoles(4L, 0);
        }

        @Test
        @DisplayName("查询祖先角色 - 角色ID为null")
        void testFindAncestorRoles_NullRoleId() {
            // When
            List<RoleDTO> result = roleService.findAncestorRoles(null);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("循环引用检查测试")
    class CycleCheckTest {

        @Test
        @DisplayName("检查循环引用 - 会形成循环")
        void testWouldCreateCycle_WouldCreate() {
            // Given - 尝试将根角色设为孙子角色的父角色，会形成循环
            when(roleRepository.findAllChildRoles(1L, 0))
                    .thenReturn(List.of(parentRole, childRole, grandChildRole));

            // When
            boolean result = roleService.wouldCreateCycle(1L, 4L);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("检查循环引用 - 不会形成循环")
        void testWouldCreateCycle_WouldNotCreate() {
            // Given - 正常的父子关系设置
            when(roleRepository.findAllChildRoles(2L, 0))
                    .thenReturn(List.of(childRole, grandChildRole));

            // When
            boolean result = roleService.wouldCreateCycle(2L, 1L);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("检查循环引用 - 角色ID相同")
        void testWouldCreateCycle_SameId() {
            // When
            boolean result = roleService.wouldCreateCycle(1L, 1L);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("检查循环引用 - 参数为null")
        void testWouldCreateCycle_NullParams() {
            // When & Then
            assertThat(roleService.wouldCreateCycle(null, 1L)).isTrue();
            assertThat(roleService.wouldCreateCycle(1L, null)).isFalse(); // parentId为null表示设为根角色，不会形成循环
            assertThat(roleService.wouldCreateCycle(null, null)).isTrue();
        }
    }

    @Nested
    @DisplayName("设置父角色测试")
    class SetParentRoleTest {

        @Test
        @DisplayName("设置父角色 - 成功")
        void testSetParentRole_Success() {
            // Given
            when(roleRepository.findById(2L)).thenReturn(Optional.of(parentRole));
            when(roleRepository.findById(1L)).thenReturn(Optional.of(rootRole));
            when(roleRepository.findAllChildRoles(2L, 0)).thenReturn(List.of(childRole));
            when(roleRepository.save(any(Role.class))).thenReturn(parentRole);

            // When
            roleService.setParentRole(2L, 1L);

            // Then
            verify(roleRepository).save(argThat(role -> 
                role.getId().equals(2L) && role.getParentId().equals(1L)
            ));
        }

        @Test
        @DisplayName("设置父角色 - 角色ID为null")
        void testSetParentRole_NullRoleId() {
            // When & Then
            assertThatThrownBy(() -> roleService.setParentRole(null, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("角色ID不能为空");
        }

        @Test
        @DisplayName("设置父角色 - 角色不存在")
        void testSetParentRole_RoleNotFound() {
            // Given
            when(roleRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.setParentRole(999L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("角色不存在: 999");
        }

        @Test
        @DisplayName("设置父角色 - 父角色不存在")
        void testSetParentRole_ParentNotFound() {
            // Given
            when(roleRepository.findById(2L)).thenReturn(Optional.of(parentRole));
            when(roleRepository.findById(999L)).thenReturn(Optional.empty());
            when(roleRepository.findAllChildRoles(2L, 0)).thenReturn(List.of());

            // When & Then
            assertThatThrownBy(() -> roleService.setParentRole(2L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("父角色不存在: 999");
        }

        @Test
        @DisplayName("设置父角色 - 会形成循环引用")
        void testSetParentRole_WouldCreateCycle() {
            // Given
            when(roleRepository.findById(1L)).thenReturn(Optional.of(rootRole));
            when(roleRepository.findAllChildRoles(1L, 0))
                    .thenReturn(List.of(parentRole, childRole));

            // When & Then
            assertThatThrownBy(() -> roleService.setParentRole(1L, 3L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("设置父角色会形成循环引用");
        }

        @Test
        @DisplayName("设置父角色为null - 成功")
        void testSetParentRole_SetToRoot() {
            // Given
            when(roleRepository.findById(2L)).thenReturn(Optional.of(parentRole));
            when(roleRepository.save(any(Role.class))).thenReturn(parentRole);

            // When
            roleService.setParentRole(2L, null);

            // Then
            verify(roleRepository).save(argThat(role -> 
                role.getId().equals(2L) && role.getParentId() == null
            ));
        }
    }

    @Nested
    @DisplayName("构建角色层级树测试")
    class BuildRoleHierarchyTreeTest {

        @Test
        @DisplayName("构建角色层级树 - 成功")
        void testBuildRoleHierarchyTree_Success() {
            // Given
            when(roleRepository.findByDelFlagOrderByRoleSortAsc(0))
                    .thenReturn(List.of(rootRole, parentRole, childRole, grandChildRole));

            // When
            List<RoleDTO> result = roleService.buildRoleHierarchyTree();

            // Then
            assertThat(result).hasSize(1); // 只有一个根角色
            assertThat(result.get(0).getRoleName()).isEqualTo("根角色");
            verify(roleRepository).findByDelFlagOrderByRoleSortAsc(0);
        }

        @Test
        @DisplayName("构建角色层级树 - 空列表")
        void testBuildRoleHierarchyTree_EmptyList() {
            // Given
            when(roleRepository.findByDelFlagOrderByRoleSortAsc(0))
                    .thenReturn(List.of());

            // When
            List<RoleDTO> result = roleService.buildRoleHierarchyTree();

            // Then
            assertThat(result).isEmpty();
        }
    }
}