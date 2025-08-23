package com.yc.admin.system.log.entity;

import com.yc.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 系统登录日志实体
 * 记录用户登录、登出等认证相关操作
 *
 * @author yc
 * @since 2024-01-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "sys_login_log", indexes = {
    @Index(name = "idx_login_log_username", columnList = "username"),
    @Index(name = "idx_login_log_login_time", columnList = "loginTime"),
    @Index(name = "idx_login_log_status", columnList = "status")
})
@AttributeOverride(name = "id", column = @Column(name = "login_log_id"))
public class SysLoginLog extends BaseEntity {

    /**
     * 用户名
     */
    @Column(name = "username", length = 50, nullable = false)
    private String username;

    /**
     * 登录IP地址
     */
    @Column(name = "ip_addr", length = 128)
    private String ipAddr;

    /**
     * 登录地点
     */
    @Column(name = "login_location", length = 255)
    private String loginLocation;

    /**
     * 浏览器类型
     */
    @Column(name = "browser", length = 50)
    private String browser;

    /**
     * 操作系统
     */
    @Column(name = "os", length = 50)
    private String os;

    /**
     * 登录状态（0成功 1失败）
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 提示消息
     */
    @Column(name = "msg", length = 255)
    private String msg;

    /**
     * 登录时间
     */
    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    /**
     * 用户代理信息
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 登录类型（login-登录，logout-登出）
     */
    @Column(name = "login_type", length = 20)
    private String loginType;

    /**
     * 登录状态常量
     */
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAIL = 1;

    /**
     * 登录类型常量
     */
    public static final String TYPE_LOGIN = "login";
    public static final String TYPE_LOGOUT = "logout";
}