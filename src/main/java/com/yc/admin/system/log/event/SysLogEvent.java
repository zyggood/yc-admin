package com.yc.admin.system.log.event;

import org.springframework.context.ApplicationEvent;

public class SysLogEvent extends ApplicationEvent {
    public SysLogEvent(Object source) {
        super(source);
    }
}
