package com.yc.admin.system.permission.constants;

/**
 * 数据权限常量
 * 
 * @author yc
 * @since 2024-01-01
 */
public final class DataPermissionConstants {
    
    private DataPermissionConstants() {
        // 工具类，禁止实例化
    }
    
    // ==================== SQL 关键字 ====================
    
    /** WHERE 关键字 */
    public static final String WHERE = "WHERE";
    
    /** AND 关键字 */
    public static final String AND = "AND";
    
    /** OR 关键字 */
    public static final String OR = "OR";
    
    /** IN 关键字 */
    public static final String IN = "IN";
    
    /** EXISTS 关键字 */
    public static final String EXISTS = "EXISTS";
    
    // ==================== 默认字段名 ====================
    
    /** 创建者字段 */
    public static final String CREATE_BY_COLUMN = "create_by";
    
    /** 部门ID字段 */
    public static final String DEPT_ID_COLUMN = "dept_id";
    
    /** 用户ID字段 */
    public static final String USER_ID_COLUMN = "user_id";
    
    // ==================== 表别名 ====================
    
    /** 默认表别名 */
    public static final String DEFAULT_ALIAS = "t";
    
    /** 用户表别名 */
    public static final String USER_ALIAS = "u";
    
    /** 部门表别名 */
    public static final String DEPT_ALIAS = "d";
    
    // ==================== 权限过滤模板 ====================
    
    /** 用户权限过滤模板 */
    public static final String USER_FILTER_TEMPLATE = "{alias}.{column} = {userId}";
    
    /** 部门权限过滤模板 */
    public static final String DEPT_FILTER_TEMPLATE = "{alias}.{column} = {deptId}";
    
    /** 部门及子部门权限过滤模板 */
    public static final String DEPT_AND_CHILD_FILTER_TEMPLATE = 
        "{alias}.{column} IN (SELECT dept_id FROM sys_dept WHERE dept_id = {deptId} OR find_in_set({deptId}, ancestors))";
    
    /** 自定义部门权限过滤模板 */
    public static final String CUSTOM_DEPT_FILTER_TEMPLATE = 
        "{alias}.{column} IN ({deptIds})";
    
    // ==================== 缓存相关 ====================
    
    /** 数据权限缓存前缀 */
    public static final String DATA_PERMISSION_CACHE_PREFIX = "data_permission:";
    
    /** 用户数据权限缓存键 */
    public static final String USER_DATA_PERMISSION_CACHE_KEY = DATA_PERMISSION_CACHE_PREFIX + "user:";
    
    /** 角色数据权限缓存键 */
    public static final String ROLE_DATA_PERMISSION_CACHE_KEY = DATA_PERMISSION_CACHE_PREFIX + "role:";
    
    /** 部门权限缓存键 */
    public static final String DEPT_PERMISSION_CACHE_KEY = DATA_PERMISSION_CACHE_PREFIX + "dept:";
    
    // ==================== 配置相关 ====================
    
    /** 数据权限开关配置键 */
    public static final String DATA_PERMISSION_ENABLED_KEY = "sys.data.permission.enabled";
    
    /** 数据权限默认开启 */
    public static final String DATA_PERMISSION_ENABLED_DEFAULT = "true";
    
    /** 超级管理员角色键 */
    public static final String SUPER_ADMIN_ROLE_KEY = "admin";
}