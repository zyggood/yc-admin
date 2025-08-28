package com.yc.admin.system.permission.service;

import com.yc.admin.system.permission.entity.UserDataPermission;
import com.yc.admin.system.permission.enums.DataScope;
import com.yc.admin.system.permission.repository.UserDataPermissionRepository;
import com.yc.admin.system.user.entity.User;
import com.yc.admin.system.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户数据权限服务测试类
 *
 * @author yc
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
class UserDataPermissionServiceTest {

    @Mock
    private UserDataPermissionRepository userDataPermissionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDataPermissionService userDataPermissionService;

    private User testUser;
    private UserDataPermission testPermission;

    @BeforeEach
    void setUp() {
        // 设置测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testuser");
        testUser.setDeptId(100L);

        // 设置测试权限
        testPermission = new UserDataPermission();
        testPermission.setId(1L);
        testPermission.setUserId(1L);
        testPermission.setDataScopeEnum(DataScope.DEPT_AND_CHILD);
        testPermission.setCustomDeptIds("100,101,102");
    }

    @Test
    void testGetUserDataPermissionScope() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.of(testPermission));

        // 执行测试
        DataScope scope = userDataPermissionService.getUserDataPermissionScope(1L);

        // 验证结果
        assertEquals(DataScope.DEPT_AND_CHILD, scope);
        verify(userDataPermissionRepository).findByUserId(1L);
    }

    @Test
    void testGetUserDataPermissionScopeWhenNotFound() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        // 执行测试
        DataScope scope = userDataPermissionService.getUserDataPermissionScope(1L);

        // 验证结果
        assertEquals(DataScope.SELF, scope); // 默认返回仅本人权限
        verify(userDataPermissionRepository).findByUserId(1L);
    }

    @Test
    void testGetUserCustomDeptIds() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.of(testPermission));

        // 执行测试
        List<Long> deptIds = userDataPermissionService.getUserCustomDeptIds(1L);

        // 验证结果
        assertEquals(Arrays.asList(100L, 101L, 102L), deptIds);
        verify(userDataPermissionRepository).findByUserId(1L);
    }

    @Test
    void testGetUserCustomDeptIdsWhenEmpty() {
        // 准备数据
        testPermission.setCustomDeptIds("");
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.of(testPermission));

        // 执行测试
        List<Long> deptIds = userDataPermissionService.getUserCustomDeptIds(1L);

        // 验证结果
        assertTrue(deptIds.isEmpty());
    }

    @Test
    void testGetUserCustomDeptIdsWhenNotFound() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        // 执行测试
        List<Long> deptIds = userDataPermissionService.getUserCustomDeptIds(1L);

        // 验证结果
        assertTrue(deptIds.isEmpty());
    }

    @Test
    void testSetUserDataScope() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.of(testPermission));
        when(userDataPermissionRepository.save(any(UserDataPermission.class)))
                .thenReturn(testPermission);

        // 执行测试
        userDataPermissionService.setUserDataScope(1L, DataScope.CUSTOM);

        // 验证结果
        verify(userDataPermissionRepository).findByUserId(1L);
        verify(userDataPermissionRepository).save(any(UserDataPermission.class));
    }

    @Test
    void testSetUserDataScopeWithCustomDepts() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.of(testPermission));
        when(userDataPermissionRepository.save(any(UserDataPermission.class)))
                .thenReturn(testPermission);

        // 执行测试
        userDataPermissionService.setUserDataScope(1L, DataScope.CUSTOM, Arrays.asList(200L, 201L));

        // 验证结果
        verify(userDataPermissionRepository).findByUserId(1L);
        verify(userDataPermissionRepository).save(any(UserDataPermission.class));
        assertEquals(DataScope.CUSTOM, testPermission.getDataScopeEnum());
        assertEquals("200,201", testPermission.getCustomDeptIds());
    }

    @Test
    void testSetUserDataScopeWhenNotExists() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(2L))
                .thenReturn(Optional.empty());
        when(userDataPermissionRepository.save(any(UserDataPermission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 执行测试
        userDataPermissionService.setUserDataScope(2L, DataScope.CUSTOM, Arrays.asList(200L, 201L));

        // 验证结果
        verify(userDataPermissionRepository).findByUserId(2L);
        verify(userDataPermissionRepository).save(any(UserDataPermission.class));
    }

    @Test
    void testDeleteUserDataPermission() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.of(testPermission));

        // 执行测试
        userDataPermissionService.deleteUserDataPermission(1L);

        // 验证结果
        verify(userDataPermissionRepository).findByUserId(1L);
        verify(userDataPermissionRepository).delete(testPermission);
    }

    @Test
    void testDeleteUserDataPermissionWhenNotFound() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        // 执行测试
        userDataPermissionService.deleteUserDataPermission(1L);

        // 验证结果
        verify(userDataPermissionRepository).findByUserId(1L);
        verify(userDataPermissionRepository, never()).delete(any());
    }

    @Test
    void testGetUserDataPermission() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.of(testPermission));

        // 执行测试
        Optional<UserDataPermission> result = userDataPermissionService.getUserDataPermission(1L);

        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(testPermission, result.get());
        verify(userDataPermissionRepository).findByUserId(1L);
    }

    @Test
    void testGetUserDataPermissionWhenNotFound() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        // 执行测试
        Optional<UserDataPermission> result = userDataPermissionService.getUserDataPermission(1L);

        // 验证结果
        assertFalse(result.isPresent());
        verify(userDataPermissionRepository).findByUserId(1L);
    }

    @Test
    void testHasUserDataPermission() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.of(testPermission));

        // 执行测试
        boolean hasPermission = userDataPermissionService.hasUserDataPermission(1L);

        // 验证结果
        assertTrue(hasPermission);
        verify(userDataPermissionRepository).findByUserId(1L);
    }

    @Test
    void testHasUserDataPermissionWhenNotFound() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        // 执行测试
        boolean hasPermission = userDataPermissionService.hasUserDataPermission(1L);

        // 验证结果
        assertFalse(hasPermission);
        verify(userDataPermissionRepository).findByUserId(1L);
    }

    @Test
    void testHasAccessToDept() {
        // 准备数据
        when(userDataPermissionRepository.findByUserId(1L))
                .thenReturn(Optional.of(testPermission));

        // 执行测试
        boolean hasAccess = userDataPermissionService.hasAccessToDept(1L, 100L);

        // 验证结果
        assertTrue(hasAccess);
        verify(userDataPermissionRepository).findByUserId(1L);
    }
}