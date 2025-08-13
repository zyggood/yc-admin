package com.yc.admin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数据传输对象
 *
 * @author YC
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户数据传输对象")
public class UserDTO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别：0=男,1=女,2=未知")
    private String sex;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "用户状态：0=正常,1=停用")
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

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;

    @Schema(description = "角色名称列表")
    private List<String> roleNames;

    // ==================== 查询条件DTO ====================

    /**
     * 用户查询条件DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户查询条件")
    public static class QueryDTO {

        @Schema(description = "用户名关键字")
        private String userName;

        @Schema(description = "昵称关键字")
        private String nickName;

        @Schema(description = "手机号关键字")
        private String phone;

        @Schema(description = "邮箱关键字")
        private String email;

        @Schema(description = "用户状态")
        private String status;

        @Schema(description = "部门ID")
        private Long deptId;

        @Schema(description = "角色ID")
        private Long roleId;

        @Schema(description = "创建时间开始")
        private LocalDateTime createTimeStart;

        @Schema(description = "创建时间结束")
        private LocalDateTime createTimeEnd;
    }

    // ==================== 创建/更新DTO ====================

    /**
     * 用户创建DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户创建数据")
    public static class CreateDTO {

        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 30, message = "用户名长度必须在2-30个字符之间")
        @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
        private String userName;

        @NotBlank(message = "昵称不能为空")
        @Size(min = 2, max = 30, message = "昵称长度必须在2-30个字符之间")
        @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String nickName;

        @Email(message = "邮箱格式不正确")
        @Schema(description = "邮箱")
        private String email;

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        @Schema(description = "手机号")
        private String phone;

        @Pattern(regexp = "^[012]$", message = "性别值不正确")
        @Schema(description = "性别：0=男,1=女,2=未知")
        private String sex;

        @Schema(description = "头像地址")
        private String avatar;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
        @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
        private String password;

        @Pattern(regexp = "^[01]$", message = "状态值不正确")
        @Schema(description = "用户状态：0=正常,1=停用")
        private String status = "0";

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "部门ID")
        private Long deptId;

        @Schema(description = "角色ID列表")
        private List<Long> roleIds;
    }

    /**
     * 用户更新DTO
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "用户更新数据")
    public static class UpdateDTO extends CreateDTO {

        @NotNull(message = "用户ID不能为空")
        @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;
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

        @NotEmpty(message = "用户ID列表不能为空")
        @Schema(description = "用户ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<Long> userIds;

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

        @NotEmpty(message = "用户ID列表不能为空")
        @Schema(description = "用户ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<Long> userIds;
    }

    // ==================== 密码相关DTO ====================

    /**
     * 密码重置DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "密码重置数据")
    public static class PasswordResetDTO {

        @NotNull(message = "用户ID不能为空")
        @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long userId;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
        @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
        private String newPassword;
    }

    /**
     * 密码修改DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "密码修改数据")
    public static class PasswordChangeDTO {

        @NotBlank(message = "原密码不能为空")
        @Schema(description = "原密码", requiredMode = Schema.RequiredMode.REQUIRED)
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
        @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
        private String newPassword;

        @NotBlank(message = "确认密码不能为空")
        @Schema(description = "确认密码", requiredMode = Schema.RequiredMode.REQUIRED)
        private String confirmPassword;
    }

    // ==================== 统计相关DTO ====================

    /**
     * 用户统计DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户统计数据")
    public static class StatisticsDTO {

        @Schema(description = "总用户数")
        private Long totalUsers;

        @Schema(description = "正常用户数")
        private Long normalUsers;

        @Schema(description = "停用用户数")
        private Long disabledUsers;

        @Schema(description = "今日新增用户数")
        private Long todayNewUsers;

        @Schema(description = "本月新增用户数")
        private Long monthNewUsers;
    }

    // ==================== 导入导出DTO ====================

    /**
     * 用户导出DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户导出数据")
    public static class ExportDTO {

        @Schema(description = "用户名")
        private String userName;

        @Schema(description = "昵称")
        private String nickName;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "手机号")
        private String phone;

        @Schema(description = "性别")
        private String sexLabel;

        @Schema(description = "状态")
        private String statusLabel;

        @Schema(description = "部门名称")
        private String deptName;

        @Schema(description = "角色名称")
        private String roleNames;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

        @Schema(description = "注释")
        private String remark;
    }

    /**
     * 用户导入DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户导入数据")
    public static class ImportDTO {

        @NotBlank(message = "用户名不能为空")
        @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
        private String userName;

        @NotBlank(message = "昵称不能为空")
        @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String nickName;

        @Email(message = "邮箱格式不正确")
        @Schema(description = "邮箱")
        private String email;

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        @Schema(description = "手机号")
        private String phone;

        @Schema(description = "性别")
        private String sex;

        @Schema(description = "部门名称")
        private String deptName;

        @Schema(description = "角色名称（多个用逗号分隔）")
        private String roleNames;
    }

    // ==================== 选择器DTO ====================

    /**
     * 用户选择器DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户选择器数据")
    public static class SelectorDTO {

        @Schema(description = "用户ID")
        private Long id;

        @Schema(description = "用户名")
        private String userName;

        @Schema(description = "昵称")
        private String nickName;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "手机号")
        private String phone;

        @Schema(description = "部门名称")
        private String deptName;

        @Schema(description = "用户状态：0=正常,1=停用")
        private String status;
    }
}