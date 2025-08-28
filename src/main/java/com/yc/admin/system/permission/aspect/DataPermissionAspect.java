package com.yc.admin.system.permission.aspect;

import com.yc.admin.system.permission.annotation.DataPermission;
import com.yc.admin.system.permission.context.DataPermissionContext;
import com.yc.admin.system.permission.filter.DataPermissionFilter;
import com.yc.admin.system.permission.service.DataPermissionService;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 数据权限AOP切面
 * 自动拦截带有@DataPermission注解的方法，应用数据权限过滤
 * 
 * @author yc
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DataPermissionAspect {
    
    private final DataPermissionFilter dataPermissionFilter;
    private final DataPermissionService dataPermissionService;
    
    /**
     * 定义切点：拦截带有@DataPermission注解的方法
     */
    @Pointcut("@annotation(com.yc.admin.system.permission.annotation.DataPermission) || " +
              "@within(com.yc.admin.system.permission.annotation.DataPermission)")
    public void dataPermissionPointcut() {
    }
    
    /**
     * 环绕通知：在方法执行前后应用数据权限过滤
     */
    @Around("dataPermissionPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取@DataPermission注解
        DataPermission dataPermission = getDataPermissionAnnotation(method);
        
        if (dataPermission == null || !dataPermission.enabled()) {
            // 没有注解或未启用权限过滤，直接执行方法
            return joinPoint.proceed();
        }
        
        try {
            // 应用数据权限过滤
            applyDataPermission(dataPermission, method);
            
            // 执行目标方法
            return joinPoint.proceed();
            
        } finally {
            // 清理权限上下文
            DataPermissionContext.clear();
        }
    }
    
    /**
     * 获取@DataPermission注解
     * 优先从方法上获取，如果没有则从类上获取
     */
    private DataPermission getDataPermissionAnnotation(Method method) {
        // 先从方法上查找注解
        DataPermission annotation = AnnotationUtils.findAnnotation(method, DataPermission.class);
        if (annotation != null) {
            return annotation;
        }
        
        // 再从类上查找注解
        return AnnotationUtils.findAnnotation(method.getDeclaringClass(), DataPermission.class);
    }
    
    /**
     * 应用数据权限过滤
     */
    private void applyDataPermission(DataPermission dataPermission, Method method) {
        try {
            // 获取当前用户信息
            Long userId = dataPermissionService.getCurrentUserId();
            Long deptId = dataPermissionService.getCurrentUserDeptId();
            
            // 检查是否为超级管理员
            if (!dataPermission.ignoreSuperAdmin() && dataPermissionService.isSuperAdmin(userId)) {
                log.debug("当前用户为超级管理员，跳过数据权限过滤");
                return;
            }
            
            // 获取用户的数据权限范围
            var dataScope = dataPermissionService.getUserDataScope(userId);
            if (dataScope == null) {
                dataScope = dataPermission.value();
            }
            
            // 构建权限过滤条件
            List<String> conditions = new ArrayList<>();
            
            // 处理自定义权限条件
            if (StringUtils.hasText(dataPermission.condition())) {
                String customCondition = processCustomCondition(dataPermission.condition(), userId, deptId);
                if (StringUtils.hasText(customCondition)) {
                    conditions.add(customCondition);
                }
            }
            
            // 根据数据权限范围构建过滤条件
            String scopeCondition = buildScopeCondition(dataPermission, dataScope, userId, deptId);
            if (StringUtils.hasText(scopeCondition)) {
                conditions.add(scopeCondition);
            }
            
            // 合并所有权限条件
            if (!conditions.isEmpty()) {
                String finalCondition = dataPermissionFilter.mergeConditions(conditions, dataPermission.operator());
                if (StringUtils.hasText(finalCondition)) {
                    // 设置到权限上下文中
                    DataPermissionContext.setCondition(finalCondition);
                    DataPermissionContext.setTableAlias(dataPermission.tableAlias());
                    DataPermissionContext.setColumnName(dataPermission.columnName());
                    
                    log.debug("应用数据权限过滤 - 方法: {}, 条件: {}", method.getName(), finalCondition);
                }
            }
            
        } catch (Exception e) {
            log.error("应用数据权限过滤失败 - 方法: {}", method.getName(), e);
            // 权限过滤失败时，为了安全考虑，可以选择抛出异常或记录日志
            // 这里选择记录日志并继续执行，具体策略可根据业务需求调整
        }
    }
    
    /**
     * 根据数据权限范围构建过滤条件
     */
    private String buildScopeCondition(DataPermission dataPermission, 
                                      com.yc.admin.system.permission.enums.DataScope dataScope,
                                      Long userId, Long deptId) {
        
        String tableAlias = dataPermission.tableAlias();
        String columnName = dataPermission.columnName();
        
        switch (dataScope) {
            case SELF:
                return dataPermissionFilter.buildFilterCondition(
                    dataScope, userId, deptId, null, tableAlias, columnName);
                
            case DEPT:
                return dataPermissionFilter.buildFilterCondition(
                    dataScope, userId, deptId, null, tableAlias, columnName);
                
            case DEPT_AND_CHILD:
                return dataPermissionFilter.buildFilterCondition(
                    dataScope, userId, deptId, null, tableAlias, columnName);
                
            case CUSTOM:
                // 获取用户自定义部门权限
                List<Long> customDeptIds = dataPermissionService.getUserCustomDeptIds(userId);
                return dataPermissionFilter.buildFilterCondition(
                    dataScope, userId, deptId, customDeptIds, tableAlias, columnName);
                
            case ALL:
            default:
                return "";
        }
    }
    
    /**
     * 处理自定义权限条件（支持SpEL表达式）
     */
    private String processCustomCondition(String condition, Long userId, Long deptId) {
        if (!StringUtils.hasText(condition)) {
            return "";
        }
        
        // 简单的变量替换，实际项目中可以集成SpEL表达式解析
        return condition.replace("#{userId}", String.valueOf(userId))
                       .replace("#{deptId}", String.valueOf(deptId));
    }
    
    /**
     * 获取方法的完整名称（用于日志记录）
     */
    private String getMethodFullName(Method method) {
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }
}