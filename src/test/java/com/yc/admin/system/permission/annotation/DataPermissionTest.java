package com.yc.admin.system.permission.annotation;

import com.yc.admin.system.permission.enums.DataScope;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据权限注解测试类
 *
 * @author yc
 * @since 2024-01-01
 */
@SpringBootTest
class DataPermissionTest {

    /**
     * 测试注解的默认值
     */
    @Test
    void testDataPermissionDefaultValues() throws NoSuchMethodException {
        Method method = TestService.class.getMethod("methodWithDefaultAnnotation");
        DataPermission annotation = method.getAnnotation(DataPermission.class);
        
        assertNotNull(annotation);
        assertEquals(DataScope.ALL, annotation.value());
        assertEquals("", annotation.tableAlias());
        assertEquals("", annotation.columnName());
    }

    /**
     * 测试注解的自定义值
     */
    @Test
    void testDataPermissionCustomValues() throws NoSuchMethodException {
        Method method = TestService.class.getMethod("methodWithCustomAnnotation");
        DataPermission annotation = method.getAnnotation(DataPermission.class);
        
        assertNotNull(annotation);
        assertEquals(DataScope.CUSTOM, annotation.value());
        assertEquals("u", annotation.tableAlias());
        assertEquals("create_by", annotation.columnName());
    }

    /**
     * 测试注解的SELF权限范围
     */
    @Test
    void testDataPermissionSelfScope() throws NoSuchMethodException {
        Method method = TestService.class.getMethod("methodWithSelfScope");
        DataPermission annotation = method.getAnnotation(DataPermission.class);
        
        assertNotNull(annotation);
        assertEquals(DataScope.SELF, annotation.value());
        assertEquals("t", annotation.tableAlias());
        assertEquals("user_id", annotation.columnName());
    }

    /**
     * 测试注解的ALL权限范围
     */
    @Test
    void testDataPermissionAllScope() throws NoSuchMethodException {
        Method method = TestService.class.getMethod("methodWithAllScope");
        DataPermission annotation = method.getAnnotation(DataPermission.class);
        
        assertNotNull(annotation);
        assertEquals(DataScope.ALL, annotation.value());
        assertEquals("", annotation.tableAlias());
        assertEquals("", annotation.columnName());
    }

    /**
     * 测试注解的DEPT权限范围
     */
    @Test
    void testDataPermissionDeptScope() throws NoSuchMethodException {
        Method method = TestService.class.getMethod("methodWithDeptScope");
        DataPermission annotation = method.getAnnotation(DataPermission.class);
        
        assertNotNull(annotation);
        assertEquals(DataScope.DEPT, annotation.value());
        assertEquals("d", annotation.tableAlias());
        assertEquals("dept_id", annotation.columnName());
    }

    /**
     * 测试方法没有注解的情况
     */
    @Test
    void testMethodWithoutAnnotation() throws NoSuchMethodException {
        Method method = TestService.class.getMethod("methodWithoutAnnotation");
        DataPermission annotation = method.getAnnotation(DataPermission.class);
        
        assertNull(annotation);
    }

    /**
     * 测试注解是否可以在运行时获取
     */
    @Test
    void testAnnotationRetentionPolicy() {
        DataPermission annotation = TestService.class.getAnnotation(DataPermission.class);
        // 类级别没有注解
        assertNull(annotation);
        
        // 验证注解在运行时可用
        Method[] methods = TestService.class.getDeclaredMethods();
        boolean hasAnnotatedMethod = false;
        for (Method method : methods) {
            if (method.isAnnotationPresent(DataPermission.class)) {
                hasAnnotatedMethod = true;
                break;
            }
        }
        assertTrue(hasAnnotatedMethod);
    }

    /**
     * 测试服务类，用于验证注解功能
     */
    static class TestService {
        
        @DataPermission
        public void methodWithDefaultAnnotation() {
            // 使用默认注解值的方法
        }
        
        @DataPermission(value = DataScope.CUSTOM, tableAlias = "u", columnName = "create_by")
        public void methodWithCustomAnnotation() {
            // 使用自定义注解值的方法
        }
        
        @DataPermission(value = DataScope.SELF, tableAlias = "t", columnName = "user_id")
        public void methodWithSelfScope() {
            // 使用SELF权限范围的方法
        }
        
        @DataPermission(value = DataScope.ALL, tableAlias = "", columnName = "")
        public void methodWithAllScope() {
            // 使用ALL权限范围的方法
        }
        
        @DataPermission(value = DataScope.DEPT, tableAlias = "d", columnName = "dept_id")
        public void methodWithDeptScope() {
            // 使用DEPT权限范围的方法
        }
        
        public void methodWithoutAnnotation() {
            // 没有注解的方法
        }
    }
}