package com.yc.admin.system.dict.repository;

import com.yc.admin.system.dict.dto.DictDto;
import com.yc.admin.system.dict.entity.Dict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DictRepository extends JpaRepository<Dict, Long> {

    @Query("SELECT d FROM Dict d WHERE " +
            "(:#{#queryDto.code} IS NULL OR d.code LIKE %:#{#queryDto.code}%) AND " +
            "(:#{#queryDto.type} IS NULL OR d.type LIKE %:#{#queryDto.type}%) AND " +
            "(:#{#queryDto.value} IS NULL OR d.value = :#{#queryDto.value})")
    Page<Dict> findByQueryDto(@Param("queryDto") DictDto.QueryDto queryDto, Pageable pageable);

    List<Dict> findByType(String type);
}
