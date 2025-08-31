package com.yc.admin.system.rbac;

import com.yc.admin.system.user.entity.User;
import com.yc.admin.system.user.repository.UserRepository;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.repository.RoleRepository;
import com.yc.admin.system.menu.entity.Menu;
import com.yc.admin.system.menu.repository.MenuRepository;
import com.yc.admin.system.role.entity.RoleMenu;
import com.yc.admin.system.role.repository.RoleMenuRepository;
import com.yc.admin.system.user.entity.UserRole;
import com.yc.admin.system.user.repository.UserRoleRepository;
import com.yc.admin.system.permission.PermissionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * RBAC测试工具类
 * 提供便捷的测试数据创建和权限验证方法
 *
 * @author YC
 * @since 1.0.0
 */
@Component
public class RbacTestUtils {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private MenuRepository menuRepository;
    
    @Autowired
    private RoleMenuRepository roleMenuRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 创建测试用户
     */
    public User createTestUser(String userNamePrefix) {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        User user = new User()
            .setUserName(userNamePrefix + "_" + uniqueId)
            .setNickName("测试用户")
            .setEmail("test_" + uniqueId + "@example.com")
            .setPhone("138" + String.format("%08d", Long.parseLong(uniqueId) % 100000000))
            .setPassword(passwordEncoder.encode("123456"))
            .setStatus(User.Status.NORMAL)
            .setDeptId(null);
            
        return userRepository.save(user);
    }
    
    /**
     * 创建测试角色
     */
    public Role createTestRole(String roleNamePrefix, String roleKeyPrefix) {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        Role role = new Role();
        role.setRoleName(roleNamePrefix + "_" + uniqueId);
        role.setRoleKey(roleKeyPrefix + "_" + uniqueId);
        role.setRoleSort(100);
        role.setDataScope(Role.DataScope.DEPT);
        role.setStatus(Role.Status.NORMAL);
        role.setRemark("RBAC测试角色");
        
        return roleRepository.save(role);
    }
    
    /**
     * 创建测试菜单
     */
    public Menu createTestMenu(String menuNamePrefix, String perms) {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        Menu menu = Menu.builder()
            .menuName(menuNamePrefix + "_" + uniqueId)
            .parentId(0L)
            .orderNum(1)
            .path("/test_" + uniqueId)
            .component("test/index")
            .perms(perms)
            .icon("test")
            .menuType("C")  // 菜单类型：C表示菜单
            .isFrame(0)     // 不是外链
            .isCache(0)     // 缓存
            .visible(0)     // 显示
            .status(0)      // 正常状态
            .build();
            
        return menuRepository.save(menu);
    }
    
    /**
     * 为角色分配菜单权限
     */
    public void assignMenuToRole(Long roleId, Long menuId) {
        RoleMenu roleMenu = RoleMenu.of(roleId, menuId);
        roleMenuRepository.save(roleMenu);
    }
    
    /**
     * 为用户分配角色
     */
    public void assignRoleToUser(Long userId, Long roleId) {
        UserRole userRole = UserRole.of(userId, roleId);
        userRoleRepository.save(userRole);
    }
    
    /**
     * 验证用户是否拥有指定权限
     */
    public boolean verifyUserPermission(Long userId, String permission) {
        return permissionService.hasPermission(userId, permission);
    }
    
    /**
     * 获取用户的所有权限
     */
    public Set<String> getUserPermissions(Long userId) {
        List<String> permissions = permissionService.getPermissionsByUserId(userId);
        return new HashSet<>(permissions);
    }
    
    /**
     * 获取用户的所有菜单ID
     */
    public Set<Long> getUserMenuIds(Long userId) {
        List<Long> menuIds = permissionService.getMenuIdsByUserId(userId);
        return new HashSet<>(menuIds);
    }
    
    /**
     * 清理测试数据
     */
    @Transactional
    public void cleanupTestData() {
        userRoleRepository.deleteAll();
        roleMenuRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        menuRepository.deleteAll();
    }
    
    /**
     * RBAC测试场景数据
     */
    public static class RbacTestScenario {
        private final User user;
        private final Role role;
        private final Menu menu;
        private final String expectedPermission;
        
        public RbacTestScenario(User user, Role role, Menu menu, String expectedPermission) {
            this.user = user;
            this.role = role;
            this.menu = menu;
            this.expectedPermission = expectedPermission;
        }
        
        public User getUser() { return user; }
        public Role getRole() { return role; }
        public Menu getMenu() { return menu; }
        public String getExpectedPermission() { return expectedPermission; }
    }
    
    /**
     * 创建完整的RBAC测试场景
     */
    public RbacTestScenario createCompleteScenario(String scenarioName, String permission) {
        // 创建用户
        User user = createTestUser("user_" + scenarioName);
        
        // 创建角色
        Role role = createTestRole("role_" + scenarioName, "role_key_" + scenarioName);
        
        // 创建菜单
        Menu menu = createTestMenu("menu_" + scenarioName, permission);
        
        // 建立关联关系
        assignMenuToRole(role.getId(), menu.getId());
        assignRoleToUser(user.getId(), role.getId());
        
        return new RbacTestScenario(user, role, menu, permission);
    }
}