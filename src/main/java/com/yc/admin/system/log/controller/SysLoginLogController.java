package com.yc.admin.system.log.controller;

import com.yc.admin.common.core.Result;
import com.yc.admin.system.log.dto.SysLoginLogDto;
import com.yc.admin.system.log.entity.SysLoginLog;
import com.yc.admin.system.log.service.SysLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统登录日志控制器
 *
 * @author yc
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/system/loginLog")
@RequiredArgsConstructor
@Tag(name = "系统登录日志", description = "系统登录日志管理")
public class SysLoginLogController {

    private final SysLoginLogService sysLoginLogService;

    /**
     * 分页查询登录日志
     *
     * @param dto 查询条件
     * @return 登录日志分页数据
     */
    @Operation(summary = "分页查询登录日志")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:loginLog:list')")
    public Result<Page<SysLoginLog>> page(SysLoginLogDto dto) {
        // 创建分页参数
        Pageable pageable = PageRequest.of(
                dto.getPageNum() - 1, 
                dto.getPageSize(), 
                Sort.by(Sort.Direction.DESC, "loginTime")
        );
        
        // 执行查询
        Page<SysLoginLog> page = sysLoginLogService.findByConditions(
                dto.getUsername(), 
                dto.getStatus(), 
                dto.getStartTime(), 
                dto.getEndTime(), 
                pageable
        );
        
        return Result.success(page);
    }

    /**
     * 根据ID查询登录日志详情
     *
     * @param id 日志ID
     * @return 登录日志详情
     */
    @Operation(summary = "查询登录日志详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:loginLog:query')")
    public Result<SysLoginLog> getById(
            @Parameter(description = "日志ID") @PathVariable Long id) {
        SysLoginLog loginLog = sysLoginLogService.findById(id);
        if (loginLog == null) {
            return Result.error("登录日志不存在");
        }
        return Result.success(loginLog);
    }

    /**
     * 删除登录日志
     *
     * @param id 日志ID
     * @return 删除结果
     */
    @Operation(summary = "删除登录日志")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:loginLog:remove')")
    public Result<Void> deleteById(
            @Parameter(description = "日志ID") @PathVariable Long id) {
        sysLoginLogService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除登录日志
     *
     * @param ids 日志ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除登录日志")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:loginLog:remove')")
    public Result<Void> deleteBatch(
            @Parameter(description = "日志ID列表") @RequestBody List<Long> ids) {
        for (Long id : ids) {
            sysLoginLogService.deleteById(id);
        }
        return Result.success();
    }

    /**
     * 清理指定天数之前的登录日志
     *
     * @param days 保留天数
     * @return 清理结果
     */
    @Operation(summary = "清理历史登录日志")
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasAuthority('system:loginLog:remove')")
    public Result<Map<String, Object>> cleanup(
            @Parameter(description = "保留天数") @RequestParam(defaultValue = "30") int days) {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(days);
        int deletedCount = sysLoginLogService.cleanupLogsBefore(beforeTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("deletedCount", deletedCount);
        result.put("beforeTime", beforeTime);
        
        return Result.success(result);
    }

    /**
     * 获取登录统计信息
     *
     * @param days 统计天数
     * @return 统计信息
     */
    @Operation(summary = "获取登录统计信息")
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('system:loginLog:list')")
    public Result<Map<String, Object>> getStatistics(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        LocalDateTime endTime = LocalDateTime.now();
        
        Long successCount = sysLoginLogService.countSuccessLogins(startTime, endTime);
        Long failedCount = sysLoginLogService.countFailedLogins(startTime, endTime);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("successCount", successCount);
        statistics.put("failedCount", failedCount);
        statistics.put("totalCount", successCount + failedCount);
        statistics.put("successRate", successCount + failedCount > 0 ? 
                String.format("%.2f%%", (double) successCount / (successCount + failedCount) * 100) : "0.00%");
        statistics.put("startTime", startTime);
        statistics.put("endTime", endTime);
        statistics.put("days", days);
        
        return Result.success(statistics);
    }

    /**
     * 获取用户最近登录记录
     *
     * @param username 用户名
     * @param limit    限制数量
     * @return 最近登录记录
     */
    @Operation(summary = "获取用户最近登录记录")
    @GetMapping("/recent")
    @PreAuthorize("hasAuthority('system:loginLog:list')")
    public Result<List<SysLoginLog>> getRecentLogins(
            @Parameter(description = "用户名") @RequestParam String username,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") int limit) {
        List<SysLoginLog> recentLogins = sysLoginLogService.findRecentLoginsByUsername(username, limit);
        return Result.success(recentLogins);
    }
}