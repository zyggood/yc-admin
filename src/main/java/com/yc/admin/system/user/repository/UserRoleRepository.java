package com.yc.admin.system.user.repository;

import com.yc.admin.system.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户角色关联表数据访问接口
 * 
 * @author YC
 * @since 1.0.0
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据用户ID查询用户角色关联列表
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByUserId(Long userId);

    /**
     * 根据角色ID查询用户角色关联列表
     * @param roleId 角色ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByRoleId(Long roleId);

    /**
     * 根据用户ID和角色ID查询用户角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 用户角色关联对象
     */
    UserRole findByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * 根据用户ID列表查询用户角色关联列表
     * @param userIds 用户ID列表
     * @return 用户角色关联列表
     */
    List<UserRole> findByUserIdIn(List<Long> userIds);

    /**
     * 根据角色ID列表查询用户角色关联列表
     * @param roleIds 角色ID列表
     * @return 用户角色关联列表
     */
    List<UserRole> findByRoleIdIn(List<Long> roleIds);

    // ==================== 存在性检查方法 ====================

    /**
     * 检查用户是否拥有指定角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return true：存在，false：不存在
     */
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * 检查用户是否拥有任意角色
     * @param userId 用户ID
     * @return true：存在，false：不存在
     */
    boolean existsByUserId(Long userId);

    /**
     * 检查角色是否被任意用户使用
     * @param roleId 角色ID
     * @return true：存在，false：不存在
     */
    boolean existsByRoleId(Long roleId);

    // ==================== 统计查询方法 ====================

    /**
     * 统计用户的角色数量
     * @param userId 用户ID
     * @return 角色数量
     */
    long countByUserId(Long userId);

    /**
     * 统计角色的用户数量
     * @param roleId 角色ID
     * @return 用户数量
     */
    long countByRoleId(Long roleId);

    /**
     * 统计指定角色列表的用户数量
     * @param roleIds 角色ID列表
     * @return 用户数量
     */
    @Query("SELECT COUNT(DISTINCT ur.userId) FROM UserRole ur WHERE ur.roleId IN :roleIds")
    long countDistinctUsersByRoleIdIn(@Param("roleIds") List<Long> roleIds);

    // ==================== 自定义查询方法 ====================

    /**
     * 根据用户ID查询角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Query("SELECT ur.roleId FROM UserRole ur WHERE ur.userId = :userId")
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询用户ID列表
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    @Query("SELECT ur.userId FROM UserRole ur WHERE ur.roleId = :roleId")
    List<Long> findUserIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID列表查询角色ID列表
     * @param userIds 用户ID列表
     * @return 角色ID列表
     */
    @Query("SELECT DISTINCT ur.roleId FROM UserRole ur WHERE ur.userId IN :userIds")
    List<Long> findDistinctRoleIdsByUserIdIn(@Param("userIds") List<Long> userIds);

    /**
     * 根据角色ID列表查询用户ID列表
     * @param roleIds 角色ID列表
     * @return 用户ID列表
     */
    @Query("SELECT DISTINCT ur.userId FROM UserRole ur WHERE ur.roleId IN :roleIds")
    List<Long> findDistinctUserIdsByRoleIdIn(@Param("roleIds") List<Long> roleIds);

    // ==================== 删除方法 ====================

    /**
     * 根据用户ID删除用户角色关联
     * @param userId 用户ID
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID删除用户角色关联
     * @param roleId 角色ID
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.roleId = :roleId")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID和角色ID删除用户角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId AND ur.roleId = :roleId")
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 根据用户ID列表删除用户角色关联
     * @param userIds 用户ID列表
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.userId IN :userIds")
    int deleteByUserIdIn(@Param("userIds") List<Long> userIds);

    /**
     * 根据角色ID列表删除用户角色关联
     * @param roleIds 角色ID列表
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.roleId IN :roleIds")
    int deleteByRoleIdIn(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据用户ID查询菜单ID列表（通过用户角色关联）
     * @param userId 用户ID
     * @return 菜单ID列表
     */
    @Query("SELECT DISTINCT rm.menuId FROM RoleMenu rm " +
           "INNER JOIN UserRole ur ON rm.roleId = ur.roleId " +
           "WHERE ur.userId = :userId")
    List<Long> findMenuIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID列表查询菜单ID列表（通过用户角色关联）
     * @param userIds 用户ID列表
     * @return 菜单ID列表
     */
    @Query("SELECT DISTINCT rm.menuId FROM RoleMenu rm " +
           "INNER JOIN UserRole ur ON rm.roleId = ur.roleId " +
           "WHERE ur.userId IN :userIds")
    List<Long> findMenuIdsByUserIdIn(@Param("userIds") List<Long> userIds);

    /**
     * 根据用户ID和角色ID列表删除用户角色关联
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId AND ur.roleId IN :roleIds")
    int deleteByUserIdAndRoleIdIn(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /**
     * 批量删除用户角色关联
     * @param userRoles 用户角色关联列表
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE (ur.userId, ur.roleId) IN :userRoles")
    int deleteBatch(@Param("userRoles") List<Object[]> userRoles);

}