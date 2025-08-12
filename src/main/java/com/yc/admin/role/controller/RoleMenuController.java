package com.yc.admin.role.controller;

import com.yc.admin.common.core.Result;
import com.yc.admin.role.entity.RoleMenu;
import com.yc.admin.role.service.RoleMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色菜单关联控制器
 * 提供角色菜单关联的REST API接口
 *
 * @author YC
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/role-menus")
@RequiredArgsConstructor
@Validated
@Tag(name = "角色菜单关联管理", description = "角色菜单关联的增删改查接口")
public class RoleMenuController {

    private final RoleMenuService roleMenuService;

    @GetMapping("/role/{roleId}/menus")
    @Operation(summary = "根据角色ID查询菜单ID列表", description = "获取指定角色的所有菜单ID")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<List<Long>> getMenusByRoleId(
            @Parameter(description = "角色ID", required = true)
            @PathVariable @NotNull Long roleId) {
        List<Long> menuIds = roleMenuService.getMenuIdsByRoleId(roleId);
        return Result.success(menuIds);
    }

    @GetMapping("/menu/{menuId}/roles")
    @Operation(summary = "根据菜单ID查询角色ID列表", description = "获取指定菜单的所有角色ID")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public Result<List<Long>> getRolesByMenuId(
            @Parameter(description = "菜单ID", required = true)
            @PathVariable @NotNull Long menuId) {
        List<Long> roleIds = roleMenuService.getRoleIdsByMenuId(menuId);
        return Result.success(roleIds);
    }

    @GetMapping("/user/{userId}/menus")
    @Operation(summary = "根据用户ID查询菜单ID列表", description = "通过用户角色关联获取用户的所有菜单ID")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<List<Long>> getMenusByUserId(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        List<Long> menuIds = roleMenuService.getMenuIdsByUserId(userId);
        return Result.success(menuIds);
    }

    @GetMapping("/permission/{permission}/menus")
    @Operation(summary = "根据权限标识查询菜单ID列表", description = "获取指定权限标识的所有菜单ID")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public Result<List<Long>> getMenusByPermission(
            @Parameter(description = "权限标识", required = true)
            @PathVariable @NotNull String permission) {
        List<Long> menuIds = roleMenuService.getMenuIdsByPermission(permission);
        return Result.success(menuIds);
    }

    @PostMapping("/roles/menus")
    @Operation(summary = "批量查询角色菜单关联", description = "根据角色ID列表查询角色菜单关联")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<List<RoleMenu>> getRoleMenusByRoleIds(
            @Parameter(description = "角色ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> roleIds) {
        List<RoleMenu> roleMenus = roleMenuService.getRoleMenusByRoleIds(roleIds);
        return Result.success(roleMenus);
    }

    @PostMapping("/menus/roles")
    @Operation(summary = "批量查询菜单角色关联", description = "根据菜单ID列表查询菜单角色关联")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public Result<List<RoleMenu>> getRoleMenusByMenuIds(
            @Parameter(description = "菜单ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> menuIds) {
        List<RoleMenu> roleMenus = roleMenuService.getRoleMenusByMenuIds(menuIds);
        return Result.success(roleMenus);
    }

    @GetMapping("/exists")
    @Operation(summary = "检查角色菜单关联是否存在", description = "检查指定角色和菜单的关联是否存在")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<Boolean> existsRoleMenu(
            @Parameter(description = "角色ID", required = true)
            @RequestParam @NotNull Long roleId,
            @Parameter(description = "菜单ID", required = true)
            @RequestParam @NotNull Long menuId) {
        boolean exists = roleMenuService.existsRoleMenu(roleId, menuId);
        return Result.success(exists);
    }

    @GetMapping("/role/{roleId}/count")
    @Operation(summary = "统计角色的菜单数量", description = "获取指定角色的菜单数量")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<Long> countMenusByRoleId(
            @Parameter(description = "角色ID", required = true)
            @PathVariable @NotNull Long roleId) {
        long count = roleMenuService.countMenusByRoleId(roleId);
        return Result.success(count);
    }

    @GetMapping("/menu/{menuId}/count")
    @Operation(summary = "统计菜单的角色数量", description = "获取指定菜单的角色数量")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public Result<Long> countRolesByMenuId(
            @Parameter(description = "菜单ID", required = true)
            @PathVariable @NotNull Long menuId) {
        long count = roleMenuService.countRolesByMenuId(menuId);
        return Result.success(count);
    }

    @PostMapping("/role/{roleId}/assign")
    @Operation(summary = "为角色分配菜单权限", description = "为指定角色分配菜单权限列表，会覆盖原有权限")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> assignMenusToRole(
            @Parameter(description = "角色ID", required = true)
            @PathVariable @NotNull Long roleId,
            @Parameter(description = "菜单ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> menuIds) {
        roleMenuService.assignMenusToRole(roleId, menuIds);
        return Result.success();
    }

    @PostMapping("/menu/{menuId}/assign")
    @Operation(summary = "为菜单分配角色", description = "为指定菜单分配角色列表，会覆盖原有角色")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Void> assignRolesToMenu(
            @Parameter(description = "菜单ID", required = true)
            @PathVariable @NotNull Long menuId,
            @Parameter(description = "角色ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> roleIds) {
        roleMenuService.assignRolesToMenu(menuId, roleIds);
        return Result.success();
    }

    @PostMapping("/add")
    @Operation(summary = "添加角色菜单关联", description = "添加单个角色菜单关联")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> addRoleMenu(
            @Parameter(description = "角色ID", required = true)
            @RequestParam @NotNull Long roleId,
            @Parameter(description = "菜单ID", required = true)
            @RequestParam @NotNull Long menuId) {
        roleMenuService.addRoleMenu(roleId, menuId);
        return Result.success();
    }

    @PostMapping("/role/{roleId}/add")
    @Operation(summary = "批量添加角色菜单关联", description = "为指定角色批量添加菜单关联")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> addRoleMenus(
            @Parameter(description = "角色ID", required = true)
            @PathVariable @NotNull Long roleId,
            @Parameter(description = "菜单ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> menuIds) {
        roleMenuService.addRoleMenus(roleId, menuIds);
        return Result.success();
    }

    @DeleteMapping("/remove")
    @Operation(summary = "删除角色菜单关联", description = "删除单个角色菜单关联")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> removeRoleMenu(
            @Parameter(description = "角色ID", required = true)
            @RequestParam @NotNull Long roleId,
            @Parameter(description = "菜单ID", required = true)
            @RequestParam @NotNull Long menuId) {
        roleMenuService.removeRoleMenu(roleId, menuId);
        return Result.success();
    }

    @DeleteMapping("/role/{roleId}/remove")
    @Operation(summary = "批量删除角色菜单关联", description = "批量删除指定角色的菜单关联")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> removeRoleMenus(
            @Parameter(description = "角色ID", required = true)
            @PathVariable @NotNull Long roleId,
            @Parameter(description = "菜单ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> menuIds) {
        roleMenuService.removeRoleMenus(roleId, menuIds);
        return Result.success();
    }

    @DeleteMapping("/role/{roleId}/all")
    @Operation(summary = "删除角色的所有菜单关联", description = "删除指定角色的所有菜单关联")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> removeAllRoleMenus(
            @Parameter(description = "角色ID", required = true)
            @PathVariable @NotNull Long roleId) {
        roleMenuService.removeAllRoleMenus(roleId);
        return Result.success();
    }

    @DeleteMapping("/menu/{menuId}/all")
    @Operation(summary = "删除菜单的所有角色关联", description = "删除指定菜单的所有角色关联")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Void> removeAllMenuRoles(
            @Parameter(description = "菜单ID", required = true)
            @PathVariable @NotNull Long menuId) {
        roleMenuService.removeAllMenuRoles(menuId);
        return Result.success();
    }

    @DeleteMapping("/roles/batch")
    @Operation(summary = "批量删除角色菜单关联", description = "根据角色ID列表批量删除角色菜单关联")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public Result<Void> removeRoleMenusByRoleIds(
            @Parameter(description = "角色ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> roleIds) {
        roleMenuService.removeRoleMenusByRoleIds(roleIds);
        return Result.success();
    }

    @DeleteMapping("/menus/batch")
    @Operation(summary = "批量删除菜单角色关联", description = "根据菜单ID列表批量删除菜单角色关联")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    public Result<Void> removeRoleMenusByMenuIds(
            @Parameter(description = "菜单ID列表", required = true)
            @RequestBody @Valid @NotEmpty List<Long> menuIds) {
        roleMenuService.removeRoleMenusByMenuIds(menuIds);
        return Result.success();
    }

    @GetMapping("/user/{userId}/permission/{menuId}")
    @Operation(summary = "检查用户菜单权限", description = "检查用户是否有指定菜单的权限")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<Boolean> hasMenuPermission(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @Parameter(description = "菜单ID", required = true)
            @PathVariable @NotNull Long menuId) {
        boolean hasPermission = roleMenuService.hasMenuPermission(userId, menuId);
        return Result.success(hasPermission);
    }

    @GetMapping("/user/{userId}/permission")
    @Operation(summary = "检查用户权限标识", description = "检查用户是否有指定权限标识的权限")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<Boolean> hasPermission(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @Parameter(description = "权限标识", required = true)
            @RequestParam @NotNull String permission) {
        boolean hasPermission = roleMenuService.hasPermission(userId, permission);
        return Result.success(hasPermission);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取角色菜单关联统计信息", description = "获取角色菜单关联的统计数据")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<Map<String, Object>> getStatistics() {
        // 这里可以根据需要添加统计逻辑
        Map<String, Object> statistics = Map.of(
                "message", "角色菜单关联统计功能待实现"
        );
        return Result.success(statistics);
    }
}