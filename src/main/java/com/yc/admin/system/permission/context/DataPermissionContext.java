package com.yc.admin.system.permission.context;

/**
 * 数据权限上下文
 * 用于在当前线程中存储数据权限过滤条件
 * 
 * @author yc
 * @since 2024-01-01
 */
public class DataPermissionContext {
    
    private static final ThreadLocal<String> CONDITION_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> TABLE_ALIAS_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> COLUMN_NAME_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> ENABLED_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置权限过滤条件
     */
    public static void setCondition(String condition) {
        CONDITION_HOLDER.set(condition);
    }
    
    /**
     * 获取权限过滤条件
     */
    public static String getCondition() {
        return CONDITION_HOLDER.get();
    }
    
    /**
     * 设置表别名
     */
    public static void setTableAlias(String tableAlias) {
        TABLE_ALIAS_HOLDER.set(tableAlias);
    }
    
    /**
     * 获取表别名
     */
    public static String getTableAlias() {
        return TABLE_ALIAS_HOLDER.get();
    }
    
    /**
     * 设置列名
     */
    public static void setColumnName(String columnName) {
        COLUMN_NAME_HOLDER.set(columnName);
    }
    
    /**
     * 获取列名
     */
    public static String getColumnName() {
        return COLUMN_NAME_HOLDER.get();
    }
    
    /**
     * 设置是否启用数据权限
     */
    public static void setEnabled(boolean enabled) {
        ENABLED_HOLDER.set(enabled);
    }
    
    /**
     * 获取是否启用数据权限
     */
    public static boolean isEnabled() {
        Boolean enabled = ENABLED_HOLDER.get();
        return enabled != null && enabled;
    }
    
    /**
     * 检查是否有权限条件
     */
    public static boolean hasCondition() {
        String condition = getCondition();
        return condition != null && !condition.trim().isEmpty();
    }
    
    /**
     * 清理当前线程的权限上下文
     */
    public static void clear() {
        CONDITION_HOLDER.remove();
        TABLE_ALIAS_HOLDER.remove();
        COLUMN_NAME_HOLDER.remove();
        ENABLED_HOLDER.remove();
    }
    
    /**
     * 获取完整的权限信息
     */
    public static DataPermissionInfo getPermissionInfo() {
        return DataPermissionInfo.builder()
                .condition(getCondition())
                .tableAlias(getTableAlias())
                .columnName(getColumnName())
                .enabled(isEnabled())
                .build();
    }
    
    /**
     * 数据权限信息封装类
     */
    public static class DataPermissionInfo {
        private String condition;
        private String tableAlias;
        private String columnName;
        private boolean enabled;
        
        private DataPermissionInfo(Builder builder) {
            this.condition = builder.condition;
            this.tableAlias = builder.tableAlias;
            this.columnName = builder.columnName;
            this.enabled = builder.enabled;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public String getCondition() {
            return condition;
        }
        
        public String getTableAlias() {
            return tableAlias;
        }
        
        public String getColumnName() {
            return columnName;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public static class Builder {
            private String condition;
            private String tableAlias;
            private String columnName;
            private boolean enabled;
            
            public Builder condition(String condition) {
                this.condition = condition;
                return this;
            }
            
            public Builder tableAlias(String tableAlias) {
                this.tableAlias = tableAlias;
                return this;
            }
            
            public Builder columnName(String columnName) {
                this.columnName = columnName;
                return this;
            }
            
            public Builder enabled(boolean enabled) {
                this.enabled = enabled;
                return this;
            }
            
            public DataPermissionInfo build() {
                return new DataPermissionInfo(this);
            }
        }
    }
}