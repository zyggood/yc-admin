/**
 * 公共模块 - 提供基础设施和共享组件
 *
 * 公开API：
 * - core: 统一返回结果、分页工具
 * - entity: 基础实体类
 * - exception: 异常处理
 *
 * 内部实现：
 * - config: 配置类（不对外暴露）
 * - service: 内部服务（不对外暴露）
 */
@org.springframework.modulith.ApplicationModule(
        type = ApplicationModule.Type.OPEN
)
package com.yc.admin.common;

import org.springframework.modulith.ApplicationModule;