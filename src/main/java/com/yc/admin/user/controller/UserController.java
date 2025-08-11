package com.yc.admin.user.controller;

import com.yc.admin.common.core.Result;
import com.yc.admin.user.entity.User;
import com.yc.admin.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 *
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {

    private final UserService userService;

    // ==================== 查询接口 ====================

    /**
     * 分页查询用户列表
     *
     * @param page     页码（从0开始）
     * @param size     每页大小
     * @param userName 用户名关键字
     * @param nickName 昵称关键字
     * @param phone    手机号关键字
     * @param status   用户状态
     * @return 用户分页列表
     */
    @Operation(summary = "分页查询用户列表", description = "根据条件分页查询用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping
    public ResponseEntity<Result<Page<User>>> getUserList(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "用户名关键字") @RequestParam(required = false) String userName,
            @Parameter(description = "昵称关键字") @RequestParam(required = false) String nickName,
            @Parameter(description = "手机号关键字") @RequestParam(required = false) String phone,
            @Parameter(description = "用户状态") @RequestParam(required = false) String status) {

        log.info("查询用户列表 - 页码: {}, 大小: {}, 用户名: {}, 昵称: {}, 手机号: {}, 状态: {}",
                page, size, userName, nickName, phone, status);

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage;

        // 根据查询条件选择不同的查询方法
        if (StringUtils.hasText(userName) || StringUtils.hasText(nickName) ||
                StringUtils.hasText(phone) || StringUtils.hasText(status)) {
            userPage = userService.findByConditions(userName, nickName, phone, status, pageable);
        } else {
            userPage = userService.findAll(pageable);
        }

        return ResponseEntity.ok(Result.success(userPage));
    }

    /**
     * 根据ID查询用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    @Operation(summary = "查询用户详情", description = "根据用户ID查询用户详细信息")
    @GetMapping("/{userId}")
    public ResponseEntity<Result<User>> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("查询用户详情 - ID: {}", userId);

        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(Result.success(user)))
                .orElse(ResponseEntity.ok(Result.error("用户不存在")));
    }

    /**
     * 检查用户名是否可用
     *
     * @param userName      用户名
     * @param excludeUserId 排除的用户ID（可选）
     * @return 是否可用
     */
    @GetMapping("/check-username")
    public ResponseEntity<Result<Boolean>> checkUserName(
            @RequestParam String userName,
            @RequestParam(required = false) Long excludeUserId) {

        log.info("检查用户名可用性 - 用户名: {}, 排除ID: {}", userName, excludeUserId);

        boolean available = userService.isUserNameAvailable(userName, excludeUserId);
        return ResponseEntity.ok(Result.success(available));
    }

    /**
     * 检查邮箱是否可用
     *
     * @param email         邮箱
     * @param excludeUserId 排除的用户ID（可选）
     * @return 是否可用
     */
    @GetMapping("/check-email")
    public ResponseEntity<Result<Boolean>> checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) Long excludeUserId) {

        log.info("检查邮箱可用性 - 邮箱: {}, 排除ID: {}", email, excludeUserId);

        boolean available = userService.isEmailAvailable(email, excludeUserId);
        return ResponseEntity.ok(Result.success(available));
    }

    /**
     * 检查手机号是否可用
     *
     * @param phone         手机号
     * @param excludeUserId 排除的用户ID（可选）
     * @return 是否可用
     */
    @GetMapping("/check-phone")
    public ResponseEntity<Result<Boolean>> checkPhone(
            @RequestParam String phone,
            @RequestParam(required = false) Long excludeUserId) {

        log.info("检查手机号可用性 - 手机号: {}, 排除ID: {}", phone, excludeUserId);

        boolean available = userService.isPhoneAvailable(phone, excludeUserId);
        return ResponseEntity.ok(Result.success(available));
    }

    /**
     * 获取用户统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Result<Map<String, Object>>> getUserStatistics() {
        log.info("获取用户统计信息");

        long normalUserCount = userService.countNormalUsers();

        Map<String, Object> statistics = Map.of(
                "normalUserCount", normalUserCount
        );

        return ResponseEntity.ok(Result.success(statistics));
    }

    // ==================== 创建接口 ====================

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 创建的用户
     */
    @PostMapping
    public ResponseEntity<Result<User>> createUser(@Valid @RequestBody User user) {
        log.info("创建用户 - 用户名: {}", user.getUserName());

        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(Result.success("用户创建成功", createdUser));
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    // ==================== 更新接口 ====================

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param user   更新的用户信息
     * @return 更新后的用户
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Result<User>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody User user) {

        log.info("更新用户信息 - ID: {}", userId);

        try {
            User updatedUser = userService.updateUser(userId, user);
            return ResponseEntity.ok(Result.success("用户更新成功", updatedUser));
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 新状态
     * @return 更新结果
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<Result<User>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam String status) {

        log.info("更新用户状态 - ID: {}, 状态: {}", userId, status);

        try {
            User updatedUser = userService.updateUserStatus(userId, status);
            return ResponseEntity.ok(Result.success("用户状态更新成功", updatedUser));
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 批量更新用户状态
     *
     * @param userIds 用户ID列表
     * @param status  新状态
     * @return 更新结果
     */
    @PutMapping("/batch/status")
    public ResponseEntity<Result<Integer>> batchUpdateStatus(
            @RequestBody List<Long> userIds,
            @RequestParam String status) {

        log.info("批量更新用户状态 - IDs: {}, 状态: {}", userIds, status);

        try {
            int updatedCount = userService.batchUpdateStatus(userIds, status);
            return ResponseEntity.ok(Result.success("批量更新成功，共更新 " + updatedCount + " 条记录", updatedCount));
        } catch (Exception e) {
            log.error("批量更新用户状态失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 重置用户密码
     *
     * @param userId      用户ID
     * @param newPassword 新密码
     * @return 重置结果
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<Result<String>> resetPassword(
            @PathVariable Long userId,
            @RequestParam String newPassword) {

        log.info("重置用户密码 - ID: {}", userId);

        try {
            int updatedCount = userService.resetPassword(userId, newPassword);
            if (updatedCount > 0) {
                return ResponseEntity.ok(Result.success("密码重置成功"));
            } else {
                return ResponseEntity.ok(Result.error("密码重置失败"));
            }
        } catch (Exception e) {
            log.error("重置用户密码失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    // ==================== 删除接口 ====================

    /**
     * 删除用户（逻辑删除）
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Result<String>> deleteUser(@PathVariable Long userId) {
        log.info("删除用户 - ID: {}", userId);

        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(Result.success("用户删除成功"));
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 批量删除用户（逻辑删除）
     *
     * @param userIds 用户ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Result<Integer>> batchDeleteUsers(@RequestBody List<Long> userIds) {
        log.info("批量删除用户 - IDs: {}", userIds);

        try {
            int deletedCount = userService.batchDeleteUsers(userIds);
            return ResponseEntity.ok(Result.success("批量删除成功，共删除 " + deletedCount + " 条记录", deletedCount));
        } catch (Exception e) {
            log.error("批量删除用户失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    // ==================== 导出接口 ====================

    /**
     * 导出用户数据
     *
     * @return 用户列表
     */
    @GetMapping("/export")
    public ResponseEntity<Result<List<User>>> exportUsers() {
        log.info("导出用户数据");

        try {
            List<User> users = userService.findAllForExport();
            return ResponseEntity.ok(Result.success("导出成功，共 " + users.size() + " 条记录", users));
        } catch (Exception e) {
            log.error("导出用户数据失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
}