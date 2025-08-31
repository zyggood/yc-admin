package com.yc.admin.system.rbac;

import com.yc.admin.system.user.entity.User;
import com.yc.admin.system.user.entity.UserRole;
import com.yc.admin.system.user.repository.UserRepository;
import com.yc.admin.system.user.repository.UserRoleRepository;
import com.yc.admin.system.user.service.UserService;
import com.yc.admin.system.user.dto.UserDTO;

import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.entity.RoleMenu;
import com.yc.admin.system.role.repository.RoleRepository;
import com.yc.admin.system.role.repository.RoleMenuRepository;

import com.yc.admin.system.menu.entity.Menu;
import com.yc.admin.system.menu.repository.MenuRepository;

import com.yc.admin.system.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RBAC测试辅助工具类
 * 提供便捷的测试数据创建和权限验证方法
 * 
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RbacTestHelper {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final MenuRepository menuRepository;
    private final PermissionService permissionService;
    
    /**
     * 创建测试用户
     */
    @Transactional
    public Long createTestUser(String userName, String nickName, String email, String phone) {
        UserDTO.CreateDTO createDTO = new UserDTO.CreateDTO();
        createDTO.setUserName(userName);
        createDTO.setNickName(nickName);
        createDTO.setEmail(email);
        createDTO.setPhone(phone);
        createDTO.setPassword("123456");
        createDTO.setSex("0");
        createDTO.setStatus("0");
        createDTO.setDeptId(1L);
        createDTO.setRemark("测试用户");
        
        UserDTO userDTO = userService.createUser(createDTO);
        log.info("创建测试用户成功：{} (ID={})", userName, userDTO.getId());
        return userDTO.getId();
    }
    
    /**
     * 创建测试角色
     */
    @Transactional
    public Long createTestRole(String roleName, String roleKey) {
        Role role = new Role();
        role.setRoleName(roleName);
        role.setRoleKey(roleKey);
        role.setRoleSort(100);
        role.setDataScope(Role.DataScope.DEPT);
        role.setStatus(Role.Status.NORMAL);
        role.setRemark("测试角色");
        
        Role savedRole = roleRepository.save(role);
        log.info("创建测试角色成功：{} (ID={})", roleName, savedRole.getId());
        return savedRole.getId();
    }
    
    /**
     * 创建测试菜单
     */
    @Transactional
    public Long createTestMenu(String menuName, Long parentId, String menuType, String perms) {
        Menu.MenuBuilder builder = Menu.builder()
            .menuName(menuName)
            .parentId(parentId != null ? parentId : 0L)
            .orderNum(1)
            .menuType(menuType)
            .visible(Menu.Visible.SHOW)
            .status(Menu.Status.NORMAL)
            .remark("测试菜单");
        
        if (Menu.Type.DIRECTORY.equals(menuType)) {
            builder.path("/" + menuName.toLowerCase())
                   .component("")
                   .icon("folder");
        } else if (Menu.Type.MENU.equals(menuType)) {
            builder.path("/" + menuName.toLowerCase())
                   .component(menuName.toLowerCase() + "/index")
                   .perms(perms)
                   .icon("page");
        } else if (Menu.Type.BUTTON.equals(menuType)) {
            builder.perms(perms);
        }
        
        Menu menu = builder.build();
        Menu savedMenu = menuRepository.save(menu);
        log.info("创建测试菜单成功：{} (ID={}, 类型={}, 权限={})", menuName, savedMenu.getId(), menuType, perms);
        return savedMenu.getId();
    }
    
    /**
     * 为角色分配菜单权限
     */
    @Transactional
    public void assignMenuToRole(Long roleId, Long menuId) {
        RoleMenu roleMenu = new RoleMenu(roleId, menuId);
        roleMenuRepository.save(roleMenu);
        log.info("为角色({})分配菜单权限({})成功", roleId, menuId);
    }
    
    /**
     * 为角色批量分配菜单权限
     */
    @Transactional
    public void assignMenusToRole(Long roleId, List<Long> menuIds) {
        for (Long menuId : menuIds) {
            assignMenuToRole(roleId, menuId);
        }
        log.info("为角色({})批量分配菜单权限成功，共{}个菜单", roleId, menuIds.size());
    }
    
    /**
     * 为用户分配角色
     */
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        UserRole userRole = new UserRole(userId, roleId);
        userRoleRepository.save(userRole);
        log.info("为用户({})分配角色({})成功", userId, roleId);
    }
    
    /**
     * 为用户批量分配角色
     */
    @Transactional
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        for (Long roleId : roleIds) {
            assignRoleToUser(userId, roleId);
        }
        log.info("为用户({})批量分配角色成功，共{}个角色", userId, roleIds.size());
    }
    
    /**
     * 验证用户是否拥有指定权限
     */
    public boolean verifyUserPermission(Long userId, String permission) {
        if (permissionService == null) {
            log.warn("PermissionService未找到，无法验证权限");
            return false;
        }
        
        boolean hasPermission = permissionService.hasPermission(userId, permission);
        log.info("用户({})权限({})验证结果：{}", userId, permission, hasPermission);
        return hasPermission;
    }
    
    /**
     * 获取用户所有权限
     */
    public Set<String> getUserPermissions(Long userId) {
        if (permissionService == null) {
            log.warn("PermissionService未找到，无法获取用户权限");
            return Set.of();
        }
        
        Set<String> permissions = new HashSet<>(permissionService.getPermissionsByUserId(userId));
        log.info("用户({})拥有权限：{}", userId, permissions);
        return permissions;
    }
    
    /**
     * 获取用户菜单ID列表
     */
    public List<Long> getUserMenuIds(Long userId) {
        List<Long> menuIds = userRoleRepository.findMenuIdsByUserId(userId);
        log.info("用户({})拥有菜单ID：{}", userId, menuIds);
        return menuIds;
    }
    
    /**
     * 清理测试数据
     */
    @Transactional
    public void cleanupTestData(Long userId, Long roleId, List<Long> menuIds) {
        try {
            // 清理用户角色关联
            if (userId != null) {
                userRoleRepository.deleteByUserId(userId);
                log.info("清理用户({})角色关联成功", userId);
            }
            
            // 清理角色菜单关联
            if (roleId != null) {
                roleMenuRepository.deleteByRoleId(roleId);
                log.info("清理角色({})菜单关联成功", roleId);
            }
            
            // 清理用户
            if (userId != null) {
                userRepository.deleteById(userId);
                log.info("清理用户({})成功", userId);
            }
            
            // 清理角色
            if (roleId != null) {
                roleRepository.deleteById(roleId);
                log.info("清理角色({})成功", roleId);
            }
            
            // 清理菜单（倒序删除，先删子菜单）
            if (menuIds != null && !menuIds.isEmpty()) {
                for (int i = menuIds.size() - 1; i >= 0; i--) {
                    Long menuId = menuIds.get(i);
                    menuRepository.deleteById(menuId);
                    log.info("清理菜单({})成功", menuId);
                }
            }
            
        } catch (Exception e) {
            log.warn("清理测试数据时发生异常（可忽略）：{}", e.getMessage());
        }
    }
    
    /**
     * 创建完整的RBAC测试场景
     * @return RbacTestScenario 包含所有创建的ID
     */
    @Transactional
    public RbacTestScenario createCompleteTestScenario() {
        // 1. 创建用户
        Long userId = createTestUser("testuser", "测试用户", "test@example.com", "13800138000");
        
        // 2. 创建角色
        Long roleId = createTestRole("测试角色", "test_role");
        
        // 3. 创建菜单层级
        Long parentMenuId = createTestMenu("测试模块", null, Menu.Type.DIRECTORY, null);
        Long childMenuId = createTestMenu("测试页面", parentMenuId, Menu.Type.MENU, "test:page:view");
        Long buttonId = createTestMenu("添加按钮", childMenuId, Menu.Type.BUTTON, "test:page:add");
        
        List<Long> menuIds = List.of(parentMenuId, childMenuId, buttonId);
        
        // 4. 分配权限
        assignMenusToRole(roleId, menuIds);
        assignRoleToUser(userId, roleId);
        
        log.info("完整RBAC测试场景创建成功");
        return new RbacTestScenario(userId, roleId, menuIds);
    }
    
    /**
     * RBAC测试场景数据类
     */
    public record RbacTestScenario(
        Long userId,
        Long roleId,
        List<Long> menuIds
    ) {
        public Long getParentMenuId() {
            return menuIds.get(0);
        }
        
        public Long getChildMenuId() {
            return menuIds.get(1);
        }
        
        public Long getButtonId() {
            return menuIds.get(2);
        }
    }
}