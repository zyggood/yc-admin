package com.yc.admin.system.permission.service;

import com.yc.admin.system.permission.enums.DataScope;
import com.yc.admin.system.user.entity.User;
import com.yc.admin.system.user.repository.UserRepository;
import com.yc.admin.system.dept.entity.Dept;
import com.yc.admin.system.dept.repository.DeptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 数据权限服务测试类
 * 
 * @author YC
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DataPermissionServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private DeptRepository deptRepository;
    
    @Mock
    private UserDataPermissionService userDataPermissionService;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private DataPermissionService dataPermissionService;
    
    private User testUser;
    private Dept testDept;
    
    @BeforeEach
    void setUp() {
        // 设置测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testuser");
        testUser.setDeptId(100L);
        
        // 设置测试部门
        testDept = new Dept();
        testDept.setId(100L);
        testDept.setDeptName("测试部门");
        testDept.setParentId(0L);
        
        // 模拟Spring Security上下文
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("testuser");
    }
    
    /**
     * 创建测试用的部门对象
     */
    private Dept createDept(Long id, String name, Long parentId) {
        Dept dept = new Dept();
        dept.setId(id);
        dept.setDeptName(name);
        dept.setParentId(parentId);
        return dept;
    }
    
    @Test
    void testGetCurrentUserId() {
        // 准备数据
        when(userRepository.findByUserName("testuser"))
                .thenReturn(Optional.of(testUser));
        
        // 执行测试
        Long userId = dataPermissionService.getCurrentUserId();
        
        // 验证结果
        assertEquals(1L, userId);
        verify(userRepository).findByUserNameAndDelFlag("testuser", 0);
    }
    
    @Test
    void testGetCurrentUserIdWhenUserNotFound() {
        // 准备数据
        when(userRepository.findByUserName("testuser"))
                .thenReturn(Optional.empty());
        
        // 执行测试
        Long userId = dataPermissionService.getCurrentUserId();
        
        // 验证结果
        assertNull(userId);
    }
    
    @Test
    void testGetCurrentUserDeptId() {
        // 准备数据
        when(userRepository.findByUserName("testuser"))
                .thenReturn(Optional.of(testUser));
        
        // 执行测试
        Long deptId = dataPermissionService.getCurrentUserDeptId();
        
        // 验证结果
        assertEquals(100L, deptId);
    }
    
    @Test
    void testIsSuperAdmin() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(testUser.isAdmin()).thenReturn(true);
        
        // 执行测试
        boolean isSuperAdmin = dataPermissionService.isSuperAdmin(1L);
        
        // 验证结果
        assertTrue(isSuperAdmin);
    }
    
    @Test
    void testGetUserDataPermissionScope() {
        // 准备数据
        when(userDataPermissionService.getUserDataPermissionScope(1L))
                .thenReturn(DataScope.DEPT_AND_CHILD);
        
        // 执行测试
        DataScope scope = dataPermissionService.getUserDataScope(1L);
        
        // 验证结果
        assertEquals(DataScope.DEPT_AND_CHILD, scope);
        verify(userDataPermissionService).getUserDataPermissionScope(1L);
    }
    
    @Test
    void testGetDeptAndChildrenIds() {
        // 准备数据
        List<Long> expectedIds = Arrays.asList(100L, 101L, 102L);
        List<Dept> childDepts = Arrays.asList(
            createDept(101L, "子部门1", 100L),
            createDept(102L, "子部门2", 100L)
        );
        when(deptRepository.findByParentIdAndDelFlagOrderByOrderNumAsc(100L, 0))
                .thenReturn(childDepts);
        
        // 执行测试
        List<Long> deptIds = dataPermissionService.getDeptAndChildrenIds(100L);
        
        // 验证结果
        assertFalse(deptIds.isEmpty());
        assertTrue(deptIds.contains(100L)); // 包含自己
    }
    
    @Test
    void testGetAccessibleDeptIds() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDataPermissionService.getUserDataPermissionScope(1L))
                .thenReturn(DataScope.DEPT_AND_CHILD);
        
        List<Dept> childDepts = Arrays.asList(
            createDept(101L, "子部门1", 100L),
            createDept(102L, "子部门2", 100L)
        );
        when(deptRepository.findByParentIdAndDelFlagOrderByOrderNumAsc(100L, 0))
                .thenReturn(childDepts);
        
        // 执行测试
        List<Long> accessibleIds = dataPermissionService.getAccessibleDeptIds(1L);
        
        // 验证结果
        assertFalse(accessibleIds.isEmpty());
    }
    
    @Test
    void testGetAccessibleDeptIdsForSuperAdmin() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(testUser.isAdmin()).thenReturn(true);
        
        // 执行测试
        List<Long> accessibleIds = dataPermissionService.getAccessibleDeptIds(1L);
        
        // 验证结果
        assertTrue(accessibleIds.isEmpty()); // 超级管理员返回空列表，表示无限制
    }
    
    @Test
    void testGetAccessibleDeptIdsForCustomScope() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDataPermissionService.getUserDataPermissionScope(1L))
                .thenReturn(DataScope.CUSTOM);
        
        List<Long> customDeptIds = Arrays.asList(200L, 201L);
        when(userDataPermissionService.getUserCustomDeptIds(1L)).thenReturn(customDeptIds);
        
        // 执行测试
        List<Long> accessibleIds = dataPermissionService.getAccessibleDeptIds(1L);
        
        // 验证结果
        assertEquals(customDeptIds, accessibleIds);
        verify(userDataPermissionService).getUserCustomDeptIds(1L);
    }
    
    @Test
    void testHasUserDeptAccess() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDataPermissionService.getUserDataPermissionScope(1L))
                .thenReturn(DataScope.DEPT_AND_CHILD);
        
        List<Dept> childDepts = Arrays.asList(
            createDept(101L, "子部门1", 100L),
            createDept(102L, "子部门2", 100L)
        );
        when(deptRepository.findByParentIdAndDelFlagOrderByOrderNumAsc(100L, 0))
                .thenReturn(childDepts);
        
        // 执行测试
        boolean hasAccess = dataPermissionService.hasAccessToDept(1L, 101L);
        
        // 验证结果
        assertTrue(hasAccess);
    }
    
    @Test
    void testHasUserDeptAccessForSuperAdmin() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(testUser.isAdmin()).thenReturn(true);
        
        // 执行测试
        boolean hasAccess = dataPermissionService.hasAccessToDept(1L, 999L);
        
        // 验证结果
        assertTrue(hasAccess); // 超级管理员有所有部门访问权限
    }
}