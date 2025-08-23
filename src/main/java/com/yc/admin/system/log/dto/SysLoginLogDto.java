package com.yc.admin.system.log.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 系统登录日志查询DTO
 *
 * @author yc
 * @since 2024-01-01
 */
@Data
@Schema(description = "登录日志查询条件")
public class SysLoginLogDto {

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "登录IP地址", example = "192.168.1.100")
    private String ipAddr;

    @Schema(description = "登录状态（0成功 1失败）", example = "0")
    private Integer status;

    @Schema(description = "登录类型（login-登录，logout-登出）", example = "login")
    private String loginType;

    @Schema(description = "开始时间", example = "2024-01-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2024-01-31 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}