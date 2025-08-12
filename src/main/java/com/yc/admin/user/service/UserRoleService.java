package com.yc.admin.user.service;

import com.yc.admin.user.entity.UserRole;
import com.yc.admin.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户角色关联服务层
 * 提供用户角色关联的业务逻辑处理
 *
 * @author YC
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    /**
     * 根据用户ID查询角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    public List<Long> getRoleIdsByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return userRoleRepository.findRoleIdsByUserId(userId);
    }

    /**
     * 根据角色ID查询用户ID列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    public List<Long> getUserIdsByRoleId(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        return userRoleRepository.findUserIdsByRoleId(roleId);
    }

    /**
     * 批量根据用户ID查询角色ID列表
     *
     * @param userIds 用户ID列表
     * @return 用户角色关联列表
     */
    public List<UserRole> getUserRolesByUserIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return List.of();
        }
        return userRoleRepository.findByUserIdIn(userIds);
    }

    /**
     * 批量根据角色ID查询用户ID列表
     *
     * @param roleIds 角色ID列表
     * @return 用户角色关联列表
     */
    public List<UserRole> getUserRolesByRoleIds(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }
        return userRoleRepository.findByRoleIdIn(roleIds);
    }

    /**
     * 检查用户角色关联是否存在
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否存在
     */
    public boolean existsUserRole(Long userId, Long roleId) {
        if (userId == null || roleId == null) {
            return false;
        }
        return userRoleRepository.existsByUserIdAndRoleId(userId, roleId);
    }

    /**
     * 统计用户的角色数量
     *
     * @param userId 用户ID
     * @return 角色数量
     */
    public long countRolesByUserId(Long userId) {
        if (userId == null) {
            return 0L;
        }
        return userRoleRepository.countByUserId(userId);
    }

    /**
     * 统计角色的用户数量
     *
     * @param roleId 角色ID
     * @return 用户数量
     */
    public long countUsersByRoleId(Long roleId) {
        if (roleId == null) {
            return 0L;
        }
        return userRoleRepository.countByRoleId(roleId);
    }

    /**
     * 为用户分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        if (userId == null || CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        // 删除用户现有的角色关联
        userRoleRepository.deleteByUserId(userId);

        // 创建新的角色关联
        List<UserRole> userRoles = roleIds.stream()
                .distinct()
                .map(roleId -> UserRole.of(userId, roleId))
                .collect(Collectors.toList());

        userRoleRepository.saveAll(userRoles);
        log.info("为用户[{}]分配角色[{}]成功", userId, roleIds);
    }

    /**
     * 为角色分配用户
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignUsersToRole(Long roleId, List<Long> userIds) {
        if (roleId == null || CollectionUtils.isEmpty(userIds)) {
            return;
        }

        // 删除角色现有的用户关联
        userRoleRepository.deleteByRoleId(roleId);

        // 创建新的用户关联
        List<UserRole> userRoles = userIds.stream()
                .distinct()
                .map(userId -> UserRole.of(userId, roleId))
                .collect(Collectors.toList());

        userRoleRepository.saveAll(userRoles);
        log.info("为角色[{}]分配用户[{}]成功", roleId, userIds);
    }

    /**
     * 添加用户角色关联
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUserRole(Long userId, Long roleId) {
        if (userId == null || roleId == null) {
            return;
        }

        // 检查关联是否已存在
        if (existsUserRole(userId, roleId)) {
            log.warn("用户[{}]和角色[{}]的关联已存在", userId, roleId);
            return;
        }

        UserRole userRole = UserRole.of(userId, roleId);
        userRoleRepository.save(userRole);
        log.info("添加用户[{}]和角色[{}]的关联成功", userId, roleId);
    }

    /**
     * 批量添加用户角色关联
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUserRoles(Long userId, List<Long> roleIds) {
        if (userId == null || CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        // 获取已存在的角色ID
        Set<Long> existingRoleIds = userRoleRepository.findRoleIdsByUserId(userId)
                .stream().collect(Collectors.toSet());

        // 过滤出需要新增的角色ID
        List<UserRole> newUserRoles = roleIds.stream()
                .distinct()
                .filter(roleId -> !existingRoleIds.contains(roleId))
                .map(roleId -> UserRole.of(userId, roleId))
                .collect(Collectors.toList());

        if (!newUserRoles.isEmpty()) {
            userRoleRepository.saveAll(newUserRoles);
            log.info("为用户[{}]批量添加角色关联成功，新增角色数量: {}", userId, newUserRoles.size());
        }
    }

    /**
     * 删除用户角色关联
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeUserRole(Long userId, Long roleId) {
        if (userId == null || roleId == null) {
            return;
        }

        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
        log.info("删除用户[{}]和角色[{}]的关联成功", userId, roleId);
    }

    /**
     * 批量删除用户的角色关联
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeUserRoles(Long userId, List<Long> roleIds) {
        if (userId == null || CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        userRoleRepository.deleteByUserIdAndRoleIdIn(userId, roleIds);
        log.info("批量删除用户[{}]的角色关联成功，删除角色数量: {}", userId, roleIds.size());
    }

    /**
     * 删除用户的所有角色关联
     *
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAllUserRoles(Long userId) {
        if (userId == null) {
            return;
        }

        long deletedCount = userRoleRepository.deleteByUserId(userId);
        log.info("删除用户[{}]的所有角色关联成功，删除数量: {}", userId, deletedCount);
    }

    /**
     * 删除角色的所有用户关联
     *
     * @param roleId 角色ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAllRoleUsers(Long roleId) {
        if (roleId == null) {
            return;
        }

        long deletedCount = userRoleRepository.deleteByRoleId(roleId);
        log.info("删除角色[{}]的所有用户关联成功，删除数量: {}", roleId, deletedCount);
    }

    /**
     * 批量删除用户的角色关联
     *
     * @param userIds 用户ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeUserRolesByUserIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        long deletedCount = userRoleRepository.deleteByUserIdIn(userIds);
        log.info("批量删除用户角色关联成功，删除数量: {}", deletedCount);
    }

    /**
     * 批量删除角色的用户关联
     *
     * @param roleIds 角色ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeUserRolesByRoleIds(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        long deletedCount = userRoleRepository.deleteByRoleIdIn(roleIds);
        log.info("批量删除角色用户关联成功，删除数量: {}", deletedCount);
    }
}