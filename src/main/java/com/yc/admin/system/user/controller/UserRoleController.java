package com.yc.admin.system.user.controller;

import com.yc.admin.common.core.Result;
import com.yc.admin.system.user.entity.UserRole;
import com.yc.admin.system.user.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户角色关联控制器
 * 提供用户角色关联的REST API接口
 *
 * @author YC
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/user-roles")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户角色关联管理", description = "用户角色关联的增删改查接口")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping("/user/{userId}/roles")
    @Operation(summary = "根据用户ID查询角色ID列表", description = "获取指定用户的所有角色ID")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<List<Long>> getRolesByUserId(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        List<Long> roleIds = userRoleService.getRoleIdsByUserId(userId);
        return Result.success(roleIds);
    }

    @GetMapping("/role/{roleId}/users")
    @Operation(summary = "根据角色ID查询用户ID列表", description = "获取指定角色的所有用户ID")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<List<Long>> getUsersByRoleId(
            @Parameter(description = "角色ID", required = true)
            @PathVariable @NotNull Long roleId) {
        List<Long> userIds = userRoleService.getUserIdsByRoleId(roleId);
        return Result.success(userIds);
    }

    @PostMapping("/users/roles")
    @Operation(summary = "批量查询用户角色关联", description = "根据用户ID列表查询用户角色关联")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<List<UserRole>> getUserRolesByUserIds(
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> userIds) {
        List<UserRole> userRoles = userRoleService.getUserRolesByUserIds(userIds);
        return Result.success(userRoles);
    }

    @PostMapping("/roles/users")
    @Operation(summary = "批量查询角色用户关联", description = "根据角色ID列表查询角色用户关联")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<List<UserRole>> getUserRolesByRoleIds(
            @Parameter(description = "角色ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> roleIds) {
        List<UserRole> userRoles = userRoleService.getUserRolesByRoleIds(roleIds);
        return Result.success(userRoles);
    }

    @GetMapping("/exists")
    @Operation(summary = "检查用户角色关联是否存在", description = "检查指定用户和角色的关联是否存在")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<Boolean> existsUserRole(
            @Parameter(description = "用户ID", required = true)
            @RequestParam @NotNull Long userId,
            @Parameter(description = "角色ID", required = true)
            @RequestParam @NotNull Long roleId) {
        boolean exists = userRoleService.existsUserRole(userId, roleId);
        return Result.success(exists);
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "统计用户的角色数量", description = "获取指定用户的角色数量")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<Long> countRolesByUserId(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        long count = userRoleService.countRolesByUserId(userId);
        return Result.success(count);
    }

    @GetMapping("/role/{roleId}/count")
    @Operation(summary = "统计角色的用户数量", description = "获取指定角色的用户数量")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<Long> countUsersByRoleId(
            @Parameter(description = "角色ID", required = true)
            @PathVariable @NotNull Long roleId) {
        long count = userRoleService.countUsersByRoleId(roleId);
        return Result.success(count);
    }

    @PostMapping("/user/{userId}/assign")
    @Operation(summary = "为用户分配角色", description = "为指定用户分配角色列表，会覆盖原有角色")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> assignRolesToUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @Parameter(description = "角色ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> roleIds) {
        userRoleService.assignRolesToUser(userId, roleIds);
        return Result.success();
    }

    @PostMapping("/role/{roleId}/assign")
    @Operation(summary = "为角色分配用户", description = "为指定角色分配用户列表，会覆盖原有用户")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> assignUsersToRole(
            @Parameter(description = "角色ID", required = true)
            @PathVariable @NotNull Long roleId,
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> userIds) {
        userRoleService.assignUsersToRole(roleId, userIds);
        return Result.success();
    }

    @PostMapping("/add")
    @Operation(summary = "添加用户角色关联", description = "添加单个用户角色关联")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> addUserRole(
            @Parameter(description = "用户ID", required = true)
            @RequestParam @NotNull Long userId,
            @Parameter(description = "角色ID", required = true)
            @RequestParam @NotNull Long roleId) {
        userRoleService.addUserRole(userId, roleId);
        return Result.success();
    }

    @PostMapping("/user/{userId}/add")
    @Operation(summary = "批量添加用户角色关联", description = "为指定用户批量添加角色关联")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> addUserRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @Parameter(description = "角色ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> roleIds) {
        userRoleService.addUserRoles(userId, roleIds);
        return Result.success();
    }

    @DeleteMapping("/remove")
    @Operation(summary = "删除用户角色关联", description = "删除单个用户角色关联")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> removeUserRole(
            @Parameter(description = "用户ID", required = true)
            @RequestParam @NotNull Long userId,
            @Parameter(description = "角色ID", required = true)
            @RequestParam @NotNull Long roleId) {
        userRoleService.removeUserRole(userId, roleId);
        return Result.success();
    }

    @DeleteMapping("/user/{userId}/remove")
    @Operation(summary = "批量删除用户角色关联", description = "批量删除指定用户的角色关联")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> removeUserRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @Parameter(description = "角色ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> roleIds) {
        userRoleService.removeUserRoles(userId, roleIds);
        return Result.success();
    }

    @DeleteMapping("/user/{userId}/all")
    @Operation(summary = "删除用户的所有角色关联", description = "删除指定用户的所有角色关联")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> removeAllUserRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        userRoleService.removeAllUserRoles(userId);
        return Result.success();
    }

    @DeleteMapping("/role/{roleId}/all")
    @Operation(summary = "删除角色的所有用户关联", description = "删除指定角色的所有用户关联")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> removeAllRoleUsers(
            @Parameter(description = "角色ID", required = true)
            @PathVariable @NotNull Long roleId) {
        userRoleService.removeAllRoleUsers(roleId);
        return Result.success();
    }

    @DeleteMapping("/users/batch")
    @Operation(summary = "批量删除用户角色关联", description = "根据用户ID列表批量删除用户角色关联")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Void> removeUserRolesByUserIds(
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> userIds) {
        userRoleService.removeUserRolesByUserIds(userIds);
        return Result.success();
    }

    @DeleteMapping("/roles/batch")
    @Operation(summary = "批量删除角色用户关联", description = "根据角色ID列表批量删除角色用户关联")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public Result<Void> removeUserRolesByRoleIds(
            @Parameter(description = "角色ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> roleIds) {
        userRoleService.removeUserRolesByRoleIds(roleIds);
        return Result.success();
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取用户角色关联统计信息", description = "获取用户角色关联的统计数据")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<Map<String, Object>> getStatistics() {
        // 这里可以根据需要添加统计逻辑
        Map<String, Object> statistics = Map.of(
                "message", "用户角色关联统计功能待实现"
        );
        return Result.success(statistics);
    }
}