package com.yc.admin.user.entity;

import com.yc.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serial;
import java.util.Objects;


/**
 * 系统用户实体类
 * 
 * @author YC
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "sys_user", indexes = {
    @Index(name = "idx_nick_name", columnList = "nickName"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_status_del_flag", columnList = "status,delFlag")
})
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 超级管理员用户ID
     */
    public static final Long ADMIN_USER_ID = 1L;

    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 2, max = 30, message = "用户账号长度必须在2-30个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户账号只能包含字母、数字和下划线")
    @Column(name = "user_name", nullable = false, unique = true, length = 30)
    private String userName;

    /**
     * 用户昵称
     */
    @NotBlank(message = "用户昵称不能为空")
    @Size(min = 1, max = 30, message = "用户昵称长度必须在1-30个字符之间")
    @Column(name = "nick_name", nullable = false, length = 30)
    private String nickName;

    /**
     * 用户邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    @Column(name = "email", unique = true, length = 50)
    private String email;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    @Column(name = "phone", unique = true, length = 11)
    private String phone;

    /**
     * 用户性别（0男 1女 2未知）
     */
    @Column(name = "sex", length = 1)
    private String sex = "0";

    /**
     * 头像地址
     */
    @Size(max = 100, message = "头像地址长度不能超过100个字符")
    @Column(name = "avatar", length = 100)
    private String avatar = "";

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    /**
     * 帐号状态（0正常 1停用）
     */
    @Column(name = "status", length = 1)
    private String status = "0";

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Column(name = "remark", length = 500)
    private String remark;

    // ==================== 业务方法 ====================

    /**
     * 判断用户是否正常状态
     * @return true：正常，false：停用
     */
    public boolean isNormal() {
        return "0".equals(this.status);
    }

    /**
     * 判断用户是否停用
     * @return true：停用，false：正常
     */
    public boolean isDisabled() {
        return "1".equals(this.status);
    }

    /**
     * 启用用户
     */
    public void enable() {
        this.status = "0";
    }

    /**
     * 停用用户
     */
    public void disable() {
        this.status = "1";
    }

    /**
     * 判断是否为男性
     * @return true：男性，false：非男性
     */
    public boolean isMale() {
        return "0".equals(this.sex);
    }

    /**
     * 判断是否为女性
     * @return true：女性，false：非女性
     */
    public boolean isFemale() {
        return "1".equals(this.sex);
    }

    /**
     * 获取性别描述
     * @return 性别描述
     */
    public String getSexDesc() {
        return switch (this.sex) {
            case "0" -> "男";
            case "1" -> "女";
            default -> "未知";
        };
    }

    /**
     * 获取状态描述
     * @return 状态描述
     */
    public String getStatusDesc() {
        return "0".equals(this.status) ? "正常" : "停用";
    }

    /**
     * 脱敏显示手机号
     * @return 脱敏后的手机号
     */
    public String getMaskedPhone() {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 脱敏显示邮箱
     * @return 脱敏后的邮箱
     */
    public String getMaskedEmail() {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return username.charAt(0) + "*@" + domain;
        } else {
            return username.substring(0, 2) + "***@" + domain;
        }
    }

    // ==================== 常量定义 ====================

    /**
     * 性别枚举
     */
    public static class Sex {
        public static final String MALE = "0";     // 男
        public static final String FEMALE = "1";   // 女
        public static final String UNKNOWN = "2";  // 未知
    }

    /**
     * 状态枚举
     */
    public static class Status {
        public static final String NORMAL = "0";   // 正常
        public static final String DISABLED = "1"; // 停用
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}