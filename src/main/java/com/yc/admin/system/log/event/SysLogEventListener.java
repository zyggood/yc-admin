package com.yc.admin.system.log.event;

import com.yc.admin.system.log.entity.SysLog;
import com.yc.admin.system.log.service.SysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 系统操作日志事件监听器
 * 异步处理系统操作日志记录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysLogEventListener {

    private final SysLogService sysLogService;

    /**
     * 处理系统操作日志事件
     * 使用@ApplicationModuleListener提供更好的模块化支持
     * 自动包含@Async、@Transactional和@TransactionalEventListener功能
     * @param event 系统操作日志事件
     */
    @ApplicationModuleListener
    public void handleSysLogEvent(SysLogEvent event) {
        try {
            SysLog sysLog = (SysLog) event.getSource();
            
            log.debug("处理系统操作日志事件: 标题={}, 方法={}, 耗时={}ms", 
                     sysLog.getTitle(), sysLog.getMethod(), sysLog.getTime());
            
            // 异步保存系统操作日志
            sysLogService.save(sysLog);
            
            log.debug("系统操作日志记录成功: 标题={}, 方法={}", 
                     sysLog.getTitle(), sysLog.getMethod());
            
        } catch (Exception e) {
            log.error("处理系统操作日志事件失败: 错误信息={}", e.getMessage(), e);
            // 记录异常但不抛出，避免影响主业务流程
        }
    }
}
