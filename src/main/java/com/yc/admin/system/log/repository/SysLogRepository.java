package com.yc.admin.system.log.repository;

import com.yc.admin.system.log.entity.SysLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysLogRepository extends JpaRepository<SysLog, Long> {

}
