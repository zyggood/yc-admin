package com.yc.admin.system.permission.repository;

import com.yc.admin.system.permission.entity.UserDataPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据权限配置数据访问层接口
 * 
 * @author YC
 * @since 1.0.0
 */
@Repository
public interface UserDataPermissionRepository extends JpaRepository<UserDataPermission, Long> {

    /**
     * 根据用户ID查找数据权限配置
     * 
     * @param userId 用户ID
     * @return 用户数据权限配置
     */
    Optional<UserDataPermission> findByUserId(Long userId);

    /**
     * 根据用户ID查找数据权限配置（排除已删除）
     * 
     * @param userId 用户ID
     * @param delFlag 删除标志
     * @return 用户数据权限配置
     */
    Optional<UserDataPermission> findByUserIdAndDelFlag(Long userId, Integer delFlag);

    /**
     * 根据数据权限范围查询用户列表
     * 
     * @param dataScope 数据权限范围
     * @param delFlag 删除标志
     * @return 用户数据权限配置列表
     */
    List<UserDataPermission> findByDataScopeAndDelFlag(String dataScope, Integer delFlag);

    /**
     * 根据状态查询用户数据权限配置
     * 
     * @param status 状态
     * @param delFlag 删除标志
     * @return 用户数据权限配置列表
     */
    List<UserDataPermission> findByStatusAndDelFlag(String status, Integer delFlag);

    /**
     * 分页查询用户数据权限配置
     * 
     * @param delFlag 删除标志
     * @param pageable 分页参数
     * @return 用户数据权限配置分页列表
     */
    Page<UserDataPermission> findByDelFlagOrderByCreateTimeDesc(Integer delFlag, Pageable pageable);

    /**
     * 根据条件分页查询用户数据权限配置
     * 
     * @param userId 用户ID（可选）
     * @param dataScope 数据权限范围（可选）
     * @param status 状态（可选）
     * @param delFlag 删除标志
     * @param pageable 分页参数
     * @return 用户数据权限配置分页列表
     */
    @Query("SELECT udp FROM UserDataPermission udp WHERE " +
           "(:userId IS NULL OR udp.userId = :userId) AND " +
           "(:dataScope IS NULL OR udp.dataScope = :dataScope) AND " +
           "(:status IS NULL OR udp.status = :status) AND " +
           "udp.delFlag = :delFlag " +
           "ORDER BY udp.createTime DESC")
    Page<UserDataPermission> findByConditions(@Param("userId") Long userId,
                                              @Param("dataScope") String dataScope,
                                              @Param("status") String status,
                                              @Param("delFlag") Integer delFlag,
                                              Pageable pageable);

    /**
     * 检查用户是否已存在数据权限配置
     * 
     * @param userId 用户ID
     * @param delFlag 删除标志
     * @return true：存在，false：不存在
     */
    boolean existsByUserIdAndDelFlag(Long userId, Integer delFlag);

    /**
     * 统计指定数据权限范围的用户数量
     * 
     * @param dataScope 数据权限范围
     * @param delFlag 删除标志
     * @return 用户数量
     */
    long countByDataScopeAndDelFlag(String dataScope, Integer delFlag);

    /**
     * 统计指定状态的用户数据权限配置数量
     * 
     * @param status 状态
     * @param delFlag 删除标志
     * @return 配置数量
     */
    long countByStatusAndDelFlag(String status, Integer delFlag);

    /**
     * 批量更新用户数据权限状态
     * 
     * @param userIds 用户ID列表
     * @param status 新状态
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE UserDataPermission udp SET udp.status = :status, udp.updateTime = CURRENT_TIMESTAMP " +
           "WHERE udp.userId IN :userIds AND udp.delFlag = 0")
    int updateStatusByUserIds(@Param("userIds") List<Long> userIds, @Param("status") String status);

    /**
     * 批量删除用户数据权限配置（逻辑删除）
     * 
     * @param userIds 用户ID列表
     * @return 删除的记录数
     */
    @Modifying
    @Query("UPDATE UserDataPermission udp SET udp.delFlag = 1, udp.updateTime = CURRENT_TIMESTAMP " +
           "WHERE udp.userId IN :userIds")
    int deleteByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 根据用户ID删除数据权限配置（逻辑删除）
     * 
     * @param userId 用户ID
     * @return 删除的记录数
     */
    @Modifying
    @Query("UPDATE UserDataPermission udp SET udp.delFlag = 1, udp.updateTime = CURRENT_TIMESTAMP " +
           "WHERE udp.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 查询包含指定部门ID的自定义权限配置
     * 
     * @param deptId 部门ID
     * @param delFlag 删除标志
     * @return 用户数据权限配置列表
     */
    @Query("SELECT udp FROM UserDataPermission udp WHERE " +
           "udp.dataScope = '2' AND " +
           "udp.customDeptIds LIKE CONCAT('%', :deptId, '%') AND " +
           "udp.delFlag = :delFlag")
    List<UserDataPermission> findByCustomDeptIdsContaining(@Param("deptId") Long deptId, @Param("delFlag") Integer delFlag);
}