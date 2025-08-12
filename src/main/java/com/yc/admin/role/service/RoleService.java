package com.yc.admin.role.service;

import com.yc.admin.role.entity.Role;
import com.yc.admin.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 角色服务类
 * 
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    // ==================== 查询方法 ====================

    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色信息
     */
    public Optional<Role> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return roleRepository.findById(id).filter(role -> role.getDelFlag() == 0);
    }

    /**
     * 根据角色权限字符串查询角色
     * @param roleKey 角色权限字符串
     * @return 角色信息
     */
    public Optional<Role> findByRoleKey(String roleKey) {
        if (!StringUtils.hasText(roleKey)) {
            return Optional.empty();
        }
        return roleRepository.findByRoleKeyAndDelFlag(roleKey, 0);
    }

    /**
     * 根据角色名称查询角色
     * @param roleName 角色名称
     * @return 角色信息
     */
    public Optional<Role> findByRoleName(String roleName) {
        if (!StringUtils.hasText(roleName)) {
            return Optional.empty();
        }
        return roleRepository.findByRoleNameAndDelFlag(roleName, 0);
    }

    /**
     * 查询所有角色
     * @return 角色列表
     */
    public List<Role> findAll() {
        return roleRepository.findByDelFlagOrderByRoleSortAsc(0);
    }

    /**
     * 根据状态查询角色
     * @param status 角色状态
     * @return 角色列表
     */
    public List<Role> findByStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return findAll();
        }
        return roleRepository.findAllForSelect(status, 0);
    }

    /**
     * 分页查询角色
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 角色分页列表
     */
    public Page<Role> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return roleRepository.findByDelFlagOrderByRoleSortAsc(0, pageable);
    }

    /**
     * 复合条件分页查询角色
     * @param roleName 角色名称关键字
     * @param roleKey 角色权限字符串关键字
     * @param status 角色状态
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 角色分页列表
     */
    public Page<Role> findByConditions(String roleName, String roleKey, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return roleRepository.findByConditions(
            StringUtils.hasText(roleName) ? roleName.trim() : null,
            StringUtils.hasText(roleKey) ? roleKey.trim() : null,
            StringUtils.hasText(status) ? status : null,
            0,
            pageable
        );
    }

    /**
     * 统计正常状态的角色数量
     * @return 角色数量
     */
    public long countNormalRoles() {
        return roleRepository.countByStatusAndDelFlag(Role.Status.NORMAL, 0);
    }

    /**
     * 查询所有正常状态的角色（用于下拉选择）
     * @return 角色列表
     */
    public List<Role> findAllForSelect() {
        return roleRepository.findAllForSelect(Role.Status.NORMAL, 0);
    }

    // ==================== 创建和更新方法 ====================

    /**
     * 创建角色
     * @param role 角色信息
     * @return 创建的角色
     */
    @Transactional
    public Role createRole(Role role) {
        log.info("开始创建角色: {}", role.getRoleName());
        
        // 参数校验
        validateRoleForCreate(role);
        
        // 检查角色权限字符串是否已存在
        if (roleRepository.findByRoleKeyAndDelFlag(role.getRoleKey(), 0).isPresent()) {
            throw new IllegalArgumentException("角色权限字符串已存在: " + role.getRoleKey());
        }
        
        // 检查角色名称是否已存在
        if (roleRepository.findByRoleNameAndDelFlag(role.getRoleName(), 0).isPresent()) {
            throw new IllegalArgumentException("角色名称已存在: " + role.getRoleName());
        }
        
        // 设置默认值
        if (role.getRoleSort() == null) {
            role.setRoleSort(0);
        }
        if (!StringUtils.hasText(role.getDataScope())) {
            role.setDataScope(Role.DataScope.DEPT);
        }
        if (role.getMenuCheckStrictly() == null) {
            role.setMenuCheckStrictly(true);
        }
        if (role.getDeptCheckStrictly() == null) {
            role.setDeptCheckStrictly(true);
        }
        if (!StringUtils.hasText(role.getStatus())) {
            role.setStatus(Role.Status.NORMAL);
        }
        
        // 设置创建时间和删除标志
        role.setCreateTime(LocalDateTime.now());
        role.setDelFlag(0);
        
        Role savedRole = roleRepository.save(role);
        log.info("角色创建成功: ID={}, 名称={}", savedRole.getId(), savedRole.getRoleName());
        
        return savedRole;
    }

    /**
     * 更新角色
     * @param role 角色信息
     * @return 更新的角色
     */
    @Transactional
    public Role updateRole(Role role) {
        log.info("开始更新角色: ID={}, 名称={}", role.getId(), role.getRoleName());
        
        // 参数校验
        validateRoleForUpdate(role);
        
        // 检查角色是否存在
        Role existingRole = findById(role.getId())
            .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + role.getId()));
        
        // 检查角色权限字符串是否已被其他角色使用
        if (roleRepository.existsByRoleKeyAndIdNotAndDelFlag(role.getRoleKey(), role.getId(), 0)) {
            throw new IllegalArgumentException("角色权限字符串已被其他角色使用: " + role.getRoleKey());
        }
        
        // 检查角色名称是否已被其他角色使用
        if (roleRepository.existsByRoleNameAndIdNotAndDelFlag(role.getRoleName(), role.getId(), 0)) {
            throw new IllegalArgumentException("角色名称已被其他角色使用: " + role.getRoleName());
        }
        
        // 更新字段
        existingRole.setRoleName(role.getRoleName());
        existingRole.setRoleKey(role.getRoleKey());
        existingRole.setRoleSort(role.getRoleSort());
        existingRole.setDataScope(role.getDataScope());
        existingRole.setMenuCheckStrictly(role.getMenuCheckStrictly());
        existingRole.setDeptCheckStrictly(role.getDeptCheckStrictly());
        existingRole.setStatus(role.getStatus());
        existingRole.setRemark(role.getRemark());
        existingRole.setUpdateTime(LocalDateTime.now());
        
        Role savedRole = roleRepository.save(existingRole);
        log.info("角色更新成功: ID={}, 名称={}", savedRole.getId(), savedRole.getRoleName());
        
        return savedRole;
    }

    // ==================== 删除方法 ====================

    /**
     * 删除角色（逻辑删除）
     * @param id 角色ID
     */
    @Transactional
    public void deleteRole(Long id) {
        log.info("开始删除角色: ID={}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        Role role = findById(id)
            .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + id));
        
        // 检查是否为超级管理员角色
        if (role.isAdmin()) {
            throw new IllegalArgumentException("不能删除超级管理员角色");
        }
        
        // 逻辑删除
        role.setDelFlag(1);
        role.setUpdateTime(LocalDateTime.now());
        roleRepository.save(role);
        
        log.info("角色删除成功: ID={}, 名称={}", role.getId(), role.getRoleName());
    }

    /**
     * 批量删除角色（逻辑删除）
     * @param ids 角色ID列表
     */
    @Transactional
    public void deleteRoles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("角色ID列表不能为空");
        }
        
        log.info("开始批量删除角色: IDs={}", ids);
        
        for (Long id : ids) {
            deleteRole(id);
        }
        
        log.info("批量删除角色完成: 共删除{}个角色", ids.size());
    }

    // ==================== 状态管理方法 ====================

    /**
     * 启用角色
     * @param id 角色ID
     */
    @Transactional
    public void enableRole(Long id) {
        log.info("开始启用角色: ID={}", id);
        
        Role role = findById(id)
            .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + id));
        
        role.enable();
        role.setUpdateTime(LocalDateTime.now());
        roleRepository.save(role);
        
        log.info("角色启用成功: ID={}, 名称={}", role.getId(), role.getRoleName());
    }

    /**
     * 停用角色
     * @param id 角色ID
     */
    @Transactional
    public void disableRole(Long id) {
        log.info("开始停用角色: ID={}", id);
        
        Role role = findById(id)
            .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + id));
        
        // 检查是否为超级管理员角色
        if (role.isAdmin()) {
            throw new IllegalArgumentException("不能停用超级管理员角色");
        }
        
        role.disable();
        role.setUpdateTime(LocalDateTime.now());
        roleRepository.save(role);
        
        log.info("角色停用成功: ID={}, 名称={}", role.getId(), role.getRoleName());
    }

    // ==================== 私有方法 ====================

    /**
     * 校验角色创建参数
     * @param role 角色信息
     */
    private void validateRoleForCreate(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("角色信息不能为空");
        }
        if (!StringUtils.hasText(role.getRoleName())) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
        if (!StringUtils.hasText(role.getRoleKey())) {
            throw new IllegalArgumentException("角色权限字符串不能为空");
        }
        if (role.getRoleName().length() > 30) {
            throw new IllegalArgumentException("角色名称长度不能超过30个字符");
        }
        if (role.getRoleKey().length() > 100) {
            throw new IllegalArgumentException("角色权限字符串长度不能超过100个字符");
        }
        if (role.getRemark() != null && role.getRemark().length() > 500) {
            throw new IllegalArgumentException("备注长度不能超过500个字符");
        }
    }

    /**
     * 校验角色更新参数
     * @param role 角色信息
     */
    private void validateRoleForUpdate(Role role) {
        validateRoleForCreate(role);
        if (role.getId() == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
    }
}