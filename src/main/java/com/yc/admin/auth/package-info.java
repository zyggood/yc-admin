/**
 * 认证授权模块
 * 负责用户登录、JWT令牌管理、权限验证等
 */
@ApplicationModule(
        allowedDependencies = {"common", "system"}
)
package com.yc.admin.auth;

import org.springframework.modulith.ApplicationModule;