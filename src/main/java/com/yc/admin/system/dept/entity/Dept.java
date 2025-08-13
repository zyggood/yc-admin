package com.yc.admin.system.dept.entity;

import com.yc.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 部门实体类
 * 支持树形结构的部门管理
 * 
 * @author admin
 * @since 2025-01-01
 */
@Entity
@Table(name = "sys_dept", indexes = {
    @Index(name = "idx_sys_dept_parent_id", columnList = "parent_id"),
    @Index(name = "idx_sys_dept_ancestors", columnList = "ancestors"),
    @Index(name = "idx_sys_dept_status", columnList = "status")
})
@Getter
@Setter
@ToString(callSuper = true)
@AttributeOverride(name = "id", column = @Column(name = "dept_id"))
public class Dept extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 父部门ID（顶级部门为0）
     */
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    /**
     * 祖级列表（用逗号分隔的ID列表，如：0,1,2）
     * 便于查询某个部门的所有子部门
     */
    @Column(name = "ancestors", length = 500)
    private String ancestors = "0";

    /**
     * 部门名称
     */
    @Column(name = "dept_name", nullable = false, length = 30)
    private String deptName;

    /**
     * 显示顺序
     */
    @Column(name = "order_num", nullable = false)
    private Integer orderNum = 0;

    /**
     * 负责人
     */
    @Column(name = "leader", length = 20)
    private String leader;

    /**
     * 联系电话
     */
    @Column(name = "phone", length = 11)
    private String phone;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 50)
    private String email;

    /**
     * 部门状态（0正常 1停用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /**
     * 子部门列表（不持久化到数据库）
     */
    @Transient
    private List<Dept> children = new ArrayList<>();

    /**
     * 父部门名称（不持久化到数据库）
     */
    @Transient
    private String parentName;

    /**
     * 获取部门层级
     * @return 部门层级（根据ancestors计算）
     */
    @Transient
    public int getLevel() {
        if (ancestors == null || ancestors.isEmpty()) {
            return 0;
        }
        return ancestors.split(",").length;
    }

    /**
     * 判断是否为根部门
     * @return true-根部门，false-非根部门
     */
    @Transient
    public boolean isRoot() {
        return parentId == null || parentId == 0L;
    }

    /**
     * 判断是否有子部门
     * @return true-有子部门，false-无子部门
     */
    @Transient
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * 添加子部门
     * @param child 子部门
     */
    public void addChild(Dept child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    /**
     * 构建祖级列表
     * @param parentAncestors 父部门的祖级列表
     * @param parentId 父部门ID
     */
    public void buildAncestors(String parentAncestors, Long parentId) {
        if (parentAncestors == null || parentAncestors.isEmpty()) {
            this.ancestors = String.valueOf(parentId);
        } else {
            this.ancestors = parentAncestors + "," + parentId;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dept dept = (Dept) o;
        return Objects.equals(getId(), dept.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}