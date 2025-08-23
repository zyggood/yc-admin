package com.yc.admin.system.log.event;

import com.yc.admin.system.api.dto.LoginLogEvent;
import com.yc.admin.system.log.entity.SysLoginLog;
import com.yc.admin.system.log.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 登录日志事件监听器
 * 异步处理登录日志记录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginLogEventListener {

    private final SysLoginLogService sysLoginLogService;

    /**
     * 处理登录日志事件
     * @param event 登录日志事件
     */
    @EventListener(LoginLogEvent.class)
    @Async
    @Order
    public void handleLoginLogEvent(LoginLogEvent event) {
        try {
            log.debug("处理登录日志事件: 用户={}, 事件类型={}, IP={}", 
                     event.getUsername(), event.getEventType(), event.getIpAddr());
            
            // 创建登录日志实体
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUsername(event.getUsername());
            loginLog.setIpAddr(event.getIpAddr());
            loginLog.setUserAgent(event.getUserAgent());
            loginLog.setLoginLocation(event.getLoginLocation());
            loginLog.setBrowser(event.getBrowser());
            loginLog.setOs(event.getOs());
            loginLog.setMsg(event.getMessage());
            loginLog.setLoginTime(LocalDateTime.now());
            
            // 根据事件类型设置状态和登录类型
            switch (event.getEventType()) {
                case LOGIN_SUCCESS:
                    loginLog.setStatus(SysLoginLog.STATUS_SUCCESS);
                    loginLog.setLoginType(SysLoginLog.TYPE_LOGIN);
                    break;
                case LOGIN_FAILURE:
                    loginLog.setStatus(SysLoginLog.STATUS_FAIL);
                    loginLog.setLoginType(SysLoginLog.TYPE_LOGIN);
                    break;
                case LOGOUT:
                    loginLog.setStatus(SysLoginLog.STATUS_SUCCESS);
                    loginLog.setLoginType(SysLoginLog.TYPE_LOGOUT);
                    break;
                default:
                    log.warn("未知的登录事件类型: {}", event.getEventType());
                    return;
            }
            
            // 异步保存登录日志
            sysLoginLogService.save(loginLog);
            
            log.debug("登录日志记录成功: 用户={}, 事件类型={}", 
                     event.getUsername(), event.getEventType());
            
        } catch (Exception e) {
            log.error("处理登录日志事件失败: 用户={}, 事件类型={}, 错误信息={}", 
                     event.getUsername(), event.getEventType(), e.getMessage(), e);
            
            // 记录异常到系统日志，但不抛出异常，避免影响主业务流程
            try {
                SysLoginLog errorLog = new SysLoginLog();
                errorLog.setUsername(event.getUsername());
                errorLog.setIpAddr(event.getIpAddr());
                errorLog.setUserAgent(event.getUserAgent());
                errorLog.setLoginLocation(event.getLoginLocation());
                errorLog.setBrowser(event.getBrowser());
                errorLog.setOs(event.getOs());
                errorLog.setMsg("登录日志记录异常: " + e.getMessage());
                errorLog.setStatus(SysLoginLog.STATUS_FAIL);
                errorLog.setLoginType("SYSTEM_ERROR");
                errorLog.setLoginTime(LocalDateTime.now());
                
                sysLoginLogService.save(errorLog);
            } catch (Exception saveErrorException) {
                log.error("保存登录日志异常记录失败: {}", saveErrorException.getMessage(), saveErrorException);
            }
        }
    }
}