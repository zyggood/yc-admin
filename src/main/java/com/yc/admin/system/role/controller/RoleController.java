package com.yc.admin.system.role.controller;

import com.yc.admin.common.core.Result;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.dto.RoleDTO;
import com.yc.admin.system.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 * 
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // ==================== 查询接口 ====================

    /**
     * 分页查询角色列表
     * @param roleName 角色名称关键字
     * @param roleKey 角色权限字符串关键字
     * @param status 角色状态
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 角色分页列表
     */
    @GetMapping
    public Result<Map<String, Object>> getRoles(
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleKey,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Page<RoleDTO> rolePage = roleService.findByConditions(roleName, roleKey, status, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", rolePage.getContent());
            response.put("total", rolePage.getTotalElements());
            response.put("totalPages", rolePage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            
            return Result.success("查询成功", response);
        } catch (Exception e) {
            log.error("查询角色列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询角色详情
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
    public Result<RoleDTO> getRoleById(@PathVariable Long id) {
        try {
            RoleDTO role = roleService.findById(id);
            return Result.success("查询成功", role);
        } catch (Exception e) {
            log.error("查询角色详情失败: ID={}", id, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有正常状态的角色（用于下拉选择）
     * @return 角色列表
     */
    @GetMapping("/select")
    public Result<List<RoleDTO.SelectorDTO>> getRolesForSelect() {
        try {
            List<RoleDTO.SelectorDTO> roles = roleService.findAllForSelect();
            return Result.success("查询成功", roles);
        } catch (Exception e) {
            log.error("查询角色下拉列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取角色统计信息
     * @return 统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getRoleStats() {
        try {
            long totalRoles = roleService.findAll().size();
            long normalRoles = roleService.countNormalRoles();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRoles", totalRoles);
            stats.put("normalRoles", normalRoles);
            stats.put("disabledRoles", totalRoles - normalRoles);
            
            return Result.success("查询成功", stats);
        } catch (Exception e) {
            log.error("查询角色统计信息失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    // ==================== 创建和更新接口 ====================

    /**
     * 创建角色
     * @param createDTO 角色信息
     * @return 创建结果
     */
    @PostMapping
    public Result<RoleDTO> createRole(@Valid @RequestBody RoleDTO.CreateDTO createDTO) {
        try {
            RoleDTO createdRole = roleService.createRole(createDTO);
            return Result.success("角色创建成功", createdRole);
        } catch (Exception e) {
            log.error("创建角色失败: {}", createDTO.getRoleName(), e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新角色
     * @param id 角色ID
     * @param updateDTO 角色信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<RoleDTO> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO.UpdateDTO updateDTO) {
        try {
            RoleDTO updatedRole = roleService.updateRole(updateDTO);
            return Result.success("角色更新成功", updatedRole);
        } catch (Exception e) {
            log.error("更新角色失败: ID={}", id, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    // ==================== 删除接口 ====================

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return Result.success("角色删除成功");
        } catch (Exception e) {
            log.error("删除角色失败: ID={}", id, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除角色
     * @param ids 角色ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteRoles(@RequestBody List<Long> ids) {
        try {
            roleService.deleteRoles(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            log.error("批量删除角色失败: IDs={}", ids, e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    // ==================== 状态管理接口 ====================

    /**
     * 启用角色
     * @param id 角色ID
     * @return 操作结果
     */
    @PutMapping("/{id}/enable")
    public Result<Void> enableRole(@PathVariable Long id) {
        try {
            roleService.enableRole(id);
            return Result.success("角色启用成功");
        } catch (Exception e) {
            log.error("启用角色失败: ID={}", id, e);
            return Result.error("启用失败: " + e.getMessage());
        }
    }

    /**
     * 停用角色
     * @param id 角色ID
     * @return 操作结果
     */
    @PutMapping("/{id}/disable")
    public Result<Void> disableRole(@PathVariable Long id) {
        try {
            roleService.disableRole(id);
            return Result.success("角色停用成功");
        } catch (Exception e) {
            log.error("停用角色失败: ID={}", id, e);
            return Result.error("停用失败: " + e.getMessage());
        }
    }

    // ==================== 数据权限相关接口 ====================

    /**
     * 获取数据权限范围选项
     * @return 数据权限范围选项
     */
    @GetMapping("/data-scopes")
    public Result<Map<String, String>> getDataScopes() {
        try {
            Map<String, String> dataScopes = new HashMap<>();
            dataScopes.put(Role.DataScope.ALL, "全部数据权限");
            dataScopes.put(Role.DataScope.CUSTOM, "自定数据权限");
            dataScopes.put(Role.DataScope.DEPT, "本部门数据权限");
            dataScopes.put(Role.DataScope.DEPT_AND_CHILD, "本部门及以下数据权限");
            dataScopes.put(Role.DataScope.SELF, "仅本人数据权限");
            
            return Result.success("查询成功", dataScopes);
        } catch (Exception e) {
            log.error("查询数据权限范围选项失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    // ==================== 角色层级相关接口 ====================

    /**
     * 查询角色层级树
     * @return 角色层级树
     */
    @GetMapping("/hierarchy/tree")
    public Result<List<RoleDTO>> getRoleHierarchyTree() {
        try {
            List<RoleDTO> tree = roleService.buildRoleHierarchyTree();
            return Result.success("查询成功", tree);
        } catch (Exception e) {
            log.error("查询角色层级树失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有根角色
     * @return 根角色列表
     */
    @GetMapping("/hierarchy/roots")
    public Result<List<RoleDTO>> getRootRoles() {
        try {
            List<RoleDTO> rootRoles = roleService.findRootRoles();
            return Result.success("查询成功", rootRoles);
        } catch (Exception e) {
            log.error("查询根角色失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定角色的子角色
     * @param parentId 父角色ID
     * @return 子角色列表
     */
    @GetMapping("/hierarchy/{parentId}/children")
    public Result<List<RoleDTO>> getChildRoles(@PathVariable Long parentId) {
        try {
            List<RoleDTO> childRoles = roleService.findChildRoles(parentId);
            return Result.success("查询成功", childRoles);
        } catch (Exception e) {
            log.error("查询子角色失败, parentId: {}", parentId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定角色的所有子角色（递归）
     * @param parentId 父角色ID
     * @return 所有子角色列表
     */
    @GetMapping("/hierarchy/{parentId}/all-children")
    public Result<List<RoleDTO>> getAllChildRoles(@PathVariable Long parentId) {
        try {
            List<RoleDTO> allChildRoles = roleService.findAllChildRoles(parentId);
            return Result.success("查询成功", allChildRoles);
        } catch (Exception e) {
            log.error("查询所有子角色失败, parentId: {}", parentId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定角色的父角色
     * @param roleId 角色ID
     * @return 父角色信息
     */
    @GetMapping("/hierarchy/{roleId}/parent")
    public Result<RoleDTO> getParentRole(@PathVariable Long roleId) {
        try {
            return roleService.findParentRole(roleId)
                    .map(parent -> Result.success("查询成功", parent))
                    .orElse(Result.success("该角色没有父角色", null));
        } catch (Exception e) {
            log.error("查询父角色失败, roleId: {}", roleId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定角色的所有祖先角色
     * @param roleId 角色ID
     * @return 祖先角色列表
     */
    @GetMapping("/hierarchy/{roleId}/ancestors")
    public Result<List<RoleDTO>> getAncestorRoles(@PathVariable Long roleId) {
        try {
            List<RoleDTO> ancestorRoles = roleService.findAncestorRoles(roleId);
            return Result.success("查询成功", ancestorRoles);
        } catch (Exception e) {
            log.error("查询祖先角色失败, roleId: {}", roleId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 设置角色的父角色
     * @param roleId 角色ID
     * @param parentId 父角色ID（可为null表示设为根角色）
     * @return 操作结果
     */
    @PutMapping("/hierarchy/{roleId}/parent")
    public Result<Void> setParentRole(@PathVariable Long roleId, @RequestParam(required = false) Long parentId) {
        try {
            roleService.setParentRole(roleId, parentId);
            String message = parentId != null ? 
                    String.format("角色层级关系设置成功: 角色ID=%d, 父角色ID=%d", roleId, parentId) :
                    String.format("角色设置为根角色成功: 角色ID=%d", roleId);
            return Result.success(message);
        } catch (Exception e) {
            log.error("设置角色层级关系失败, roleId: {}, parentId: {}", roleId, parentId, e);
            return Result.error("设置失败: " + e.getMessage());
        }
    }

    /**
     * 检查设置父角色是否会形成循环引用
     * @param roleId 角色ID
     * @param parentId 要设置的父角色ID
     * @return 检查结果
     */
    @GetMapping("/hierarchy/{roleId}/check-cycle")
    public Result<Map<String, Object>> checkCycle(@PathVariable Long roleId, @RequestParam Long parentId) {
        try {
            boolean wouldCreateCycle = roleService.wouldCreateCycle(roleId, parentId);
            Map<String, Object> result = new HashMap<>();
            result.put("wouldCreateCycle", wouldCreateCycle);
            result.put("message", wouldCreateCycle ? "设置此父角色会形成循环引用" : "可以安全设置此父角色");
            return Result.success("检查完成", result);
        } catch (Exception e) {
            log.error("检查循环引用失败, roleId: {}, parentId: {}", roleId, parentId, e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }
}