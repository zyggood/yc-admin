package com.yc.admin.auth.controller;

import com.yc.admin.auth.dto.LoginDTO;
import com.yc.admin.auth.dto.RefreshTokenDTO;
import com.yc.admin.auth.service.LoginService;
import com.yc.admin.common.core.Result;
import com.yc.admin.system.user.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * @author yc
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @param request 请求对象
     * @param response 响应对象
     * @return 登录结果
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Validated @RequestBody LoginDTO loginDTO,
            HttpServletRequest request,
            HttpServletResponse response) {
        Map<String, Object> result = loginService.login(loginDTO);
        return Result.success(result);
    }

    /**
     * 用户登出
     * @param request 请求对象
     * @param response 响应对象
     * @return 登出结果
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        loginService.logout(request);
        return Result.success();
    }

    /**
     * 刷新令牌
     * @param refreshTokenDTO 刷新令牌信息
     * @return 新的令牌信息
     */
    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh(@Validated @RequestBody RefreshTokenDTO refreshTokenDTO) {
        Map<String, Object> result = loginService.refreshToken(refreshTokenDTO.getRefreshToken());
        return Result.success(result);
    }

    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser() {
        UserDTO user = loginService.getCurrentUser();
        return Result.success(user);
    }

    /**
     * 获取当前用户权限
     * @return 权限列表
     */
    @Operation(summary = "获取当前用户权限")
    @GetMapping("/permissions")
    public Result<Map<String, Object>> getCurrentUserPermissions() {
        Map<String, Object> permissions = loginService.getCurrentUserPermissions();
        return Result.success(permissions);
    }
}