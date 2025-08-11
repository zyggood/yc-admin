package com.yc.admin.dept.service;

import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.dept.entity.Dept;
import com.yc.admin.dept.repository.DeptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务类
 * 
 * @author admin
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptService {

    private final DeptRepository deptRepository;

    public List<Dept> selectDeptTree() {
        List<Dept> deptList = deptRepository.findByDelFlagOrderByOrderNumAsc(0);
        return buildDeptTree(deptList);
    }

    public List<Dept> selectDeptTreeNormal() {
        List<Dept> deptList = deptRepository.findByDelFlagAndStatusOrderByOrderNumAsc(0, 0);
        return buildDeptTree(deptList);
    }

    public Dept selectDeptById(Long deptId) {
        return deptRepository.findById(deptId).orElse(null);
    }

    public List<Long> selectChildrenIdsByDeptId(Long deptId) {
        return deptRepository.findChildrenIdsByDeptId(deptId, 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertDept(Dept dept) {
        // 校验部门名称唯一性
        if (!checkDeptNameUnique(dept)) {
            throw new BusinessException("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }

        // 设置祖级列表
        if (dept.getParentId() != null && dept.getParentId() != 0) {
            Dept parentDept = selectDeptById(dept.getParentId());
            if (parentDept == null) {
                throw new BusinessException("父部门不存在");
            }
            if (!Integer.valueOf(0).equals(parentDept.getStatus())) {
                throw new BusinessException("父部门已停用，不允许新增");
            }
            dept.buildAncestors(parentDept.getAncestors(), dept.getParentId());
        } else {
            dept.setParentId(0L);
            dept.setAncestors("0");
        }

        // 设置默认值
        if (dept.getOrderNum() == null) {
            dept.setOrderNum(0);
        }
        if (dept.getStatus() == null) {
            dept.setStatus(0);
        }
        // delFlag字段由BaseEntity自动处理，无需手动设置

        deptRepository.save(dept);
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateDept(Dept dept) {
        // 校验部门名称唯一性
        if (!checkDeptNameUnique(dept)) {
            throw new BusinessException("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }

        Dept oldDept = selectDeptById(dept.getId());
        if (oldDept == null) {
            throw new BusinessException("部门不存在");
        }

        // 如果父部门发生变化，需要更新祖级列表
        if (!oldDept.getParentId().equals(dept.getParentId())) {
            // 不能将部门设置为自己的子部门
            List<Long> childrenIds = selectChildrenIdsByDeptId(dept.getId());
            if (childrenIds.contains(dept.getParentId())) {
                throw new BusinessException("修改部门失败，不能将部门设置为自己的子部门");
            }

            // 构建新的祖级列表
            String oldAncestors = oldDept.getAncestors();
            if (dept.getParentId() != null && dept.getParentId() != 0) {
                Dept parentDept = selectDeptById(dept.getParentId());
                if (parentDept == null) {
                    throw new BusinessException("父部门不存在");
                }
                if (!Integer.valueOf(0).equals(parentDept.getStatus())) {
                    throw new BusinessException("父部门已停用，不允许修改");
                }
                dept.buildAncestors(parentDept.getAncestors(), dept.getParentId());
            } else {
                dept.setParentId(0L);
                dept.setAncestors("0");
            }

            // 更新子部门的祖级列表
            String newAncestors = dept.getAncestors();
            deptRepository.updateChildrenAncestors(oldAncestors + "," + dept.getId(), newAncestors + "," + dept.getId());
        }

        deptRepository.save(dept);
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteDeptById(Long deptId) {
        // 检查是否存在子部门
        if (hasChildByDeptId(deptId)) {
            throw new BusinessException("存在下级部门，不允许删除");
        }

        // 检查部门是否存在用户
        // TODO: 这里需要根据实际的用户表来检查
        // if (checkDeptExistUser(deptId)) {
        //     throw new BusinessException("部门存在用户，不允许删除");
        // }

        Dept dept = selectDeptById(deptId);
        if (dept != null) {
            dept.markDeleted(); // 使用BaseEntity提供的方法
            deptRepository.save(dept);
            return 1;
        }
        return 0;
    }

    public boolean checkDeptNameUnique(Dept dept) {
        Long deptId = dept.getId() == null ? -1L : dept.getId();
        return !deptRepository.existsByDeptNameAndIdNotAndDelFlag(dept.getDeptName(), deptId, 0);
    }

    public boolean hasChildByDeptId(Long deptId) {
        return deptRepository.existsByParentIdAndDelFlag(deptId, 0);
    }

    public boolean checkDeptDataScope(Long deptId) {
        // TODO: 实现数据权限校验逻辑
        // 这里需要根据当前用户的数据权限来判断是否有权限操作该部门
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateDeptStatus(Long deptId, Integer status) {
        Dept dept = selectDeptById(deptId);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        // 如果停用部门，需要检查是否有子部门
        if (Integer.valueOf(1).equals(status) && hasChildByDeptId(deptId)) {
            throw new BusinessException("该部门包含未停用的子部门！");
        }

        dept.setStatus(status);
        deptRepository.save(dept);
        return 1;
    }

    public List<Dept> selectDeptTreeByRoleId(Long roleId) {
        // TODO: 根据角色ID查询部门树
        // 这里需要根据角色权限来过滤部门
        return selectDeptTreeNormal();
    }

    public List<Dept> selectDeptTreeByUserId(Long userId) {
        // TODO: 根据用户ID查询部门树
        // 这里需要根据用户的数据权限来过滤部门
        return selectDeptTreeNormal();
    }

    public List<Dept> buildDeptTree(List<Dept> depts) {
        List<Dept> returnList = new ArrayList<>();
        List<Long> tempList = depts.stream().map(Dept::getId).collect(Collectors.toList());
        
        for (Dept dept : depts) {
            // 如果是顶级节点，遍历该父节点的所有子节点
            if (!tempList.contains(dept.getParentId())) {
                recursionFn(depts, dept);
                returnList.add(dept);
            }
        }
        
        if (returnList.isEmpty()) {
            returnList = depts;
        }
        return returnList;
    }

    public List<Dept> buildDeptTreeSelect(List<Dept> depts) {
        List<Dept> deptTrees = buildDeptTree(depts);
        return deptTrees;
    }

    /**
     * 递归列表
     * @param list 分类表
     * @param dept 子节点
     */
    private void recursionFn(List<Dept> list, Dept dept) {
        // 得到子节点列表
        List<Dept> childList = getChildList(list, dept);
        dept.setChildren(childList);
        for (Dept tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<Dept> getChildList(List<Dept> list, Dept dept) {
        List<Dept> tlist = new ArrayList<>();
        Iterator<Dept> it = list.iterator();
        while (it.hasNext()) {
            Dept n = it.next();
            if (n.getParentId() != null && n.getParentId().longValue() == dept.getId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<Dept> list, Dept dept) {
        return getChildList(list, dept).size() > 0;
    }
}