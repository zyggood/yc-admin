package com.yc.admin.system.api.impl;

import com.yc.admin.system.api.LoginLogApiService;
import com.yc.admin.system.log.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 登录日志API服务实现类
 *
 * @author yc
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
public class LoginLogApiServiceImpl implements LoginLogApiService {

    private final SysLoginLogService sysLoginLogService;

    @Override
    public void recordLoginSuccess(String username, String ipAddr, String userAgent, 
                                   String loginLocation, String browser, String os, String message) {
        sysLoginLogService.recordLoginSuccess(username, ipAddr, userAgent, 
                loginLocation, browser, os, message);
    }

    @Override
    public void recordLoginFailure(String username, String ipAddr, String userAgent, 
                                   String loginLocation, String browser, String os, String message) {
        sysLoginLogService.recordLoginFailure(username, ipAddr, userAgent, 
                loginLocation, browser, os, message);
    }

    @Override
    public void recordLogout(String username, String ipAddr, String userAgent, 
                             String loginLocation, String browser, String os, String message) {
        sysLoginLogService.recordLogout(username, ipAddr, userAgent, 
                loginLocation, browser, os, message);
    }
}