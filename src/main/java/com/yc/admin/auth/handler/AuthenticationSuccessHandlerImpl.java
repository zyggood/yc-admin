package com.yc.admin.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yc.admin.auth.service.LoginService;
import com.yc.admin.common.core.Result;
import com.yc.admin.user.entity.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证成功处理器
 * 处理用户登录成功后的逻辑，生成 JWT 令牌并返回给客户端
 *
 * @author yc
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    private final LoginService loginService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        log.info("用户登录成功: {}", authentication.getName());
        
        try {
            // 获取登录用户信息
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            
            // 生成 JWT 令牌
            String token = loginService.createToken(loginUser);
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("tokenType", "Bearer");
            data.put("expiresIn", loginService.getTokenExpiration());
            data.put("userInfo", buildUserInfo(loginUser));
            
            // 构建成功响应
            Result<Map<String, Object>> result = Result.success("登录成功", data);
            
            // 设置响应头
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            
            // 写入响应
            response.getWriter().write(objectMapper.writeValueAsString(result));
            
            log.info("登录成功响应已发送: {}", authentication.getName());
            
        } catch (Exception e) {
            log.error("处理登录成功时发生错误: {}", e.getMessage(), e);
            
            // 构建错误响应
            Result<Object> errorResult = Result.error("登录处理失败");
            
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(objectMapper.writeValueAsString(errorResult));
        }
    }

    /**
     * 构建用户信息
     * 
     * @param loginUser 登录用户
     * @return 用户信息
     */
    private Map<String, Object> buildUserInfo(LoginUser loginUser) {
        Map<String, Object> userInfo = new HashMap<>();
        
        if (loginUser.getUser() != null) {
            userInfo.put("userId", loginUser.getUser().getId());
            userInfo.put("username", loginUser.getUser().getUserName());
            userInfo.put("nickname", loginUser.getUser().getNickName());
            userInfo.put("email", loginUser.getUser().getEmail());
            userInfo.put("phone", loginUser.getUser().getPhone());
            userInfo.put("avatar", loginUser.getUser().getAvatar());
            userInfo.put("status", loginUser.getUser().getStatus());
            userInfo.put("loginTime", System.currentTimeMillis());
        }
        
        // 添加权限信息
        userInfo.put("permissions", loginUser.getPermissions());
        userInfo.put("roles", loginUser.getRoles());
        
        return userInfo;
    }
}