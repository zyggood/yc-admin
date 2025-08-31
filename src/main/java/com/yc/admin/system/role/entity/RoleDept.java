package com.yc.admin.system.role.entity;

import com.yc.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 角色部门关联实体
 * 用于存储角色的自定义数据权限部门关联关系
 *
 * @author YC
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_role_dept")
public class RoleDept extends BaseEntity {

    /**
     * 角色ID
     */
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    /**
     * 部门ID
     */
    @Column(name = "dept_id", nullable = false)
    private Long deptId;

}