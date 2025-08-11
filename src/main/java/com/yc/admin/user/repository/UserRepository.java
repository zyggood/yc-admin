package com.yc.admin.user.repository;

import com.yc.admin.user.entity.User;
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
 * 用户数据访问层接口
 * 
 * @author YC
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * @param userName 用户名
     * @return 用户信息
     */
    Optional<User> findByUserNameAndDelFlag(String userName, Integer delFlag);

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByEmailAndDelFlag(String email, Integer delFlag);

    /**
     * 根据手机号查找用户
     * @param phone 手机号
     * @return 用户信息
     */
    Optional<User> findByPhoneAndDelFlag(String phone, Integer delFlag);

    /**
     * 检查用户名是否存在（排除指定用户ID）
     * @param userName 用户名
     * @param userId 排除的用户ID
     * @param delFlag 删除标志
     * @return 是否存在
     */
    boolean existsByUserNameAndIdNotAndDelFlag(String userName, Long userId, Integer delFlag);

    /**
     * 检查邮箱是否存在（排除指定用户ID）
     * @param email 邮箱
     * @param userId 排除的用户ID
     * @param delFlag 删除标志
     * @return 是否存在
     */
    boolean existsByEmailAndIdNotAndDelFlag(String email, Long userId, Integer delFlag);

    /**
     * 检查手机号是否存在（排除指定用户ID）
     * @param phone 手机号
     * @param userId 排除的用户ID
     * @param delFlag 删除标志
     * @return 是否存在
     */
    boolean existsByPhoneAndIdNotAndDelFlag(String phone, Long userId, Integer delFlag);

    /**
     * 根据状态和删除标志查询用户列表
     * @param status 用户状态
     * @param delFlag 删除标志
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByStatusAndDelFlagOrderByCreateTimeDesc(String status, Integer delFlag, Pageable pageable);

    /**
     * 根据删除标志查询用户列表
     * @param delFlag 删除标志
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByDelFlagOrderByCreateTimeDesc(Integer delFlag, Pageable pageable);

    /**
     * 根据用户名模糊查询
     * @param userName 用户名关键字
     * @param delFlag 删除标志
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByUserNameContainingAndDelFlagOrderByCreateTimeDesc(String userName, Integer delFlag, Pageable pageable);

    /**
     * 根据昵称模糊查询
     * @param nickName 昵称关键字
     * @param delFlag 删除标志
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByNickNameContainingAndDelFlagOrderByCreateTimeDesc(String nickName, Integer delFlag, Pageable pageable);

    /**
     * 根据手机号模糊查询
     * @param phone 手机号关键字
     * @param delFlag 删除标志
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByPhoneContainingAndDelFlagOrderByCreateTimeDesc(String phone, Integer delFlag, Pageable pageable);

    /**
     * 复合条件查询用户
     * @param userName 用户名关键字
     * @param nickName 昵称关键字
     * @param phone 手机号关键字
     * @param status 用户状态
     * @param delFlag 删除标志
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:userName IS NULL OR u.userName LIKE %:userName%) AND " +
           "(:nickName IS NULL OR u.nickName LIKE %:nickName%) AND " +
           "(:phone IS NULL OR u.phone LIKE %:phone%) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "u.delFlag = :delFlag " +
           "ORDER BY u.createTime DESC")
    Page<User> findByConditions(@Param("userName") String userName,
                               @Param("nickName") String nickName,
                               @Param("phone") String phone,
                               @Param("status") String status,
                               @Param("delFlag") Integer delFlag,
                               Pageable pageable);

    /**
     * 统计正常状态的用户数量
     * @param status 用户状态
     * @param delFlag 删除标志
     * @return 用户数量
     */
    long countByStatusAndDelFlag(String status, Integer delFlag);

    /**
     * 批量更新用户状态
     * @param userIds 用户ID列表
     * @param status 新状态
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :status, u.updateTime = CURRENT_TIMESTAMP WHERE u.id IN :userIds AND u.delFlag = 0")
    int updateStatusByIds(@Param("userIds") List<Long> userIds, @Param("status") String status);

    /**
     * 批量逻辑删除用户
     * @param userIds 用户ID列表
     * @return 删除的记录数
     */
    @Modifying
    @Query("UPDATE User u SET u.delFlag = 1, u.updateTime = CURRENT_TIMESTAMP WHERE u.id IN :userIds")
    int deleteByIds(@Param("userIds") List<Long> userIds);

    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码（已加密）
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE User u SET u.password = :newPassword, u.updateTime = CURRENT_TIMESTAMP WHERE u.id = :userId AND u.delFlag = 0")
    int resetPassword(@Param("userId") Long userId, @Param("newPassword") String newPassword);

    /**
     * 查询所有正常状态的用户（用于导出）
     * @param delFlag 删除标志
     * @return 用户列表
     */
    List<User> findByDelFlagOrderByCreateTimeDesc(Integer delFlag);
}