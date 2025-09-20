package com.yc.admin.system.menu.controller;

import com.yc.admin.common.core.Result;
import com.yc.admin.system.menu.dto.MenuDTO;
import com.yc.admin.system.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 菜单管理控制器
 *
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@Validated
@Tag(name = "菜单管理", description = "菜单管理相关接口")
public class MenuController {

    private final MenuService menuService;

    // ==================== 查询接口 ====================

    /**
     * 获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取菜单详情", description = "根据菜单ID获取菜单详细信息")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public Result<?> getMenuById(
            @Parameter(description = "菜单ID", required = true)
            @PathVariable @NotNull Long id) {

        return Result.success(menuService.findById(id));
    }

    /**
     * 获取菜单列表
     *
     * @param menuName 菜单名称（可选）
     * @param menuType 菜单类型（可选）
     * @param status   状态（可选）
     * @param visible  可见性（可选）
     * @return 菜单列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取菜单列表", description = "根据条件查询菜单列表")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<MenuDTO>> getMenuList(
            @Parameter(description = "菜单名称")
            @RequestParam(required = false) String menuName,
            @Parameter(description = "菜单类型：M=目录,C=菜单,F=按钮")
            @RequestParam(required = false) String menuType,
            @Parameter(description = "状态：0=正常,1=停用")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "可见性：0=显示,1=隐藏")
            @RequestParam(required = false) Integer visible) {

        List<MenuDTO> menus;

        // 根据不同条件查询
        if (menuType != null) {
            menus = menuService.findByMenuType(menuType);
        } else if (status != null) {
            menus = menuService.findByStatus(status);
        } else {
            menus = menuService.findAll();
        }

        // 进一步过滤
        if (menuName != null && !menuName.trim().isEmpty()) {
            String keyword = menuName.trim().toLowerCase();
            menus = menus.stream()
                    .filter(menu -> menu.getMenuName().toLowerCase().contains(keyword))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (visible != null) {
            menus = menus.stream()
                    .filter(menu -> visible.equals(menu.getVisible()))
                    .collect(java.util.stream.Collectors.toList());
        }

        return Result.success(menus);
    }

    /**
     * 分页查询菜单
     *
     * @param menuName 菜单名称（可选）
     * @param menuType 菜单类型（可选）
     * @param status   状态（可选）
     * @param visible  可见性（可选）
     * @param page     页码（从0开始）
     * @param size     每页大小
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询菜单", description = "根据条件分页查询菜单列表")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<Page<MenuDTO>> getMenuPage(
            @Parameter(description = "菜单名称")
            @RequestParam(required = false) String menuName,
            @Parameter(description = "菜单类型：M=目录,C=菜单,F=按钮")
            @RequestParam(required = false) String menuType,
            @Parameter(description = "状态：0=正常,1=停用")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "可见性：0=显示,1=隐藏")
            @RequestParam(required = false) Integer visible,
            @Parameter(description = "页码（从0开始）")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "10") int size) {

        Page<MenuDTO> menuPage = menuService.findByConditions(menuName, menuType, status, visible, page, size);
        return Result.success(menuPage);
    }

    /**
     * 获取菜单树
     *
     * @return 菜单树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取菜单树", description = "获取完整的菜单树结构")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<MenuDTO.TreeNodeDTO>> getMenuTree() {
        List<MenuDTO.TreeNodeDTO> menuTree = menuService.buildMenuTree();
        return Result.success(menuTree);
    }

    /**
     * 获取子菜单列表
     *
     * @param parentId 父菜单ID
     * @return 子菜单列表
     */
    @GetMapping("/children/{parentId}")
    @Operation(summary = "获取子菜单列表", description = "根据父菜单ID获取子菜单列表")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<MenuDTO>> getChildrenMenus(
            @Parameter(description = "父菜单ID", required = true)
            @PathVariable @NotNull Long parentId) {

        List<MenuDTO> children = menuService.findByParentId(parentId);
        return Result.success(children);
    }

    /**
     * 根据用户ID获取菜单权限
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户菜单权限", description = "根据用户ID获取用户拥有的菜单权限")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public Result<List<MenuDTO>> getUserMenus(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {

        List<MenuDTO> userMenus = menuService.findByUserId(userId);
        return Result.success(userMenus);
    }

    /**
     * 根据角色ID获取菜单权限
     *
     * @param roleId 角色ID
     * @return 菜单列表
     */
    @GetMapping("/role/{roleId}")
    @Operation(summary = "获取角色菜单权限", description = "根据角色ID获取角色拥有的菜单权限")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public Result<List<MenuDTO>> getRoleMenus(
            @Parameter(description = "角色ID", required = true)
            @PathVariable @NotNull Long roleId) {

        List<MenuDTO> roleMenus = menuService.findByRoleId(roleId);
        return Result.success(roleMenus);
    }

    // ==================== 创建和更新接口 ====================

    /**
     * 创建菜单
     *
     * @param menu 菜单信息
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建菜单", description = "创建新的菜单")
    //@PreAuthorize("hasAuthority('system:menu:add')")
    public Result<MenuDTO> createMenu(
            @Parameter(description = "菜单信息", required = true)
            @RequestBody @Valid MenuDTO.CreateDTO menu) {

        MenuDTO createdMenu = menuService.createMenu(menu);
        return Result.success("菜单创建成功", createdMenu);
    }

    /**
     * 更新菜单
     *
     * @param id   菜单ID
     * @param menu 菜单信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新菜单", description = "更新指定菜单的信息")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<MenuDTO> updateMenu(
            @Parameter(description = "菜单ID", required = true)
            @PathVariable @NotNull Long id,
            @Parameter(description = "菜单信息", required = true)
            @RequestBody @Valid MenuDTO.UpdateDTO menu) {

        menu.setId(id);
        MenuDTO updatedMenu = menuService.updateMenu(menu);
        return Result.success("菜单更新成功", updatedMenu);
    }

    // ==================== 删除接口 ====================

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单", description = "删除指定的菜单")
    @PreAuthorize("hasAuthority('system:menu:remove')")
    public Result<Void> deleteMenu(
            @Parameter(description = "菜单ID", required = true)
            @PathVariable @NotNull Long id) {

        menuService.deleteMenu(id);
        return Result.success("菜单删除成功");
    }

    /**
     * 批量删除菜单
     *
     * @param ids 菜单ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除菜单", description = "批量删除指定的菜单")
    @PreAuthorize("hasAuthority('system:menu:remove')")
    public Result<Integer> deleteMenus(
            @Parameter(description = "菜单ID列表", required = true)
            @RequestBody @NotEmpty List<Long> ids) {

        int deletedCount = menuService.deleteMenus(ids);
        return Result.success("批量删除菜单成功，删除数量: " + deletedCount, deletedCount);
    }

    // ==================== 状态管理接口 ====================

    /**
     * 启用菜单
     *
     * @param id 菜单ID
     * @return 操作结果
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用菜单", description = "启用指定的菜单")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Void> enableMenu(
            @Parameter(description = "菜单ID", required = true)
            @PathVariable @NotNull Long id) {

        menuService.enableMenu(id);
        return Result.success("菜单启用成功");
    }

    /**
     * 停用菜单
     *
     * @param id 菜单ID
     * @return 操作结果
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "停用菜单", description = "停用指定的菜单")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Void> disableMenu(
            @Parameter(description = "菜单ID", required = true)
            @PathVariable @NotNull Long id) {

        menuService.disableMenu(id);
        return Result.success("菜单停用成功");
    }

    /**
     * 批量更新菜单状态
     *
     * @param request 批量状态更新请求
     * @return 操作结果
     */
    @PutMapping("/batch/status")
    @Operation(summary = "批量更新菜单状态", description = "批量更新菜单的状态")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Integer> updateMenuStatus(
            @Parameter(description = "批量状态更新请求", required = true)
            @RequestBody @Valid BatchStatusUpdateRequest request) {

        int updatedCount = menuService.updateMenuStatus(request.getIds(), request.getStatus());
        String statusText = request.getStatus() == 0 ? "启用" : "停用";
        return Result.success("批量" + statusText + "菜单成功，更新数量: " + updatedCount, updatedCount);
    }

    // ==================== 统计接口 ====================

    /**
     * 获取菜单统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取菜单统计信息", description = "获取菜单的各种统计数据")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<Map<String, Object>> getMenuStatistics() {
        Map<String, Object> statistics = menuService.getMenuStatistics();
        return Result.success(statistics);
    }

    // ==================== 内部类 ====================

    /**
     * 批量状态更新请求
     */
    @lombok.Data
    public static class BatchStatusUpdateRequest {

        @NotEmpty(message = "菜单ID列表不能为空")
        @Parameter(description = "菜单ID列表", required = true)
        private List<Long> ids;

        @NotNull(message = "状态不能为空")
        @Parameter(description = "状态：0=正常,1=停用", required = true)
        private Integer status;
    }
}