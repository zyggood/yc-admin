package com.yc.admin.system.menu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单数据传输对象
 * 
 * @author YC
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "菜单数据传输对象")
public class MenuDTO {

    @Schema(description = "菜单ID")
    private Long id;

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户管理")
    private String menuName;

    @Schema(description = "父菜单ID", example = "0")
    private Long parentId;

    @Min(value = 0, message = "显示顺序不能小于0")
    @Schema(description = "显示顺序", example = "1")
    private Integer orderNum;

    @Size(max = 200, message = "路由地址长度不能超过200个字符")
    @Schema(description = "路由地址", example = "/system/user")
    private String path;

    @Size(max = 255, message = "组件路径长度不能超过255个字符")
    @Schema(description = "组件路径", example = "system/user/index")
    private String component;

    @Size(max = 255, message = "路由参数长度不能超过255个字符")
    @Schema(description = "路由参数", example = "userId=1")
    private String query;

    @Schema(description = "是否为外链（0否 1是）", example = "0")
    private Integer isFrame;

    @Schema(description = "是否缓存（0缓存 1不缓存）", example = "0")
    private Integer isCache;

    @NotBlank(message = "菜单类型不能为空")
    @Pattern(regexp = "^[MCF]$", message = "菜单类型只能是M、C、F")
    @Schema(description = "菜单类型（M目录 C菜单 F按钮）", requiredMode = Schema.RequiredMode.REQUIRED, example = "C")
    private String menuType;

    @Schema(description = "菜单状态（0显示 1隐藏）", example = "0")
    private Integer visible;

    @Schema(description = "菜单状态（0正常 1停用）", example = "0")
    private Integer status;

    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    @Schema(description = "权限标识", example = "system:user:list")
    private String perms;

    @Size(max = 100, message = "菜单图标长度不能超过100个字符")
    @Schema(description = "菜单图标", example = "fa fa-user")
    private String icon;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建者")
    private String createBy;

    @Schema(description = "更新者")
    private String updateBy;

    // ==================== 扩展字段 ====================

    @Schema(description = "父菜单名称")
    private String parentName;

    @Schema(description = "子菜单列表")
    @Builder.Default
    private List<MenuDTO> children = new ArrayList<>();

    @Schema(description = "是否有子菜单")
    private Boolean hasChildren;

    @Schema(description = "菜单层级")
    private Integer level;

    @Schema(description = "菜单路径（从根到当前节点的完整路径）")
    private String fullPath;

    @Schema(description = "是否选中（用于权限分配）")
    private Boolean checked;

    @Schema(description = "是否展开（用于树形显示）")
    private Boolean expanded;

    // ==================== 查询条件DTO ====================

    /**
     * 菜单查询条件DTO
     */
    @Data
    @Schema(description = "菜单查询条件")
    public static class QueryDTO {

        @Schema(description = "菜单名称（模糊查询）")
        private String menuName;

        @Schema(description = "父菜单ID")
        private Long parentId;

        @Schema(description = "菜单类型（M目录 C菜单 F按钮）")
        private String menuType;

        @Schema(description = "菜单状态（0正常 1停用）")
        private Integer status;

        @Schema(description = "可见性（0显示 1隐藏）")
        private Integer visible;

        @Schema(description = "权限标识（模糊查询）")
        private String perms;

        @Schema(description = "创建时间范围-开始")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTimeStart;

        @Schema(description = "创建时间范围-结束")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTimeEnd;

        @Schema(description = "是否只查询顶级菜单")
        private Boolean onlyTopLevel;

        @Schema(description = "是否包含子菜单")
        private Boolean includeChildren;

        @Schema(description = "排序字段")
        private String orderBy;

        @Schema(description = "排序方向（asc/desc）")
        private String orderDirection;
    }

    // ==================== 创建/更新DTO ====================

    /**
     * 菜单创建DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "MenuCreateDTO", description = "菜单创建请求")
    public static class CreateDTO {

        @NotBlank(message = "菜单名称不能为空")
        @Size(max = 50, message = "菜单名称长度不能超过50个字符")
        @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String menuName;

        @Schema(description = "父菜单ID，默认为0（顶级菜单）")
        @Builder.Default
        private Long parentId = 0L;

        @Min(value = 0, message = "显示顺序不能小于0")
        @Schema(description = "显示顺序")
        @Builder.Default
        private Integer orderNum = 0;

        @Size(max = 200, message = "路由地址长度不能超过200个字符")
        @Schema(description = "路由地址")
        private String path;

        @Size(max = 255, message = "组件路径长度不能超过255个字符")
        @Schema(description = "组件路径")
        private String component;

        @Size(max = 255, message = "路由参数长度不能超过255个字符")
        @Schema(description = "路由参数")
        private String query;

        @Schema(description = "是否为外链（0否 1是）")
        @Builder.Default
        private Integer isFrame = 0;

        @Schema(description = "是否缓存（0缓存 1不缓存）")
        @Builder.Default
        private Integer isCache = 0;

        @NotBlank(message = "菜单类型不能为空")
        @Pattern(regexp = "^[MCF]$", message = "菜单类型只能是M、C、F")
        @Schema(description = "菜单类型（M目录 C菜单 F按钮）", requiredMode = Schema.RequiredMode.REQUIRED)
        private String menuType;

        @Schema(description = "菜单状态（0显示 1隐藏）")
        @Builder.Default
        private Integer visible = 0;

        @Schema(description = "菜单状态（0正常 1停用）")
        @Builder.Default
        private Integer status = 0;

        @Size(max = 100, message = "权限标识长度不能超过100个字符")
        @Schema(description = "权限标识")
        private String perms;

        @Size(max = 100, message = "菜单图标长度不能超过100个字符")
        @Schema(description = "菜单图标")
        private String icon;

        @Size(max = 500, message = "备注长度不能超过500个字符")
        @Schema(description = "备注")
        private String remark;
    }

    /**
     * 菜单更新DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "菜单更新请求")
    public static class UpdateDTO {

        @NotNull(message = "菜单ID不能为空")
        @Schema(description = "菜单ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @Size(max = 50, message = "菜单名称长度不能超过50个字符")
        @Schema(description = "菜单名称")
        private String menuName;

        @Schema(description = "父菜单ID")
        private Long parentId;

        @Min(value = 0, message = "显示顺序不能小于0")
        @Schema(description = "显示顺序")
        private Integer orderNum;

        @Size(max = 200, message = "路由地址长度不能超过200个字符")
        @Schema(description = "路由地址")
        private String path;

        @Size(max = 255, message = "组件路径长度不能超过255个字符")
        @Schema(description = "组件路径")
        private String component;

        @Size(max = 255, message = "路由参数长度不能超过255个字符")
        @Schema(description = "路由参数")
        private String query;

        @Schema(description = "是否为外链（0否 1是）")
        private Integer isFrame;

        @Schema(description = "是否缓存（0缓存 1不缓存）")
        private Integer isCache;

        @NotBlank(message = "菜单类型不能为空")
        @Pattern(regexp = "^[MCF]$", message = "菜单类型只能是M、C、F")
        @Schema(description = "菜单类型（M目录 C菜单 F按钮）", requiredMode = Schema.RequiredMode.REQUIRED)
        private String menuType;

        @Schema(description = "菜单状态（0显示 1隐藏）")
        private Integer visible;

        @Schema(description = "菜单状态（0正常 1停用）")
        private Integer status;

        @Size(max = 100, message = "权限标识长度不能超过100个字符")
        @Schema(description = "权限标识")
        private String perms;

        @Size(max = 100, message = "菜单图标长度不能超过100个字符")
        @Schema(description = "菜单图标")
        private String icon;

        @Size(max = 500, message = "备注长度不能超过500个字符")
        @Schema(description = "备注")
        private String remark;
    }

    // ==================== 批量操作DTO ====================

    /**
     * 批量状态更新DTO
     */
    @Data
    @Schema(description = "批量状态更新请求")
    public static class BatchStatusUpdateDTO {

        @NotEmpty(message = "菜单ID列表不能为空")
        @Schema(description = "菜单ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<Long> ids;

        @NotNull(message = "状态不能为空")
        @Schema(description = "状态（0正常 1停用）", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer status;
    }


    // ==================== 树形结构DTO ====================

    /**
     * 菜单树节点DTO
     */
    @Data
    @Builder
    @Schema(description = "菜单树节点")
    public static class TreeNodeDTO {

        @Schema(description = "节点ID")
        private Long id;

        @Schema(description = "节点标签")
        private String label;

        @Schema(description = "父节点ID")
        private Long parentId;

        @Schema(description = "节点值")
        private Object value;

        @Schema(description = "节点图标")
        private String icon;

        @Schema(description = "是否禁用")
        private Boolean disabled;

        @Schema(description = "是否选中")
        private Boolean checked;

        @Schema(description = "是否展开")
        private Boolean expanded;

        @Schema(description = "子节点列表")
        @Builder.Default
        private List<TreeNodeDTO> children = new ArrayList<>();

        @Schema(description = "节点层级")
        private Integer level;

        @Schema(description = "是否叶子节点")
        private Boolean isLeaf;

        @Schema(description = "节点类型")
        private String type;

        @Schema(description = "排序号")
        private Integer orderNum;

        @Schema(description = "节点路径")
        private String path;

        @Schema(description = "扩展属性")
        private Object extra;
    }

    // ==================== 权限相关DTO ====================

    /**
     * 菜单权限DTO
     */
    @Data
    @Builder
    @Schema(description = "菜单权限信息")
    public static class PermissionDTO {

        @Schema(description = "权限ID")
        private Long id;

        @Schema(description = "菜单ID")
        private Long menuId;

        @Schema(description = "菜单名称")
        private String menuName;

        @Schema(description = "权限标识")
        private String perms;

        @Schema(description = "菜单类型")
        private String menuType;

        @Schema(description = "菜单路径")
        private String path;

        @Schema(description = "是否有权限")
        private Boolean hasPermission;

        @Schema(description = "权限来源（role/user）")
        private String permissionSource;

        @Schema(description = "权限描述")
        private String description;
    }

    /**
     * 用户菜单权限DTO
     */
    @Data
    @Schema(description = "用户菜单权限")
    public static class UserMenuPermissionDTO {

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "菜单权限列表")
        private List<PermissionDTO> menuPermissions;

        @Schema(description = "角色权限列表")
        private List<String> rolePermissions;

        @Schema(description = "直接权限列表")
        private List<String> directPermissions;
    }

    // ==================== 统计相关DTO ====================

    /**
     * 菜单统计DTO
     */
    @Data
    @Schema(description = "菜单统计信息")
    public static class StatisticsDTO {

        @Schema(description = "总菜单数")
        private Long totalCount;

        @Schema(description = "目录数量")
        private Long directoryCount;

        @Schema(description = "菜单数量")
        private Long menuCount;

        @Schema(description = "按钮数量")
        private Long buttonCount;

        @Schema(description = "正常状态数量")
        private Long normalCount;

        @Schema(description = "停用状态数量")
        private Long disabledCount;

        @Schema(description = "显示状态数量")
        private Long visibleCount;

        @Schema(description = "隐藏状态数量")
        private Long hiddenCount;

        @Schema(description = "最大层级")
        private Integer maxLevel;

        @Schema(description = "平均子菜单数")
        private Double avgChildrenCount;

        @Schema(description = "各层级菜单数量分布")
        private java.util.Map<Integer, Long> levelDistribution;

        @Schema(description = "各类型菜单数量分布")
        private java.util.Map<String, Long> typeDistribution;
    }

    // ==================== 导入导出DTO ====================

    /**
     * 菜单导出DTO
     */
    @Data
    @Builder
    @Schema(description = "菜单导出数据")
    public static class ExportDTO {

        @Schema(description = "菜单名称")
        private String menuName;

        @Schema(description = "父菜单名称")
        private String parentName;

        @Schema(description = "菜单类型")
        private String menuTypeName;

        @Schema(description = "显示顺序")
        private Integer orderNum;

        @Schema(description = "路由地址")
        private String path;

        @Schema(description = "权限标识")
        private String perms;

        @Schema(description = "状态")
        private String statusName;

        @Schema(description = "可见性")
        private String visibleName;

        @Schema(description = "创建时间")
        private String createTime;

        @Schema(description = "备注")
        private String remark;
    }

    /**
     * 菜单导入DTO
     */
    @Data
    @Schema(description = "菜单导入数据")
    public static class ImportDTO {

        @Schema(description = "菜单名称")
        private String menuName;

        @Schema(description = "父菜单名称")
        private String parentName;

        @Schema(description = "菜单类型")
        private String menuType;

        @Schema(description = "显示顺序")
        private Integer orderNum;

        @Schema(description = "路由地址")
        private String path;

        @Schema(description = "组件路径")
        private String component;

        @Schema(description = "权限标识")
        private String perms;

        @Schema(description = "菜单图标")
        private String icon;

        @Schema(description = "状态")
        private String status;

        @Schema(description = "可见性")
        private String visible;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "导入结果")
        private String importResult;

        @Schema(description = "错误信息")
        private String errorMessage;
    }
}