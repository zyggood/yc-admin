package com.yc.admin.system.log.service;

import com.yc.admin.system.log.entity.SysLoginLog;
import com.yc.admin.system.log.repository.SysLoginLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统登录日志业务服务类
 *
 * @author yc
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginLogService {

    private final SysLoginLogRepository loginLogRepository;

    /**
     * 保存登录日志
     *
     * @param loginLog 登录日志
     * @return 保存后的登录日志
     */
    @Transactional
    public SysLoginLog save(SysLoginLog loginLog) {
        try {
            if (loginLog.getLoginTime() == null) {
                loginLog.setLoginTime(LocalDateTime.now());
            }
            SysLoginLog saved = loginLogRepository.save(loginLog);
            log.debug("保存登录日志成功: 用户={}, 状态={}, IP={}", 
                    loginLog.getUsername(), loginLog.getStatus(), loginLog.getIpAddr());
            return saved;
        } catch (Exception e) {
            log.error("保存登录日志失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 记录登录成功日志
     *
     * @param username      用户名
     * @param ipAddr        IP地址
     * @param userAgent     用户代理
     * @param loginLocation 登录地点
     * @param browser       浏览器
     * @param os            操作系统
     * @param message       消息
     */
    @Transactional
    public void recordLoginSuccess(String username, String ipAddr, String userAgent, 
                                   String loginLocation, String browser, String os, String message) {
        SysLoginLog loginLog = new SysLoginLog()
                .setUsername(username)
                .setIpAddr(ipAddr)
                .setUserAgent(userAgent)
                .setLoginLocation(loginLocation)
                .setBrowser(browser)
                .setOs(os)
                .setStatus(SysLoginLog.STATUS_SUCCESS)
                .setLoginType(SysLoginLog.TYPE_LOGIN)
                .setMsg(message)
                .setLoginTime(LocalDateTime.now());
        
        save(loginLog);
    }

    /**
     * 记录登录失败日志
     *
     * @param username      用户名
     * @param ipAddr        IP地址
     * @param userAgent     用户代理
     * @param loginLocation 登录地点
     * @param browser       浏览器
     * @param os            操作系统
     * @param message       失败原因
     */
    @Transactional
    public void recordLoginFailure(String username, String ipAddr, String userAgent, 
                                   String loginLocation, String browser, String os, String message) {
        SysLoginLog loginLog = new SysLoginLog()
                .setUsername(username)
                .setIpAddr(ipAddr)
                .setUserAgent(userAgent)
                .setLoginLocation(loginLocation)
                .setBrowser(browser)
                .setOs(os)
                .setStatus(SysLoginLog.STATUS_FAIL)
                .setLoginType(SysLoginLog.TYPE_LOGIN)
                .setMsg(message)
                .setLoginTime(LocalDateTime.now());
        
        save(loginLog);
    }

    /**
     * 记录登出日志
     *
     * @param username      用户名
     * @param ipAddr        IP地址
     * @param userAgent     用户代理
     * @param loginLocation 登录地点
     * @param browser       浏览器
     * @param os            操作系统
     * @param message       消息
     */
    @Transactional
    public void recordLogout(String username, String ipAddr, String userAgent, 
                             String loginLocation, String browser, String os, String message) {
        SysLoginLog loginLog = new SysLoginLog()
                .setUsername(username)
                .setIpAddr(ipAddr)
                .setUserAgent(userAgent)
                .setLoginLocation(loginLocation)
                .setBrowser(browser)
                .setOs(os)
                .setStatus(SysLoginLog.STATUS_SUCCESS)
                .setLoginType(SysLoginLog.TYPE_LOGOUT)
                .setMsg(message)
                .setLoginTime(LocalDateTime.now());
        
        save(loginLog);
    }

    /**
     * 分页查询登录日志
     *
     * @param pageable 分页参数
     * @return 登录日志分页数据
     */
    public Page<SysLoginLog> findAll(Pageable pageable) {
        // 默认按登录时间倒序排列
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                    Sort.by(Sort.Direction.DESC, "loginTime"));
        }
        return loginLogRepository.findAll(pageable);
    }

    /**
     * 多条件查询登录日志
     *
     * @param username  用户名
     * @param status    登录状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageable  分页参数
     * @return 登录日志分页数据
     */
    public Page<SysLoginLog> findByConditions(String username, Integer status, 
                                              LocalDateTime startTime, LocalDateTime endTime, 
                                              Pageable pageable) {
        // 处理空字符串
        username = StringUtils.hasText(username) ? username.trim() : null;
        
        return loginLogRepository.findByConditions(username, status, startTime, endTime, pageable);
    }

    /**
     * 根据用户名查询最近的登录记录
     *
     * @param username 用户名
     * @param limit    限制数量
     * @return 最近的登录记录
     */
    public List<SysLoginLog> findRecentLoginsByUsername(String username, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return loginLogRepository.findRecentLoginsByUsername(username, pageable);
    }

    /**
     * 统计指定时间范围内的登录次数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 登录次数
     */
    public Long countSuccessLogins(LocalDateTime startTime, LocalDateTime endTime) {
        return loginLogRepository.countSuccessLoginsBetween(startTime, endTime);
    }

    /**
     * 统计指定时间范围内的登录失败次数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 登录失败次数
     */
    public Long countFailedLogins(LocalDateTime startTime, LocalDateTime endTime) {
        return loginLogRepository.countFailedLoginsBetween(startTime, endTime);
    }

    /**
     * 清理指定时间之前的登录日志
     *
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    @Transactional
    public int cleanupLogsBefore(LocalDateTime beforeTime) {
        try {
            int deletedCount = loginLogRepository.deleteByLoginTimeBefore(beforeTime);
            log.info("清理登录日志完成，删除记录数: {}, 清理时间点: {}", deletedCount, beforeTime);
            return deletedCount;
        } catch (Exception e) {
            log.error("清理登录日志失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 根据ID查询登录日志
     *
     * @param id 日志ID
     * @return 登录日志
     */
    public SysLoginLog findById(Long id) {
        return loginLogRepository.findById(id).orElse(null);
    }

    /**
     * 删除登录日志
     *
     * @param id 日志ID
     */
    @Transactional
    public void deleteById(Long id) {
        loginLogRepository.deleteById(id);
        log.info("删除登录日志: {}", id);
    }
}