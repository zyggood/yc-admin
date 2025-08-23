package com.yc.admin.auth.service;

import com.yc.admin.auth.dto.LoginDTO;
import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.auth.dto.AuthLoginUser;
import com.yc.admin.system.api.dto.AuthRoleDTO;
import com.yc.admin.system.api.dto.AuthUserDTO;
import com.yc.admin.system.api.UserApiService;
import com.yc.admin.system.api.RoleApiService;
import com.yc.admin.common.util.UserAgentUtils;
import com.yc.admin.system.api.dto.LoginLogEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 登录服务
 * 处理用户登录、登出等认证相关业务
 *
 * @author yc
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserApiService userApiService;
    private final RoleApiService roleApiService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 用户登录
     * 
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    public Map<String, Object> login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        // 参数校验
        if (!StringUtils.hasText(username)) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(password)) {
            throw new BusinessException("密码不能为空");
        }
        
        try {
            // 用户认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            // 获取认证用户信息
            AuthLoginUser authLoginUser = (AuthLoginUser) authentication.getPrincipal();
            
            // 记录登录日志
            recordLoginLog(authLoginUser.getUser(), true, "登录成功");
            
            // 生成令牌
            String token = tokenService.createToken(authLoginUser);
            
            log.info("用户 {} 登录成功", username);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", getTokenExpiration());
            result.put("user", userApiService.findById(authLoginUser.getUser().getId())); //TODO 这里把加密后密码传出去了，待修复
            
            return result;
            
        } catch (Exception e) {
            // 记录登录失败日志
            AuthUserDTO user = userApiService.findAuthUserByUsername(username).orElse(null);
            recordLoginLog(user, false, e.getMessage());
            
            log.warn("用户 {} 登录失败: {}", username, e.getMessage());
            throw new BusinessException("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     * 
     * @param request 请求对象
     */
    public void logout(HttpServletRequest request) {
        // 从请求头获取token
        String token = getTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            return;
        }
        
        try {
            // 获取当前用户信息
            AuthLoginUser authLoginUser = tokenService.getLoginUser(token);
            if (authLoginUser != null) {
                // 记录登出日志
                recordLogoutLog(authLoginUser.getUser(), "登出成功");
                
                log.info("用户 {} 登出成功", authLoginUser.getUsername());
            }
            
            // 删除令牌
            tokenService.deleteToken(token);
            
        } catch (Exception e) {
            log.warn("用户登出时发生错误: {}", e.getMessage());
        }
    }

    /**
     * 刷新令牌
     * 
     * @param token 当前令牌
     * @return 新令牌信息
     */
    public Map<String, Object> refreshToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException("令牌不能为空");
        }
        
        try {
            // 获取用户信息
            AuthLoginUser authLoginUser = tokenService.getLoginUser(token);
            if (authLoginUser == null) {
                throw new BusinessException("令牌无效");
            }
            
            // 验证令牌
            if (!tokenService.verifyToken(authLoginUser)) {
                throw new BusinessException("令牌验证失败");
            }
            
            // 生成新令牌
            String newToken = tokenService.createToken(authLoginUser);
            
            // 删除旧令牌
            tokenService.deleteToken(token);
            
            log.info("用户 {} 令牌刷新成功", authLoginUser.getUsername());
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", newToken);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", getTokenExpiration());
            
            return result;
            
        } catch (Exception e) {
            log.warn("刷新令牌失败: {}", e.getMessage());
            throw new BusinessException("刷新令牌失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户
     * 
     * @return 当前登录用户
     */
    public AuthLoginUser getCurrentLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthLoginUser) {
            return (AuthLoginUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户信息
     * 
     * @return 当前用户信息
     */
    public AuthUserDTO getCurrentUser() {
        AuthLoginUser authLoginUser = getCurrentLoginUser();
        if (authLoginUser != null && authLoginUser.getUser() != null) {
            return userApiService.findById(authLoginUser.getUser().getId()).orElse(null);
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID
     */
    public Long getCurrentUserId() {
        AuthLoginUser authLoginUser = getCurrentLoginUser();
        return authLoginUser != null && authLoginUser.getUser() != null ? authLoginUser.getUser().getId() : null;
    }

    /**
     * 获取当前登录用户名
     * 
     * @return 用户名
     */
    public String getCurrentUsername() {
        AuthLoginUser authLoginUser = getCurrentLoginUser();
        return authLoginUser != null ? authLoginUser.getUsername() : null;
    }

    /**
     * 创建令牌
     * 
     * @param authLoginUser 登录用户
     * @return JWT 令牌
     */
    public String createToken(AuthLoginUser authLoginUser) {
        return tokenService.createToken(authLoginUser);
    }

    /**
     * 获取当前用户权限
     * 
     * @return 权限信息
     */
    public Map<String, Object> getCurrentUserPermissions() {
        AuthLoginUser authLoginUser = getCurrentLoginUser();
        if (authLoginUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("userId", authLoginUser.getUser().getId());
        permissions.put("username", authLoginUser.getUsername());
        permissions.put("authorities", authLoginUser.getAuthorities());
        // 获取用户角色
        List<AuthRoleDTO> userRoles = roleApiService.findAuthRolesByUserId(authLoginUser.getUser().getId());
        List<String> roleKeys = userRoles.stream()
            .map(AuthRoleDTO::getRoleKey)
            .collect(java.util.stream.Collectors.toList());
        permissions.put("roles", roleKeys);
        
        return permissions;
    }
    
    /**
     * 从请求中获取token
     * 
     * @param request 请求对象
     * @return token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取令牌过期时间
     * 
     * @return 过期时间（秒）
     */
    public long getTokenExpiration() {
        return tokenService.getTokenExpiration();
    }

    /**
     * 记录登录日志
     * 
     * @param user 用户信息
     * @param success 是否成功
     * @param message 消息
     */
    private void recordLoginLog(AuthUserDTO user, boolean success, String message) {
        try {
            HttpServletRequest request = getCurrentHttpRequest();
            if (request == null) {
                log.warn("无法获取HTTP请求，跳过登录日志记录");
                return;
            }
            
            String username = user != null ? user.getUserName() : "未知";
            String ipAddr = UserAgentUtils.getClientIpAddress(request);
            String userAgent = UserAgentUtils.getUserAgent(request);
            String browser = UserAgentUtils.getBrowser(userAgent);
            String os = UserAgentUtils.getOperatingSystem(userAgent);
            String loginLocation = UserAgentUtils.getLoginLocation(ipAddr);
            
            // 发布登录日志事件（异步处理）
            LoginLogEvent.EventType eventType = success ? 
                    LoginLogEvent.EventType.LOGIN_SUCCESS : LoginLogEvent.EventType.LOGIN_FAILURE;
            
            LoginLogEvent loginLogEvent = new LoginLogEvent(
                    this, eventType, username, ipAddr, userAgent, 
                    loginLocation, browser, os, message
            );
            
            eventPublisher.publishEvent(loginLogEvent);
            
            log.debug("登录日志事件发布成功 - 用户: {}, 成功: {}, IP: {}, 消息: {}", 
                    username, success, ipAddr, message);
        } catch (Exception e) {
            log.warn("发布登录日志事件失败: {}", e.getMessage());
        }
    }
    
    /**
     * 记录登出日志
     * 
     * @param user 用户信息
     * @param message 消息
     */
    private void recordLogoutLog(AuthUserDTO user, String message) {
        try {
            HttpServletRequest request = getCurrentHttpRequest();
            if (request == null) {
                log.warn("无法获取HTTP请求，跳过登出日志记录");
                return;
            }
            
            String username = user != null ? user.getUserName() : "未知";
            String ipAddr = UserAgentUtils.getClientIpAddress(request);
            String userAgent = UserAgentUtils.getUserAgent(request);
            String browser = UserAgentUtils.getBrowser(userAgent);
            String os = UserAgentUtils.getOperatingSystem(userAgent);
            String loginLocation = UserAgentUtils.getLoginLocation(ipAddr);
            
            // 发布登出日志事件（异步处理）
            LoginLogEvent logoutEvent = new LoginLogEvent(
                    this, LoginLogEvent.EventType.LOGOUT, username, ipAddr, userAgent, 
                    loginLocation, browser, os, message
            );
            
            eventPublisher.publishEvent(logoutEvent);
            
            log.debug("登出日志事件发布成功 - 用户: {}, IP: {}, 消息: {}", 
                    username, ipAddr, message);
        } catch (Exception e) {
            log.warn("发布登出日志事件失败: {}", e.getMessage());
        }
    }
    
    /**
     * 获取当前HTTP请求
     * 
     * @return HTTP请求对象
     */
    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("获取HTTP请求失败: {}", e.getMessage());
            return null;
        }
    }
}