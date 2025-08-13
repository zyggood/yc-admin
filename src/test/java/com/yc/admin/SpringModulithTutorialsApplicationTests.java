package com.yc.admin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class SpringModulithTutorialsApplicationTests {

    @Test
    void verifyApplicationModuleModel() {
        System.out.println("\n--- 验证 Spring Modulith 模块结构 ---");

        // 1. 获取应用程序模块模型
        ApplicationModules modules = ApplicationModules.of(AdminApplication.class);

        // 2. 打印模块信息（可选，用于观察和调试）
        System.out.println("--- 检测到的模块信息 ---");
        modules.forEach(System.out::println);
        System.out.println("--------------------");

        // 3. 验证模块是否符合模块化约束
        // 这会检查所有模块间的依赖是否符合Modulith的规则，例如：
        // - 没有循环依赖
        // - 模块没有直接访问其他模块的内部类
        modules.verify();

        System.out.println("--- 模块结构验证成功！---");
        System.out.println("--- 验证 Spring Modulith 模块结构完毕 ---\n");
    }
}
