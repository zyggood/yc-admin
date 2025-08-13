package com.yc.admin.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yc.admin.auth.service.TokenService;
import com.yc.admin.common.core.Result;
import com.yc.admin.user.entity.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 登出成功处理器
 * 处理用户登出成功的逻辑，清除 JWT 令牌
 *
 * @author yc
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    /**
     * JWT 令牌在请求头中的键名
     */
    private static final String TOKEN_HEADER = "Authorization";
    
    /**
     * JWT 令牌的前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        
        String username = "未知用户";
        
        try {
            // 获取当前用户信息
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                username = loginUser.getUsername();
            }
            
            // 获取请求头中的令牌
            String token = getToken(request);
            
            if (StringUtils.hasText(token)) {
                // 删除令牌
                tokenService.deleteToken(token);
                log.info("用户 {} 登出成功，令牌已清除", username);
            } else {
                log.info("用户 {} 登出成功，未找到令牌", username);
            }
            
            // 构建成功响应
            Result<Object> result = Result.success("登出成功");
            
            // 设置响应头
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            
            // 写入响应
            response.getWriter().write(objectMapper.writeValueAsString(result));
            
            log.info("登出成功响应已发送: {}", username);
            
        } catch (Exception e) {
            log.error("处理登出时发生错误: {}", e.getMessage(), e);
            
            // 构建错误响应
            Result<Object> errorResult = Result.error("登出处理失败");
            
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(objectMapper.writeValueAsString(errorResult));
        }
    }

    /**
     * 从请求头中获取令牌
     * 
     * @param request HTTP 请求
     * @return JWT 令牌，如果不存在则返回 null
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        
        if (StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());
        }
        
        return null;
    }
}