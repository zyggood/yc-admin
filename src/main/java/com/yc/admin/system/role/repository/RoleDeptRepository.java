package com.yc.admin.system.role.repository;

import com.yc.admin.system.role.entity.RoleDept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 角色部门关联Repository接口
 * 提供角色部门关联的数据访问功能
 *
 * @author YC
 * @since 1.0.0
 */
@Repository
public interface RoleDeptRepository extends JpaRepository<RoleDept, Long> {

    /**
     * 根据角色ID查询关联的部门ID列表
     *
     * @param roleId 角色ID
     * @return 部门ID列表
     */
    @Query("SELECT rd.deptId FROM RoleDept rd WHERE rd.roleId = :roleId")
    List<Long> findDeptIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID列表查询关联的部门ID列表
     *
     * @param roleIds 角色ID列表
     * @return 部门ID列表
     */
    @Query("SELECT rd.deptId FROM RoleDept rd WHERE rd.roleId IN :roleIds")
    List<Long> findDeptIdsByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据部门ID查询关联的角色ID列表
     *
     * @param deptId 部门ID
     * @return 角色ID列表
     */
    @Query("SELECT rd.roleId FROM RoleDept rd WHERE rd.deptId = :deptId")
    List<Long> findRoleIdsByDeptId(@Param("deptId") Long deptId);

    /**
     * 检查角色是否关联了指定部门
     *
     * @param roleId 角色ID
     * @param deptId 部门ID
     * @return 是否关联
     */
    boolean existsByRoleIdAndDeptId(Long roleId, Long deptId);

    /**
     * 根据角色ID删除所有关联关系
     *
     * @param roleId 角色ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RoleDept rd WHERE rd.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据部门ID删除所有关联关系
     *
     * @param deptId 部门ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RoleDept rd WHERE rd.deptId = :deptId")
    void deleteByDeptId(@Param("deptId") Long deptId);

    /**
     * 批量保存角色部门关联关系
     *
     * @param roleId 角色ID
     * @param deptIds 部门ID集合
     */
    @Modifying
    @Transactional
    default void saveRoleDeptBatch(Long roleId, Set<Long> deptIds) {
        if (roleId != null && deptIds != null && !deptIds.isEmpty()) {
            List<RoleDept> roleDepts = deptIds.stream()
                    .map(deptId -> new RoleDept(roleId, deptId))
                    .toList();
            saveAll(roleDepts);
        }
    }

    /**
     * 更新角色的部门关联关系
     *
     * @param roleId 角色ID
     * @param deptIds 新的部门ID集合
     */
    @Transactional
    default void updateRoleDepts(Long roleId, Set<Long> deptIds) {
        // 先删除原有关联关系
        deleteByRoleId(roleId);
        // 再保存新的关联关系
        saveRoleDeptBatch(roleId, deptIds);
    }
}