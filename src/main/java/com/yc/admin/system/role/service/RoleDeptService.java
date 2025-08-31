package com.yc.admin.system.role.service;

import com.yc.admin.system.role.entity.RoleDept;
import com.yc.admin.system.role.repository.RoleDeptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * 角色部门关联服务层
 * 提供角色部门关联的业务逻辑处理
 *
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleDeptService {

    private final RoleDeptRepository roleDeptRepository;

    /**
     * 根据角色ID获取关联的部门ID列表
     *
     * @param roleId 角色ID
     * @return 部门ID列表
     */
    public List<Long> getDeptIdsByRoleId(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        
        try {
            return roleDeptRepository.findDeptIdsByRoleId(roleId);
        } catch (Exception e) {
            log.error("获取角色{}关联的部门ID失败: {}", roleId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 根据角色ID列表获取关联的部门ID列表
     *
     * @param roleIds 角色ID列表
     * @return 部门ID列表
     */
    public List<Long> getDeptIdsByRoleIds(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }
        
        try {
            return roleDeptRepository.findDeptIdsByRoleIds(roleIds);
        } catch (Exception e) {
            log.error("获取角色{}关联的部门ID失败: {}", roleIds, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 根据部门ID获取关联的角色ID列表
     *
     * @param deptId 部门ID
     * @return 角色ID列表
     */
    public List<Long> getRoleIdsByDeptId(Long deptId) {
        if (deptId == null) {
            return List.of();
        }
        
        try {
            return roleDeptRepository.findRoleIdsByDeptId(deptId);
        } catch (Exception e) {
            log.error("获取部门{}关联的角色ID失败: {}", deptId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 检查角色是否关联了指定部门
     *
     * @param roleId 角色ID
     * @param deptId 部门ID
     * @return 是否关联
     */
    public boolean isRoleDeptAssociated(Long roleId, Long deptId) {
        if (roleId == null || deptId == null) {
            return false;
        }
        
        try {
            return roleDeptRepository.existsByRoleIdAndDeptId(roleId, deptId);
        } catch (Exception e) {
            log.error("检查角色{}和部门{}关联关系失败: {}", roleId, deptId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 保存角色部门关联关系
     *
     * @param roleId 角色ID
     * @param deptIds 部门ID集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRoleDepts(Long roleId, Set<Long> deptIds) {
        if (roleId == null) {
            log.warn("角色ID不能为空");
            return;
        }
        
        try {
            roleDeptRepository.saveRoleDeptBatch(roleId, deptIds);
            log.info("保存角色{}的部门关联关系成功，部门数量: {}", roleId, 
                    deptIds != null ? deptIds.size() : 0);
        } catch (Exception e) {
            log.error("保存角色{}的部门关联关系失败: {}", roleId, e.getMessage(), e);
            throw new RuntimeException("保存角色部门关联关系失败", e);
        }
    }

    /**
     * 更新角色的部门关联关系
     *
     * @param roleId 角色ID
     * @param deptIds 新的部门ID集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleDepts(Long roleId, Set<Long> deptIds) {
        if (roleId == null) {
            log.warn("角色ID不能为空");
            return;
        }
        
        try {
            roleDeptRepository.updateRoleDepts(roleId, deptIds);
            log.info("更新角色{}的部门关联关系成功，部门数量: {}", roleId, 
                    deptIds != null ? deptIds.size() : 0);
        } catch (Exception e) {
            log.error("更新角色{}的部门关联关系失败: {}", roleId, e.getMessage(), e);
            throw new RuntimeException("更新角色部门关联关系失败", e);
        }
    }

    /**
     * 删除角色的所有部门关联关系
     *
     * @param roleId 角色ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoleDeptsByRoleId(Long roleId) {
        if (roleId == null) {
            log.warn("角色ID不能为空");
            return;
        }
        
        try {
            roleDeptRepository.deleteByRoleId(roleId);
            log.info("删除角色{}的所有部门关联关系成功", roleId);
        } catch (Exception e) {
            log.error("删除角色{}的部门关联关系失败: {}", roleId, e.getMessage(), e);
            throw new RuntimeException("删除角色部门关联关系失败", e);
        }
    }

    /**
     * 删除部门的所有角色关联关系
     *
     * @param deptId 部门ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoleDeptsByDeptId(Long deptId) {
        if (deptId == null) {
            log.warn("部门ID不能为空");
            return;
        }
        
        try {
            roleDeptRepository.deleteByDeptId(deptId);
            log.info("删除部门{}的所有角色关联关系成功", deptId);
        } catch (Exception e) {
            log.error("删除部门{}的角色关联关系失败: {}", deptId, e.getMessage(), e);
            throw new RuntimeException("删除部门角色关联关系失败", e);
        }
    }
}