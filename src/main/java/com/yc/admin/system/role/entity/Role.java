package com.yc.admin.system.role.entity;

import com.yc.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serial;
import java.util.Objects;

/**
 * 系统角色实体类
 * 
 * @author YC
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "sys_role", indexes = {
    @Index(name = "idx_role_key", columnList = "roleKey", unique = true),
    @Index(name = "idx_role_sort", columnList = "roleSort"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_status_del_flag", columnList = "status,delFlag"),
    @Index(name = "idx_parent_id", columnList = "parentId"),
    @Index(name = "idx_parent_inheritance", columnList = "parentId,enableInheritance")
})
@AttributeOverride(name = "id", column = @Column(name = "role_id"))
public class Role extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 1, max = 30, message = "角色名称长度必须在1-30个字符之间")
    @Column(name = "role_name", nullable = false, length = 30)
    private String roleName;

    /**
     * 角色权限字符串
     */
    @NotBlank(message = "角色权限字符串不能为空")
    @Size(min = 1, max = 100, message = "角色权限字符串长度必须在1-100个字符之间")
    @Column(name = "role_key", nullable = false, unique = true, length = 100)
    private String roleKey;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    @Column(name = "role_sort", nullable = false)
    private Integer roleSort = 0;

    /**
     * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限）
     */
    @Column(name = "data_scope", length = 1)
    private String dataScope = DataScope.DEPT;

    /**
     * 菜单树选择项是否关联显示
     */
    @Column(name = "menu_check_strictly")
    private Boolean menuCheckStrictly = true;

    /**
     * 部门树选择项是否关联显示
     */
    @Column(name = "dept_check_strictly")
    private Boolean deptCheckStrictly = true;

    /**
     * 角色状态（0正常 1停用）
     */
    @Column(name = "status", length = 1)
    private String status = Status.NORMAL;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 父角色ID
     * 用于构建角色层级关系，支持权限继承
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 是否启用权限继承（0：不启用 1：启用）
     * 启用权限继承的角色可以继承父级权限，提供更灵活的权限管理
     */
    @Column(name = "enable_inheritance", length = 1)
    private String enableInheritance = InheritanceStatus.DISABLED;

    // ==================== 业务方法 ====================

    /**
     * 判断角色是否正常状态
     * @return true：正常，false：停用
     */
    public boolean isNormal() {
        return Status.NORMAL.equals(this.status);
    }

    /**
     * 判断角色是否停用
     * @return true：停用，false：正常
     */
    public boolean isDisabled() {
        return Status.DISABLED.equals(this.status);
    }

    /**
     * 启用角色
     */
    public void enable() {
        this.status = Status.NORMAL;
    }

    /**
     * 停用角色
     */
    public void disable() {
        this.status = Status.DISABLED;
    }

    /**
     * 获取状态描述
     * @return 状态描述
     */
    public String getStatusDesc() {
        return Status.NORMAL.equals(this.status) ? "正常" : "停用";
    }

    /**
     * 获取数据权限描述
     * @return 数据权限描述
     */
    public String getDataScopeDesc() {
        return switch (this.dataScope) {
            case DataScope.ALL -> "全部数据权限";
            case DataScope.CUSTOM -> "自定数据权限";
            case DataScope.DEPT -> "本部门数据权限";
            case DataScope.DEPT_AND_CHILD -> "本部门及以下数据权限";
            case DataScope.SELF -> "仅本人数据权限";
            default -> "未知";
        };
    }

    /**
     * 判断是否为超级管理员角色
     * @return true：超级管理员，false：普通角色
     */
    public boolean isAdmin() {
        return "admin".equals(this.roleKey);
    }

    /**
     * 判断是否启用权限继承
     * @return true：启用，false：不启用
     */
    public boolean isInheritanceEnabled() {
        return InheritanceStatus.ENABLED.equals(this.enableInheritance);
    }

    /**
     * 启用权限继承
     */
    public void enableInheritance() {
        this.enableInheritance = InheritanceStatus.ENABLED;
    }

    /**
     * 禁用权限继承
     */
    public void disableInheritance() {
        this.enableInheritance = InheritanceStatus.DISABLED;
    }

    /**
     * 判断是否为根角色（没有父角色）
     * @return true：根角色，false：子角色
     */
    public boolean isRootRole() {
        return this.parentId == null;
    }

    /**
     * 判断是否有父角色
     * @return true：有父角色，false：没有父角色
     */
    public boolean hasParent() {
        return this.parentId != null;
    }

    /**
     * 设置父角色
     * @param parentRole 父角色
     */
    public void setParent(Role parentRole) {
        this.parentId = parentRole != null ? parentRole.getId() : null;
    }

    /**
     * 清除父角色关系
     */
    public void clearParent() {
        this.parentId = null;
    }

    /**
     * 判断是否为指定角色的子角色
     * @param roleId 角色ID
     * @return true：是子角色，false：不是子角色
     */
    public boolean isChildOf(Long roleId) {
        return roleId != null && roleId.equals(this.parentId);
    }

    /**
     * 获取角色层级描述
     * @return 层级描述
     */
    public String getHierarchyDesc() {
        if (isRootRole()) {
            return "根角色";
        } else {
            return "子角色 (父角色ID: " + parentId + ")";
        }
    }

    // ==================== 常量定义 ====================

    /**
     * 角色状态枚举
     */
    public static class Status {
        public static final String NORMAL = "0";   // 正常
        public static final String DISABLED = "1"; // 停用
    }

    /**
     * 数据权限范围常量
     */
    public static class DataScope {
        public static final String ALL = "1";           // 全部数据权限
        public static final String CUSTOM = "2";       // 自定数据权限
        public static final String DEPT = "3";         // 本部门数据权限
        public static final String DEPT_AND_CHILD = "4"; // 本部门及以下数据权限
        public static final String SELF = "5";         // 仅本人数据权限
    }

    /**
     * 权限继承状态常量
     */
    public static class InheritanceStatus {
        public static final String ENABLED = "1";   // 启用权限继承
        public static final String DISABLED = "0";  // 不启用权限继承
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Role role = (Role) o;
        return getId() != null && Objects.equals(getId(), role.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}