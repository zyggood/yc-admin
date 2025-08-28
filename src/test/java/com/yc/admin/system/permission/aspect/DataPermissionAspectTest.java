package com.yc.admin.system.permission.aspect;

import com.yc.admin.system.permission.annotation.DataPermission;
import com.yc.admin.system.permission.enums.DataScope;
import com.yc.admin.system.permission.service.DataPermissionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 数据权限切面测试类
 *
 * @author yc
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
class DataPermissionAspectTest {

    @Mock
    private DataPermissionService dataPermissionService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DataPermissionAspect dataPermissionAspect;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
    }

    @Test
    void testDataPermissionWithDeptAndChild() throws Throwable {
        // 准备数据
        Method method = createMockMethod(DataScope.DEPT_AND_CHILD, "u", "dept_id");
        when(methodSignature.getMethod()).thenReturn(method);
        when(dataPermissionService.getCurrentUserId()).thenReturn(1L);
        when(dataPermissionService.isSuperAdmin(1L)).thenReturn(false);
        when(dataPermissionService.getUserDataScope(1L)).thenReturn(DataScope.DEPT_AND_CHILD);
        when(dataPermissionService.getCurrentUserDeptId()).thenReturn(100L);
        when(dataPermissionService.getDeptAndChildrenIds(100L))
                .thenReturn(Arrays.asList(100L, 101L, 102L));
        
        Object expectedResult = "test result";
        when(joinPoint.proceed()).thenReturn(expectedResult);
        
        // 执行测试
        Object result = dataPermissionAspect.around(joinPoint);
        
        // 验证结果
        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
        verify(dataPermissionService).getCurrentUserId();
        verify(dataPermissionService).isSuperAdmin(1L);
        verify(dataPermissionService).getUserDataScope(1L);
    }

    @Test
    void testDataPermissionForSuperAdmin() throws Throwable {
        // 准备数据
        Method method = createMockMethod(DataScope.DEPT_AND_CHILD, "u", "dept_id");
        when(methodSignature.getMethod()).thenReturn(method);
        when(dataPermissionService.getCurrentUserId()).thenReturn(1L);
        when(dataPermissionService.isSuperAdmin(1L)).thenReturn(true);
        
        Object expectedResult = "test result";
        when(joinPoint.proceed()).thenReturn(expectedResult);
        
        // 执行测试
        Object result = dataPermissionAspect.around(joinPoint);
        
        // 验证结果
        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
        verify(dataPermissionService).getCurrentUserId();
        verify(dataPermissionService).isSuperAdmin(1L);
        // 超级管理员不需要进一步的权限检查
        verify(dataPermissionService, never()).getUserDataScope(anyLong());
    }

    @Test
    void testDataPermissionWithCustomScope() throws Throwable {
        // 准备数据
        Method method = createMockMethod(DataScope.CUSTOM, "u", "dept_id");
        when(methodSignature.getMethod()).thenReturn(method);
        when(dataPermissionService.getCurrentUserId()).thenReturn(1L);
        when(dataPermissionService.isSuperAdmin(1L)).thenReturn(false);
        when(dataPermissionService.getUserDataScope(1L)).thenReturn(DataScope.CUSTOM);
        when(dataPermissionService.getAccessibleDeptIds(1L))
                .thenReturn(Arrays.asList(200L, 201L));
        
        Object expectedResult = "test result";
        when(joinPoint.proceed()).thenReturn(expectedResult);
        
        // 执行测试
        Object result = dataPermissionAspect.around(joinPoint);
        
        // 验证结果
        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
        verify(dataPermissionService).getAccessibleDeptIds(1L);
    }

    @Test
    void testDataPermissionWithSelfOnlyScope() throws Throwable {
        // 准备数据
        Method method = createMockMethod(DataScope.SELF, "u", "user_id");
        when(methodSignature.getMethod()).thenReturn(method);
        when(dataPermissionService.getCurrentUserId()).thenReturn(1L);
        when(dataPermissionService.isSuperAdmin(1L)).thenReturn(false);
        when(dataPermissionService.getUserDataScope(1L)).thenReturn(DataScope.SELF);
        
        Object expectedResult = "test result";
        when(joinPoint.proceed()).thenReturn(expectedResult);
        
        // 执行测试
        Object result = dataPermissionAspect.around(joinPoint);
        
        // 验证结果
        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
    }

    @Test
    void testDataPermissionDisabled() throws Throwable {
        // 准备数据
        Method method = createMockMethodDisabled();
        when(methodSignature.getMethod()).thenReturn(method);
        
        Object expectedResult = "test result";
        when(joinPoint.proceed()).thenReturn(expectedResult);
        
        // 执行测试
        Object result = dataPermissionAspect.around(joinPoint);
        
        // 验证结果
        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
        // 权限被禁用，不应该调用任何权限检查方法
        verify(dataPermissionService, never()).getCurrentUserId();
    }

    @Test
    void testDataPermissionWhenNotAuthenticated() throws Throwable {
        // 准备数据
        when(securityContext.getAuthentication()).thenReturn(null);
        Method method = createMockMethod(DataScope.DEPT_AND_CHILD, "u", "dept_id");
        when(methodSignature.getMethod()).thenReturn(method);
        
        Object expectedResult = "test result";
        when(joinPoint.proceed()).thenReturn(expectedResult);
        
        // 执行测试
        Object result = dataPermissionAspect.around(joinPoint);
        
        // 验证结果
        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
        // 未认证用户不应该调用权限检查方法
        verify(dataPermissionService, never()).getCurrentUserId();
    }

    /**
     * 创建模拟的带有DataPermission注解的方法
     */
    private Method createMockMethod(DataScope scope, String tableAlias, String columnName) {
        try {
            Method method = TestService.class.getMethod("testMethod");
            DataPermission annotation = mock(DataPermission.class);
            when(annotation.value()).thenReturn(scope);
            when(annotation.tableAlias()).thenReturn(tableAlias);
            when(annotation.columnName()).thenReturn(columnName);
            when(annotation.enabled()).thenReturn(true);
            
            Method spyMethod = spy(method);
            when(spyMethod.getAnnotation(DataPermission.class)).thenReturn(annotation);
            return spyMethod;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建模拟的禁用DataPermission注解的方法
     */
    private Method createMockMethodDisabled() {
        try {
            Method method = TestService.class.getMethod("testMethod");
            DataPermission annotation = mock(DataPermission.class);
            when(annotation.enabled()).thenReturn(false);
            
            Method spyMethod = spy(method);
            when(spyMethod.getAnnotation(DataPermission.class)).thenReturn(annotation);
            return spyMethod;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试用的服务类
     */
    public static class TestService {
        public void testMethod() {
            // 测试方法
        }
    }
}