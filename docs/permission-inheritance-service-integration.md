# PermissionInheritanceService 实际应用建议

## 概述

本文档提供了 `PermissionInheritanceService` 在实际项目中的集成和应用建议，帮助开发团队快速上手并正确使用权限继承功能。

## 1. 服务集成方案

### 1.1 在控制器层集成

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private PermissionApiService permissionApiService;
    
    @GetMapping
    @PreAuthorize("@permissionApiService.hasPermission(authentication.principal.id, 'system:user:list')")
    public ResponseEntity<PageResult<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        Long currentUserId = getCurrentUserId(authentication);
        
        // 获取当前用户的数据权限
        Set<DataScope> dataScopes = permissionApiService.getUserDataScopes(currentUserId);
        Set<Long> deptIds = permissionApiService.getUserDataDeptIds(currentUserId);
        
        // 根据数据权限查询用户列表
        PageResult<UserVO> result = userService.findUsersByDataPermission(
            page, size, dataScopes, deptIds);
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping
    @PreAuthorize("@permissionApiService.hasPermission(authentication.principal.id, 'system:user:create')")
    public ResponseEntity<Void> createUser(@RequestBody @Valid CreateUserRequest request,
                                          Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        
        // 检查是否有权限在指定部门创建用户
        if (request.getDeptId() != null) {
            boolean hasDeptPermission = permissionApiService.hasDeptDataPermission(
                currentUserId, request.getDeptId());
            if (!hasDeptPermission) {
                throw new AccessDeniedException("无权限在该部门创建用户");
            }
        }
        
        userService.createUser(request);
        return ResponseEntity.ok().build();
    }
}
```

### 1.2 在服务层集成

```java
@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    @Autowired
    private PermissionApiService permissionApiService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public PageResult<UserVO> findUsersByDataPermission(int page, int size, 
                                                        Set<DataScope> dataScopes, 
                                                        Set<Long> deptIds) {
        
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> userPage;
        
        if (dataScopes.contains(DataScope.ALL)) {
            // 全部数据权限
            userPage = userRepository.findAll(pageable);
        } else if (dataScopes.contains(DataScope.CUSTOM)) {
            // 自定义数据权限
            userPage = userRepository.findByDeptIdIn(deptIds, pageable);
        } else if (dataScopes.contains(DataScope.DEPT)) {
            // 本部门数据权限
            userPage = userRepository.findByDeptIdIn(deptIds, pageable);
        } else if (dataScopes.contains(DataScope.DEPT_AND_SUB)) {
            // 本部门及以下数据权限
            Set<Long> allDeptIds = deptService.getDeptAndSubDeptIds(deptIds);
            userPage = userRepository.findByDeptIdIn(allDeptIds, pageable);
        } else {
            // 仅本人数据权限
            Long currentUserId = SecurityUtils.getCurrentUserId();
            userPage = userRepository.findByIdIn(Set.of(currentUserId), pageable);
        }
        
        return PageResult.of(userPage.map(this::convertToVO));
    }
    
    @Override
    public void updateUser(Long userId, UpdateUserRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 检查是否有权限修改该用户
        User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        boolean hasPermission = permissionApiService.hasDeptDataPermission(
            currentUserId, targetUser.getDeptId());
        
        if (!hasPermission) {
            throw new AccessDeniedException("无权限修改该用户");
        }
        
        // 执行更新逻辑
        updateUserEntity(targetUser, request);
        userRepository.save(targetUser);
    }
}
```

### 1.3 在数据访问层集成

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.deptId IN :deptIds")
    Page<User> findByDeptIdIn(@Param("deptIds") Set<Long> deptIds, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.id IN :userIds")
    Page<User> findByIdIn(@Param("userIds") Set<Long> userIds, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:dataScope = 'ALL') OR " +
           "(:dataScope = 'CUSTOM' AND u.deptId IN :deptIds) OR " +
           "(:dataScope = 'DEPT' AND u.deptId IN :deptIds) OR " +
           "(:dataScope = 'DEPT_AND_SUB' AND u.deptId IN :allDeptIds) OR " +
           "(:dataScope = 'SELF' AND u.id = :currentUserId)")
    Page<User> findByDataPermission(@Param("dataScope") String dataScope,
                                   @Param("deptIds") Set<Long> deptIds,
                                   @Param("allDeptIds") Set<Long> allDeptIds,
                                   @Param("currentUserId") Long currentUserId,
                                   Pageable pageable);
}
```

## 2. 权限检查工具类

### 2.1 权限检查注解

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    
    /**
     * 需要的权限标识
     */
    String value();
    
    /**
     * 权限检查模式
     */
    PermissionMode mode() default PermissionMode.ALL;
    
    /**
     * 是否检查数据权限
     */
    boolean checkDataPermission() default false;
    
    enum PermissionMode {
        ALL,  // 需要所有权限
        ANY   // 需要任意一个权限
    }
}
```

### 2.2 权限检查切面

```java
@Aspect
@Component
public class PermissionCheckAspect {
    
    @Autowired
    private PermissionApiService permissionApiService;
    
    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, 
                                 RequirePermission requirePermission) throws Throwable {
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String permission = requirePermission.value();
        
        // 检查功能权限
        boolean hasPermission = permissionApiService.hasPermission(currentUserId, permission);
        if (!hasPermission) {
            throw new AccessDeniedException("权限不足: " + permission);
        }
        
        // 检查数据权限（如果需要）
        if (requirePermission.checkDataPermission()) {
            checkDataPermission(joinPoint, currentUserId);
        }
        
        return joinPoint.proceed();
    }
    
    private void checkDataPermission(ProceedingJoinPoint joinPoint, Long currentUserId) {
        Object[] args = joinPoint.getArgs();
        
        // 查找参数中的部门ID或用户ID
        for (Object arg : args) {
            if (arg instanceof Long) {
                Long targetId = (Long) arg;
                // 根据业务逻辑检查数据权限
                // 这里需要根据具体业务场景实现
            }
        }
    }
}
```

### 2.3 权限工具类

```java
@Component
public class PermissionUtils {
    
    @Autowired
    private PermissionApiService permissionApiService;
    
    /**
     * 检查当前用户是否有指定权限
     */
    public boolean hasPermission(String permission) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return permissionApiService.hasPermission(currentUserId, permission);
    }
    
    /**
     * 检查当前用户是否有任意一个权限
     */
    public boolean hasAnyPermission(String... permissions) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return Arrays.stream(permissions)
            .anyMatch(permission -> permissionApiService.hasPermission(currentUserId, permission));
    }
    
    /**
     * 检查当前用户是否有所有权限
     */
    public boolean hasAllPermissions(String... permissions) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return Arrays.stream(permissions)
            .allMatch(permission -> permissionApiService.hasPermission(currentUserId, permission));
    }
    
    /**
     * 获取当前用户的数据权限范围
     */
    public Set<DataScope> getCurrentUserDataScopes() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return permissionApiService.getUserDataScopes(currentUserId);
    }
    
    /**
     * 获取当前用户的数据权限部门ID
     */
    public Set<Long> getCurrentUserDataDeptIds() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return permissionApiService.getUserDataDeptIds(currentUserId);
    }
    
    /**
     * 检查当前用户是否有指定部门的数据权限
     */
    public boolean hasDeptDataPermission(Long deptId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return permissionApiService.hasDeptDataPermission(currentUserId, deptId);
    }
}
```

## 3. 前端集成方案

### 3.1 权限指令

```javascript
// Vue.js 权限指令
Vue.directive('permission', {
  inserted(el, binding) {
    const { value } = binding;
    const permissions = store.getters.permissions;
    
    if (value && value instanceof Array && value.length > 0) {
      const hasPermission = value.some(permission => {
        return permissions.includes(permission);
      });
      
      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el);
      }
    }
  }
});

// 使用示例
<template>
  <div>
    <button v-permission="['system:user:create']">新增用户</button>
    <button v-permission="['system:user:update']">编辑用户</button>
    <button v-permission="['system:user:delete']">删除用户</button>
  </div>
</template>
```

### 3.2 权限检查函数

```javascript
// 权限检查工具函数
export function hasPermission(permission) {
  const permissions = store.getters.permissions;
  return permissions.includes(permission);
}

export function hasAnyPermission(permissions) {
  const userPermissions = store.getters.permissions;
  return permissions.some(permission => userPermissions.includes(permission));
}

export function hasAllPermissions(permissions) {
  const userPermissions = store.getters.permissions;
  return permissions.every(permission => userPermissions.includes(permission));
}

// 使用示例
import { hasPermission } from '@/utils/permission';

export default {
  computed: {
    canCreateUser() {
      return hasPermission('system:user:create');
    },
    canUpdateUser() {
      return hasPermission('system:user:update');
    }
  }
};
```

## 4. 配置建议

### 4.1 应用配置

```yaml
# application.yml
permission:
  inheritance:
    # 启用权限继承
    enabled: true
    # 默认继承策略
    default-strategy: ADDITIVE
    # 默认合并策略
    default-merge-strategy: UNION
    # 启用缓存
    enable-cache: true
    # 缓存时间（分钟）
    cache-time-minutes: 30
    # 超级管理员自动授权（生产环境建议关闭）
    super-admin-auto-grant: false
    # 权限检查日志
    enable-audit-log: true
```

### 4.2 缓存配置

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());
        
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
```

## 5. 测试建议

### 5.1 单元测试

```java
@ExtendWith(MockitoExtension.class)
class PermissionInheritanceServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private RoleDeptRepository roleDeptRepository;
    
    @InjectMocks
    private PermissionInheritanceService permissionInheritanceService;
    
    @Test
    void testCalculateUserPermissions() {
        // Given
        Long userId = 1L;
        User user = createTestUser();
        List<Role> roles = createTestRoles();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByUserId(userId)).thenReturn(roles);
        
        // When
        PermissionSet result = permissionInheritanceService.calculateUserPermissions(userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.hasPermission("system:user:list")).isTrue();
    }
    
    @Test
    void testDataScopePermissions() {
        // Given
        Long userId = 1L;
        Long deptId = 100L;
        
        // When
        boolean hasPermission = permissionInheritanceService.hasDeptDataPermission(userId, deptId);
        
        // Then
        assertThat(hasPermission).isTrue();
    }
}
```

### 5.2 集成测试

```java
@SpringBootTest
@Transactional
class PermissionIntegrationTest {
    
    @Autowired
    private PermissionApiService permissionApiService;
    
    @Autowired
    private TestDataBuilder testDataBuilder;
    
    @Test
    void testUserPermissionFlow() {
        // 1. 创建测试数据
        User user = testDataBuilder.createUser();
        Role role = testDataBuilder.createRole();
        testDataBuilder.assignRoleToUser(user.getId(), role.getId());
        
        // 2. 测试权限计算
        PermissionSet permissions = permissionApiService.calculateUserPermissions(user.getId());
        assertThat(permissions).isNotNull();
        
        // 3. 测试权限检查
        boolean hasPermission = permissionApiService.hasPermission(user.getId(), "system:user:list");
        assertThat(hasPermission).isTrue();
        
        // 4. 测试数据权限
        Set<DataScope> dataScopes = permissionApiService.getUserDataScopes(user.getId());
        assertThat(dataScopes).isNotEmpty();
    }
}
```

## 6. 监控和运维

### 6.1 权限使用监控

```java
@Component
public class PermissionMonitor {
    
    private final MeterRegistry meterRegistry;
    private final Counter permissionCheckCounter;
    private final Timer permissionCalculationTimer;
    
    public PermissionMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.permissionCheckCounter = Counter.builder("permission.check.total")
            .description("Total permission checks")
            .register(meterRegistry);
        this.permissionCalculationTimer = Timer.builder("permission.calculation.time")
            .description("Permission calculation time")
            .register(meterRegistry);
    }
    
    public void recordPermissionCheck(String permission, boolean granted) {
        permissionCheckCounter.increment(
            Tags.of(
                Tag.of("permission", permission),
                Tag.of("granted", String.valueOf(granted))
            )
        );
    }
    
    public Timer.Sample startPermissionCalculation() {
        return Timer.start(meterRegistry);
    }
}
```

### 6.2 健康检查

```java
@Component
public class PermissionHealthIndicator implements HealthIndicator {
    
    @Autowired
    private PermissionApiService permissionApiService;
    
    @Override
    public Health health() {
        try {
            // 测试权限服务是否正常
            Long testUserId = 1L;
            permissionApiService.calculateUserPermissions(testUserId);
            
            return Health.up()
                .withDetail("permission-service", "Available")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("permission-service", "Unavailable")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 7. 总结

通过以上集成方案，可以在项目中有效地使用 `PermissionInheritanceService`：

1. **分层集成**：在控制器、服务、数据访问层分别集成权限检查
2. **工具支持**：提供注解、切面、工具类等便利工具
3. **前端配合**：前端权限指令和检查函数
4. **配置优化**：合理的缓存和配置策略
5. **测试保障**：完善的单元测试和集成测试
6. **监控运维**：权限使用监控和健康检查

这样可以构建一个完整、可靠的权限管理体系。