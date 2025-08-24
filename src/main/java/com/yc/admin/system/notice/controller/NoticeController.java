package com.yc.admin.system.notice.controller;

import com.yc.admin.common.core.Result;
import com.yc.admin.system.notice.dto.NoticeDto;
import com.yc.admin.system.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知公告控制器
 *
 * @author yc
 */
@Slf4j
@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
@Tag(name = "通知公告管理", description = "通知公告的增删改查和状态管理")
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 分页查询公告列表
     */
    @GetMapping
    @Operation(summary = "分页查询公告列表", description = "支持按标题、类型、状态、时间范围等条件查询")
    @PreAuthorize("hasAuthority('system:notice:list')")
    public Result<Page<NoticeDto>> findPage(
            @Parameter(description = "查询条件") NoticeDto.Query queryDto,
            @Parameter(description = "分页参数") @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("分页查询公告列表，查询条件: {}, 分页参数: {}", queryDto, pageable);
        Page<NoticeDto> result = noticeService.findPage(queryDto, pageable);
        return Result.success(result);
    }

    /**
     * 查询已发布的公告列表（前台使用）
     */
    @GetMapping("/published")
    @Operation(summary = "查询已发布的公告列表", description = "前台展示用，只返回已发布的公告")
    public Result<Page<NoticeDto>> findPublishedNotices(
            @Parameter(description = "分页参数") @PageableDefault(size = 10) Pageable pageable) {
        
        log.info("查询已发布公告列表，分页参数: {}", pageable);
        Page<NoticeDto> result = noticeService.findPublishedNotices(pageable);
        return Result.success(result);
    }

    /**
     * 根据ID查询公告详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询公告详情", description = "根据ID查询公告的详细信息")
    @PreAuthorize("hasAuthority('system:notice:query')")
    public Result<NoticeDto> findById(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        
        log.info("查询公告详情，ID: {}", id);
        return noticeService.findById(id)
                .map(Result::success)
                .orElse(Result.error("公告不存在"));
    }

    /**
     * 创建公告
     */
    @PostMapping
    @Operation(summary = "创建公告", description = "新增一条公告信息")
    @PreAuthorize("hasAuthority('system:notice:add')")
    public Result<NoticeDto> create(
            @Parameter(description = "公告创建信息") @Valid @RequestBody NoticeDto.Create createDto) {
        
        log.info("创建公告，标题: {}", createDto.getNoticeTitle());
        NoticeDto result = noticeService.create(createDto);
        return Result.success(result);
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新公告", description = "修改公告信息")
    @PreAuthorize("hasAuthority('system:notice:edit')")
    public Result<NoticeDto> update(
            @Parameter(description = "公告ID") @PathVariable Long id,
            @Parameter(description = "公告更新信息") @Valid @RequestBody NoticeDto.Update updateDto) {
        
        // 确保路径参数和请求体中的ID一致
        updateDto.setId(id);
        
        log.info("更新公告，ID: {}, 标题: {}", id, updateDto.getNoticeTitle());
        NoticeDto result = noticeService.update(updateDto);
        return Result.success(result);
    }

    /**
     * 更新公告状态
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "更新公告状态", description = "修改公告的发布状态")
    @PreAuthorize("hasAuthority('system:notice:edit')")
    public Result<NoticeDto> updateStatus(
            @Parameter(description = "公告ID") @PathVariable Long id,
            @Parameter(description = "状态更新信息") @Valid @RequestBody NoticeDto.StatusUpdate statusUpdateDto) {
        
        // 确保路径参数和请求体中的ID一致
        statusUpdateDto.setId(id);
        
        log.info("更新公告状态，ID: {}, 新状态: {}", id, statusUpdateDto.getStatus());
        NoticeDto result = noticeService.updateStatus(statusUpdateDto);
        return Result.success(result);
    }

    /**
     * 发布公告
     */
    @PostMapping("/{id}/publish")
    @Operation(summary = "发布公告", description = "将草稿状态的公告发布")
    @PreAuthorize("hasAuthority('system:notice:edit')")
    public Result<NoticeDto> publish(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        
        log.info("发布公告，ID: {}", id);
        NoticeDto result = noticeService.publish(id);
        return Result.success(result);
    }

    /**
     * 关闭公告
     */
    @PostMapping("/{id}/close")
    @Operation(summary = "关闭公告", description = "将发布状态的公告关闭")
    @PreAuthorize("hasAuthority('system:notice:edit')")
    public Result<NoticeDto> close(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        
        log.info("关闭公告，ID: {}", id);
        NoticeDto result = noticeService.close(id);
        return Result.success(result);
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告", description = "逻辑删除指定的公告")
    @PreAuthorize("hasAuthority('system:notice:remove')")
    public Result<Void> delete(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        
        log.info("删除公告，ID: {}", id);
        noticeService.delete(id);
        return Result.success();
    }

    /**
     * 批量更新公告状态
     */
    @PatchMapping("/batch/status")
    @Operation(summary = "批量更新公告状态", description = "批量修改多个公告的状态")
    @PreAuthorize("hasAuthority('system:notice:edit')")
    public Result<Integer> batchUpdateStatus(
            @Parameter(description = "公告ID列表") @RequestParam List<Long> ids,
            @Parameter(description = "新状态") @RequestParam Integer status) {
        
        log.info("批量更新公告状态，IDs: {}, 新状态: {}", ids, status);
        // TODO: 获取当前用户信息
        String updateBy = "system"; // 临时使用，实际应从安全上下文获取
        int updateCount = noticeService.batchUpdateStatus(ids, status, updateBy);
        return Result.success(updateCount);
    }

    /**
     * 批量删除公告
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除公告", description = "批量逻辑删除多个公告")
    @PreAuthorize("hasAuthority('system:notice:remove')")
    public Result<Integer> batchDelete(
            @Parameter(description = "公告ID列表") @RequestParam List<Long> ids) {
        
        log.info("批量删除公告，IDs: {}", ids);
        // TODO: 获取当前用户信息
        String updateBy = "system"; // 临时使用，实际应从安全上下文获取
        int deleteCount = noticeService.batchDelete(ids, updateBy);
        return Result.success(deleteCount);
    }

    /**
     * 获取公告统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取公告统计信息", description = "获取各状态和类型的公告数量统计")
    @PreAuthorize("hasAuthority('system:notice:list')")
    public Result<NoticeService.NoticeStatistics> getStatistics() {
        
        log.info("获取公告统计信息");
        NoticeService.NoticeStatistics statistics = noticeService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取公告类型选项
     */
    @GetMapping("/types")
    @Operation(summary = "获取公告类型选项", description = "获取公告类型的枚举值")
    public Result<List<TypeOption>> getNoticeTypes() {
        List<TypeOption> types = List.of(
                new TypeOption(1, "通知"),
                new TypeOption(2, "公告")
        );
        return Result.success(types);
    }

    /**
     * 获取公告状态选项
     */
    @GetMapping("/statuses")
    @Operation(summary = "获取公告状态选项", description = "获取公告状态的枚举值")
    public Result<List<StatusOption>> getNoticeStatuses() {
        List<StatusOption> statuses = List.of(
                new StatusOption(0, "草稿"),
                new StatusOption(1, "发布"),
                new StatusOption(2, "关闭")
        );
        return Result.success(statuses);
    }

    /**
     * 类型选项
     */
    public record TypeOption(Integer value, String label) {}

    /**
     * 状态选项
     */
    public record StatusOption(Integer value, String label) {}
}