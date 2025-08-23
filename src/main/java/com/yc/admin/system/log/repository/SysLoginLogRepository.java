package com.yc.admin.system.log.repository;

import com.yc.admin.system.log.entity.SysLoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统登录日志数据访问接口
 *
 * @author yc
 * @since 2024-01-01
 */
@Repository
public interface SysLoginLogRepository extends JpaRepository<SysLoginLog, Long> {

    /**
     * 根据用户名查询登录日志
     *
     * @param username 用户名
     * @param pageable 分页参数
     * @return 登录日志分页数据
     */
    Page<SysLoginLog> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    /**
     * 根据登录状态查询登录日志
     *
     * @param status   登录状态
     * @param pageable 分页参数
     * @return 登录日志分页数据
     */
    Page<SysLoginLog> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据时间范围查询登录日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageable  分页参数
     * @return 登录日志分页数据
     */
    Page<SysLoginLog> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 多条件查询登录日志
     *
     * @param username  用户名（模糊查询）
     * @param status    登录状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageable  分页参数
     * @return 登录日志分页数据
     */
    @Query("SELECT l FROM SysLoginLog l WHERE " +
            "(:username IS NULL OR l.username LIKE %:username%) AND " +
            "(:status IS NULL OR l.status = :status) AND " +
            "(:startTime IS NULL OR l.loginTime >= :startTime) AND " +
            "(:endTime IS NULL OR l.loginTime <= :endTime) " +
            "ORDER BY l.loginTime DESC")
    Page<SysLoginLog> findByConditions(@Param("username") String username,
                                       @Param("status") Integer status,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       Pageable pageable);

    /**
     * 根据用户名查询最近的登录记录
     *
     * @param username 用户名
     * @param limit    限制数量
     * @return 最近的登录记录
     */
    @Query("SELECT l FROM SysLoginLog l WHERE l.username = :username " +
            "ORDER BY l.loginTime DESC")
    List<SysLoginLog> findRecentLoginsByUsername(@Param("username") String username, Pageable pageable);

    /**
     * 统计指定时间范围内的登录次数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 登录次数
     */
    @Query("SELECT COUNT(l) FROM SysLoginLog l WHERE " +
            "l.loginTime BETWEEN :startTime AND :endTime AND l.status = 0")
    Long countSuccessLoginsBetween(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的登录失败次数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 登录失败次数
     */
    @Query("SELECT COUNT(l) FROM SysLoginLog l WHERE " +
            "l.loginTime BETWEEN :startTime AND :endTime AND l.status = 1")
    Long countFailedLoginsBetween(@Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的登录日志
     *
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    @Query("DELETE FROM SysLoginLog l WHERE l.loginTime < :beforeTime")
    int deleteByLoginTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);
}