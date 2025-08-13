package com.yc.admin.system.role.controller;

import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.dto.RoleDTO;
import com.yc.admin.system.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, Object>> getRoles(
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleKey,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Page<RoleDTO> rolePage = roleService.findByConditions(roleName, roleKey, status, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", rolePage.getContent());
            response.put("total", rolePage.getTotalElements());
            response.put("totalPages", rolePage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询角色列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据ID查询角色详情
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRoleById(@PathVariable Long id) {
        try {
            RoleDTO role = roleService.findById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", role);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询角色详情失败: ID={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 查询所有正常状态的角色（用于下拉选择）
     * @return 角色列表
     */
    @GetMapping("/select")
    public ResponseEntity<Map<String, Object>> getRolesForSelect() {
        try {
            List<RoleDTO.SelectorDTO> roles = roleService.findAllForSelect();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", roles);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询角色下拉列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取角色统计信息
     * @return 统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getRoleStats() {
        try {
            long totalRoles = roleService.findAll().size();
            long normalRoles = roleService.countNormalRoles();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRoles", totalRoles);
            stats.put("normalRoles", normalRoles);
            stats.put("disabledRoles", totalRoles - normalRoles);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询角色统计信息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== 创建和更新接口 ====================

    /**
     * 创建角色
     * @param createDTO 角色信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRole(@Valid @RequestBody RoleDTO.CreateDTO createDTO) {
        try {
            RoleDTO createdRole = roleService.createRole(createDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "角色创建成功");
            response.put("data", createdRole);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建角色失败: {}", createDTO.getRoleName(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新角色
     * @param id 角色ID
     * @param updateDTO 角色信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO.UpdateDTO updateDTO) {
        try {
            RoleDTO updatedRole = roleService.updateRole(updateDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "角色更新成功");
            response.put("data", updatedRole);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新角色失败: ID={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== 删除接口 ====================

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "角色删除成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除角色失败: ID={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量删除角色
     * @param ids 角色ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> deleteRoles(@RequestBody List<Long> ids) {
        try {
            roleService.deleteRoles(ids);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "批量删除成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量删除角色失败: IDs={}", ids, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== 状态管理接口 ====================

    /**
     * 启用角色
     * @param id 角色ID
     * @return 操作结果
     */
    @PutMapping("/{id}/enable")
    public ResponseEntity<Map<String, Object>> enableRole(@PathVariable Long id) {
        try {
            roleService.enableRole(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "角色启用成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("启用角色失败: ID={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "启用失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 停用角色
     * @param id 角色ID
     * @return 操作结果
     */
    @PutMapping("/{id}/disable")
    public ResponseEntity<Map<String, Object>> disableRole(@PathVariable Long id) {
        try {
            roleService.disableRole(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "角色停用成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("停用角色失败: ID={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "停用失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== 数据权限相关接口 ====================

    /**
     * 获取数据权限范围选项
     * @return 数据权限范围选项
     */
    @GetMapping("/data-scopes")
    public ResponseEntity<Map<String, Object>> getDataScopes() {
        try {
            Map<String, String> dataScopes = new HashMap<>();
            dataScopes.put(Role.DataScope.ALL, "全部数据权限");
            dataScopes.put(Role.DataScope.CUSTOM, "自定数据权限");
            dataScopes.put(Role.DataScope.DEPT, "本部门数据权限");
            dataScopes.put(Role.DataScope.DEPT_AND_CHILD, "本部门及以下数据权限");
            dataScopes.put(Role.DataScope.SELF, "仅本人数据权限");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", dataScopes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询数据权限范围选项失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}