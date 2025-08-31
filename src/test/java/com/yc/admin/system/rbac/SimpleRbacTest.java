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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 简化版RBAC集成测试
 * 测试用户-角色-菜单权限的完整流程
 *
 * @author YC
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SimpleRbacTest {

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
    
    private User testUser;
    private Role testRole;
    private Menu testMenu;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        userRoleRepository.deleteAll();
        roleMenuRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        menuRepository.deleteAll();
    }
    
    @Test
    void testCompleteRbacFlow() {
        // 1. 创建测试用户
        createTestUser();
        
        // 2. 创建测试角色
        createTestRole();
        
        // 3. 创建测试菜单
        createTestMenu();
        
        // 4. 为角色分配菜单权限
        assignMenuToRole();
        
        // 5. 为用户分配角色
        assignRoleToUser();
        
        // 6. 验证用户权限
        verifyUserPermissions();
        
        System.out.println("✅ RBAC完整流程测试通过！");
    }
    
    private void createTestUser() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        testUser = new User()
            .setUserName("test_user_" + uniqueId)
            .setNickName("测试用户")
            .setEmail("test_" + uniqueId + "@example.com")
            .setPhone("138" + String.format("%08d", Long.parseLong(uniqueId) % 100000000))
            .setPassword(passwordEncoder.encode("123456"))
            .setStatus(User.Status.NORMAL)
            .setDeptId(null);
            
        testUser = userRepository.save(testUser);
        
        assertThat(testUser.getId()).isNotNull();
        System.out.println("✅ 创建测试用户成功，ID: " + testUser.getId());
    }
    
    private void createTestRole() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        testRole = new Role();
        testRole.setRoleName("测试角色_" + uniqueId);
        testRole.setRoleKey("test_role_" + uniqueId);
        testRole.setRoleSort(100);
        testRole.setDataScope(Role.DataScope.DEPT);
        testRole.setStatus(Role.Status.NORMAL);
        testRole.setRemark("RBAC测试角色");
        
        testRole = roleRepository.save(testRole);
        
        assertThat(testRole.getId()).isNotNull();
        System.out.println("✅ 创建测试角色成功，ID: " + testRole.getId());
    }
    
    private void createTestMenu() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        testMenu = Menu.builder()
            .menuName("测试菜单_" + uniqueId)
            .parentId(0L)
            .orderNum(1)
            .path("/test")
            .component("test/index")
            .perms("test:page:view")
            .icon("test")
            .menuType("C")  // 菜单类型：C表示菜单
            .isFrame(0)     // 不是外链
            .isCache(0)     // 缓存
            .visible(0)     // 显示
            .status(0)      // 正常状态
            .build();
            
        testMenu = menuRepository.save(testMenu);
        
        assertThat(testMenu.getId()).isNotNull();
        System.out.println("✅ 创建测试菜单成功，ID: " + testMenu.getId());
    }
    
    private void assignMenuToRole() {
        RoleMenu roleMenu = RoleMenu.of(testRole.getId(), testMenu.getId());
        roleMenuRepository.save(roleMenu);
        
        System.out.println("✅ 为角色分配菜单权限成功");
    }
    
    private void assignRoleToUser() {
        UserRole userRole = UserRole.of(testUser.getId(), testRole.getId());
        userRoleRepository.save(userRole);
        
        System.out.println("✅ 为用户分配角色成功");
    }
    
    private void verifyUserPermissions() {
        // 验证用户拥有的菜单ID
        List<Long> menuIds = permissionService.getMenuIdsByUserId(testUser.getId());
        assertThat(menuIds).isNotEmpty();
        assertThat(menuIds).contains(testMenu.getId());
        System.out.println("✅ 用户菜单权限验证成功，菜单ID: " + menuIds);
        
        // 验证用户拥有的权限标识
        List<String> permissions = permissionService.getPermissionsByUserId(testUser.getId());
        assertThat(permissions).isNotEmpty();
        assertThat(permissions).contains("test:page:view");
        System.out.println("✅ 用户权限标识验证成功，权限: " + permissions);
        
        // 验证用户是否有指定权限
        boolean hasPermission = permissionService.hasPermission(testUser.getId(), "test:page:view");
        assertThat(hasPermission).isTrue();
        System.out.println("✅ 用户权限检查验证成功");
        
        // 验证用户是否有指定菜单权限
        boolean hasMenuPermission = permissionService.hasMenuPermission(testUser.getId(), testMenu.getId());
        assertThat(hasMenuPermission).isTrue();
        System.out.println("✅ 用户菜单权限检查验证成功");
    }
}