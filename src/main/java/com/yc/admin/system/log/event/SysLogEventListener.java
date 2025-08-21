package com.yc.admin.system.log.event;

import com.yc.admin.system.log.entity.SysLog;
import com.yc.admin.system.log.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;

@RequiredArgsConstructor
public class SysLogEventListener {

    private final SysLogService sysLogService;

    @EventListener(SysLog.class)
    @Async
    @Order
    public void handleSysLogEvent(SysLogEvent event) {
        SysLog sysLog = (SysLog) event.getSource();

        sysLogService.save(sysLog);
    }
}
