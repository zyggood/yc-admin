package com.yc.admin.system.permission.filter;

import com.yc.admin.system.permission.constants.DataPermissionConstants;
import com.yc.admin.system.permission.enums.DataScope;
import com.yc.admin.system.permission.enums.DataPermissionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限过滤器
 * 负责根据用户的数据权限范围生成相应的SQL过滤条件
 * 
 * @author yc
 * @since 2024-01-01
 */
@Slf4j
@Component
public class DataPermissionFilter {
    
    /**
     * 构建数据权限过滤条件
     * 
     * @param dataScope 数据权限范围
     * @param userId 用户ID
     * @param deptId 部门ID
     * @param deptIds 自定义部门ID列表
     * @param tableAlias 表别名
     * @param columnName 字段名
     * @return SQL过滤条件
     */
    public String buildFilterCondition(DataScope dataScope, Long userId, Long deptId, 
                                      List<Long> deptIds, String tableAlias, String columnName) {
        if (dataScope == null) {
            log.warn("数据权限范围为空，跳过权限过滤");
            return "";
        }
        
        String alias = StringUtils.hasText(tableAlias) ? tableAlias : DataPermissionConstants.DEFAULT_ALIAS;
        String column = StringUtils.hasText(columnName) ? columnName : getDefaultColumn(dataScope);
        
        switch (dataScope) {
            case ALL:
                // 全部数据权限，不添加过滤条件
                return "";
                
            case SELF:
                // 仅本人数据权限
                return buildUserFilter(alias, column, userId);
                
            case DEPT:
                // 本部门数据权限
                return buildDeptFilter(alias, column, deptId);
                
            case DEPT_AND_CHILD:
                // 本部门及以下数据权限
                return buildDeptAndChildFilter(alias, column, deptId);
                
            case CUSTOM:
                // 自定义数据权限
                return buildCustomDeptFilter(alias, column, deptIds);
                
            default:
                log.warn("未知的数据权限范围: {}", dataScope);
                return "";
        }
    }
    
    /**
     * 构建用户权限过滤条件
     * 
     * @param alias 表别名
     * @param column 字段名
     * @param userId 用户ID
     * @return SQL过滤条件
     */
    private String buildUserFilter(String alias, String column, Long userId) {
        if (userId == null) {
            log.warn("用户ID为空，无法构建用户权限过滤条件");
            return "";
        }
        
        return DataPermissionConstants.USER_FILTER_TEMPLATE
                .replace("{alias}", alias)
                .replace("{column}", column)
                .replace("{userId}", userId.toString());
    }
    
    /**
     * 构建部门权限过滤条件
     * 
     * @param alias 表别名
     * @param column 字段名
     * @param deptId 部门ID
     * @return SQL过滤条件
     */
    private String buildDeptFilter(String alias, String column, Long deptId) {
        if (deptId == null) {
            log.warn("部门ID为空，无法构建部门权限过滤条件");
            return "";
        }
        
        return DataPermissionConstants.DEPT_FILTER_TEMPLATE
                .replace("{alias}", alias)
                .replace("{column}", column)
                .replace("{deptId}", deptId.toString());
    }
    
    /**
     * 构建部门及子部门权限过滤条件
     * 
     * @param alias 表别名
     * @param column 字段名
     * @param deptId 部门ID
     * @return SQL过滤条件
     */
    private String buildDeptAndChildFilter(String alias, String column, Long deptId) {
        if (deptId == null) {
            log.warn("部门ID为空，无法构建部门及子部门权限过滤条件");
            return "";
        }
        
        return DataPermissionConstants.DEPT_AND_CHILD_FILTER_TEMPLATE
                .replace("{alias}", alias)
                .replace("{column}", column)
                .replace("{deptId}", deptId.toString());
    }
    
    /**
     * 构建自定义部门权限过滤条件
     * 
     * @param alias 表别名
     * @param column 字段名
     * @param deptIds 部门ID列表
     * @return SQL过滤条件
     */
    private String buildCustomDeptFilter(String alias, String column, List<Long> deptIds) {
        if (CollectionUtils.isEmpty(deptIds)) {
            log.warn("自定义部门ID列表为空，无法构建自定义部门权限过滤条件");
            return "";
        }
        
        String deptIdStr = deptIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        
        return DataPermissionConstants.CUSTOM_DEPT_FILTER_TEMPLATE
                .replace("{alias}", alias)
                .replace("{column}", column)
                .replace("{deptIds}", deptIdStr);
    }
    
    /**
     * 合并多个过滤条件
     * 
     * @param conditions 过滤条件列表
     * @param operator 操作符（AND/OR）
     * @return 合并后的过滤条件
     */
    public String mergeConditions(List<String> conditions, String operator) {
        if (CollectionUtils.isEmpty(conditions)) {
            return "";
        }
        
        List<String> validConditions = conditions.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        
        if (validConditions.isEmpty()) {
            return "";
        }
        
        if (validConditions.size() == 1) {
            return validConditions.get(0);
        }
        
        String op = StringUtils.hasText(operator) ? operator : DataPermissionConstants.AND;
        return validConditions.stream()
                .map(condition -> "(" + condition + ")")
                .collect(Collectors.joining(" " + op + " "));
    }
    
    /**
     * 获取默认字段名
     * 
     * @param dataScope 数据权限范围
     * @return 默认字段名
     */
    private String getDefaultColumn(DataScope dataScope) {
        switch (dataScope) {
            case SELF:
                return DataPermissionConstants.CREATE_BY_COLUMN;
            case DEPT:
            case DEPT_AND_CHILD:
            case CUSTOM:
                return DataPermissionConstants.DEPT_ID_COLUMN;
            default:
                return DataPermissionConstants.CREATE_BY_COLUMN;
        }
    }
    
    /**
     * 验证过滤条件是否有效
     * 
     * @param condition 过滤条件
     * @return true：有效，false：无效
     */
    public boolean isValidCondition(String condition) {
        return StringUtils.hasText(condition) && 
               !condition.trim().isEmpty() && 
               !condition.contains("null") &&
               !condition.contains("undefined");
    }
    
}