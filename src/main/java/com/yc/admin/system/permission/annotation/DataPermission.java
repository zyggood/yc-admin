package com.yc.admin.system.permission.annotation;

import com.yc.admin.system.permission.enums.DataScope;
import com.yc.admin.system.permission.enums.DataPermissionType;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 用于标记需要进行数据权限过滤的方法
 * 
 * @author yc
 * @since 2024-01-01
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {
    
    /**
     * 数据权限范围
     * 默认为全部数据权限
     */
    DataScope value() default DataScope.ALL;
    
    /**
     * 数据权限类型
     * 默认为用户数据权限
     */
    DataPermissionType type() default DataPermissionType.USER;
    
    /**
     * 表别名
     * 用于多表查询时指定主表别名
     */
    String tableAlias() default "";
    
    /**
     * 权限字段名
     * 用于指定权限过滤的字段名
     */
    String columnName() default "";
    
    /**
     * 是否启用权限过滤
     * 默认启用
     */
    boolean enabled() default true;
    
    /**
     * 权限过滤条件的逻辑操作符
     * 当有多个权限条件时使用（AND/OR）
     */
    String operator() default "AND";
    
    /**
     * 自定义权限过滤条件
     * 支持SpEL表达式
     */
    String condition() default "";
    
    /**
     * 是否忽略超级管理员权限
     * 默认false，超级管理员不受数据权限限制
     */
    boolean ignoreSuperAdmin() default false;
    
    /**
     * 权限过滤的优先级
     * 数值越小优先级越高
     */
    int order() default 0;
    
    /**
     * 权限描述
     * 用于日志记录和调试
     */
    String description() default "";
}