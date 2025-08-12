package com.yc.admin.role.entity;

import com.yc.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serial;
import java.util.Objects;

/**
 * 角色菜单关联表实体类
 * 
 * @author YC
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "sys_role_menu", indexes = {
    @Index(name = "idx_role_id", columnList = "roleId"),
    @Index(name = "idx_menu_id", columnList = "menuId"),
    @Index(name = "idx_role_menu", columnList = "roleId,menuId", unique = true)
})
public class RoleMenu extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    /**
     * 菜单ID
     */
    @NotNull(message = "菜单ID不能为空")
    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    // ==================== 构造方法 ====================

    /**
     * 默认构造方法
     */
    public RoleMenu() {
    }

    /**
     * 构造方法
     * @param roleId 角色ID
     * @param menuId 菜单ID
     */
    public RoleMenu(Long roleId, Long menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }

    // ==================== 业务方法 ====================

    /**
     * 创建角色菜单关联
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 角色菜单关联对象
     */
    public static RoleMenu of(Long roleId, Long menuId) {
        return new RoleMenu(roleId, menuId);
    }

    /**
     * 判断是否为有效的关联关系
     * @return true：有效，false：无效
     */
    public boolean isValid() {
        return roleId != null && roleId > 0 && menuId != null && menuId > 0;
    }

    // ==================== equals & hashCode ====================

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy hibernateProxy ? 
            hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hibernateProxy ? 
            hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        RoleMenu roleMenu = (RoleMenu) o;
        return getId() != null && Objects.equals(getId(), roleMenu.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hibernateProxy ? 
            hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    // ==================== 常量定义 ====================

    /**
     * 表名常量
     */
    public static final String TABLE_NAME = "sys_role_menu";

    /**
     * 字段名常量
     */
    public static final class Fields {
        public static final String ROLE_ID = "roleId";
        public static final String MENU_ID = "menuId";
    }

    /**
     * 数据库列名常量
     */
    public static final class Columns {
        public static final String ROLE_ID = "role_id";
        public static final String MENU_ID = "menu_id";
    }
}