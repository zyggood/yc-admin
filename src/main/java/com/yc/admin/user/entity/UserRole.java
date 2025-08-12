package com.yc.admin.user.entity;

import com.yc.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serial;
import java.util.Objects;

/**
 * 用户角色关联表实体类
 * 
 * @author YC
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "sys_user_role", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_role_id", columnList = "roleId"),
    @Index(name = "idx_user_role", columnList = "userId,roleId", unique = true)
})
public class UserRole extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    // ==================== 构造方法 ====================

    /**
     * 默认构造方法
     */
    public UserRole() {
    }

    /**
     * 构造方法
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    // ==================== 业务方法 ====================

    /**
     * 创建用户角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 用户角色关联对象
     */
    public static UserRole of(Long userId, Long roleId) {
        return new UserRole(userId, roleId);
    }

    /**
     * 判断是否为有效的关联关系
     * @return true：有效，false：无效
     */
    public boolean isValid() {
        return userId != null && userId > 0 && roleId != null && roleId > 0;
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
        UserRole userRole = (UserRole) o;
        return getId() != null && Objects.equals(getId(), userRole.getId());
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
    public static final String TABLE_NAME = "sys_user_role";

    /**
     * 字段名常量
     */
    public static final class Fields {
        public static final String USER_ID = "userId";
        public static final String ROLE_ID = "roleId";
    }

    /**
     * 数据库列名常量
     */
    public static final class Columns {
        public static final String USER_ID = "user_id";
        public static final String ROLE_ID = "role_id";
    }
}