package com.yc.admin.system.role.service;

import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.dto.RoleDTO;
import com.yc.admin.system.role.dto.RoleDTOConverter;
import com.yc.admin.system.role.repository.RoleRepository;
import com.yc.admin.common.exception.BusinessException;
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
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleDTOConverter roleDTOConverter;

    // ==================== 查询方法 ====================

    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色信息
     */
    public RoleDTO findById(Long id) {
        if (id == null) {
            throw new BusinessException("角色ID不能为空");
        }
        Role role = roleRepository.findById(id)
                .filter(r -> r.getDelFlag() == 0)
                .orElseThrow(() -> new BusinessException("角色不存在: " + id));
        return roleDTOConverter.toDTO(role);
    }

    /**
     * 根据用户ID查询角色
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<Role> findByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return roleRepository.findByUserId(userId);
    }

    /**
     * 根据角色权限字符串查询角色
     * @param roleKey 角色权限字符串
     * @return 角色信息
     */
    public Optional<RoleDTO> findByRoleKey(String roleKey) {
        if (!StringUtils.hasText(roleKey)) {
            return Optional.empty();
        }
        return roleRepository.findByRoleKeyAndDelFlag(roleKey, 0)
                .map(roleDTOConverter::toDTO);
    }

    /**
     * 根据角色名称查询角色
     * @param roleName 角色名称
     * @return 角色信息
     */
    public Optional<RoleDTO> findByRoleName(String roleName) {
        if (!StringUtils.hasText(roleName)) {
            return Optional.empty();
        }
        return roleRepository.findByRoleNameAndDelFlag(roleName, 0)
                .map(roleDTOConverter::toDTO);
    }

    /**
     * 查询所有角色
     * @return 角色列表
     */
    public List<RoleDTO> findAll() {
        List<Role> roles = roleRepository.findByDelFlagOrderByRoleSortAsc(0);
        return roleDTOConverter.toDTOList(roles);
    }

    /**
     * 根据状态查询角色
     * @param status 角色状态
     * @return 角色列表
     */
    public List<RoleDTO> findByStatus(String status) {
        List<Role> roles;
        if (!StringUtils.hasText(status)) {
            roles = roleRepository.findByDelFlagOrderByRoleSortAsc(0);
        } else {
            roles = roleRepository.findAllForSelect(status, 0);
        }
        return roleDTOConverter.toDTOList(roles);
    }

    /**
     * 分页查询角色
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 角色分页列表
     */
    public Page<RoleDTO> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Role> rolePage = roleRepository.findByDelFlagOrderByRoleSortAsc(0, pageable);
        return roleDTOConverter.toDTOPage(rolePage);
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
    public Page<RoleDTO> findByConditions(String roleName, String roleKey, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Role> rolePage = roleRepository.findByConditions(
            StringUtils.hasText(roleName) ? roleName.trim() : null,
            StringUtils.hasText(roleKey) ? roleKey.trim() : null,
            StringUtils.hasText(status) ? status : null,
            0,
            pageable
        );
        return roleDTOConverter.toDTOPage(rolePage);
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
    public List<RoleDTO.SelectorDTO> findAllForSelect() {
        List<Role> roles = roleRepository.findAllForSelect(Role.Status.NORMAL, 0);
        return roleDTOConverter.toSelectorDTOList(roles);
    }

    // ==================== 创建和更新方法 ====================

    /**
     * 创建角色
     * @param createDTO 角色创建信息
     * @return 创建的角色
     */
    @Transactional
    public RoleDTO createRole(RoleDTO.CreateDTO createDTO) {
        log.info("开始创建角色: {}", createDTO.getRoleName());
        
        // 参数校验
        validateRoleForCreate(createDTO);
        
        // 检查角色权限字符串是否已存在
        if (roleRepository.findByRoleKeyAndDelFlag(createDTO.getRoleKey(), 0).isPresent()) {
            throw new BusinessException("角色权限字符串已存在: " + createDTO.getRoleKey());
        }
        
        // 检查角色名称是否已存在
        if (roleRepository.findByRoleNameAndDelFlag(createDTO.getRoleName(), 0).isPresent()) {
            throw new BusinessException("角色名称已存在: " + createDTO.getRoleName());
        }
        
        // 转换为实体
        Role role = roleDTOConverter.toEntity(createDTO);
        
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
        
        return roleDTOConverter.toDTO(savedRole);
    }

    /**
     * 更新角色
     * @param updateDTO 角色更新信息
     * @return 更新的角色
     */
    @Transactional
    public RoleDTO updateRole(RoleDTO.UpdateDTO updateDTO) {
        log.info("开始更新角色: ID={}, 名称={}", updateDTO.getId(), updateDTO.getRoleName());
        
        // 参数校验
        validateRoleForUpdate(updateDTO);
        
        // 检查角色是否存在
        Role existingRole = roleRepository.findById(updateDTO.getId())
                .filter(r -> r.getDelFlag() == 0)
                .orElseThrow(() -> new BusinessException("角色不存在: " + updateDTO.getId()));
        
        // 检查角色权限字符串是否已被其他角色使用
        if (roleRepository.existsByRoleKeyAndIdNotAndDelFlag(updateDTO.getRoleKey(), updateDTO.getId(), 0)) {
            throw new BusinessException("角色权限字符串已被其他角色使用: " + updateDTO.getRoleKey());
        }
        
        // 检查角色名称是否已被其他角色使用
        if (roleRepository.existsByRoleNameAndIdNotAndDelFlag(updateDTO.getRoleName(), updateDTO.getId(), 0)) {
            throw new BusinessException("角色名称已被其他角色使用: " + updateDTO.getRoleName());
        }
        
        // 更新实体
        roleDTOConverter.updateEntity(existingRole, updateDTO);
        existingRole.setUpdateTime(LocalDateTime.now());
        
        Role savedRole = roleRepository.save(existingRole);
        log.info("角色更新成功: ID={}, 名称={}", savedRole.getId(), savedRole.getRoleName());
        
        return roleDTOConverter.toDTO(savedRole);
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
        
        Role role = roleRepository.findById(id)
                .filter(r -> r.getDelFlag() == 0)
                .orElseThrow(() -> new BusinessException("角色不存在: " + id));
        
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
        
        Role role = roleRepository.findById(id)
                .filter(r -> r.getDelFlag() == 0)
                .orElseThrow(() -> new BusinessException("角色不存在: " + id));
        
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
        
        Role role = roleRepository.findById(id)
                .filter(r -> r.getDelFlag() == 0)
                .orElseThrow(() -> new BusinessException("角色不存在: " + id));
        
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
     * @param createDTO 角色创建信息
     */
    private void validateRoleForCreate(RoleDTO.CreateDTO createDTO) {
        if (createDTO == null) {
            throw new BusinessException("角色信息不能为空");
        }
        if (!StringUtils.hasText(createDTO.getRoleName())) {
            throw new BusinessException("角色名称不能为空");
        }
        if (!StringUtils.hasText(createDTO.getRoleKey())) {
            throw new BusinessException("角色权限字符串不能为空");
        }
        if (createDTO.getRoleName().length() > 30) {
            throw new BusinessException("角色名称长度不能超过30个字符");
        }
        if (createDTO.getRoleKey().length() > 100) {
            throw new BusinessException("角色权限字符串长度不能超过100个字符");
        }
        if (createDTO.getRemark() != null && createDTO.getRemark().length() > 500) {
            throw new BusinessException("备注长度不能超过500个字符");
        }
    }

    /**
     * 校验角色更新参数
     * @param updateDTO 角色更新信息
     */
    private void validateRoleForUpdate(RoleDTO.UpdateDTO updateDTO) {
        if (updateDTO == null) {
            throw new BusinessException("角色信息不能为空");
        }
        if (updateDTO.getId() == null) {
            throw new BusinessException("角色ID不能为空");
        }
        if (!StringUtils.hasText(updateDTO.getRoleName())) {
            throw new BusinessException("角色名称不能为空");
        }
        if (!StringUtils.hasText(updateDTO.getRoleKey())) {
            throw new BusinessException("角色权限字符串不能为空");
        }
        if (updateDTO.getRoleName().length() > 30) {
            throw new BusinessException("角色名称长度不能超过30个字符");
        }
        if (updateDTO.getRoleKey().length() > 100) {
            throw new BusinessException("角色权限字符串长度不能超过100个字符");
        }
        if (updateDTO.getRemark() != null && updateDTO.getRemark().length() > 500) {
            throw new BusinessException("备注长度不能超过500个字符");
        }
    }
}