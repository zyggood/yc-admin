package com.yc.admin.system.log.service;

import com.yc.admin.system.log.entity.SysLog;
import com.yc.admin.system.log.repository.SysLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysLogService {

    private final SysLogRepository sysLogRepository;

    public SysLog save(SysLog sysLog) {
        return sysLogRepository.save(sysLog);
    }

    public Page<SysLog> page(Pageable pageable) {
        return sysLogRepository.findAll(pageable);
    }
}
