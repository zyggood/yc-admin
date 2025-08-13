package com.yc.admin.auth.filter;

import com.yc.admin.auth.service.TokenService;
import com.yc.admin.system.user.entity.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 从请求头中获取 JWT 令牌，验证并设置用户认证信息
 *
 * @author yc
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    /**
     * JWT 令牌在请求头中的键名
     */
    private static final String TOKEN_HEADER = "Authorization";
    
    /**
     * JWT 令牌的前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 获取请求头中的令牌
        String token = getToken(request);
        
        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 验证令牌并获取用户信息
                LoginUser loginUser = tokenService.getLoginUser(token);
                
                if (loginUser != null && tokenService.verifyToken(loginUser)) {
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authenticationToken = 
                        new UsernamePasswordAuthenticationToken(
                            loginUser, 
                            null, 
                            loginUser.getAuthorities()
                        );
                    
                    // 设置请求详情
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    
                    log.debug("用户 {} 认证成功", loginUser.getUsername());
                }
            } catch (Exception e) {
                log.warn("JWT 令牌验证失败: {}", e.getMessage());
                // 清除安全上下文
                SecurityContextHolder.clearContext();
            }
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
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

    /**
     * 判断是否需要过滤
     * 对于某些特定路径，可以跳过 JWT 验证
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 登录接口不需要验证
        if (path.equals("/api/auth/login") || 
            path.equals("/api/auth/logout") ||
            path.equals("/api/auth/captcha") ||
            path.equals("/api/auth/refresh")) {
            return true;
        }
        
        // 静态资源不需要验证
        if (path.startsWith("/css/") || 
            path.startsWith("/js/") ||
            path.startsWith("/images/") ||
            path.startsWith("/fonts/") ||
            path.startsWith("/static/") ||
            path.startsWith("/webjars/")) {
            return true;
        }
        
        // Swagger 文档不需要验证（开发环境）
        if (path.startsWith("/swagger-ui/") || 
            path.startsWith("/v3/api-docs/") ||
            path.startsWith("/swagger-resources/") ||
            path.equals("/doc.html")) {
            return true;
        }
        
        // 健康检查接口不需要验证
        if (path.startsWith("/actuator/health") || 
            path.startsWith("/actuator/info")) {
            return true;
        }
        
        return false;
    }
}