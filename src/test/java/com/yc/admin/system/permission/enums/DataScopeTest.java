package com.yc.admin.system.permission.enums;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据权限范围枚举测试类
 *
 * @author yc
 * @since 2024-01-01
 */
@SpringBootTest
class DataScopeTest {

    /**
     * 测试枚举值的完整性
     */
    @Test
    void testDataScopeValues() {
        DataScope[] values = DataScope.values();
        
        assertEquals(5, values.length);
        
        // 验证所有枚举值都存在
        assertTrue(containsValue(values, DataScope.ALL));
        assertTrue(containsValue(values, DataScope.CUSTOM));
        assertTrue(containsValue(values, DataScope.DEPT));
        assertTrue(containsValue(values, DataScope.DEPT_AND_CHILD));
        assertTrue(containsValue(values, DataScope.SELF));
    }

    /**
     * 测试ALL权限范围
     */
    @Test
    void testAllScope() {
        DataScope scope = DataScope.ALL;
        
        assertNotNull(scope);
        assertEquals("ALL", scope.name());
        assertEquals("全部数据权限", scope.getDescription());
        assertEquals("1", scope.getCode());
    }

    /**
     * 测试CUSTOM权限范围
     */
    @Test
    void testCustomScope() {
        DataScope scope = DataScope.CUSTOM;
        
        assertNotNull(scope);
        assertEquals("CUSTOM", scope.name());
        assertEquals("自定数据权限", scope.getDescription());
        assertEquals("2", scope.getCode());
    }

    /**
     * 测试DEPT权限范围
     */
    @Test
    void testDeptScope() {
        DataScope scope = DataScope.DEPT;
        
        assertNotNull(scope);
        assertEquals("DEPT", scope.name());
        assertEquals("本部门数据权限", scope.getDescription());
        assertEquals("3", scope.getCode());
    }

    /**
     * 测试DEPT_AND_CHILD权限范围
     */
    @Test
    void testDeptAndChildScope() {
        DataScope scope = DataScope.DEPT_AND_CHILD;
        
        assertNotNull(scope);
        assertEquals("DEPT_AND_CHILD", scope.name());
        assertEquals("本部门及以下数据权限", scope.getDescription());
        assertEquals("4", scope.getCode());
    }

    /**
     * 测试SELF权限范围
     */
    @Test
    void testSelfScope() {
        DataScope scope = DataScope.SELF;
        
        assertNotNull(scope);
        assertEquals("SELF", scope.name());
        assertEquals("仅本人数据权限", scope.getDescription());
        assertEquals("5", scope.getCode());
    }

    /**
     * 测试valueOf方法
     */
    @Test
    void testValueOf() {
        assertEquals(DataScope.ALL, DataScope.valueOf("ALL"));
        assertEquals(DataScope.CUSTOM, DataScope.valueOf("CUSTOM"));
        assertEquals(DataScope.DEPT, DataScope.valueOf("DEPT"));
        assertEquals(DataScope.DEPT_AND_CHILD, DataScope.valueOf("DEPT_AND_CHILD"));
        assertEquals(DataScope.SELF, DataScope.valueOf("SELF"));
    }

    /**
     * 测试valueOf方法异常情况
     */
    @Test
    void testValueOfWithInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            DataScope.valueOf("INVALID");
        });
    }

    /**
     * 测试根据代码获取枚举
     */
    @Test
    void testFromCode() {
        assertEquals(DataScope.ALL, DataScope.fromCode("1"));
        assertEquals(DataScope.CUSTOM, DataScope.fromCode("2"));
        assertEquals(DataScope.DEPT, DataScope.fromCode("3"));
        assertEquals(DataScope.DEPT_AND_CHILD, DataScope.fromCode("4"));
        assertEquals(DataScope.SELF, DataScope.fromCode("5"));
    }
    

    /**
     * 测试权限范围的代码顺序
     */
    @Test
    void testScopeCodeOrder() {
        // 验证代码的字符串顺序
        assertEquals("1", DataScope.ALL.getCode());
        assertEquals("2", DataScope.CUSTOM.getCode());
        assertEquals("3", DataScope.DEPT.getCode());
        assertEquals("4", DataScope.DEPT_AND_CHILD.getCode());
        assertEquals("5", DataScope.SELF.getCode());
    }

    /**
     * 测试权限过滤方法
     */
    @Test
    void testPermissionFilters() {
        // 测试needDeptFilter方法
        assertFalse(DataScope.ALL.needDeptFilter());
        assertTrue(DataScope.CUSTOM.needDeptFilter());
        assertTrue(DataScope.DEPT.needDeptFilter());
        assertTrue(DataScope.DEPT_AND_CHILD.needDeptFilter());
        assertFalse(DataScope.SELF.needDeptFilter());
        
        // 测试needUserFilter方法
        assertFalse(DataScope.ALL.needUserFilter());
        assertFalse(DataScope.CUSTOM.needUserFilter());
        assertFalse(DataScope.DEPT.needUserFilter());
        assertFalse(DataScope.DEPT_AND_CHILD.needUserFilter());
        assertTrue(DataScope.SELF.needUserFilter());
        
        // 测试includeChildDept方法
        assertFalse(DataScope.ALL.includeChildDept());
        assertFalse(DataScope.CUSTOM.includeChildDept());
        assertFalse(DataScope.DEPT.includeChildDept());
        assertTrue(DataScope.DEPT_AND_CHILD.includeChildDept());
        assertFalse(DataScope.SELF.includeChildDept());
    }

    /**
     * 测试枚举的字符串表示
     */
    @Test
    void testToString() {
        assertEquals("ALL", DataScope.ALL.toString());
        assertEquals("CUSTOM", DataScope.CUSTOM.toString());
        assertEquals("DEPT", DataScope.DEPT.toString());
        assertEquals("DEPT_AND_CHILD", DataScope.DEPT_AND_CHILD.toString());
        assertEquals("SELF", DataScope.SELF.toString());
    }

    /**
     * 辅助方法：检查数组中是否包含指定值
     */
    private boolean containsValue(DataScope[] values, DataScope target) {
        for (DataScope value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}