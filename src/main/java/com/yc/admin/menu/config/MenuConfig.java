package com.yc.admin.menu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 菜单配置类
 * 
 * @author YC
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "admin.menu")
public class MenuConfig {

    /**
     * 菜单缓存配置
     */
    private Cache cache = new Cache();

    /**
     * 菜单权限配置
     */
    private Permission permission = new Permission();

    /**
     * 菜单显示配置
     */
    private Display display = new Display();

    /**
     * 菜单验证配置
     */
    private Validation validation = new Validation();

    /**
     * 缓存配置
     */
    @Data
    public static class Cache {
        /**
         * 是否启用菜单缓存
         */
        private boolean enabled = true;

        /**
         * 缓存过期时间（秒）
         */
        private long expireTime = 3600;

        /**
         * 缓存键前缀
         */
        private String keyPrefix = "menu:";

        /**
         * 用户菜单缓存键前缀
         */
        private String userMenuKeyPrefix = "user:menu:";

        /**
         * 角色菜单缓存键前缀
         */
        private String roleMenuKeyPrefix = "role:menu:";
    }

    /**
     * 权限配置
     */
    @Data
    public static class Permission {
        /**
         * 是否启用权限验证
         */
        private boolean enabled = true;

        /**
         * 超级管理员角色标识
         */
        private String superAdminRole = "admin";

        /**
         * 匿名访问的菜单路径
         */
        private List<String> anonymousPaths = List.of(
            "/login",
            "/register",
            "/captcha",
            "/error"
        );

        /**
         * 默认权限前缀
         */
        private String defaultPrefix = "system";

        /**
         * 权限分隔符
         */
        private String separator = ":";
    }

    /**
     * 显示配置
     */
    @Data
    public static class Display {
        /**
         * 默认菜单图标
         */
        private String defaultIcon = "fa fa-circle-o";

        /**
         * 菜单类型图标映射
         */
        private Map<String, String> typeIcons = Map.of(
            "M", "fa fa-folder",
            "C", "fa fa-file-o",
            "F", "fa fa-dot-circle-o"
        );

        /**
         * 最大菜单层级
         */
        private int maxLevel = 5;

        /**
         * 是否显示菜单面包屑
         */
        private boolean showBreadcrumb = true;

        /**
         * 是否显示菜单图标
         */
        private boolean showIcon = true;

        /**
         * 菜单展开模式：accordion（手风琴）、normal（普通）
         */
        private String expandMode = "accordion";
    }

    /**
     * 验证配置
     */
    @Data
    public static class Validation {
        /**
         * 菜单名称最大长度
         */
        private int maxMenuNameLength = 50;

        /**
         * 路由地址最大长度
         */
        private int maxPathLength = 200;

        /**
         * 组件路径最大长度
         */
        private int maxComponentLength = 255;

        /**
         * 权限标识最大长度
         */
        private int maxPermsLength = 100;

        /**
         * 备注最大长度
         */
        private int maxRemarkLength = 500;

        /**
         * 是否允许重复的菜单名称（不同父级下）
         */
        private boolean allowDuplicateNames = false;

        /**
         * 是否允许重复的权限标识
         */
        private boolean allowDuplicatePerms = false;

        /**
         * 菜单名称正则表达式
         */
        private String menuNamePattern = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-\\s]+$";

        /**
         * 权限标识正则表达式
         */
        private String permsPattern = "^[a-zA-Z0-9_:\\-]+$";

        /**
         * 路由地址正则表达式
         */
        private String pathPattern = "^[a-zA-Z0-9_/\\-]+$";
    }

    // ==================== 常量定义 ====================

    /**
     * 菜单类型常量
     */
    public static class MenuType {
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
    public static class MenuStatus {
        /** 正常 */
        public static final int NORMAL = 0;
        /** 停用 */
        public static final int DISABLED = 1;
    }

    /**
     * 菜单可见性常量
     */
    public static class MenuVisible {
        /** 显示 */
        public static final int SHOW = 0;
        /** 隐藏 */
        public static final int HIDE = 1;
    }

    /**
     * 外链常量
     */
    public static class MenuFrame {
        /** 是外链 */
        public static final int YES = 1;
        /** 不是外链 */
        public static final int NO = 0;
    }

    /**
     * 缓存常量
     */
    public static class MenuCache {
        /** 缓存 */
        public static final int YES = 0;
        /** 不缓存 */
        public static final int NO = 1;
    }

    /**
     * 删除标志常量
     */
    public static class DelFlag {
        /** 存在 */
        public static final int EXIST = 0;
        /** 删除 */
        public static final int DELETED = 1;
    }

    /**
     * 顶级父菜单ID
     */
    public static final Long TOP_PARENT_ID = 0L;

    /**
     * 默认排序号
     */
    public static final int DEFAULT_ORDER_NUM = 0;

    /**
     * 菜单缓存键
     */
    public static class CacheKey {
        /** 所有菜单缓存键 */
        public static final String ALL_MENUS = "menu:all";
        /** 菜单树缓存键 */
        public static final String MENU_TREE = "menu:tree";
        /** 用户菜单缓存键前缀 */
        public static final String USER_MENUS = "menu:user:";
        /** 角色菜单缓存键前缀 */
        public static final String ROLE_MENUS = "menu:role:";
        /** 菜单权限缓存键前缀 */
        public static final String MENU_PERMS = "menu:perms:";
    }

    /**
     * 菜单操作类型
     */
    public static class OperationType {
        /** 查询 */
        public static final String QUERY = "query";
        /** 新增 */
        public static final String ADD = "add";
        /** 修改 */
        public static final String EDIT = "edit";
        /** 删除 */
        public static final String REMOVE = "remove";
        /** 导出 */
        public static final String EXPORT = "export";
        /** 导入 */
        public static final String IMPORT = "import";
    }

    /**
     * 菜单事件类型
     */
    public static class EventType {
        /** 菜单创建 */
        public static final String MENU_CREATED = "menu.created";
        /** 菜单更新 */
        public static final String MENU_UPDATED = "menu.updated";
        /** 菜单删除 */
        public static final String MENU_DELETED = "menu.deleted";
        /** 菜单状态变更 */
        public static final String MENU_STATUS_CHANGED = "menu.status.changed";
        /** 菜单权限变更 */
        public static final String MENU_PERMISSION_CHANGED = "menu.permission.changed";
    }

    // ==================== 工具方法 ====================

    /**
     * 获取菜单类型名称
     * @param menuType 菜单类型
     * @return 类型名称
     */
    public static String getMenuTypeName(String menuType) {
        switch (menuType) {
            case MenuType.DIRECTORY:
                return "目录";
            case MenuType.MENU:
                return "菜单";
            case MenuType.BUTTON:
                return "按钮";
            default:
                return "未知";
        }
    }

    /**
     * 获取菜单状态名称
     * @param status 状态值
     * @return 状态名称
     */
    public static String getMenuStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case MenuStatus.NORMAL:
                return "正常";
            case MenuStatus.DISABLED:
                return "停用";
            default:
                return "未知";
        }
    }

    /**
     * 获取可见性名称
     * @param visible 可见性值
     * @return 可见性名称
     */
    public static String getVisibleName(Integer visible) {
        if (visible == null) {
            return "未知";
        }
        switch (visible) {
            case MenuVisible.SHOW:
                return "显示";
            case MenuVisible.HIDE:
                return "隐藏";
            default:
                return "未知";
        }
    }

    /**
     * 检查是否为有效的菜单类型
     * @param menuType 菜单类型
     * @return 是否有效
     */
    public static boolean isValidMenuType(String menuType) {
        return MenuType.DIRECTORY.equals(menuType) || 
               MenuType.MENU.equals(menuType) || 
               MenuType.BUTTON.equals(menuType);
    }

    /**
     * 检查是否为有效的状态值
     * @param status 状态值
     * @return 是否有效
     */
    public static boolean isValidStatus(Integer status) {
        return status != null && 
               (status == MenuStatus.NORMAL || status == MenuStatus.DISABLED);
    }

    /**
     * 检查是否为有效的可见性值
     * @param visible 可见性值
     * @return 是否有效
     */
    public static boolean isValidVisible(Integer visible) {
        return visible != null && 
               (visible == MenuVisible.SHOW || visible == MenuVisible.HIDE);
    }

    /**
     * 生成用户菜单缓存键
     * @param userId 用户ID
     * @return 缓存键
     */
    public String getUserMenuCacheKey(Long userId) {
        return cache.getUserMenuKeyPrefix() + userId;
    }

    /**
     * 生成角色菜单缓存键
     * @param roleId 角色ID
     * @return 缓存键
     */
    public String getRoleMenuCacheKey(Long roleId) {
        return cache.getRoleMenuKeyPrefix() + roleId;
    }

    /**
     * 生成菜单权限缓存键
     * @param menuId 菜单ID
     * @return 缓存键
     */
    public String getMenuPermsCacheKey(Long menuId) {
        return CacheKey.MENU_PERMS + menuId;
    }
}