package com.yc.admin.auth.service;

import com.yc.admin.auth.dto.LoginDTO;
import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.user.dto.UserDTO;
import com.yc.admin.system.user.entity.LoginUser;
import com.yc.admin.system.user.entity.User;
import com.yc.admin.system.api.UserApiService;
import com.yc.admin.system.api.RoleApiService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            
            // 记录登录日志
            recordLoginLog(loginUser.getUser(), true, "登录成功");
            
            // 生成令牌
            String token = tokenService.createToken(loginUser);
            
            log.info("用户 {} 登录成功", username);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", getTokenExpiration());
            result.put("user", userApiService.findById(loginUser.getUser().getId()));
            
            return result;
            
        } catch (Exception e) {
            // 记录登录失败日志
            User user = userApiService.findByUsername(username).orElse(null);
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
            LoginUser loginUser = tokenService.getLoginUser(token);
            if (loginUser != null) {
                // 记录登出日志
                recordLoginLog(loginUser.getUser(), true, "登出成功");
                
                log.info("用户 {} 登出成功", loginUser.getUsername());
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
            LoginUser loginUser = tokenService.getLoginUser(token);
            if (loginUser == null) {
                throw new BusinessException("令牌无效");
            }
            
            // 验证令牌
            if (!tokenService.verifyToken(loginUser)) {
                throw new BusinessException("令牌验证失败");
            }
            
            // 生成新令牌
            String newToken = tokenService.createToken(loginUser);
            
            // 删除旧令牌
            tokenService.deleteToken(token);
            
            log.info("用户 {} 令牌刷新成功", loginUser.getUsername());
            
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
    public LoginUser getCurrentLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户信息
     * 
     * @return 当前用户信息
     */
    public UserDTO getCurrentUser() {
        LoginUser loginUser = getCurrentLoginUser();
        if (loginUser != null && loginUser.getUser() != null) {
            return userApiService.findById(loginUser.getUser().getId());
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID
     */
    public Long getCurrentUserId() {
        LoginUser loginUser = getCurrentLoginUser();
        return loginUser != null && loginUser.getUser() != null ? loginUser.getUser().getId() : null;
    }

    /**
     * 获取当前登录用户名
     * 
     * @return 用户名
     */
    public String getCurrentUsername() {
        LoginUser loginUser = getCurrentLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    /**
     * 创建令牌
     * 
     * @param loginUser 登录用户
     * @return JWT 令牌
     */
    public String createToken(LoginUser loginUser) {
        return tokenService.createToken(loginUser);
    }

    /**
     * 获取当前用户权限
     * 
     * @return 权限信息
     */
    public Map<String, Object> getCurrentUserPermissions() {
        LoginUser loginUser = getCurrentLoginUser();
        if (loginUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("userId", loginUser.getUser().getId());
        permissions.put("username", loginUser.getUsername());
        permissions.put("authorities", loginUser.getAuthorities());
        // 获取用户角色
        List<Role> userRoles = roleApiService.findByUserId(loginUser.getUser().getId());
        List<String> roleKeys = userRoles.stream()
            .map(Role::getRoleKey)
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
    private void recordLoginLog(User user, boolean success, String message) {
        try {
            // TODO: 实现登录日志记录功能
            // 可以记录到数据库或日志文件
            log.info("登录日志 - 用户: {}, 成功: {}, 消息: {}", 
                user != null ? user.getUserName() : "未知", success, message);
        } catch (Exception e) {
            log.warn("记录登录日志失败: {}", e.getMessage());
        }
    }
}