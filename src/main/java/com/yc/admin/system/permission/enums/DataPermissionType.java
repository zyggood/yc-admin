package com.yc.admin.system.permission.enums;

/**
 * 数据权限类型枚举
 * 
 * @author yc
 * @since 2024-01-01
 */
public enum DataPermissionType {
    
    /**
     * 用户数据权限
     */
    USER("user", "用户数据权限", "create_by"),
    
    /**
     * 部门数据权限
     */
    DEPT("dept", "部门数据权限", "dept_id"),
    
    /**
     * 自定义数据权限
     */
    CUSTOM("custom", "自定义数据权限", "");
    
    private final String code;
    private final String description;
    private final String defaultColumn;
    
    DataPermissionType(String code, String description, String defaultColumn) {
        this.code = code;
        this.description = description;
        this.defaultColumn = defaultColumn;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getDefaultColumn() {
        return defaultColumn;
    }
    
    /**
     * 根据代码获取枚举
     * 
     * @param code 代码
     * @return 数据权限类型枚举
     */
    public static DataPermissionType fromCode(String code) {
        for (DataPermissionType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return USER; // 默认返回用户权限
    }
}