package com.yc.admin.system.dept.service;

import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.system.dept.entity.Dept;
import com.yc.admin.system.dept.repository.DeptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DeptService单元测试类
 * 测试部门管理功能包括树形结构构建、增删改查等
 * 
 * @author admin
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class DeptServiceTest {

    @Mock
    private DeptRepository deptRepository;

    @InjectMocks
    private DeptService deptService;

    private Dept rootDept;
    private Dept childDept1;
    private Dept childDept2;
    private Dept grandChildDept;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        rootDept = createDept(1L, 0L, "总公司", "0", 0, 0);
        childDept1 = createDept(2L, 1L, "技术部", "0,1", 1, 0);
        childDept2 = createDept(3L, 1L, "市场部", "0,1", 2, 0);
        grandChildDept = createDept(4L, 2L, "开发组", "0,1,2", 1, 0);
    }

    private Dept createDept(Long id, Long parentId, String deptName, String ancestors, Integer orderNum, Integer status) {
        Dept dept = new Dept();
        dept.setId(id);
        dept.setParentId(parentId);
        dept.setDeptName(deptName);
        dept.setAncestors(ancestors);
        dept.setOrderNum(orderNum);
        dept.setStatus(status);
        dept.setDelFlag(0);
        return dept;
    }

    @Test
    @DisplayName("查询部门树 - 正常情况")
    void testSelectDeptTree_Success() {
        // Given
        List<Dept> deptList = Arrays.asList(rootDept, childDept1, childDept2, grandChildDept);
        when(deptRepository.findByDelFlagOrderByOrderNumAsc(0)).thenReturn(deptList);

        // When
        List<Dept> result = deptService.selectDeptTree();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("总公司", result.get(0).getDeptName());
        assertEquals(2, result.get(0).getChildren().size());
        verify(deptRepository).findByDelFlagOrderByOrderNumAsc(0);
    }

    @Test
    @DisplayName("查询正常状态部门树 - 正常情况")
    void testSelectDeptTreeNormal_Success() {
        // Given
        List<Dept> deptList = Arrays.asList(rootDept, childDept1, childDept2);
        when(deptRepository.findByDelFlagAndStatusOrderByOrderNumAsc(0, 0)).thenReturn(deptList);

        // When
        List<Dept> result = deptService.selectDeptTreeNormal();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("总公司", result.get(0).getDeptName());
        verify(deptRepository).findByDelFlagAndStatusOrderByOrderNumAsc(0, 0);
    }

    @Test
    @DisplayName("根据ID查询部门 - 存在")
    void testSelectDeptById_Exists() {
        // Given
        when(deptRepository.findById(1L)).thenReturn(Optional.of(rootDept));

        // When
        Dept result = deptService.selectDeptById(1L);

        // Then
        assertNotNull(result);
        assertEquals("总公司", result.getDeptName());
        verify(deptRepository).findById(1L);
    }

    @Test
    @DisplayName("根据ID查询部门 - 不存在")
    void testSelectDeptById_NotExists() {
        // Given
        when(deptRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Dept result = deptService.selectDeptById(999L);

        // Then
        assertNull(result);
        verify(deptRepository).findById(999L);
    }

    @Test
    @DisplayName("查询子部门ID列表 - 正常情况")
    void testSelectChildrenIdsByDeptId_Success() {
        // Given
        List<Long> childrenIds = Arrays.asList(2L, 3L, 4L);
        when(deptRepository.findChildrenIdsByDeptId(1L, 0)).thenReturn(childrenIds);

        // When
        List<Long> result = deptService.selectChildrenIdsByDeptId(1L);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(2L));
        assertTrue(result.contains(3L));
        assertTrue(result.contains(4L));
        verify(deptRepository).findChildrenIdsByDeptId(1L, 0);
    }

    @Test
    @DisplayName("新增部门 - 正常情况")
    void testInsertDept_Success() {
        // Given
        Dept newDept = createDept(null, 1L, "人事部", null, 3, 0);
        when(deptRepository.existsByDeptNameAndIdNotAndDelFlag("人事部", -1L, 0)).thenReturn(false);
        when(deptRepository.findById(1L)).thenReturn(Optional.of(rootDept));
        when(deptRepository.save(any(Dept.class))).thenReturn(newDept);

        // When
        int result = deptService.insertDept(newDept);

        // Then
        assertEquals(1, result);
        assertEquals("0,1", newDept.getAncestors());
        verify(deptRepository).existsByDeptNameAndIdNotAndDelFlag("人事部", -1L, 0);
        verify(deptRepository).findById(1L);
        verify(deptRepository).save(newDept);
    }

    @Test
    @DisplayName("新增部门 - 部门名称已存在")
    void testInsertDept_DeptNameExists() {
        // Given
        Dept newDept = createDept(null, 1L, "技术部", null, 3, 0);
        when(deptRepository.existsByDeptNameAndIdNotAndDelFlag("技术部", -1L, 0)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> deptService.insertDept(newDept));
        assertEquals("新增部门'技术部'失败，部门名称已存在", exception.getMessage());
        verify(deptRepository).existsByDeptNameAndIdNotAndDelFlag("技术部", -1L, 0);
        verify(deptRepository, never()).save(any());
    }

    @Test
    @DisplayName("新增部门 - 父部门不存在")
    void testInsertDept_ParentNotExists() {
        // Given
        Dept newDept = createDept(null, 999L, "人事部", null, 3, 0);
        when(deptRepository.existsByDeptNameAndIdNotAndDelFlag("人事部", -1L, 0)).thenReturn(false);
        when(deptRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> deptService.insertDept(newDept));
        assertEquals("父部门不存在", exception.getMessage());
        verify(deptRepository).existsByDeptNameAndIdNotAndDelFlag("人事部", -1L, 0);
        verify(deptRepository).findById(999L);
        verify(deptRepository, never()).save(any());
    }

    @Test
    @DisplayName("新增部门 - 父部门已停用")
    void testInsertDept_ParentDisabled() {
        // Given
        Dept disabledParent = createDept(1L, 0L, "总公司", "0", 0, 1); // status = 1 表示停用
        Dept newDept = createDept(null, 1L, "人事部", null, 3, 0);
        when(deptRepository.existsByDeptNameAndIdNotAndDelFlag("人事部", -1L, 0)).thenReturn(false);
        when(deptRepository.findById(1L)).thenReturn(Optional.of(disabledParent));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> deptService.insertDept(newDept));
        assertEquals("父部门已停用，不允许新增", exception.getMessage());
        verify(deptRepository).existsByDeptNameAndIdNotAndDelFlag("人事部", -1L, 0);
        verify(deptRepository).findById(1L);
        verify(deptRepository, never()).save(any());
    }

    @Test
    @DisplayName("新增顶级部门 - 正常情况")
    void testInsertDept_TopLevel() {
        // Given
        Dept newDept = createDept(null, 0L, "分公司", null, 1, 0);
        when(deptRepository.existsByDeptNameAndIdNotAndDelFlag("分公司", -1L, 0)).thenReturn(false);
        when(deptRepository.save(any(Dept.class))).thenReturn(newDept);

        // When
        int result = deptService.insertDept(newDept);

        // Then
        assertEquals(1, result);
        assertEquals(0L, newDept.getParentId());
        assertEquals("0", newDept.getAncestors());
        verify(deptRepository).existsByDeptNameAndIdNotAndDelFlag("分公司", -1L, 0);
        verify(deptRepository).save(newDept);
    }

    @Test
    @DisplayName("修改部门 - 正常情况")
    void testUpdateDept_Success() {
        // Given
        Dept updateDept = createDept(2L, 1L, "技术部门", "0,1", 1, 0);
        when(deptRepository.existsByDeptNameAndIdNotAndDelFlag("技术部门", 2L, 0)).thenReturn(false);
        when(deptRepository.findById(2L)).thenReturn(Optional.of(childDept1));
        when(deptRepository.save(any(Dept.class))).thenReturn(updateDept);

        // When
        int result = deptService.updateDept(updateDept);

        // Then
        assertEquals(1, result);
        verify(deptRepository).existsByDeptNameAndIdNotAndDelFlag("技术部门", 2L, 0);
        verify(deptRepository).findById(2L);
        verify(deptRepository).save(updateDept);
    }

    @Test
    @DisplayName("修改部门 - 部门名称已存在")
    void testUpdateDept_DeptNameExists() {
        // Given
        Dept updateDept = createDept(2L, 1L, "市场部", "0,1", 1, 0);
        when(deptRepository.existsByDeptNameAndIdNotAndDelFlag("市场部", 2L, 0)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> deptService.updateDept(updateDept));
        assertEquals("修改部门'市场部'失败，部门名称已存在", exception.getMessage());
        verify(deptRepository).existsByDeptNameAndIdNotAndDelFlag("市场部", 2L, 0);
        verify(deptRepository, never()).save(any());
    }

    @Test
    @DisplayName("修改部门 - 部门不存在")
    void testUpdateDept_DeptNotExists() {
        // Given
        Dept updateDept = createDept(999L, 1L, "不存在部门", "0,1", 1, 0);
        when(deptRepository.existsByDeptNameAndIdNotAndDelFlag("不存在部门", 999L, 0)).thenReturn(false);
        when(deptRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> deptService.updateDept(updateDept));
        assertEquals("部门不存在", exception.getMessage());
        verify(deptRepository).existsByDeptNameAndIdNotAndDelFlag("不存在部门", 999L, 0);
        verify(deptRepository).findById(999L);
        verify(deptRepository, never()).save(any());
    }

    @Test
    @DisplayName("修改部门 - 设置为自己的子部门")
    void testUpdateDept_SetAsOwnChild() {
        // Given
        Dept updateDept = createDept(1L, 2L, "总公司", "0,2", 0, 0); // 将总公司设置为技术部的子部门
        List<Long> childrenIds = Arrays.asList(2L, 3L, 4L);
        when(deptRepository.existsByDeptNameAndIdNotAndDelFlag("总公司", 1L, 0)).thenReturn(false);
        when(deptRepository.findById(1L)).thenReturn(Optional.of(rootDept));
        when(deptRepository.findChildrenIdsByDeptId(1L, 0)).thenReturn(childrenIds);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> deptService.updateDept(updateDept));
        assertEquals("修改部门失败，不能将部门设置为自己的子部门", exception.getMessage());
        verify(deptRepository).existsByDeptNameAndIdNotAndDelFlag("总公司", 1L, 0);
        verify(deptRepository).findById(1L);
        verify(deptRepository).findChildrenIdsByDeptId(1L, 0);
        verify(deptRepository, never()).save(any());
    }

    @Test
    @DisplayName("删除部门 - 正常情况")
    void testDeleteDeptById_Success() {
        // Given
        when(deptRepository.existsByParentIdAndDelFlag(3L, 0)).thenReturn(false);
        when(deptRepository.findById(3L)).thenReturn(Optional.of(childDept2));
        when(deptRepository.save(any(Dept.class))).thenReturn(childDept2);

        // When
        int result = deptService.deleteDeptById(3L);

        // Then
        assertEquals(1, result);
        verify(deptRepository).existsByParentIdAndDelFlag(3L, 0);
        verify(deptRepository).findById(3L);
        verify(deptRepository).save(any(Dept.class));
    }

    @Test
    @DisplayName("删除部门 - 存在子部门")
    void testDeleteDeptById_HasChildren() {
        // Given
        when(deptRepository.existsByParentIdAndDelFlag(1L, 0)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> deptService.deleteDeptById(1L));
        assertEquals("存在下级部门，不允许删除", exception.getMessage());
        verify(deptRepository).existsByParentIdAndDelFlag(1L, 0);
        verify(deptRepository, never()).findById(any());
        verify(deptRepository, never()).save(any());
    }

    @Test
    @DisplayName("删除部门 - 部门不存在")
    void testDeleteDeptById_DeptNotExists() {
        // Given
        when(deptRepository.existsByParentIdAndDelFlag(999L, 0)).thenReturn(false);
        when(deptRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        int result = deptService.deleteDeptById(999L);

        // Then
        assertEquals(0, result);
        verify(deptRepository).existsByParentIdAndDelFlag(999L, 0);
        verify(deptRepository).findById(999L);
        verify(deptRepository, never()).save(any());
    }

    @Test
    @DisplayName("检查部门名称唯一性 - 新增时唯一")
    void testCheckDeptNameUnique_NewDeptUnique() {
        // Given
        Dept newDept = createDept(null, 1L, "财务部", null, 3, 0);
        when(deptRepository.existsByDeptNameAndIdNotAndDelFlag("财务部", -1L, 0)).thenReturn(false);

        // When
        boolean result = deptService.checkDeptNameUnique(newDept);

        // Then
        assertTrue(result);
        verify(deptRepository).existsByDeptNameAndIdNotAndDelFlag("财务部", -1L, 0);
    }

    @Test
    @DisplayName("检查部门名称唯一性 - 新增时不唯一")
    void testCheckDeptNameUnique_NewDeptNotUnique() {
        // Given
        Dept newDept = createDept(null, 1L, "技术部", null, 3, 0);
        when(deptRepository.existsByDeptNameAndIdNotAndDelFlag("技术部", -1L, 0)).thenReturn(true);

        // When
        boolean result = deptService.checkDeptNameUnique(newDept);

        // Then
        assertFalse(result);
        verify(deptRepository).existsByDeptNameAndIdNotAndDelFlag("技术部", -1L, 0);
    }

    @Test
    @DisplayName("检查部门名称唯一性 - 修改时唯一")
    void testCheckDeptNameUnique_UpdateDeptUnique() {
        // Given
        Dept updateDept = createDept(2L, 1L, "技术部门", "0,1", 1, 0);
        when(deptRepository.existsByDeptNameAndIdNotAndDelFlag("技术部门", 2L, 0)).thenReturn(false);

        // When
        boolean result = deptService.checkDeptNameUnique(updateDept);

        // Then
        assertTrue(result);
        verify(deptRepository).existsByDeptNameAndIdNotAndDelFlag("技术部门", 2L, 0);
    }

    @Test
    @DisplayName("检查是否有子部门 - 有子部门")
    void testHasChildByDeptId_HasChild() {
        // Given
        when(deptRepository.existsByParentIdAndDelFlag(1L, 0)).thenReturn(true);

        // When
        boolean result = deptService.hasChildByDeptId(1L);

        // Then
        assertTrue(result);
        verify(deptRepository).existsByParentIdAndDelFlag(1L, 0);
    }

    @Test
    @DisplayName("检查是否有子部门 - 无子部门")
    void testHasChildByDeptId_NoChild() {
        // Given
        when(deptRepository.existsByParentIdAndDelFlag(3L, 0)).thenReturn(false);

        // When
        boolean result = deptService.hasChildByDeptId(3L);

        // Then
        assertFalse(result);
        verify(deptRepository).existsByParentIdAndDelFlag(3L, 0);
    }

    @Test
    @DisplayName("更新部门状态 - 正常情况")
    void testUpdateDeptStatus_Success() {
        // Given
        when(deptRepository.findById(3L)).thenReturn(Optional.of(childDept2));
        when(deptRepository.save(any(Dept.class))).thenReturn(childDept2);

        // When
        int result = deptService.updateDeptStatus(3L, 1);

        // Then
        assertEquals(1, result);
        verify(deptRepository).findById(3L);
        verify(deptRepository).save(any(Dept.class));
    }

    @Test
    @DisplayName("更新部门状态 - 部门不存在")
    void testUpdateDeptStatus_DeptNotExists() {
        // Given
        when(deptRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> deptService.updateDeptStatus(999L, 1));
        assertEquals("部门不存在", exception.getMessage());
        verify(deptRepository).findById(999L);
        verify(deptRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新部门状态 - 停用时有子部门")
    void testUpdateDeptStatus_DisableWithChildren() {
        // Given
        when(deptRepository.findById(1L)).thenReturn(Optional.of(rootDept));
        when(deptRepository.existsByParentIdAndDelFlag(1L, 0)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> deptService.updateDeptStatus(1L, 1));
        assertEquals("该部门包含未停用的子部门！", exception.getMessage());
        verify(deptRepository).findById(1L);
        verify(deptRepository).existsByParentIdAndDelFlag(1L, 0);
        verify(deptRepository, never()).save(any());
    }

    @Test
    @DisplayName("构建部门树 - 正常情况")
    void testBuildDeptTree_Success() {
        // Given
        List<Dept> deptList = Arrays.asList(rootDept, childDept1, childDept2, grandChildDept);

        // When
        List<Dept> result = deptService.buildDeptTree(deptList);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Dept root = result.get(0);
        assertEquals("总公司", root.getDeptName());
        assertEquals(2, root.getChildren().size());
        
        // 验证子部门
        List<Dept> children = root.getChildren();
        assertEquals("技术部", children.get(0).getDeptName());
        assertEquals("市场部", children.get(1).getDeptName());
        
        // 验证孙子部门
        assertEquals(1, children.get(0).getChildren().size());
        assertEquals("开发组", children.get(0).getChildren().get(0).getDeptName());
    }

    @Test
    @DisplayName("构建部门树 - 空列表")
    void testBuildDeptTree_EmptyList() {
        // Given
        List<Dept> deptList = new ArrayList<>();

        // When
        List<Dept> result = deptService.buildDeptTree(deptList);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("构建部门树选择 - 正常情况")
    void testBuildDeptTreeSelect_Success() {
        // Given
        List<Dept> deptList = Arrays.asList(rootDept, childDept1, childDept2);

        // When
        List<Dept> result = deptService.buildDeptTreeSelect(deptList);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("总公司", result.get(0).getDeptName());
    }

    @Test
    @DisplayName("根据角色ID查询部门树 - 正常情况")
    void testSelectDeptTreeByRoleId_Success() {
        // Given
        List<Dept> deptList = Arrays.asList(rootDept, childDept1, childDept2);
        when(deptRepository.findByDelFlagAndStatusOrderByOrderNumAsc(0, 0)).thenReturn(deptList);

        // When
        List<Dept> result = deptService.selectDeptTreeByRoleId(1L);

        // Then
        assertNotNull(result);
        verify(deptRepository).findByDelFlagAndStatusOrderByOrderNumAsc(0, 0);
    }

    @Test
    @DisplayName("根据用户ID查询部门树 - 正常情况")
    void testSelectDeptTreeByUserId_Success() {
        // Given
        List<Dept> deptList = Arrays.asList(rootDept, childDept1, childDept2);
        when(deptRepository.findByDelFlagAndStatusOrderByOrderNumAsc(0, 0)).thenReturn(deptList);

        // When
        List<Dept> result = deptService.selectDeptTreeByUserId(1L);

        // Then
        assertNotNull(result);
        verify(deptRepository).findByDelFlagAndStatusOrderByOrderNumAsc(0, 0);
    }

    @Test
    @DisplayName("检查数据权限 - 正常情况")
    void testCheckDeptDataScope_Success() {
        // When
        boolean result = deptService.checkDeptDataScope(1L);

        // Then
        assertTrue(result); // 当前实现总是返回true
    }

    @Test
    @DisplayName("边界条件 - 空ID查询")
    void testSelectDeptById_NullId() {
        // Given
        when(deptRepository.findById(null)).thenThrow(new IllegalArgumentException("ID不能为空"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> deptService.selectDeptById(null));
    }

    @Test
    @DisplayName("边界条件 - 负数ID查询")
    void testSelectDeptById_NegativeId() {
        // Given
        when(deptRepository.findById(-1L)).thenReturn(Optional.empty());

        // When
        Dept result = deptService.selectDeptById(-1L);

        // Then
        assertNull(result);
        verify(deptRepository).findById(-1L);
    }

    @Test
    @DisplayName("边界条件 - 新增部门时设置默认值")
    void testInsertDept_DefaultValues() {
        // Given
        Dept newDept = new Dept();
        newDept.setDeptName("测试部门");
        newDept.setParentId(0L);
        // 不设置orderNum和status，测试默认值
        
        when(deptRepository.existsByDeptNameAndIdNotAndDelFlag("测试部门", -1L, 0)).thenReturn(false);
        when(deptRepository.save(any(Dept.class))).thenReturn(newDept);

        // When
        int result = deptService.insertDept(newDept);

        // Then
        assertEquals(1, result);
        assertEquals(Integer.valueOf(0), newDept.getOrderNum());
        assertEquals(Integer.valueOf(0), newDept.getStatus());
        assertEquals("0", newDept.getAncestors());
        verify(deptRepository).save(newDept);
    }
}