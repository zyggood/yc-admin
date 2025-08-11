package com.yc.admin.dept.repository;

import com.yc.admin.dept.entity.Dept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 部门数据访问层
 * 
 * @author admin
 * @since 2025-01-01
 */
@Repository
public interface DeptRepository extends JpaRepository<Dept, Long> {

    /**
     * 根据部门名称查询部门
     * @param deptName 部门名称
     * @return 部门信息
     */
    Optional<Dept> findByDeptNameAndDelFlag(String deptName, Integer delFlag);

    /**
     * 根据父部门ID查询子部门列表
     * @param parentId 父部门ID
     * @param delFlag 删除标志
     * @return 子部门列表
     */
    List<Dept> findByParentIdAndDelFlagOrderByOrderNumAsc(Long parentId, Integer delFlag);

    /**
     * 查询所有正常状态的部门
     * @param delFlag 删除标志
     * @param status 部门状态
     * @return 部门列表
     */
    List<Dept> findByDelFlagAndStatusOrderByOrderNumAsc(Integer delFlag, Integer status);

    /**
     * 查询所有未删除的部门
     * @param delFlag 删除标志
     * @return 部门列表
     */
    List<Dept> findByDelFlagOrderByOrderNumAsc(Integer delFlag);

    /**
     * 根据祖级列表查询所有子部门
     * @param ancestors 祖级列表模式
     * @param delFlag 删除标志
     * @return 子部门列表
     */
    @Query("SELECT d FROM Dept d WHERE d.ancestors LIKE CONCAT(:ancestors, '%') AND d.delFlag = :delFlag ORDER BY d.orderNum ASC")
    List<Dept> findByAncestorsLikeAndDelFlag(@Param("ancestors") String ancestors, @Param("delFlag") Integer delFlag);

    /**
     * 查询指定部门的所有子部门ID
     * @param deptId 部门ID
     * @param delFlag 删除标志
     * @return 子部门ID列表
     */
    @Query("SELECT d.id FROM Dept d WHERE (d.ancestors LIKE CONCAT('%,', :deptId, ',%') OR d.ancestors LIKE CONCAT(:deptId, ',%') OR d.ancestors LIKE CONCAT('%,', :deptId) OR d.ancestors = :deptId) AND d.delFlag = :delFlag")
    List<Long> findChildrenIdsByDeptId(@Param("deptId") Long deptId, @Param("delFlag") Integer delFlag);

    /**
     * 检查部门名称是否存在（排除指定ID）
     * @param deptName 部门名称
     * @param deptId 排除的部门ID
     * @param delFlag 删除标志
     * @return 是否存在
     */
    @Query("SELECT COUNT(d) > 0 FROM Dept d WHERE d.deptName = :deptName AND d.id != :deptId AND d.delFlag = :delFlag")
    boolean existsByDeptNameAndIdNotAndDelFlag(@Param("deptName") String deptName, @Param("deptId") Long deptId, @Param("delFlag") Integer delFlag);

    /**
     * 检查是否存在子部门
     * @param parentId 父部门ID
     * @param delFlag 删除标志
     * @return 是否存在子部门
     */
    boolean existsByParentIdAndDelFlag(Long parentId, Integer delFlag);

    /**
     * 根据负责人查询部门
     * @param leader 负责人
     * @param delFlag 删除标志
     * @return 部门列表
     */
    List<Dept> findByLeaderAndDelFlag(String leader, Integer delFlag);

    /**
     * 根据部门状态查询部门数量
     * @param status 部门状态
     * @param delFlag 删除标志
     * @return 部门数量
     */
    long countByStatusAndDelFlag(Integer status, Integer delFlag);

    /**
     * 更新部门状态
     * @param deptId 部门ID
     * @param status 新状态
     */
    @Query("UPDATE Dept d SET d.status = :status WHERE d.id = :deptId")
    void updateStatusById(@Param("deptId") Long deptId, @Param("status") String status);

    /**
     * 批量更新子部门的祖级列表
     * @param oldAncestors 旧的祖级列表
     * @param newAncestors 新的祖级列表
     */
    @Query("UPDATE Dept d SET d.ancestors = REPLACE(d.ancestors, :oldAncestors, :newAncestors) WHERE d.ancestors LIKE CONCAT(:oldAncestors, '%')")
    void updateChildrenAncestors(@Param("oldAncestors") String oldAncestors, @Param("newAncestors") String newAncestors);
}