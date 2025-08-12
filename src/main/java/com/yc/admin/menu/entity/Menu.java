package com.yc.admin.menu.entity;

import com.yc.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单权限实体类
 * 对应数据库表：sys_menu
 * 
 * @author YC
 * @since 1.0.0
 */
@Entity
@Table(name = "sys_menu", indexes = {
    @Index(name = "idx_parent_id", columnList = "parentId"),
    @Index(name = "idx_menu_type", columnList = "menuType"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_order_num", columnList = "orderNum")
})
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu extends BaseEntity {

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    @Column(name = "menu_name", nullable = false, length = 50)
    private String menuName;

    /**
     * 父菜单ID（0表示顶级菜单）
     */
    @NotNull(message = "父菜单ID不能为空")
    @Min(value = 0, message = "父菜单ID不能小于0")
    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    @Min(value = 0, message = "显示顺序不能小于0")
    @Column(name = "order_num", nullable = false)
    private Integer orderNum;

    /**
     * 路由地址
     */
    @Size(max = 200, message = "路由地址长度不能超过200个字符")
    @Column(name = "path", length = 200)
    private String path;

    /**
     * 组件路径
     */
    @Size(max = 255, message = "组件路径长度不能超过255个字符")
    @Column(name = "component", length = 255)
    private String component;

    /**
     * 路由参数
     */
    @Size(max = 255, message = "路由参数长度不能超过255个字符")
    @Column(name = "query", length = 255)
    private String query;

    /**
     * 是否为外链（0否 1是）
     */
    @NotNull(message = "是否为外链不能为空")
    @Min(value = 0, message = "是否为外链值必须为0或1")
    @Max(value = 1, message = "是否为外链值必须为0或1")
    @Column(name = "is_frame", nullable = false)
    private Integer isFrame;

    /**
     * 是否缓存（0缓存 1不缓存）
     */
    @NotNull(message = "是否缓存不能为空")
    @Min(value = 0, message = "是否缓存值必须为0或1")
    @Max(value = 1, message = "是否缓存值必须为0或1")
    @Column(name = "is_cache", nullable = false)
    private Integer isCache;

    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    @NotBlank(message = "菜单类型不能为空")
    @Pattern(regexp = "^[MCF]$", message = "菜单类型只能是M、C、F")
    @Column(name = "menu_type", nullable = false, length = 1)
    private String menuType;

    /**
     * 菜单状态（0显示 1隐藏）
     */
    @NotNull(message = "菜单状态不能为空")
    @Min(value = 0, message = "菜单状态值必须为0或1")
    @Max(value = 1, message = "菜单状态值必须为0或1")
    @Column(name = "visible", nullable = false)
    private Integer visible;

    /**
     * 菜单状态（0正常 1停用）
     */
    @NotNull(message = "菜单状态不能为空")
    @Min(value = 0, message = "菜单状态值必须为0或1")
    @Max(value = 1, message = "菜单状态值必须为0或1")
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 权限标识
     */
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    @Column(name = "perms", length = 100)
    private String perms;

    /**
     * 菜单图标
     */
    @Size(max = 100, message = "菜单图标长度不能超过100个字符")
    @Column(name = "icon", length = 100)
    private String icon;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Column(name = "remark", length = 500)
    private String remark;

    // ==================== 业务方法 ====================

    /**
     * 是否为顶级菜单
     * @return true-顶级菜单，false-非顶级菜单
     */
    public boolean isTopLevel() {
        return this.parentId != null && this.parentId == 0L;
    }

    /**
     * 是否为目录
     * @return true-目录，false-非目录
     */
    public boolean isDirectory() {
        return "M".equals(this.menuType);
    }

    /**
     * 是否为菜单
     * @return true-菜单，false-非菜单
     */
    public boolean isMenu() {
        return "C".equals(this.menuType);
    }

    /**
     * 是否为按钮
     * @return true-按钮，false-非按钮
     */
    public boolean isButton() {
        return "F".equals(this.menuType);
    }

    /**
     * 是否显示
     * @return true-显示，false-隐藏
     */
    public boolean isVisible() {
        return this.visible != null && this.visible == 0;
    }

    /**
     * 是否正常状态
     * @return true-正常，false-停用
     */
    public boolean isNormal() {
        return this.status != null && this.status == 0;
    }

    /**
     * 是否为外链
     * @return true-外链，false-非外链
     */
    public boolean isFrame() {
        return this.isFrame != null && this.isFrame == 1;
    }

    /**
     * 是否缓存
     * @return true-缓存，false-不缓存
     */
    public boolean isCached() {
        return this.isCache != null && this.isCache == 0;
    }

    /**
     * 获取菜单类型描述
     * @return 菜单类型描述
     */
    public String getMenuTypeDesc() {
        if (this.menuType == null) {
            return "未知";
        }
        return switch (this.menuType) {
            case "M" -> "目录";
            case "C" -> "菜单";
            case "F" -> "按钮";
            default -> "未知";
        };
    }

    /**
     * 设置为正常状态
     */
    public void enable() {
        this.status = 0;
    }

    /**
     * 设置为停用状态
     */
    public void disable() {
        this.status = 1;
    }

    /**
     * 设置为显示
     */
    public void show() {
        this.visible = 0;
    }

    /**
     * 设置为隐藏
     */
    public void hide() {
        this.visible = 1;
    }

    // ==================== 常量定义 ====================

    /**
     * 菜单类型常量
     */
    public static class Type {
        /** 目录 */
        public static final String DIRECTORY = "M";
        /** 菜单 */
        public static final String MENU = "C";
        /** 按钮 */
        public static final String BUTTON = "F";
    }

    /**
     * 菜单状态常量
     */
    public static class Status {
        /** 正常 */
        public static final int NORMAL = 0;
        /** 停用 */
        public static final int DISABLED = 1;
    }

    /**
     * 显示状态常量
     */
    public static class Visible {
        /** 显示 */
        public static final int SHOW = 0;
        /** 隐藏 */
        public static final int HIDE = 1;
    }

    /**
     * 外链状态常量
     */
    public static class Frame {
        /** 非外链 */
        public static final int NO = 0;
        /** 外链 */
        public static final int YES = 1;
    }

    /**
     * 缓存状态常量
     */
    public static class Cache {
        /** 缓存 */
        public static final int YES = 0;
        /** 不缓存 */
        public static final int NO = 1;
    }

    /**
     * 顶级菜单父ID
     */
    public static final Long TOP_PARENT_ID = 0L;
}