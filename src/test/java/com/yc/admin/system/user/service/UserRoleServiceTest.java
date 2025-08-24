package com.yc.admin.system.user.service;

import com.yc.admin.system.user.entity.UserRole;
import com.yc.admin.system.user.repository.UserRoleRepository;
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
 * UserRoleService 单元测试
 *
 * @author YC
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRoleService 单元测试")
class UserRoleServiceTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private UserRoleService userRoleService;

    private UserRole userRole1;
    private UserRole userRole2;
    private UserRole userRole3;

    @BeforeEach
    void setUp() {
        userRole1 = new UserRole(1L, 1L);
        userRole1.setId(1L);
        
        userRole2 = new UserRole(1L, 2L);
        userRole2.setId(2L);
        
        userRole3 = new UserRole(2L, 1L);
        userRole3.setId(3L);
    }

    @Nested
    @DisplayName("查询角色ID列表测试")
    class GetRoleIdsByUserIdTests {

        @Test
        @DisplayName("根据用户ID查询角色ID列表 - 成功")
        void testGetRoleIdsByUserId_Success() {
            // Given
            Long userId = 1L;
            List<Long> expectedRoleIds = Arrays.asList(1L, 2L);
            when(userRoleRepository.findRoleIdsByUserId(userId)).thenReturn(expectedRoleIds);

            // When
            List<Long> result = userRoleService.getRoleIdsByUserId(userId);

            // Then
            assertThat(result).isEqualTo(expectedRoleIds);
            verify(userRoleRepository).findRoleIdsByUserId(userId);
        }

        @Test
        @DisplayName("根据用户ID查询角色ID列表 - 用户ID为null")
        void testGetRoleIdsByUserId_NullUserId() {
            // When
            List<Long> result = userRoleService.getRoleIdsByUserId(null);

            // Then
            assertThat(result).isEmpty();
            verify(userRoleRepository, never()).findRoleIdsByUserId(any());
        }

        @Test
        @DisplayName("根据用户ID查询角色ID列表 - 无角色")
        void testGetRoleIdsByUserId_NoRoles() {
            // Given
            Long userId = 1L;
            when(userRoleRepository.findRoleIdsByUserId(userId)).thenReturn(Collections.emptyList());

            // When
            List<Long> result = userRoleService.getRoleIdsByUserId(userId);

            // Then
            assertThat(result).isEmpty();
            verify(userRoleRepository).findRoleIdsByUserId(userId);
        }
    }

    @Nested
    @DisplayName("查询用户ID列表测试")
    class GetUserIdsByRoleIdTests {

        @Test
        @DisplayName("根据角色ID查询用户ID列表 - 成功")
        void testGetUserIdsByRoleId_Success() {
            // Given
            Long roleId = 1L;
            List<Long> expectedUserIds = Arrays.asList(1L, 2L);
            when(userRoleRepository.findUserIdsByRoleId(roleId)).thenReturn(expectedUserIds);

            // When
            List<Long> result = userRoleService.getUserIdsByRoleId(roleId);

            // Then
            assertThat(result).isEqualTo(expectedUserIds);
            verify(userRoleRepository).findUserIdsByRoleId(roleId);
        }

        @Test
        @DisplayName("根据角色ID查询用户ID列表 - 角色ID为null")
        void testGetUserIdsByRoleId_NullRoleId() {
            // When
            List<Long> result = userRoleService.getUserIdsByRoleId(null);

            // Then
            assertThat(result).isEmpty();
            verify(userRoleRepository, never()).findUserIdsByRoleId(any());
        }
    }

    @Nested
    @DisplayName("批量查询用户角色关联测试")
    class GetUserRolesByUserIdsTests {

        @Test
        @DisplayName("批量根据用户ID查询用户角色关联 - 成功")
        void testGetUserRolesByUserIds_Success() {
            // Given
            List<Long> userIds = Arrays.asList(1L, 2L);
            List<UserRole> expectedUserRoles = Arrays.asList(userRole1, userRole2, userRole3);
            when(userRoleRepository.findByUserIdIn(userIds)).thenReturn(expectedUserRoles);

            // When
            List<UserRole> result = userRoleService.getUserRolesByUserIds(userIds);

            // Then
            assertThat(result).isEqualTo(expectedUserRoles);
            verify(userRoleRepository).findByUserIdIn(userIds);
        }

        @Test
        @DisplayName("批量根据用户ID查询用户角色关联 - 用户ID列表为空")
        void testGetUserRolesByUserIds_EmptyUserIds() {
            // When
            List<UserRole> result = userRoleService.getUserRolesByUserIds(Collections.emptyList());

            // Then
            assertThat(result).isEmpty();
            verify(userRoleRepository, never()).findByUserIdIn(any());
        }

        @Test
        @DisplayName("批量根据用户ID查询用户角色关联 - 用户ID列表为null")
        void testGetUserRolesByUserIds_NullUserIds() {
            // When
            List<UserRole> result = userRoleService.getUserRolesByUserIds(null);

            // Then
            assertThat(result).isEmpty();
            verify(userRoleRepository, never()).findByUserIdIn(any());
        }
    }

    @Nested
    @DisplayName("批量查询角色用户关联测试")
    class GetUserRolesByRoleIdsTests {

        @Test
        @DisplayName("批量根据角色ID查询用户角色关联 - 成功")
        void testGetUserRolesByRoleIds_Success() {
            // Given
            List<Long> roleIds = Arrays.asList(1L, 2L);
            List<UserRole> expectedUserRoles = Arrays.asList(userRole1, userRole2, userRole3);
            when(userRoleRepository.findByRoleIdIn(roleIds)).thenReturn(expectedUserRoles);

            // When
            List<UserRole> result = userRoleService.getUserRolesByRoleIds(roleIds);

            // Then
            assertThat(result).isEqualTo(expectedUserRoles);
            verify(userRoleRepository).findByRoleIdIn(roleIds);
        }

        @Test
        @DisplayName("批量根据角色ID查询用户角色关联 - 角色ID列表为空")
        void testGetUserRolesByRoleIds_EmptyRoleIds() {
            // When
            List<UserRole> result = userRoleService.getUserRolesByRoleIds(Collections.emptyList());

            // Then
            assertThat(result).isEmpty();
            verify(userRoleRepository, never()).findByRoleIdIn(any());
        }
    }

    @Nested
    @DisplayName("检查用户角色关联存在性测试")
    class ExistsUserRoleTests {

        @Test
        @DisplayName("检查用户角色关联是否存在 - 存在")
        void testExistsUserRole_Exists() {
            // Given
            Long userId = 1L;
            Long roleId = 1L;
            when(userRoleRepository.existsByUserIdAndRoleId(userId, roleId)).thenReturn(true);

            // When
            boolean result = userRoleService.existsUserRole(userId, roleId);

            // Then
            assertThat(result).isTrue();
            verify(userRoleRepository).existsByUserIdAndRoleId(userId, roleId);
        }

        @Test
        @DisplayName("检查用户角色关联是否存在 - 不存在")
        void testExistsUserRole_NotExists() {
            // Given
            Long userId = 1L;
            Long roleId = 1L;
            when(userRoleRepository.existsByUserIdAndRoleId(userId, roleId)).thenReturn(false);

            // When
            boolean result = userRoleService.existsUserRole(userId, roleId);

            // Then
            assertThat(result).isFalse();
            verify(userRoleRepository).existsByUserIdAndRoleId(userId, roleId);
        }

        @Test
        @DisplayName("检查用户角色关联是否存在 - 用户ID为null")
        void testExistsUserRole_NullUserId() {
            // When
            boolean result = userRoleService.existsUserRole(null, 1L);

            // Then
            assertThat(result).isFalse();
            verify(userRoleRepository, never()).existsByUserIdAndRoleId(any(), any());
        }

        @Test
        @DisplayName("检查用户角色关联是否存在 - 角色ID为null")
        void testExistsUserRole_NullRoleId() {
            // When
            boolean result = userRoleService.existsUserRole(1L, null);

            // Then
            assertThat(result).isFalse();
            verify(userRoleRepository, never()).existsByUserIdAndRoleId(any(), any());
        }
    }

    @Nested
    @DisplayName("统计角色数量测试")
    class CountRolesByUserIdTests {

        @Test
        @DisplayName("统计用户的角色数量 - 成功")
        void testCountRolesByUserId_Success() {
            // Given
            Long userId = 1L;
            long expectedCount = 3L;
            when(userRoleRepository.countByUserId(userId)).thenReturn(expectedCount);

            // When
            long result = userRoleService.countRolesByUserId(userId);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(userRoleRepository).countByUserId(userId);
        }

        @Test
        @DisplayName("统计用户的角色数量 - 用户ID为null")
        void testCountRolesByUserId_NullUserId() {
            // When
            long result = userRoleService.countRolesByUserId(null);

            // Then
            assertThat(result).isEqualTo(0L);
            verify(userRoleRepository, never()).countByUserId(any());
        }
    }

    @Nested
    @DisplayName("统计用户数量测试")
    class CountUsersByRoleIdTests {

        @Test
        @DisplayName("统计角色的用户数量 - 成功")
        void testCountUsersByRoleId_Success() {
            // Given
            Long roleId = 1L;
            long expectedCount = 2L;
            when(userRoleRepository.countByRoleId(roleId)).thenReturn(expectedCount);

            // When
            long result = userRoleService.countUsersByRoleId(roleId);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(userRoleRepository).countByRoleId(roleId);
        }

        @Test
        @DisplayName("统计角色的用户数量 - 角色ID为null")
        void testCountUsersByRoleId_NullRoleId() {
            // When
            long result = userRoleService.countUsersByRoleId(null);

            // Then
            assertThat(result).isEqualTo(0L);
            verify(userRoleRepository, never()).countByRoleId(any());
        }
    }

    @Nested
    @DisplayName("分配角色给用户测试")
    class AssignRolesToUserTests {

        @Test
        @DisplayName("为用户分配角色 - 成功")
        void testAssignRolesToUser_Success() {
            // Given
            Long userId = 1L;
            List<Long> roleIds = Arrays.asList(1L, 2L, 3L);
            when(userRoleRepository.deleteByUserId(userId)).thenReturn(2);
            when(userRoleRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

            // When
            userRoleService.assignRolesToUser(userId, roleIds);

            // Then
            verify(userRoleRepository).deleteByUserId(userId);
            verify(userRoleRepository).saveAll(argThat(userRoles -> {
                List<UserRole> userRoleList = (List<UserRole>) userRoles;
                return userRoleList.size() == 3 && 
                       userRoleList.stream().allMatch(ur -> ur.getUserId().equals(userId));
            }));
        }

        @Test
        @DisplayName("为用户分配角色 - 用户ID为null")
        void testAssignRolesToUser_NullUserId() {
            // Given
            List<Long> roleIds = Arrays.asList(1L, 2L);

            // When
            userRoleService.assignRolesToUser(null, roleIds);

            // Then
            verify(userRoleRepository, never()).deleteByUserId(any());
            verify(userRoleRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("为用户分配角色 - 角色ID列表为空")
        void testAssignRolesToUser_EmptyRoleIds() {
            // Given
            Long userId = 1L;

            // When
            userRoleService.assignRolesToUser(userId, Collections.emptyList());

            // Then
            verify(userRoleRepository, never()).deleteByUserId(any());
            verify(userRoleRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("为用户分配角色 - 去重测试")
        void testAssignRolesToUser_Distinct() {
            // Given
            Long userId = 1L;
            List<Long> roleIds = Arrays.asList(1L, 2L, 2L, 3L, 1L); // 包含重复的角色ID
            when(userRoleRepository.deleteByUserId(userId)).thenReturn(0);
            when(userRoleRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

            // When
            userRoleService.assignRolesToUser(userId, roleIds);

            // Then
            verify(userRoleRepository).saveAll(argThat(userRoles -> {
                List<UserRole> userRoleList = (List<UserRole>) userRoles;
                return userRoleList.size() == 3; // 去重后应该只有3个
            }));
        }
    }

    @Nested
    @DisplayName("分配用户给角色测试")
    class AssignUsersToRoleTests {

        @Test
        @DisplayName("为角色分配用户 - 成功")
        void testAssignUsersToRole_Success() {
            // Given
            Long roleId = 1L;
            List<Long> userIds = Arrays.asList(1L, 2L, 3L);
            when(userRoleRepository.deleteByRoleId(roleId)).thenReturn(2);
            when(userRoleRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

            // When
            userRoleService.assignUsersToRole(roleId, userIds);

            // Then
            verify(userRoleRepository).deleteByRoleId(roleId);
            verify(userRoleRepository).saveAll(argThat(userRoles -> {
                List<UserRole> userRoleList = (List<UserRole>) userRoles;
                return userRoleList.size() == 3 && 
                       userRoleList.stream().allMatch(ur -> ur.getRoleId().equals(roleId));
            }));
        }

        @Test
        @DisplayName("为角色分配用户 - 角色ID为null")
        void testAssignUsersToRole_NullRoleId() {
            // Given
            List<Long> userIds = Arrays.asList(1L, 2L);

            // When
            userRoleService.assignUsersToRole(null, userIds);

            // Then
            verify(userRoleRepository, never()).deleteByRoleId(any());
            verify(userRoleRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("为角色分配用户 - 用户ID列表为空")
        void testAssignUsersToRole_EmptyUserIds() {
            // Given
            Long roleId = 1L;

            // When
            userRoleService.assignUsersToRole(roleId, Collections.emptyList());

            // Then
            verify(userRoleRepository, never()).deleteByRoleId(any());
            verify(userRoleRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("添加用户角色关联测试")
    class AddUserRoleTests {

        @Test
        @DisplayName("添加用户角色关联 - 成功")
        void testAddUserRole_Success() {
            // Given
            Long userId = 1L;
            Long roleId = 1L;
            when(userRoleRepository.existsByUserIdAndRoleId(userId, roleId)).thenReturn(false);
            when(userRoleRepository.save(any(UserRole.class))).thenReturn(userRole1);

            // When
            userRoleService.addUserRole(userId, roleId);

            // Then
            verify(userRoleRepository).existsByUserIdAndRoleId(userId, roleId);
            verify(userRoleRepository).save(argThat(userRole -> 
                userRole.getUserId().equals(userId) && userRole.getRoleId().equals(roleId)
            ));
        }

        @Test
        @DisplayName("添加用户角色关联 - 关联已存在")
        void testAddUserRole_AlreadyExists() {
            // Given
            Long userId = 1L;
            Long roleId = 1L;
            when(userRoleRepository.existsByUserIdAndRoleId(userId, roleId)).thenReturn(true);

            // When
            userRoleService.addUserRole(userId, roleId);

            // Then
            verify(userRoleRepository).existsByUserIdAndRoleId(userId, roleId);
            verify(userRoleRepository, never()).save(any());
        }

        @Test
        @DisplayName("添加用户角色关联 - 用户ID为null")
        void testAddUserRole_NullUserId() {
            // When
            userRoleService.addUserRole(null, 1L);

            // Then
            verify(userRoleRepository, never()).existsByUserIdAndRoleId(any(), any());
            verify(userRoleRepository, never()).save(any());
        }

        @Test
        @DisplayName("添加用户角色关联 - 角色ID为null")
        void testAddUserRole_NullRoleId() {
            // When
            userRoleService.addUserRole(1L, null);

            // Then
            verify(userRoleRepository, never()).existsByUserIdAndRoleId(any(), any());
            verify(userRoleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("删除用户角色关联测试")
    class RemoveUserRoleTests {

        @Test
        @DisplayName("删除用户角色关联 - 成功")
        void testRemoveUserRole_Success() {
            // Given
            Long userId = 1L;
            Long roleId = 1L;
            when(userRoleRepository.deleteByUserIdAndRoleId(userId, roleId)).thenReturn(1);

            // When
            userRoleService.removeUserRole(userId, roleId);

            // Then
            verify(userRoleRepository).deleteByUserIdAndRoleId(userId, roleId);
        }

        @Test
        @DisplayName("删除用户角色关联 - 用户ID为null")
        void testRemoveUserRole_NullUserId() {
            // When
            userRoleService.removeUserRole(null, 1L);

            // Then
            verify(userRoleRepository, never()).deleteByUserIdAndRoleId(any(), any());
        }

        @Test
        @DisplayName("删除用户角色关联 - 角色ID为null")
        void testRemoveUserRole_NullRoleId() {
            // When
            userRoleService.removeUserRole(1L, null);

            // Then
            verify(userRoleRepository, never()).deleteByUserIdAndRoleId(any(), any());
        }
    }

    @Nested
    @DisplayName("删除用户所有角色关联测试")
    class RemoveAllUserRolesTests {

        @Test
        @DisplayName("删除用户的所有角色关联 - 成功")
        void testRemoveAllUserRoles_Success() {
            // Given
            Long userId = 1L;
            when(userRoleRepository.deleteByUserId(userId)).thenReturn(3);

            // When
            userRoleService.removeAllUserRoles(userId);

            // Then
            verify(userRoleRepository).deleteByUserId(userId);
        }

        @Test
        @DisplayName("删除用户的所有角色关联 - 用户ID为null")
        void testRemoveAllUserRoles_NullUserId() {
            // When
            userRoleService.removeAllUserRoles(null);

            // Then
            verify(userRoleRepository, never()).deleteByUserId(any());
        }
    }

    @Nested
    @DisplayName("删除角色所有用户关联测试")
    class RemoveAllRoleUsersTests {

        @Test
        @DisplayName("删除角色的所有用户关联 - 成功")
        void testRemoveAllRoleUsers_Success() {
            // Given
            Long roleId = 1L;
            when(userRoleRepository.deleteByRoleId(roleId)).thenReturn(2);

            // When
            userRoleService.removeAllRoleUsers(roleId);

            // Then
            verify(userRoleRepository).deleteByRoleId(roleId);
        }

        @Test
        @DisplayName("删除角色的所有用户关联 - 角色ID为null")
        void testRemoveAllRoleUsers_NullRoleId() {
            // When
            userRoleService.removeAllRoleUsers(null);

            // Then
            verify(userRoleRepository, never()).deleteByRoleId(any());
        }
    }
}