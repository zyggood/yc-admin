package com.yc.admin.system.api;

/**
 * 登录日志API服务接口
 * 提供登录日志记录功能的对外接口
 *
 * @author yc
 * @since 2024-01-01
 */
public interface LoginLogApiService {

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
    void recordLoginSuccess(String username, String ipAddr, String userAgent, 
                           String loginLocation, String browser, String os, String message);

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
    void recordLoginFailure(String username, String ipAddr, String userAgent, 
                           String loginLocation, String browser, String os, String message);

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
    void recordLogout(String username, String ipAddr, String userAgent, 
                     String loginLocation, String browser, String os, String message);
}