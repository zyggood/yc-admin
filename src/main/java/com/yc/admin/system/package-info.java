/**
 * 系统管理模块
 * 负责用户、角色、菜单、部门等系统基础数据管理
 * 
 * @author YC
 * @since 1.0.0
 */
@ApplicationModule(
    id = "system",
    displayName = "系统管理模块",
    type = ApplicationModule.Type.CLOSED
)
package com.yc.admin.system;

import org.springframework.modulith.ApplicationModule;