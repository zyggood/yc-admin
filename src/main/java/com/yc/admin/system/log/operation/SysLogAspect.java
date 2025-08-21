package com.yc.admin.system.log.operation;

import com.yc.admin.system.log.event.SysLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.time.LocalDateTime;

@Aspect
@Slf4j
@RequiredArgsConstructor
public class SysLogAspect {

    private final ApplicationContext applicationContext;

    @Around("@annotation(sysLog))")
    public Object around(ProceedingJoinPoint joinPoint, SysLog sysLog) throws Throwable {
        var className = joinPoint.getTarget().getClass().getName();
        var methodName = joinPoint.getSignature().getName();
        log.debug("class: {}, method: {}, sysLog: {}", className, methodName, sysLog.value());

        var start = LocalDateTime.now();
        var result = joinPoint.proceed();
        var end = LocalDateTime.now();
        var duration = Duration.between(start, end).toMillis();

        var sysLogEntity = new com.yc.admin.system.log.entity.SysLog();
        sysLogEntity.setTitle(sysLog.value())
                .setTime(duration)
                .setMethod(methodName); //TODO add more msg

        var sysLogEvent = new SysLogEvent(sysLogEntity);

        applicationContext.publishEvent(sysLogEvent);


        return result;
    }
}
