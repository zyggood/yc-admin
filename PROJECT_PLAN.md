# Spring Modulith 后台管理系统开发计划

## 项目概述

基于 Spring Modulith 的企业级后台管理系统，采用模块化架构设计，提供完整的权限管理和系统监控功能。

### 技术栈
- Java 21
- Spring Boot 3.5.4
- Spring Modulith 1.4.1
- Spring Security
- Spring Data JPA + MyBatis
- Lombok

## 模块化架构设计

### 核心模块划分
```
com.yc.admin/
├── common/           # 公共模块（基础工具、配置）
├── system/           # 系统管理模块
│   ├── user/        # 用户管理
│   ├── dept/        # 部门管理
│   ├── role/        # 角色管理
│   ├── menu/        # 菜单管理
│   └── permission/  # 权限管理
├── auth/            # 认证授权模块
├── monitor/         # 系统监控模块
└── log/             # 日志管理模块
```

### 模块依赖关系
- common: 被所有模块依赖
- user: 依赖 dept, role
- role: 依赖 menu
- system: 独立模块
- monitor: 依赖 user
- log: 独立模块

## 开发阶段规划

### 第一阶段：基础架构搭建

#### 1.1 项目初始化
- [x] 创建 Maven 项目结构
- [x] 配置 pom.xml 依赖
- [ ] 创建 application.yml 配置文件
  ```
  AI提示词: 创建 src/main/resources/application.yml 配置文件，包含数据源配置、JPA配置、MyBatis配置、日志配置。使用环境变量占位符，支持dev/test/prod多环境配置。参考项目计划中的配置模板。
  ```
- [ ] 配置数据库连接
  ```
  AI提示词: 在application.yml中配置MySQL数据库连接，包括连接池配置、事务配置。创建对应的数据库schema，确保字符集为utf8mb4。
  ```
- [x] 创建主启动类
  ```
  AI提示词: 在com.yc.admin包下创建AdminApplication主启动类，添加@SpringBootApplication注解，启用JPA和MyBatis扫描。添加@EnableScheduling支持定时任务。
  ```

#### 1.2 公共模块开发
- [x] 创建统一返回结果类 Result<T>
  ```
  AI提示词: 在com.yc.admin.common.core包下创建Result<T>泛型类，包含code、message、data字段。提供success()、error()静态方法。实现序列化接口，添加构造方法和getter/setter。
  ```
- [x] 创建全局异常处理器
  ```
  AI提示词: 创建GlobalExceptionHandler类，使用@ControllerAdvice注解。处理业务异常、参数校验异常、系统异常。返回统一的Result格式，记录异常日志。
  ```
- [x] 创建基础实体类 BaseEntity
  ```
  AI提示词: 创建BaseEntity抽象类，包含id、createTime、updateTime、createBy、updateBy、delFlag字段。使用JPA注解@MappedSuperclass，添加Lombok注解。配置自动填充策略。
  ```
- [ ] 创建分页查询工具类
  ```
  AI提示词: 创建PageUtils工具类，封装Spring Data的Pageable和Page。提供构建分页参数、转换分页结果的方法。支持排序参数处理。
  ```
- [ ] 创建常用工具类（日期、字符串、加密等）
  ```
  AI提示词: 创建DateUtils、StringUtils、PasswordUtils、SecurityUtils等工具类。DateUtils提供日期格式化、解析方法；PasswordUtils提供BCrypt加密；SecurityUtils提供当前用户信息获取。
  ```
- [ ] 创建统一常量类
  ```
  AI提示词: 创建Constants类定义系统常量，包括用户状态、删除标志、数据权限范围等。创建HttpStatus类定义响应状态码。使用public static final修饰。
  ```

#### 1.3 数据库设计
- [x] 设计用户表 (sys_user)
  ```
  AI提示词: 创建sys_user表，包含user_id(主键)、user_name、nick_name、email、phone、sex、avatar、password、status、del_flag、create_by、create_time、update_by、update_time、remark字段。设置合适的索引和约束。
  ```
- [ ] 设计部门表 (sys_dept)
  ```
  AI提示词: 创建sys_dept表，包含dept_id(主键)、parent_id、ancestors、dept_name、order_num、leader、phone、email、status、del_flag等字段。支持树形结构，ancestors字段存储祖级列表。
  ```
- [ ] 设计角色表 (sys_role)
  ```
  AI提示词: 创建sys_role表，包含role_id(主键)、role_name、role_key、role_sort、data_scope、menu_check_strictly、dept_check_strictly、status、del_flag等字段。data_scope字段控制数据权限范围。
  ```
- [ ] 设计菜单表 (sys_menu)
  ```
  AI提示词: 创建sys_menu表，包含menu_id(主键)、menu_name、parent_id、order_num、path、component、query、is_frame、is_cache、menu_type、visible、status、perms、icon等字段。支持菜单和按钮权限。
  ```
- [ ] 设计用户角色关联表 (sys_user_role)
  ```
  AI提示词: 创建sys_user_role关联表，包含user_id、role_id字段作为联合主键。建立外键约束关联sys_user和sys_role表。
  ```
- [ ] 设计角色菜单关联表 (sys_role_menu)
  ```
  AI提示词: 创建sys_role_menu关联表，包含role_id、menu_id字段作为联合主键。建立外键约束关联sys_role和sys_menu表。
  ```
- [ ] 设计字典表 (sys_dict_type, sys_dict_data)
  ```
  AI提示词: 创建sys_dict_type字典类型表和sys_dict_data字典数据表。type表包含dict_id、dict_name、dict_type、status等字段；data表包含dict_code、dict_sort、dict_label、dict_value、dict_type等字段。
  ```
- [ ] 设计参数表 (sys_config)
  ```
  AI提示词: 创建sys_config参数配置表，包含config_id(主键)、config_name、config_key、config_value、config_type、create_by、create_time、update_by、update_time、remark字段。
  ```
- [ ] 设计日志表 (sys_oper_log, sys_login_info)
  ```
  AI提示词: 创建sys_oper_log操作日志表，包含oper_id、title、business_type、method、request_method、operator_type、oper_name、dept_name、oper_url、oper_ip、oper_location、oper_param、json_result、status、error_msg、oper_time字段。创建sys_logininfor登录日志表，包含info_id、user_name、ipaddr、login_location、browser、os、status、msg、login_time字段。
  ```

### 第二阶段：核心功能开发

#### 2.1 用户管理模块 (user)
```
user/
├── domain/
│   ├── User.java              # 用户实体
│   ├── UserRepository.java    # 用户仓储接口
│   └── UserService.java       # 用户领域服务
├── infrastructure/
│   ├── UserRepositoryImpl.java # 用户仓储实现
│   └── mapper/
│       └── UserMapper.java     # MyBatis映射器
├── application/
│   ├── UserApplicationService.java # 用户应用服务
│   └── dto/
│       ├── UserCreateDTO.java
│       ├── UserUpdateDTO.java
│       └── UserQueryDTO.java
└── web/
    └── UserController.java     # 用户控制器
```

**开发任务：**
- [ ] 创建 User 实体类
  ```
  AI提示词: 在com.yc.admin.user.domain包下创建User实体类，继承BaseEntity。使用JPA注解@Entity、@Table，添加Lombok注解。包含用户名、昵称、邮箱、手机号、性别、头像、密码、状态等字段。建立与部门的多对一关系。
  ```
- [ ] 实现用户 CRUD 操作
  ```
  AI提示词: 创建UserRepository接口继承JpaRepository，定义自定义查询方法。创建UserService领域服务，实现用户的创建、更新、删除、查询业务逻辑。使用@Transactional注解确保事务一致性。
  ```
- [ ] 实现用户分页查询
  ```
  AI提示词: 在UserRepository中添加分页查询方法，支持按用户名、状态、部门等条件查询。创建UserQueryDTO封装查询条件。在UserApplicationService中实现分页查询逻辑，返回PageResult。
  ```
- [ ] 实现用户状态管理（启用/禁用）
  ```
  AI提示词: 在User实体中添加status字段（0正常 1停用）。实现changeStatus方法，支持批量状态变更。添加业务校验，防止禁用当前登录用户。记录状态变更日志。
  ```
- [ ] 实现密码加密存储
  ```
  AI提示词: 使用BCryptPasswordEncoder对密码进行加密。在用户创建和密码修改时自动加密。提供密码重置功能，生成随机密码。添加密码强度校验规则。
  ```
- [ ] 实现用户导入导出功能
  ```
  AI提示词: 创建用户导入模板，支持Excel格式。实现批量导入用户，包含数据校验、重复检查。实现用户数据导出，支持按条件筛选。使用EasyExcel或Apache POI处理Excel文件。
  ```

#### 2.2 部门管理模块 (dept)
```
dept/
├── domain/
│   ├── Dept.java
│   ├── DeptRepository.java
│   └── DeptService.java
├── infrastructure/
│   ├── DeptRepositoryImpl.java
│   └── mapper/
│       └── DeptMapper.java
├── application/
│   ├── DeptApplicationService.java
│   └── dto/
└── web/
    └── DeptController.java
```

**开发任务：**
- [ ] 创建 Dept 实体类（支持树形结构）
  ```
  AI提示词: 在com.yc.admin.dept.domain包下创建Dept实体类，继承BaseEntity。包含parentId、ancestors、deptName、orderNum、leader、phone、email、status等字段。使用@OneToMany建立父子关系，支持树形结构。
  ```
- [ ] 实现部门树形结构查询
  ```
  AI提示词: 创建递归查询方法，根据parentId构建部门树。实现buildDeptTree方法，将扁平化数据转换为树形结构。支持根据权限过滤部门数据，只显示用户有权限的部门。
  ```
- [ ] 实现部门 CRUD 操作
  ```
  AI提示词: 实现部门的增删改查功能。新增部门时自动计算ancestors字段。删除部门时检查是否存在子部门和关联用户。修改部门时同步更新子部门的ancestors字段。
  ```
- [ ] 实现部门层级权限控制
  ```
  AI提示词: 根据用户的数据权限范围过滤部门数据。实现数据权限注解@DataScope，支持全部数据权限、自定数据权限、部门数据权限、部门及以下数据权限、仅本人数据权限。
  ```
- [ ] 实现部门状态管理
  ```
  AI提示词: 添加部门状态字段（0正常 1停用）。停用部门时同时停用所有子部门。启用部门时检查父部门状态。提供批量状态变更功能。
  ```

#### 2.3 角色管理模块 (role)
```
role/
├── domain/
│   ├── Role.java
│   ├── RoleRepository.java
│   └── RoleService.java
├── infrastructure/
├── application/
└── web/
    └── RoleController.java
```

**开发任务：**
- [ ] 创建 Role 实体类
  ```
  AI提示词: 在com.yc.admin.role.domain包下创建Role实体类，继承BaseEntity。包含roleName、roleKey、roleSort、dataScope、menuCheckStrictly、deptCheckStrictly、status等字段。使用@ManyToMany建立与Menu的关联关系。
  ```
- [ ] 实现角色 CRUD 操作
  ```
  AI提示词: 创建RoleRepository和RoleService，实现角色的基本CRUD操作。添加角色唯一性校验（roleKey不能重复）。删除角色时检查是否有用户关联。支持角色排序功能。
  ```
- [ ] 实现角色权限分配
  ```
  AI提示词: 实现角色菜单权限分配功能，支持树形菜单选择。创建assignMenus方法，更新角色菜单关联关系。支持父子菜单联动选择。实现权限回显功能。
  ```
- [ ] 实现数据权限范围设置
  ```
  AI提示词: 实现数据权限范围配置：1全部数据权限、2自定数据权限、3部门数据权限、4部门及以下数据权限、5仅本人数据权限。自定数据权限支持选择特定部门。
  ```
- [ ] 实现角色状态管理
  ```
  AI提示词: 添加角色状态管理功能（0正常 1停用）。停用角色时检查是否有用户关联。提供批量状态变更功能。状态变更时记录操作日志。
  ```

#### 2.4 菜单管理模块 (menu)
```
menu/
├── domain/
│   ├── Menu.java
│   ├── MenuRepository.java
│   └── MenuService.java
├── infrastructure/
├── application/
└── web/
    └── MenuController.java
```

**开发任务：**
- [ ] 创建 Menu 实体类（支持树形结构）
  ```
  AI提示词: 在com.yc.admin.menu.domain包下创建Menu实体类，继承BaseEntity。包含menuName、parentId、orderNum、path、component、query、isFrame、isCache、menuType、visible、status、perms、icon等字段。menuType区分目录(M)、菜单(C)、按钮(F)。
  ```
- [ ] 实现菜单树形结构查询
  ```
  AI提示词: 实现菜单树形结构构建，支持按用户权限过滤。创建buildMenuTree方法，递归构建菜单树。实现getRouters方法，生成前端路由配置。支持菜单排序和层级显示。
  ```
- [ ] 实现菜单 CRUD 操作
  ```
  AI提示词: 实现菜单的增删改查功能。新增菜单时校验父菜单存在性。删除菜单时检查是否存在子菜单和角色关联。支持菜单拖拽排序功能。
  ```
- [ ] 实现按钮权限管理
  ```
  AI提示词: 实现按钮级权限控制，menuType为F的为按钮权限。perms字段存储权限标识符。创建权限注解@PreAuthorize，在Controller方法上使用。实现hasPermi权限校验方法。
  ```
- [ ] 实现菜单状态管理
  ```
  AI提示词: 添加菜单显示状态管理（visible字段：0显示 1隐藏）。隐藏菜单时同时隐藏所有子菜单。提供批量状态变更功能。状态变更时清除相关缓存。
  ```

### 第三阶段：系统功能开发

#### 3.1 系统管理模块 (system)
```
system/
├── dict/              # 字典管理
├── config/            # 参数管理
└── notice/            # 通知公告
```

**开发任务：**
- [ ] 实现字典类型管理
  ```
  AI提示词: 在com.yc.admin.system.dict包下创建DictType实体和相关服务。实现字典类型的CRUD操作，包含类型名称、类型键值、状态等字段。支持字典类型的启用禁用，禁用时同时禁用关联的字典数据。
  ```
- [ ] 实现字典数据管理
  ```
  AI提示词: 创建DictData实体，关联DictType。实现字典数据的增删改查，支持按类型查询。提供字典标签和字典值的转换方法。实现字典数据排序功能。创建字典缓存，提高查询性能。
  ```
- [ ] 实现系统参数配置
  ```
  AI提示词: 创建Config实体和ConfigService。实现系统参数的动态配置功能，支持参数的增删改查。提供根据参数键获取参数值的方法。实现参数缓存机制，支持缓存刷新。
  ```
- [ ] 实现通知公告发布
  ```
  AI提示词: 创建Notice实体，包含公告标题、内容、类型、状态等字段。实现公告的发布、编辑、删除功能。支持公告状态管理（草稿、发布、关闭）。提供公告列表查询和详情查看功能。
  ```
- [ ] 实现缓存刷新功能
  ```
  AI提示词: 集成Redis缓存，实现字典、参数等数据的缓存管理。提供缓存刷新接口，支持单个和批量缓存清除。实现缓存预热功能，系统启动时加载常用数据到缓存。
  ```

#### 3.2 日志管理模块 (log)
```
log/
├── operation/         # 操作日志
└── login/            # 登录日志
```

**开发任务：**
- [ ] 实现操作日志记录（AOP）
  ```
  AI提示词: 创建@Log注解和LogAspect切面类。在切面中拦截Controller方法，记录操作模块、操作类型、操作描述、请求参数、返回结果、操作时间、操作人、操作IP等信息。支持异步日志记录，提高性能。
  ```
- [ ] 实现登录日志记录
  ```
  AI提示词: 在用户登录成功/失败时记录登录日志。包含用户名、登录IP、登录地点、浏览器、操作系统、登录状态、提示消息等信息。集成IP地址解析，获取登录地理位置。
  ```
- [ ] 实现日志查询和导出
  ```
  AI提示词: 实现操作日志和登录日志的分页查询功能，支持按时间范围、操作人、操作模块等条件筛选。提供日志导出功能，支持Excel格式。实现日志详情查看功能。
  ```
- [ ] 实现日志清理功能
  ```
  AI提示词: 实现定时任务清理过期日志，支持按天数配置保留策略。提供手动清理功能，支持按条件批量删除日志。实现日志归档功能，将历史日志转移到归档表。
  ```

#### 3.3 系统监控模块 (monitor)
```
monitor/
├── online/           # 在线用户
├── job/              # 定时任务
├── server/           # 服务监控
├── cache/            # 缓存监控
└── druid/            # 连接池监控
```

**开发任务：**
- [ ] 实现在线用户监控
  ```
  AI提示词: 实现在线用户会话管理，记录用户登录时间、最后访问时间、登录IP等信息。提供在线用户列表查询，支持强制下线功能。实现会话超时检测和清理机制。
  ```
- [ ] 集成 Quartz 定时任务
  ```
  AI提示词: 集成Quartz框架，实现定时任务的动态管理。支持任务的增删改查、启动停止、立即执行功能。实现任务执行日志记录，包含执行时间、执行结果、异常信息。支持Cron表达式配置。
  ```
- [ ] 实现服务器信息监控
  ```
  AI提示词: 实现服务器硬件信息监控，包含CPU使用率、内存使用情况、磁盘空间、网络状态等。使用oshi库获取系统信息。提供实时监控接口，支持图表展示。
  ```
- [ ] 集成 Redis 缓存监控
  ```
  AI提示词: 实现Redis缓存监控功能，显示Redis连接信息、内存使用情况、键值统计等。提供缓存键查询、删除功能。实现缓存命中率统计和性能分析。
  ```
- [ ] 集成 Druid 连接池监控
  ```
  AI提示词: 集成Druid数据源，启用监控功能。配置Druid监控页面，显示SQL执行统计、连接池状态、慢SQL分析等。实现SQL防火墙功能，防止SQL注入攻击。
  ```

### 第四阶段：安全与认证

#### 4.1 Spring Security 配置
- [ ] 配置 Security 过滤器链
  ```
  AI提示词: 创建SecurityConfig配置类，配置Spring Security过滤器链。设置登录页面、登录接口、登出接口。配置静态资源放行规则。实现自定义认证成功/失败处理器。
  ```
- [ ] 实现 JWT 认证
  ```
  AI提示词: 集成JWT实现无状态认证。创建JwtUtils工具类，提供token生成、解析、验证功能。实现JwtAuthenticationFilter过滤器，拦截请求验证token。配置token过期时间和刷新机制。
  ```
- [ ] 实现权限拦截器
  ```
  AI提示词: 实现自定义权限拦截器，基于用户角色和菜单权限进行访问控制。创建PermissionService，提供权限校验方法。实现@PreAuthorize注解支持，在方法级别控制权限。
  ```
- [ ] 实现数据权限过滤
  ```
  AI提示词: 实现数据权限过滤器，根据用户的数据权限范围过滤查询结果。创建@DataScope注解，在Service方法上使用。实现AOP切面，动态修改SQL添加数据权限条件。
  ```
- [ ] 实现验证码功能
  ```
  AI提示词: 实现图形验证码功能，防止暴力破解。使用Java Graphics2D生成验证码图片。将验证码存储到Redis中，设置过期时间。在登录时校验验证码正确性。
  ```

#### 4.2 权限控制
- [ ] 实现基于角色的访问控制 (RBAC)
  ```
  AI提示词: 实现完整的RBAC权限模型，包含用户、角色、菜单三层关系。实现用户多角色支持，角色多菜单权限支持。提供权限继承和权限合并功能。实现权限缓存机制。
  ```
- [ ] 实现数据权限控制
  ```
  AI提示词: 实现五种数据权限范围：全部数据、自定数据、部门数据、部门及以下数据、仅本人数据。创建数据权限过滤器，在查询时自动添加权限条件。支持自定义部门权限配置。
  ```
- [ ] 实现按钮级权限控制
  ```
  AI提示词: 实现前端按钮权限控制，根据用户权限动态显示/隐藏按钮。提供权限指令v-hasPermi，在Vue组件中使用。实现权限工具类，提供JavaScript权限校验方法。
  ```
- [ ] 实现接口权限注解
  ```
  AI提示词: 创建@RequiresPermissions、@RequiresRoles等权限注解。实现注解解析器，在Controller方法执行前进行权限校验。支持权限表达式，如AND、OR逻辑组合。提供权限异常处理。
  ```

### 第五阶段：优化与测试

#### 5.1 性能优化
- [ ] 数据库查询优化
  ```
  AI提示词: 分析慢SQL查询，优化数据库索引设计。使用explain分析查询执行计划，优化复杂查询语句。实现分页查询优化，避免深度分页问题。配置数据库连接池参数，提高并发性能。
  ```
- [ ] 缓存策略优化
  ```
  AI提示词: 设计多级缓存策略，包含本地缓存和Redis缓存。实现缓存预热、缓存穿透、缓存雪崩防护。优化缓存键设计和过期策略。实现缓存监控和性能分析。
  ```
- [ ] 接口响应优化
  ```
  AI提示词: 实现接口响应时间监控，识别性能瓶颈。优化序列化性能，减少对象创建。实现异步处理机制，提高并发能力。配置Gzip压缩，减少网络传输时间。
  ```
- [ ] 前端资源优化
  ```
  AI提示词: 实现静态资源压缩和合并，减少HTTP请求数量。配置CDN加速，提高资源加载速度。实现懒加载和虚拟滚动，优化大数据量展示。优化图片资源，使用WebP格式。
  ```

#### 5.2 测试
- [ ] 单元测试编写
  ```
  AI提示词: 为每个Service类编写单元测试，使用JUnit 5和Mockito。测试覆盖正常流程、异常流程、边界条件。使用@MockBean模拟外部依赖。配置测试数据库，使用@Transactional回滚测试数据。
  ```
- [ ] 集成测试编写
  ```
  AI提示词: 编写Controller层集成测试，使用@SpringBootTest和MockMvc。测试完整的请求响应流程，包含权限验证。使用TestContainers进行数据库集成测试。编写API文档测试。
  ```
- [ ] 模块化测试（Spring Modulith）
  ```
  AI提示词: 使用Spring Modulith测试工具验证模块边界。编写模块集成测试，验证模块间事件通信。使用@ModuleTest注解进行模块隔离测试。验证模块依赖关系的正确性。
  ```
- [ ] 性能测试
  ```
  AI提示词: 使用JMeter或Gatling进行性能测试，测试系统并发能力。编写压力测试脚本，模拟真实用户场景。监控系统资源使用情况，识别性能瓶颈。制定性能基准和优化目标。
  ```

## 开发规范

### 代码规范
1. **包命名**: com.yc.admin.{module}
2. **类命名**: 使用驼峰命名法
3. **方法命名**: 动词开头，驼峰命名
4. **常量命名**: 全大写，下划线分隔
5. **注释规范**: 类和方法必须有 Javadoc

### 数据库规范
1. **表命名**: sys_ 前缀，下划线分隔
2. **字段命名**: 下划线分隔
3. **主键**: 统一使用 id，Long 类型
4. **时间字段**: create_time, update_time
5. **逻辑删除**: del_flag 字段

### API 规范
1. **RESTful 设计**: GET/POST/PUT/DELETE
2. **统一返回格式**: Result<T>
3. **错误码规范**: 业务错误码统一管理
4. **参数校验**: 使用 @Valid 注解
5. **接口文档**: 使用 Swagger/OpenAPI

## 配置文件模板

### application.yml
```yaml
spring:
  application:
    name: admin-system
  profiles:
    active: dev
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/admin_db?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
  security:
    user:
      name: admin
      password: admin123

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.yc.admin.*.domain
  configuration:
    map-underscore-to-camel-case: true

logging:
  level:
    com.yc.admin: debug
    org.springframework.modulith: debug
```

## AI 开发提示词

当使用 AI 协助开发时，请遵循以下指导原则：

### 1. 模块化开发原则
- 严格按照 Spring Modulith 模块边界开发
- 每个模块保持高内聚、低耦合
- 模块间通过事件或接口通信
- 避免循环依赖

### 2. 代码生成指导
- 生成代码时遵循 DDD 分层架构
- 实体类使用 JPA 注解和 Lombok
- 控制器使用 RESTful 风格
- 服务类使用事务注解

### 3. 数据库操作
- 简单查询使用 JPA Repository
- 复杂查询使用 MyBatis
- 统一使用乐观锁和逻辑删除
- 分页查询使用 Spring Data 分页

### 4. 安全考虑
- 所有接口需要权限控制
- 敏感操作记录日志
- 密码加密存储
- 防止 SQL 注入和 XSS 攻击

### 5. 测试要求
- 每个模块编写单元测试
- 使用 Spring Modulith 测试工具
- 模拟外部依赖
- 测试覆盖率不低于 80%

## 开发检查清单

### 功能开发完成标准
- [ ] 实体类设计完成
- [ ] 数据库表创建完成
- [ ] CRUD 接口实现完成
- [ ] 权限控制实现完成
- [ ] 单元测试编写完成
- [ ] 接口文档编写完成
- [ ] 代码审查通过

### 模块集成标准
- [ ] 模块边界清晰
- [ ] 依赖关系正确
- [ ] 事件发布订阅正常
- [ ] 集成测试通过
- [ ] 性能测试通过

## 部署说明

### 开发环境
- JDK 21
- MySQL 8.0
- Redis 6.0+
- Maven 3.6+

### 生产环境
- 容器化部署（Docker）
- 数据库集群
- Redis 集群
- 负载均衡
- 监控告警

---

**注意**: 此计划作为 AI 开发助手的指导文档，在实际开发过程中应严格遵循模块化设计原则和代码规范要求。