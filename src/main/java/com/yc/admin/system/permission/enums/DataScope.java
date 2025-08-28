package com.yc.admin.system.permission.enums;

import lombok.Getter;

/**
 * 数据权限范围枚举
 * 
 * @author yc
 * @since 2024-01-01
 */
@Getter
public enum DataScope {
    
    /**
     * 全部数据权限
     */
    ALL("1", "全部数据权限"),
    
    /**
     * 自定数据权限
     */
    CUSTOM("2", "自定数据权限"),
    
    /**
     * 本部门数据权限
     */
    DEPT("3", "本部门数据权限"),
    
    /**
     * 本部门及以下数据权限
     */
    DEPT_AND_CHILD("4", "本部门及以下数据权限"),
    
    /**
     * 仅本人数据权限
     */
    SELF("5", "仅本人数据权限");
    
    private final String code;
    private final String description;
    
    DataScope(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     * 
     * @param code 代码
     * @return 数据权限范围枚举
     */
    public static DataScope fromCode(String code) {
        for (DataScope scope : values()) {
            if (scope.getCode().equals(code)) {
                return scope;
            }
        }
        return DEPT; // 默认返回部门权限
    }
    
    /**
     * 判断是否需要部门权限过滤
     * 
     * @return true：需要部门权限过滤，false：不需要
     */
    public boolean needDeptFilter() {
        return this == DEPT || this == DEPT_AND_CHILD || this == CUSTOM;
    }
    
    /**
     * 判断是否需要用户权限过滤
     * 
     * @return true：需要用户权限过滤，false：不需要
     */
    public boolean needUserFilter() {
        return this == SELF;
    }
    
    /**
     * 判断是否包含子部门
     * 
     * @return true：包含子部门，false：不包含
     */
    public boolean includeChildDept() {
        return this == DEPT_AND_CHILD;
    }
}