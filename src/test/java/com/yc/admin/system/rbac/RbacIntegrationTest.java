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
import com.yc.admin.system.role.service.RoleService;
import com.yc.admin.system.role.dto.RoleDTO;

import com.yc.admin.system.menu.entity.Menu;
import com.yc.admin.system.menu.repository.MenuRepository;
import com.yc.admin.system.menu.service.MenuService;
import com.yc.admin.system.menu.dto.MenuDTO;

import com.yc.admin.system.permission.PermissionService;
import com.yc.admin.system.permission.PermissionInheritanceService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * RBAC权限系统集成测试
 * 测试完整的权限流程：创建用户 -> 授予角色 -> 授予菜单 -> 测试权限
 * 
 * @author YC
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class RbacIntegrationTest {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private RoleMenuRepository roleMenuRepository;
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private MenuRepository menuRepository;
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private PermissionInheritanceService permissionInheritanceService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // 测试数据
    private static Long testUserId;
    private static Long testRoleId;
    private static Long testMenuId1;
    private static Long testMenuId2;
    private static Long testButtonId;
    
    // 生成唯一标识符的方法
    private String getUniqueId() {
        return String.valueOf(System.currentTimeMillis() + (long)(Math.random() * 1000));
    }
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        cleanupTestData();
    }
    
    @AfterEach
    void tearDown() {
        // 清理测试数据
        cleanupTestData();
    }
    
    /**
     * 测试步骤1：创建测试用户
     */
    @Test
    @Order(1)
    @DisplayName("步骤1：创建测试用户")
    void step1_createTestUser() {
        // 准备用户数据
        UserDTO.CreateDTO createDTO = getCreateDTO();

        // 创建用户
        UserDTO userDTO = userService.createUser(createDTO);
        testUserId = userDTO.getId();
        
        // 验证用户创建成功
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getId()).isNotNull();
        assertThat(userDTO.getUserName()).startsWith("testuser_");
        assertThat(userDTO.getNickName()).startsWith("测试用户_");
        assertThat(userDTO.getEmail()).contains("@example.com");
        assertThat(userDTO.getPhone()).startsWith("138");
        assertThat(userDTO.getStatus()).isEqualTo("0");
        
        // 验证数据库中的用户
        User user = userRepository.findById(testUserId).orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getUserName()).startsWith("testuser_");
        assertThat(passwordEncoder.matches("123456", user.getPassword())).isTrue();
        
        System.out.println("✅ 步骤1完成：成功创建测试用户，ID=" + testUserId);
    }

    private UserDTO.CreateDTO getCreateDTO() {
        UserDTO.CreateDTO createDTO = new UserDTO.CreateDTO();
        String uniqueId = getUniqueId();
        createDTO.setUserName("testuser_" + uniqueId);
        createDTO.setNickName("测试用户_" + uniqueId);
        createDTO.setEmail("testuser_" + uniqueId + "@example.com");
        createDTO.setPhone("138" + String.format("%08d", Long.parseLong(uniqueId) % 100000000));
        createDTO.setPassword("123456");
        createDTO.setSex("0");
        createDTO.setStatus("0");
        createDTO.setDeptId(null);  // 不设置部门ID，避免部门不存在的错误
        createDTO.setRemark("RBAC集成测试用户");
        return createDTO;
    }

    /**
     * 测试步骤2：创建测试角色
     */
    @Test
    @Order(2)
    @DisplayName("步骤2：创建测试角色")
    void step2_createTestRole() {
        // 准备角色数据
        Role role = new Role();
        String uniqueId = getUniqueId();
        role.setRoleName("测试角色_" + uniqueId);
        role.setRoleKey("test_role_" + uniqueId);
        role.setRoleSort(100);
        role.setDataScope(Role.DataScope.DEPT);
        role.setStatus(Role.Status.NORMAL);
        role.setRemark("RBAC集成测试角色");
        
        // 保存角色
        Role savedRole = roleRepository.save(role);
        testRoleId = savedRole.getId();
        
        // 验证角色创建成功
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getRoleName()).startsWith("测试角色_");
        assertThat(savedRole.getRoleKey()).startsWith("test_role_");
        assertThat(savedRole.getStatus()).isEqualTo(Role.Status.NORMAL);
        
        System.out.println("✅ 步骤2完成：成功创建测试角色，ID=" + testRoleId);
    }
    
    /**
     * 测试步骤3：创建测试菜单
     */
    @Test
    @Order(3)
    @DisplayName("步骤3：创建测试菜单")
    void step3_createTestMenus() {
        // 创建父菜单（目录）
        Menu parentMenu = Menu.builder()
            .menuName("测试模块_" + getUniqueId())
            .parentId(0L)
            .orderNum(100)
            .path("/test")
            .component("")
            .menuType(Menu.Type.DIRECTORY)
            .visible(Menu.Visible.SHOW)
            .status(Menu.Status.NORMAL)
            .icon("test")
            .isFrame(0)
            .isCache(0)
            .remark("RBAC集成测试模块")
            .build();
        
        Menu savedParentMenu = menuRepository.save(parentMenu);
        testMenuId1 = savedParentMenu.getId();
        
        // 创建子菜单
        Menu childMenu = Menu.builder()
            .menuName("测试页面_" + getUniqueId())
            .parentId(testMenuId1)
            .orderNum(1)
            .path("/test/page")
            .component("test/page/index")
            .menuType(Menu.Type.MENU)
            .visible(Menu.Visible.SHOW)
            .status(Menu.Status.NORMAL)
            .perms("test:page:view")
            .icon("page")
            .isFrame(0)
            .isCache(0)
            .remark("RBAC集成测试页面")
            .build();
        
        Menu savedChildMenu = menuRepository.save(childMenu);
        testMenuId2 = savedChildMenu.getId();
        
        // 创建按钮权限
        Menu button = Menu.builder()
            .menuName("测试按钮_" + getUniqueId())
            .parentId(testMenuId2)
            .orderNum(1)
            .menuType(Menu.Type.BUTTON)
            .visible(Menu.Visible.SHOW)
            .status(Menu.Status.NORMAL)
            .perms("test:page:add")
            .isFrame(0)
            .isCache(0)
            .remark("RBAC集成测试按钮")
            .build();
        
        Menu savedButton = menuRepository.save(button);
        testButtonId = savedButton.getId();
        
        // 验证菜单创建成功
        assertThat(savedParentMenu.getId()).isNotNull();
        assertThat(savedChildMenu.getId()).isNotNull();
        assertThat(savedButton.getId()).isNotNull();
        
        System.out.println("✅ 步骤3完成：成功创建测试菜单");
        System.out.println("   - 父菜单ID=" + testMenuId1);
        System.out.println("   - 子菜单ID=" + testMenuId2);
        System.out.println("   - 按钮ID=" + testButtonId);
    }
    
    /**
     * 测试步骤4：为角色分配菜单权限
     */
    @Test
    @Order(4)
    @DisplayName("步骤4：为角色分配菜单权限")
    void step4_assignMenusToRole() {
        // 先创建必要的测试数据
        step2_createTestRole();
        step3_createTestMenus();

        doStep4();
    }

    private void doStep4() {
        // 为角色分配菜单权限
        RoleMenu roleMenu1 = new RoleMenu(testRoleId, testMenuId1);
        RoleMenu roleMenu2 = new RoleMenu(testRoleId, testMenuId2);
        RoleMenu roleMenu3 = new RoleMenu(testRoleId, testButtonId);

        roleMenuRepository.save(roleMenu1);
        roleMenuRepository.save(roleMenu2);
        roleMenuRepository.save(roleMenu3);

        // 验证角色菜单关联创建成功
        List<RoleMenu> roleMenus = roleMenuRepository.findByRoleId(testRoleId);
        assertThat(roleMenus).hasSize(3);

        // 验证菜单ID列表
        List<Long> menuIds = roleMenus.stream()
            .map(RoleMenu::getMenuId)
            .toList();
        assertThat(menuIds).containsExactlyInAnyOrder(testMenuId1, testMenuId2, testButtonId);

        System.out.println("✅ 步骤4完成：成功为角色分配菜单权限");
        System.out.println("   - 角色ID=" + testRoleId + " 拥有菜单权限：" + menuIds);
    }

    /**
     * 测试步骤5：为用户分配角色
     */
    @Test
    @Order(5)
    @DisplayName("步骤5：为用户分配角色")
    void step5_assignRoleToUser() {
        // 先创建必要的测试数据
        step1_createTestUser();
        step2_createTestRole();

        doStep5();
    }

    private void doStep5() {
        // 为用户分配角色
        UserRole userRole = new UserRole(testUserId, testRoleId);
        userRoleRepository.save(userRole);

        // 验证用户角色关联创建成功
        List<UserRole> userRoles = userRoleRepository.findByUserId(testUserId);
        assertThat(userRoles).hasSize(1);
        assertThat(userRoles.get(0).getRoleId()).isEqualTo(testRoleId);

        System.out.println("✅ 步骤5完成：成功为用户分配角色");
        System.out.println("   - 用户ID=" + testUserId + " 拥有角色ID=" + testRoleId);
    }

    /**
     * 测试步骤6：验证用户权限
     */
    @Test
    @Order(6)
    @DisplayName("步骤6：验证用户权限")
    void step6_verifyUserPermissions() {
        // 先创建完整的测试数据链
        step1_createTestUser();
        step2_createTestRole();
        step3_createTestMenus();
        doStep4();
        doStep5();

        doStep6();
    }

    private void doStep6() {
        // 验证用户拥有的菜单权限
        List<Long> userMenuIds = userRoleRepository.findMenuIdsByUserId(testUserId);
        assertThat(userMenuIds).isNotEmpty();
        assertThat(userMenuIds).containsExactlyInAnyOrder(testMenuId1, testMenuId2, testButtonId);

        // 验证用户拥有的权限标识
        if (permissionService != null) {
            Set<String> userPermissions = new HashSet<>(permissionService.getPermissionsByUserId(testUserId));
            assertThat(userPermissions).isNotEmpty();
            assertThat(userPermissions).contains("test:page:view", "test:page:add");

            // 验证具体权限检查
            boolean hasViewPermission = permissionService.hasPermission(testUserId, "test:page:view");
            boolean hasAddPermission = permissionService.hasPermission(testUserId, "test:page:add");
            boolean hasDeletePermission = permissionService.hasPermission(testUserId, "test:page:delete");

            assertThat(hasViewPermission).isTrue();
            assertThat(hasAddPermission).isTrue();
            assertThat(hasDeletePermission).isFalse(); // 用户没有删除权限

            System.out.println("✅ 步骤6完成：权限验证成功");
            System.out.println("   - 用户拥有权限：" + userPermissions);
            System.out.println("   - test:page:view权限：" + hasViewPermission);
            System.out.println("   - test:page:add权限：" + hasAddPermission);
            System.out.println("   - test:page:delete权限：" + hasDeletePermission);
        } else {
            System.out.println("⚠️  PermissionService未找到，跳过权限验证");
        }

        System.out.println("   - 用户菜单ID：" + userMenuIds);
    }

    /**
     * 测试步骤7：测试权限继承（如果启用）
     */
    @Test
    @Order(7)
    @DisplayName("步骤7：测试权限继承")
    void step7_testPermissionInheritance() {
        // 先创建完整的测试数据链
        step1_createTestUser();
        step2_createTestRole();
        step3_createTestMenus();
        doStep4();
        doStep5();

        doStep7();
    }

    private void doStep7() {
        if (permissionInheritanceService != null) {
            // 测试权限继承功能
            Set<String> inheritedPermissions = permissionInheritanceService.calculateUserPermissions(testUserId).getPermissions();
            assertThat(inheritedPermissions).isNotEmpty();

            System.out.println("✅ 步骤7完成：权限继承测试成功");
            System.out.println("   - 继承的权限：" + inheritedPermissions);
        } else {
            System.out.println("⚠️  PermissionInheritanceService未找到，跳过权限继承测试");
        }
    }

    /**
     * 完整流程集成测试
     */
    @Test
    @Order(8)
    @DisplayName("完整RBAC流程集成测试")
    void fullRbacIntegrationTest() {
        System.out.println("\n🚀 开始RBAC完整流程集成测试...");
        
        // 执行完整流程
        step1_createTestUser();
        step2_createTestRole();
        step3_createTestMenus();
        doStep4();
        doStep5();
        doStep6();
        doStep7();
        
        System.out.println("\n🎉 RBAC完整流程集成测试成功完成！");
        System.out.println("\n📊 测试总结：");
        System.out.println("   ✅ 用户创建：成功");
        System.out.println("   ✅ 角色创建：成功");
        System.out.println("   ✅ 菜单创建：成功");
        System.out.println("   ✅ 角色菜单关联：成功");
        System.out.println("   ✅ 用户角色关联：成功");
        System.out.println("   ✅ 权限验证：成功");
        System.out.println("   ✅ 权限继承：" + (permissionInheritanceService != null ? "成功" : "跳过"));
    }
    
    /**
     * 清理测试数据
     */
    private void cleanupTestData() {
        try {
            // 清理用户角色关联
            if (testUserId != null) {
                userRoleRepository.deleteByUserId(testUserId);
            }
            
            // 清理角色菜单关联
            if (testRoleId != null) {
                roleMenuRepository.deleteByRoleId(testRoleId);
            }
            
            // 清理用户
            if (testUserId != null) {
                userRepository.deleteById(testUserId);
            }
            
            // 清理角色
            if (testRoleId != null) {
                roleRepository.deleteById(testRoleId);
            }
            
            // 清理菜单（按层级删除）
            if (testButtonId != null) {
                menuRepository.deleteById(testButtonId);
            }
            if (testMenuId2 != null) {
                menuRepository.deleteById(testMenuId2);
            }
            if (testMenuId1 != null) {
                menuRepository.deleteById(testMenuId1);
            }
            
        } catch (Exception e) {
            // 忽略清理过程中的异常
            System.out.println("清理测试数据时发生异常（可忽略）：" + e.getMessage());
        }
    }
}