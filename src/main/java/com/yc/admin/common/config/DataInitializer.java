package com.yc.admin.common.config;

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

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化系统数据...");
        
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
}