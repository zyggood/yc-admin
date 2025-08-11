package com.yc.admin.dept.controller;

import com.yc.admin.common.core.Result;
import com.yc.admin.dept.entity.Dept;
import com.yc.admin.dept.service.DeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 * 
 * @author admin
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    /**
     * 获取部门树列表
     */
    @GetMapping("/tree")
    public Result<List<Dept>> tree() {
        List<Dept> depts = deptService.selectDeptTree();
        return Result.success(depts);
    }

    /**
     * 获取部门树列表（仅正常状态）
     */
    @GetMapping("/tree/normal")
    public Result<List<Dept>> treeNormal() {
        List<Dept> depts = deptService.selectDeptTreeNormal();
        return Result.success(depts);
    }

    /**
     * 根据部门编号获取详细信息
     */
    @GetMapping(value = "/{deptId}")
    public Result<Dept> getInfo(@PathVariable Long deptId) {
        Dept dept = deptService.selectDeptById(deptId);
        return Result.success(dept);
    }

    /**
     * 新增部门
     */
    @PostMapping
    public Result<Void> add(@Validated @RequestBody Dept dept) {
        if (!deptService.checkDeptNameUnique(dept)) {
            return Result.error("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        deptService.insertDept(dept);
        return Result.success();
    }

    /**
     * 修改部门
     */
    @PutMapping
    public Result<Void> edit(@Validated @RequestBody Dept dept) {
        Long deptId = dept.getId();
        if (deptId == null) {
            return Result.error("部门ID不能为空");
        }
        
        if (deptId.equals(dept.getParentId())) {
            return Result.error("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        }
        
        if (!deptService.checkDeptNameUnique(dept)) {
            return Result.error("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        
        deptService.updateDept(dept);
        return Result.success();
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{deptId}")
    public Result<Void> remove(@PathVariable Long deptId) {
        if (deptService.hasChildByDeptId(deptId)) {
            return Result.error("存在下级部门，不允许删除");
        }
        
        // TODO: 检查部门是否存在用户
        // if (deptService.checkDeptExistUser(deptId)) {
        //     return Result.error("部门存在用户，不允许删除");
        // }
        
        deptService.deleteDeptById(deptId);
        return Result.success();
    }

    /**
     * 修改部门状态
     */
    @PutMapping("/changeStatus")
    public Result<Void> changeStatus(@RequestBody Dept dept) {
        if (dept.getId() == null) {
            return Result.error("部门ID不能为空");
        }
        
        deptService.updateDeptStatus(dept.getId(), dept.getStatus());
        return Result.success();
    }

    /**
     * 获取部门下拉树列表
     */
    @GetMapping("/treeselect")
    public Result<List<Dept>> treeselect() {
        List<Dept> depts = deptService.selectDeptTreeNormal();
        return Result.success(deptService.buildDeptTreeSelect(depts));
    }

    /**
     * 加载对应角色部门列表树
     */
    @GetMapping(value = "/roleDeptTreeselect/{roleId}")
    public Result<List<Dept>> roleDeptTreeselect(@PathVariable("roleId") Long roleId) {
        List<Dept> depts = deptService.selectDeptTreeByRoleId(roleId);
        return Result.success(deptService.buildDeptTreeSelect(depts));
    }

    /**
     * 校验部门名称
     */
    @PostMapping("/checkDeptNameUnique")
    public Result<Boolean> checkDeptNameUnique(@RequestBody Dept dept) {
        boolean isUnique = deptService.checkDeptNameUnique(dept);
        return Result.success(isUnique);
    }

    /**
     * 获取部门的所有子部门ID
     */
    @GetMapping("/children/{deptId}")
    public Result<List<Long>> getChildrenIds(@PathVariable Long deptId) {
        List<Long> childrenIds = deptService.selectChildrenIdsByDeptId(deptId);
        return Result.success(childrenIds);
    }
}