/**
 * 认证授权模块
 * 负责用户登录、JWT令牌管理、权限验证等
 */
@ApplicationModule(
        displayName = "认证授权模块",
        allowedDependencies = {"common", "system :: api",  "system :: dto"}
)
package com.yc.admin.auth;

import org.springframework.modulith.ApplicationModule;