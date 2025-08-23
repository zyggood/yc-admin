package com.yc.admin.system.config.controller;

import com.yc.admin.common.core.Result;
import com.yc.admin.system.config.dto.ConfigDto;
import com.yc.admin.system.config.entity.Config;
import com.yc.admin.system.config.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统参数配置控制器
 *
 * @author YC
 * @since 1.0.0
 */
@Tag(name = "系统参数配置管理", description = "系统参数配置的增删改查操作")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
@Validated
public class ConfigController {

    private final ConfigService configService;

    /**
     * 分页查询系统参数
     */
    @Operation(summary = "分页查询系统参数", description = "根据条件分页查询系统参数配置")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:config:list')")
    public Result<Page<Config>> page(
            ConfigDto.QueryDto queryDto,
            @Parameter(description = "页码（从0开始）")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(configService.page(queryDto, PageRequest.of(page, size)));
    }

    /**
     * 根据参数键获取参数值
     */
    @Operation(summary = "获取参数值", description = "根据参数键获取参数值")
    @GetMapping("/value/{configKey}")
    public Result<String> getConfigValue(
            @Parameter(description = "参数键")
            @PathVariable String configKey) {
        return Result.success(configService.getConfigValue(configKey));
    }

    /**
     * 根据参数键获取参数详情
     */
    @Operation(summary = "获取参数详情", description = "根据参数键获取参数详细信息")
    @GetMapping("/detail/{configKey}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<ConfigDto.ValueDto> getConfigDetail(
            @Parameter(description = "参数键")
            @PathVariable String configKey) {
        Config config = configService.findByConfigKey(configKey);
        if (config == null) {
            return Result.error("参数不存在");
        }
        return Result.success(com.yc.admin.system.config.dto.ConfigDtoConverter.toValueDto(config));
    }

    /**
     * 批量获取参数值
     */
    @Operation(summary = "批量获取参数值", description = "根据参数键列表批量获取参数值")
    @PostMapping("/values")
    public Result<Map<String, String>> getConfigValues(
            @Parameter(description = "参数键列表")
            @RequestBody List<String> configKeys) {
        return Result.success(configService.getConfigValues(configKeys));
    }

    /**
     * 根据ID查询参数
     */
    @Operation(summary = "查询参数详情", description = "根据ID查询参数详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<Config> getById(
            @Parameter(description = "参数ID")
            @PathVariable Long id) {
        return Result.success(configService.findById(id));
    }

    /**
     * 根据参数类型查询参数列表
     */
    @Operation(summary = "按类型查询参数", description = "根据参数类型查询参数列表")
    @GetMapping("/type/{configType}")
    @PreAuthorize("hasAuthority('system:config:list')")
    public Result<List<Config>> getByType(
            @Parameter(description = "参数类型")
            @PathVariable String configType) {
        return Result.success(configService.findByConfigType(configType));
    }

    /**
     * 创建参数配置
     */
    @Operation(summary = "创建参数配置", description = "新增系统参数配置")
    @PostMapping
    @PreAuthorize("hasAuthority('system:config:add')")
    public Result<Config> create(
            @Parameter(description = "创建参数")
            @RequestBody @Validated ConfigDto.CreateDto createDto) {
        return Result.success(configService.create(createDto));
    }

    /**
     * 更新参数配置
     */
    @Operation(summary = "更新参数配置", description = "修改系统参数配置")
    @PutMapping
    @PreAuthorize("hasAuthority('system:config:edit')")
    public Result<Config> update(
            @Parameter(description = "更新参数")
            @RequestBody @Validated ConfigDto.UpdateDto updateDto) {
        return Result.success(configService.update(updateDto));
    }

    /**
     * 删除参数配置
     */
    @Operation(summary = "删除参数配置", description = "删除系统参数配置")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:config:remove')")
    public Result<String> delete(
            @Parameter(description = "参数ID")
            @PathVariable Long id) {
        configService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除参数配置
     */
    @Operation(summary = "批量删除参数配置", description = "批量删除系统参数配置")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:config:remove')")
    public Result<String> deleteBatch(
            @Parameter(description = "参数ID列表")
            @RequestParam List<Long> ids) {
        configService.deleteBatch(ids);
        return Result.success("删除成功");
    }

    /**
     * 刷新参数缓存
     */
    @Operation(summary = "刷新参数缓存", description = "刷新系统参数缓存")
    @PostMapping("/cache/refresh")
    @PreAuthorize("hasAuthority('system:config:edit')")
    public Result<String> refreshCache(
            @Parameter(description = "刷新参数")
            @RequestBody(required = false) ConfigDto.CacheRefreshDto refreshDto) {
        if (refreshDto == null) {
            refreshDto = new ConfigDto.CacheRefreshDto();
        }
        configService.refreshCache(refreshDto);
        return Result.success("缓存刷新成功");
    }

    /**
     * 获取所有启用的参数
     */
    @Operation(summary = "获取所有启用参数", description = "获取所有启用状态的参数配置")
    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('system:config:list')")
    public Result<List<Config>> getAllEnabled() {
        return Result.success(configService.findAllEnabled());
    }
}