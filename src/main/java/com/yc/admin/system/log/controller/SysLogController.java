package com.yc.admin.system.log.controller;

import com.yc.admin.common.core.Result;
import com.yc.admin.system.log.entity.SysLog;
import com.yc.admin.system.log.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLogService sysLogService;

    @GetMapping("/page")
    public Result<Page<SysLog>> page(PageRequest page, SysLog sysLog) {
        Page<SysLog> result = sysLogService.page(page);
        return Result.success(result);
    }


}
