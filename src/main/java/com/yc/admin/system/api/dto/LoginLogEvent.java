package com.yc.admin.system.api.dto;

import org.springframework.context.ApplicationEvent;

/**
 * 登录日志事件
 * 用于异步处理登录、登出日志记录
 */
public class LoginLogEvent extends ApplicationEvent {
    
    /**
     * 事件类型枚举
     */
    public enum EventType {
        LOGIN_SUCCESS,  // 登录成功
        LOGIN_FAILURE,  // 登录失败
        LOGOUT          // 登出
    }
    
    private final EventType eventType;
    private final String username;
    private final String ipAddr;
    private final String userAgent;
    private final String loginLocation;
    private final String browser;
    private final String os;
    private final String message;
    
    /**
     * 构造函数
     * @param source 事件源
     * @param eventType 事件类型
     * @param username 用户名
     * @param ipAddr IP地址
     * @param userAgent 用户代理
     * @param loginLocation 登录地点
     * @param browser 浏览器
     * @param os 操作系统
     * @param message 消息
     */
    public LoginLogEvent(Object source, EventType eventType, String username, 
                        String ipAddr, String userAgent, String loginLocation, 
                        String browser, String os, String message) {
        super(source);
        this.eventType = eventType;
        this.username = username;
        this.ipAddr = ipAddr;
        this.userAgent = userAgent;
        this.loginLocation = loginLocation;
        this.browser = browser;
        this.os = os;
        this.message = message;
    }
    
    // Getters
    public EventType getEventType() {
        return eventType;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getIpAddr() {
        return ipAddr;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public String getLoginLocation() {
        return loginLocation;
    }
    
    public String getBrowser() {
        return browser;
    }
    
    public String getOs() {
        return os;
    }
    
    public String getMessage() {
        return message;
    }
}