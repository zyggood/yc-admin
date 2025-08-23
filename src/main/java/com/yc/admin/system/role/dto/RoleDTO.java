package com.yc.admin.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色数据传输对象
 *
 * @author YC
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色数据传输对象")
public class RoleDTO {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色权限字符串")
    private String roleKey;

    @Schema(description = "显示顺序")
    private Integer roleSort;

    @Schema(description = "数据范围：1=全部数据权限,2=自定数据权限,3=部门数据权限,4=部门及以下数据权限,5=仅本人数据权限")
    private String dataScope;

    @Schema(description = "菜单树选择项是否关联显示")
    private Boolean menuCheckStrictly;

    @Schema(description = "部门树选择项是否关联显示")
    private Boolean deptCheckStrictly;

    @Schema(description = "角色状态：0=正常,1=停用")
    private String status;

    @Schema(description = "删除标志：0=存在,1=删除")
    private Integer delFlag;

    @Schema(description = "创建者")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新者")
    private String updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "菜单ID列表")
    private List<Long> menuIds;

    @Schema(description = "部门ID列表（数据权限）")
    private List<Long> deptIds;

    @Schema(description = "用户数量")
    private Long userCount;

    // ==================== 查询条件DTO ====================

    /**
     * 角色查询条件DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色查询条件")
    public static class QueryDTO {

        @Schema(description = "角色名称关键字")
        private String roleName;

        @Schema(description = "角色权限字符串关键字")
        private String roleKey;

        @Schema(description = "角色状态")
        private String status;

        @Schema(description = "数据范围")
        private String dataScope;

        @Schema(description = "创建时间开始")
        private LocalDateTime createTimeStart;

        @Schema(description = "创建时间结束")
        private LocalDateTime createTimeEnd;
    }

    // ==================== 创建/更新DTO ====================

    /**
     * 角色创建DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色创建数据")
    public static class CreateDTO {

        @NotBlank(message = "角色名称不能为空")
        @Size(min = 2, max = 30, message = "角色名称长度必须在2-30个字符之间")
        @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String roleName;

        @NotBlank(message = "角色权限字符串不能为空")
        @Size(min = 2, max = 100, message = "角色权限字符串长度必须在2-100个字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_:]+$", message = "角色权限字符串只能包含字母、数字、下划线和冒号")
        @Schema(description = "角色权限字符串", requiredMode = Schema.RequiredMode.REQUIRED)
        private String roleKey;

        @Min(value = 0, message = "显示顺序不能小于0")
        @Schema(description = "显示顺序")
        @Builder.Default
        private Integer roleSort = 0;

        @Pattern(regexp = "^[1-5]$", message = "数据范围值不正确")
        @Schema(description = "数据范围：1=全部数据权限,2=自定数据权限,3=部门数据权限,4=部门及以下数据权限,5=仅本人数据权限")
        @Builder.Default
        private String dataScope = "1";

        @Schema(description = "菜单树选择项是否关联显示")
        @Builder.Default
        private Boolean menuCheckStrictly = true;

        @Schema(description = "部门树选择项是否关联显示")
        @Builder.Default
        private Boolean deptCheckStrictly = true;

        @Pattern(regexp = "^[01]$", message = "状态值不正确")
        @Schema(description = "角色状态：0=正常,1=停用")
        @Builder.Default
        private String status = "0";

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "菜单ID列表")
        private List<Long> menuIds;

        @Schema(description = "部门ID列表（数据权限）")
        private List<Long> deptIds;
    }

    /**
     * 角色更新DTO
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "角色更新数据")
    public static class UpdateDTO extends CreateDTO {

        @NotNull(message = "角色ID不能为空")
        @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;
    }

    // ==================== 权限分配DTO ====================

    /**
     * 角色菜单权限分配DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色菜单权限分配数据")
    public static class MenuPermissionDTO {

        @NotNull(message = "角色ID不能为空")
        @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long roleId;

        @Schema(description = "菜单ID列表")
        private List<Long> menuIds;

        @Schema(description = "菜单树选择项是否关联显示")
        private Boolean menuCheckStrictly;
    }

    /**
     * 角色数据权限分配DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色数据权限分配数据")
    public static class DataPermissionDTO {

        @NotNull(message = "角色ID不能为空")
        @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long roleId;

        @NotBlank(message = "数据范围不能为空")
        @Pattern(regexp = "^[1-5]$", message = "数据范围值不正确")
        @Schema(description = "数据范围：1=全部数据权限,2=自定数据权限,3=部门数据权限,4=部门及以下数据权限,5=仅本人数据权限", requiredMode = Schema.RequiredMode.REQUIRED)
        private String dataScope;

        @Schema(description = "部门ID列表（自定数据权限时使用）")
        private List<Long> deptIds;

        @Schema(description = "部门树选择项是否关联显示")
        private Boolean deptCheckStrictly;
    }

    // ==================== 批量操作DTO ====================

    /**
     * 批量状态更新DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "批量状态更新数据")
    public static class BatchStatusUpdateDTO {

        @NotEmpty(message = "角色ID列表不能为空")
        @Schema(description = "角色ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<Long> roleIds;

        @NotBlank(message = "状态不能为空")
        @Pattern(regexp = "^[01]$", message = "状态值不正确")
        @Schema(description = "状态：0=正常,1=停用", requiredMode = Schema.RequiredMode.REQUIRED)
        private String status;
    }

    /**
     * 批量删除DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "批量删除数据")
    public static class BatchDeleteDTO {

        @NotEmpty(message = "角色ID列表不能为空")
        @Schema(description = "角色ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<Long> roleIds;
    }

    // ==================== 用户角色关联DTO ====================

    /**
     * 用户角色分配DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户角色分配数据")
    public static class UserRoleAssignDTO {

        @NotNull(message = "用户ID不能为空")
        @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long userId;

        @Schema(description = "角色ID列表")
        private List<Long> roleIds;
    }

    /**
     * 角色用户分配DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色用户分配数据")
    public static class RoleUserAssignDTO {

        @NotNull(message = "角色ID不能为空")
        @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long roleId;

        @Schema(description = "用户ID列表")
        private List<Long> userIds;
    }

    // ==================== 统计相关DTO ====================

    /**
     * 角色统计DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色统计数据")
    public static class StatisticsDTO {

        @Schema(description = "总角色数")
        private Long totalRoles;

        @Schema(description = "正常角色数")
        private Long normalRoles;

        @Schema(description = "停用角色数")
        private Long disabledRoles;

        @Schema(description = "今日新增角色数")
        private Long todayNewRoles;

        @Schema(description = "本月新增角色数")
        private Long monthNewRoles;
    }

    // ==================== 选择器DTO ====================

    /**
     * 角色选择器DTO（用于下拉选择）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色选择器数据")
    public static class SelectorDTO {

        @Schema(description = "角色ID")
        private Long id;

        @Schema(description = "角色名称")
        private String roleName;

        @Schema(description = "角色权限字符串")
        private String roleKey;

        @Schema(description = "是否选中")
        @Builder.Default
        private Boolean selected = false;
    }

    // ==================== 导入导出DTO ====================

    /**
     * 角色导出DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色导出数据")
    public static class ExportDTO {

        @Schema(description = "角色名称")
        private String roleName;

        @Schema(description = "角色权限字符串")
        private String roleKey;

        @Schema(description = "显示顺序")
        private Integer roleSort;

        @Schema(description = "数据范围")
        private String dataScopeLabel;

        @Schema(description = "状态")
        private String statusLabel;

        @Schema(description = "用户数量")
        private Long userCount;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

        @Schema(description = "备注")
        private String remark;
    }

    /**
     * 角色导入DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色导入数据")
    public static class ImportDTO {

        @NotBlank(message = "角色名称不能为空")
        @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String roleName;

        @NotBlank(message = "角色权限字符串不能为空")
        @Schema(description = "角色权限字符串", requiredMode = Schema.RequiredMode.REQUIRED)
        private String roleKey;

        @Schema(description = "显示顺序")
        private Integer roleSort;

        @Schema(description = "数据范围")
        private String dataScope;

        @Schema(description = "状态")
        private String status;

        @Schema(description = "备注")
        private String remark;
    }
}