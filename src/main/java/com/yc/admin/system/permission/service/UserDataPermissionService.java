package com.yc.admin.system.permission.service;

import com.yc.admin.system.permission.entity.UserDataPermission;
import com.yc.admin.system.permission.enums.DataScope;
import com.yc.admin.system.permission.repository.UserDataPermissionRepository;
import com.yc.admin.system.user.entity.User;
import com.yc.admin.system.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据权限服务
 * 负责处理用户数据权限相关的业务逻辑
 * 
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDataPermissionService {

    private final UserRepository userRepository;
    private final UserDataPermissionRepository userDataPermissionRepository;

    /**
     * 根据用户ID获取用户数据权限范围
     * 
     * @param userId 用户ID
     * @return 数据权限范围
     */
    @Cacheable(value = "userDataPermission", key = "#userId")
    public DataScope getUserDataPermissionScope(Long userId) {
        if (userId == null) {
            return DataScope.SELF;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.warn("用户不存在: {}", userId);
            return DataScope.SELF;
        }

        User user = userOpt.get();
        
        // 管理员拥有全部数据权限
        if (user.isAdmin()) {
            return DataScope.ALL;
        }

        // 首先检查是否有自定义数据权限配置
        Optional<UserDataPermission> permission = userDataPermissionRepository.findByUserIdAndDelFlag(userId, 0);
        if (permission.isPresent() && permission.get().isNormal()) {
            return permission.get().getDataScopeEnum();
        }

        // 根据用户类型确定数据权限范围
        String userType = user.getUserType();
        if ("manager".equals(userType)) {
            return DataScope.DEPT_AND_CHILD;
        } else if ("leader".equals(userType)) {
            return DataScope.DEPT;
        } else {
            return DataScope.SELF;
        }
    }

    /**
     * 获取用户的部门ID
     * 
     * @param userId 用户ID
     * @return 部门ID
     */
    @Cacheable(value = "userDept", key = "#userId")
    public Long getUserDeptId(Long userId) {
        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId)
                .map(User::getDeptId)
                .orElse(null);
    }

    /**
     * 检查用户是否为超级管理员
     * 
     * @param userId 用户ID
     * @return true：超级管理员，false：非超级管理员
     */
    @Cacheable(value = "userAdmin", key = "#userId")
    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }

        return userRepository.findById(userId)
                .map(User::isAdmin)
                .orElse(false);
    }

    /**
     * 获取用户可访问的自定义部门ID列表
     * 这里可以根据实际业务需求实现，比如从用户权限配置表中获取
     * 
     * @param userId 用户ID
     * @return 可访问的部门ID列表
     */
    @Cacheable(value = "userCustomDepts", key = "#userId")
    public List<Long> getUserCustomDeptIds(Long userId) {
        Optional<UserDataPermission> permission = userDataPermissionRepository.findByUserIdAndDelFlag(userId, 0);
        if (permission.isPresent() && permission.get().isNormal() && permission.get().isCustomScope()) {
            return permission.get().getCustomDeptIdList();
        }
        return new ArrayList<>();
    }

    /**
     * 设置用户数据权限范围
     * 
     * @param userId 用户ID
     * @param dataScope 数据权限范围
     */
    @CacheEvict(value = {"userDataPermission", "userCustomDepts"}, key = "#userId")
    @Transactional
    public void setUserDataScope(Long userId, DataScope dataScope) {
        setUserDataScope(userId, dataScope, null);
    }

    /**
     * 设置用户数据权限范围和自定义部门
     * 
     * @param userId 用户ID
     * @param dataScope 数据权限范围
     * @param customDeptIds 自定义部门ID列表（当dataScope为CUSTOM时使用）
     */
    @CacheEvict(value = {"userDataPermission", "userCustomDepts"}, key = "#userId")
    @Transactional
    public void setUserDataScope(Long userId, DataScope dataScope, List<Long> customDeptIds) {
        Optional<UserDataPermission> existingPermission = userDataPermissionRepository.findByUserIdAndDelFlag(userId, 0);
        
        UserDataPermission permission;
        if (existingPermission.isPresent()) {
            permission = existingPermission.get();
        } else {
            permission = new UserDataPermission();
            permission.setUserId(userId);
        }
        
        permission.setDataScopeEnum(dataScope);
        if (dataScope == DataScope.CUSTOM && customDeptIds != null) {
            permission.setCustomDeptIdList(customDeptIds);
        } else {
            permission.setCustomDeptIdList(null);
        }
        permission.enable();
        
        userDataPermissionRepository.save(permission);
        log.info("设置用户{}的数据权限范围为: {}", userId, dataScope);
    }

    /**
     * 删除用户数据权限配置
     * 
     * @param userId 用户ID
     */
    @CacheEvict(value = {"userDataPermission", "userCustomDepts"}, key = "#userId")
    @Transactional
    public void deleteUserDataPermission(Long userId) {
        userDataPermissionRepository.deleteByUserId(userId);
        log.info("删除用户[{}]的数据权限配置", userId);
    }

    /**
     * 启用用户数据权限
     * 
     * @param userId 用户ID
     */
    @CacheEvict(value = {"userDataPermission", "userCustomDepts"}, key = "#userId")
    @Transactional
    public void enableUserDataPermission(Long userId) {
        Optional<UserDataPermission> permission = userDataPermissionRepository.findByUserIdAndDelFlag(userId, 0);
        if (permission.isPresent()) {
            UserDataPermission userPermission = permission.get();
            userPermission.enable();
            userDataPermissionRepository.save(userPermission);
            log.info("启用用户[{}]的数据权限", userId);
        }
    }

    /**
     * 停用用户数据权限
     * 
     * @param userId 用户ID
     */
    @CacheEvict(value = {"userDataPermission", "userCustomDepts"}, key = "#userId")
    @Transactional
    public void disableUserDataPermission(Long userId) {
        Optional<UserDataPermission> permission = userDataPermissionRepository.findByUserIdAndDelFlag(userId, 0);
        if (permission.isPresent()) {
            UserDataPermission userPermission = permission.get();
            userPermission.disable();
            userDataPermissionRepository.save(userPermission);
            log.info("停用用户[{}]的数据权限", userId);
        }
    }

    /**
     * 获取用户数据权限配置
     * 
     * @param userId 用户ID
     * @return 用户数据权限配置
     */
    public Optional<UserDataPermission> getUserDataPermission(Long userId) {
        return userDataPermissionRepository.findByUserIdAndDelFlag(userId, 0);
    }

    /**
     * 检查用户是否有数据权限配置
     * 
     * @param userId 用户ID
     * @return true：有配置，false：无配置
     */
    public boolean hasUserDataPermission(Long userId) {
        return userDataPermissionRepository.existsByUserIdAndDelFlag(userId, 0);
    }

    /**
     * 清除用户数据权限缓存
     * 
     * @param userId 用户ID
     */
    @CacheEvict(value = {"userDataPermission", "userCustomDepts"}, key = "#userId")
    public void clearUserDataPermissionCache(Long userId) {
        log.debug("清除用户[{}]的数据权限缓存", userId);
    }

    /**
     * 检查用户是否有访问指定部门数据的权限
     * 
     * @param userId 用户ID
     * @param deptId 部门ID
     * @return true：有权限，false：无权限
     */
    public boolean hasAccessToDept(Long userId, Long deptId) {
        if (userId == null || deptId == null) {
            return false;
        }

        // 管理员有访问所有部门的权限
        if (isAdmin(userId)) {
            return true;
        }

        DataScope scope = getUserDataPermissionScope(userId);
        Long userDeptId = getUserDeptId(userId);

        switch (scope) {
            case ALL:
                return true;
            case DEPT:
                return deptId.equals(userDeptId);
            case DEPT_AND_CHILD:
                // TODO: 需要实现部门层级关系检查
                return deptId.equals(userDeptId);
            case CUSTOM:
                return getUserCustomDeptIds(userId).contains(deptId);
            case SELF:
            default:
                return deptId.equals(userDeptId);
        }
    }
}