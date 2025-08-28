package com.yc.admin.system.permission.service;

import com.yc.admin.auth.dto.AuthLoginUser;
import com.yc.admin.system.permission.enums.DataScope;
import com.yc.admin.system.user.entity.User;
import com.yc.admin.system.user.repository.UserRepository;
import com.yc.admin.system.dept.entity.Dept;
import com.yc.admin.system.dept.repository.DeptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据权限服务
 * 提供数据权限相关的核心功能
 * 
 * @author yc
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataPermissionService {
    
    private final UserRepository userRepository;
    private final DeptRepository deptRepository;
    private final UserDataPermissionService userDataPermissionService;
    
    /**
     * 获取当前登录用户ID
     */
    public Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof AuthLoginUser user) {
                    return user.getUserId();
                } else if (principal instanceof String) { // TODO: 有为String的情况吗？
                    // 如果principal是用户名，需要查询用户ID
                    String username = (String) principal;
                    return userRepository.findByUserName(username)
                            .map(User::getId)
                            .orElse(null);
                }
            }
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
        }
        return null;
    }
    
    /**
     * 获取当前登录用户的部门ID
     */
    public Long getCurrentUserDeptId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        
        return userRepository.findById(userId)
                .map(User::getDeptId)
                .orElse(null);
    }
    
    /**
     * 获取当前登录用户信息
     */
    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        
        return userRepository.findById(userId).orElse(null);
    }
    
    /**
     * 判断用户是否为超级管理员
     */
    public boolean isSuperAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        
        return userRepository.findById(userId)
                .map(user -> "admin".equals(user.getUserName()) || user.isAdmin())
                .orElse(false);
    }
    
    /**
     * 获取用户的数据权限范围
     */
    @Cacheable(value = "userDataScope", key = "#userId")
    public DataScope getUserDataScope(Long userId) {
        if (userId == null) {
            return DataScope.SELF;
        }
        
        // 先从用户自定义配置中获取
        DataScope customScope = userDataPermissionService.getUserDataPermissionScope(userId);
        if (customScope != null) {
            return customScope;
        }
        
        // 如果没有自定义配置，根据用户角色确定默认权限范围
        return getDefaultDataScopeByUser(userId);
    }
    
    /**
     * 根据用户角色获取默认数据权限范围
     */
    private DataScope getDefaultDataScopeByUser(Long userId) {
        // 这里可以根据用户的角色来确定默认的数据权限范围
        // 简化实现：管理员默认全部数据，普通用户默认仅本人数据
        if (isSuperAdmin(userId)) {
            return DataScope.ALL;
        }
        
        return userRepository.findById(userId)
                .map(user -> {
                    // 可以根据用户的角色或其他属性来确定默认权限
                    // 这里简化为根据用户类型判断
                    if ("manager".equals(user.getUserType())) {
                        return DataScope.DEPT_AND_CHILD;
                    } else if ("leader".equals(user.getUserType())) {
                        return DataScope.DEPT;
                    } else {
                        return DataScope.SELF;
                    }
                })
                .orElse(DataScope.SELF);
    }
    
    /**
     * 获取用户的自定义部门权限列表
     */
    @Cacheable(value = "userCustomDepts", key = "#userId")
    public List<Long> getUserCustomDeptIds(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        return userDataPermissionService.getUserCustomDeptIds(userId);
    }
    
    /**
     * 获取部门及其所有子部门ID列表
     */
    @Cacheable(value = "deptAndChildren", key = "#deptId")
    public List<Long> getDeptAndChildrenIds(Long deptId) {
        if (deptId == null) {
            return Collections.emptyList();
        }
        
        List<Long> result = new ArrayList<>();
        result.add(deptId);
        
        // 递归获取所有子部门
        List<Long> childrenIds = getChildDeptIds(deptId);
        result.addAll(childrenIds);
        
        return result;
    }
    
    /**
     * 获取部门的所有子部门ID列表（递归）
     */
    private List<Long> getChildDeptIds(Long parentDeptId) {
        List<Dept> childDepts = deptRepository.findByParentIdAndDelFlagOrderByOrderNumAsc(parentDeptId, 0);
        if (CollectionUtils.isEmpty(childDepts)) {
            return Collections.emptyList();
        }
        
        List<Long> result = new ArrayList<>();
        for (Dept dept : childDepts) {
            result.add(dept.getId());
            // 递归获取子部门的子部门
            result.addAll(getChildDeptIds(dept.getId()));
        }
        
        return result;
    }
    
    /**
     * 获取部门的所有父部门ID列表
     */
    @Cacheable(value = "deptParents", key = "#deptId")
    public List<Long> getParentDeptIds(Long deptId) {
        if (deptId == null) {
            return Collections.emptyList();
        }
        
        List<Long> result = new ArrayList<>();
        Long currentDeptId = deptId;
        
        while (currentDeptId != null) {
            Optional<Dept> deptOpt = deptRepository.findById(currentDeptId);
            if (deptOpt.isEmpty()) {
                break;
            }
            
            Dept dept = deptOpt.get();
            Long parentId = dept.getParentId();
            if (parentId != null && !parentId.equals(0L)) {
                result.add(parentId);
                currentDeptId = parentId;
            } else {
                break;
            }
        }
        
        return result;
    }
    
    /**
     * 检查用户是否有访问指定部门数据的权限
     */
    public boolean hasAccessToDept(Long userId, Long targetDeptId) {
        if (userId == null || targetDeptId == null) {
            return false;
        }
        
        // 超级管理员有所有权限
        if (isSuperAdmin(userId)) {
            return true;
        }
        
        DataScope dataScope = getUserDataScope(userId);
        Long userDeptId = getCurrentUserDeptId();
        
        switch (dataScope) {
            case ALL:
                return true;
                
            case SELF:
                // 只能访问自己的数据，不能访问部门数据
                return false;
                
            case DEPT:
                // 只能访问本部门数据
                return Objects.equals(userDeptId, targetDeptId);
                
            case DEPT_AND_CHILD:
                // 可以访问本部门及子部门数据
                if (Objects.equals(userDeptId, targetDeptId)) {
                    return true;
                }
                List<Long> childDeptIds = getDeptAndChildrenIds(userDeptId);
                return childDeptIds.contains(targetDeptId);
                
            case CUSTOM:
                // 可以访问自定义部门数据
                List<Long> customDeptIds = getUserCustomDeptIds(userId);
                return customDeptIds.contains(targetDeptId);
                
            default:
                return false;
        }
    }
    
    /**
     * 获取用户可访问的所有部门ID列表
     */
    public List<Long> getAccessibleDeptIds(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        // 超级管理员可以访问所有部门
        if (isSuperAdmin(userId)) {
            return deptRepository.findAll().stream()
                    .map(Dept::getId)
                    .collect(Collectors.toList());
        }
        
        DataScope dataScope = getUserDataScope(userId);
        Long userDeptId = getCurrentUserDeptId();
        
        switch (dataScope) {
            case ALL:
                return deptRepository.findAll().stream()
                        .map(Dept::getId)
                        .collect(Collectors.toList());
                        
            case SELF:
                return Collections.emptyList();
                
            case DEPT:
                return userDeptId != null ? Arrays.asList(userDeptId) : Collections.emptyList();
                
            case DEPT_AND_CHILD:
                return userDeptId != null ? getDeptAndChildrenIds(userDeptId) : Collections.emptyList();
                
            case CUSTOM:
                return getUserCustomDeptIds(userId);
                
            default:
                return Collections.emptyList();
        }
    }
    
    /**
     * 设置用户的数据权限范围
     */
    public void setUserDataScope(Long userId, DataScope dataScope) {
        userDataPermissionService.setUserDataScope(userId, dataScope);
        // 清除缓存
        clearUserDataScopeCache(userId);
    }
    
    /**
     * 设置用户的自定义部门权限
     */
    public void setUserCustomDeptIds(Long userId, List<Long> deptIds) {
        userDataPermissionService.setUserDataScope(userId, DataScope.CUSTOM, deptIds);
        // 清除缓存
        clearUserCustomDeptsCache(userId);
    }
    
    /**
     * 清除用户数据权限相关缓存
     */
    public void clearUserDataPermissionCache(Long userId) {
        clearUserDataScopeCache(userId);
        clearUserCustomDeptsCache(userId);
    }
    
    /**
     * 清除用户数据权限范围缓存
     */
    private void clearUserDataScopeCache(Long userId) {
        // 这里应该调用缓存管理器清除指定key的缓存
        // 简化实现，实际项目中需要注入CacheManager
        log.debug("清除用户数据权限范围缓存: userId={}", userId);
    }
    
    /**
     * 清除用户自定义部门权限缓存
     */
    private void clearUserCustomDeptsCache(Long userId) {
        // 这里应该调用缓存管理器清除指定key的缓存
        // 简化实现，实际项目中需要注入CacheManager
        log.debug("清除用户自定义部门权限缓存: userId={}", userId);
    }
}