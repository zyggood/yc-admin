package com.yc.admin.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yc.admin.common.core.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 认证失败处理器
 * 处理用户登录失败的情况，返回相应的错误信息
 *
 * @author yc
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        
        log.warn("用户登录失败: {}", exception.getMessage());

        // 根据异常类型确定错误信息
        String errorMessage = getErrorMessage(exception);
        int errorCode = getErrorCode(exception);
        
        // 构建失败响应
        Result<Object> result = Result.error(errorCode, errorMessage);
        
        // 设置响应头
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(result));
        
        log.info("登录失败响应已发送: {}", errorMessage);
    }

    /**
     * 根据异常类型获取错误信息
     * 
     * @param exception 认证异常
     * @return 错误信息
     */
    private String getErrorMessage(AuthenticationException exception) {
        if (exception instanceof UsernameNotFoundException) {
            return "用户名不存在";
        } else if (exception instanceof BadCredentialsException) {
            return "用户名或密码错误";
        } else if (exception instanceof LockedException) {
            return "账户已被锁定";
        } else if (exception instanceof DisabledException) {
            return "账户已被禁用";
        } else {
            return "登录失败: " + exception.getMessage();
        }
    }

    /**
     * 根据异常类型获取错误代码
     * 
     * @param exception 认证异常
     * @return 错误代码
     */
    private int getErrorCode(AuthenticationException exception) {
        if (exception instanceof UsernameNotFoundException) {
            return 40001; // 用户不存在
        } else if (exception instanceof BadCredentialsException) {
            return 40002; // 密码错误
        } else if (exception instanceof LockedException) {
            return 40003; // 账户锁定
        } else if (exception instanceof DisabledException) {
            return 40004; // 账户禁用
        } else {
            return 40000; // 通用认证失败
        }
    }
}