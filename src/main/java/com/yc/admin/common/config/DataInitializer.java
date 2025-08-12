package com.yc.admin.common.config;

import com.yc.admin.role.entity.Role;
import com.yc.admin.role.service.RoleService;
import com.yc.admin.user.entity.User;
import com.yc.admin.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 在应用启动时自动创建管理员账户
 * 
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final RoleService roleService;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化系统数据...");
        
        // 初始化系统角色
        initSystemRoles();
        
        // 初始化管理员账户
        initAdminUser();
        
        log.info("系统数据初始化完成");
    }

    /**
     * 初始化管理员账户
     */
    private void initAdminUser() {
        String adminUserName = "admin";
        
        // 检查管理员账户是否已存在
        if (userService.findByUserName(adminUserName).isPresent()) {
            log.info("管理员账户已存在，跳过初始化");
            return;
        }
        
        try {
            // 创建管理员用户
            User adminUser = new User()
                    .setUserName(adminUserName)
                    .setNickName("系统管理员")
                    .setEmail("admin@yc.com")
                    .setPhone("13800138000")
                    .setPassword("admin123")
                    .setSex(User.Sex.UNKNOWN)
                    .setStatus(User.Status.NORMAL)
                    .setRemark("系统默认管理员账户");
            
            User createdUser = userService.createUser(adminUser);
            log.info("管理员账户创建成功: 用户名={}, ID={}", createdUser.getUserName(), createdUser.getId());
            log.info("默认管理员登录信息: 用户名=admin, 密码=admin123");
            
        } catch (Exception e) {
            log.error("创建管理员账户失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }

    /**
     * 初始化系统角色
     */
    private void initSystemRoles() {
        // 初始化超级管理员角色
        initAdminRole();
        
        // 初始化普通用户角色
        initUserRole();
    }

    /**
     * 初始化超级管理员角色
     */
    private void initAdminRole() {
        String adminRoleKey = "admin";
        
        // 检查超级管理员角色是否已存在
        if (roleService.findByRoleKey(adminRoleKey).isPresent()) {
            log.info("超级管理员角色已存在，跳过初始化");
            return;
        }
        
        try {
            // 创建超级管理员角色
            Role adminRole = new Role()
                    .setRoleName("超级管理员")
                    .setRoleKey(adminRoleKey)
                    .setRoleSort(1)
                    .setDataScope(Role.DataScope.ALL)
                    .setMenuCheckStrictly(true)
                    .setDeptCheckStrictly(true)
                    .setStatus(Role.Status.NORMAL)
                    .setRemark("超级管理员角色，拥有系统所有权限");
            
            Role createdRole = roleService.createRole(adminRole);
            log.info("超级管理员角色创建成功: 角色名={}, ID={}", createdRole.getRoleName(), createdRole.getId());
            
        } catch (Exception e) {
            log.error("创建超级管理员角色失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }

    /**
     * 初始化普通用户角色
     */
    private void initUserRole() {
        String userRoleKey = "user";
        
        // 检查普通用户角色是否已存在
        if (roleService.findByRoleKey(userRoleKey).isPresent()) {
            log.info("普通用户角色已存在，跳过初始化");
            return;
        }
        
        try {
            // 创建普通用户角色
            Role userRole = new Role()
                    .setRoleName("普通用户")
                    .setRoleKey(userRoleKey)
                    .setRoleSort(2)
                    .setDataScope(Role.DataScope.SELF)
                    .setMenuCheckStrictly(true)
                    .setDeptCheckStrictly(true)
                    .setStatus(Role.Status.NORMAL)
                    .setRemark("普通用户角色，拥有基础功能权限");
            
            Role createdRole = roleService.createRole(userRole);
            log.info("普通用户角色创建成功: 角色名={}, ID={}", createdRole.getRoleName(), createdRole.getId());
            
        } catch (Exception e) {
            log.error("创建普通用户角色失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }
}