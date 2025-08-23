package com.yc.admin.system.config.repository;

import com.yc.admin.system.config.dto.ConfigDto;
import com.yc.admin.system.config.entity.Config;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统参数配置 Repository
 *
 * @author YC
 * @since 1.0.0
 */
@Repository
public interface ConfigRepository extends JpaRepository<Config, Long> {

    /**
     * 根据参数键查询参数
     *
     * @param configKey 参数键
     * @return 参数配置
     */
    Optional<Config> findByConfigKey(String configKey);

    /**
     * 根据参数键查询启用的参数
     *
     * @param configKey 参数键
     * @return 参数配置
     */
    @Query("SELECT c FROM Config c WHERE c.configKey = :configKey AND c.status = 0 AND c.delFlag = 0")
    Optional<Config> findEnabledByConfigKey(@Param("configKey") String configKey);

    /**
     * 根据参数类型查询参数列表
     *
     * @param configType 参数类型
     * @return 参数列表
     */
    List<Config> findByConfigTypeAndDelFlag(String configType, Integer delFlag);

    /**
     * 查询所有启用的参数
     *
     * @return 参数列表
     */
    @Query("SELECT c FROM Config c WHERE c.status = 0 AND c.delFlag = 0")
    List<Config> findAllEnabled();

    /**
     * 根据查询条件分页查询
     *
     * @param queryDto 查询条件
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT c FROM Config c WHERE " +
            "(:#{#queryDto.configName} IS NULL OR c.configName LIKE %:#{#queryDto.configName}%) AND " +
            "(:#{#queryDto.configKey} IS NULL OR c.configKey LIKE %:#{#queryDto.configKey}%) AND " +
            "(:#{#queryDto.configType} IS NULL OR c.configType = :#{#queryDto.configType}) AND " +
            "(:#{#queryDto.status} IS NULL OR c.status = :#{#queryDto.status}) AND " +
            "c.delFlag = 0 " +
            "ORDER BY c.createTime DESC")
    Page<Config> findByQueryDto(@Param("queryDto") ConfigDto.QueryDto queryDto, Pageable pageable);

    /**
     * 检查参数键是否存在（排除指定ID）
     *
     * @param configKey 参数键
     * @param id        排除的ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(c) > 0 FROM Config c WHERE c.configKey = :configKey AND c.id != :id AND c.delFlag = 0")
    boolean existsByConfigKeyAndIdNot(@Param("configKey") String configKey, @Param("id") Long id);

    /**
     * 检查参数键是否存在
     *
     * @param configKey 参数键
     * @return 是否存在
     */
    @Query("SELECT COUNT(c) > 0 FROM Config c WHERE c.configKey = :configKey AND c.delFlag = 0")
    boolean existsByConfigKey(@Param("configKey") String configKey);

    /**
     * 根据参数键列表查询参数
     *
     * @param configKeys 参数键列表
     * @return 参数列表
     */
    @Query("SELECT c FROM Config c WHERE c.configKey IN :configKeys AND c.status = 0 AND c.delFlag = 0")
    List<Config> findEnabledByConfigKeys(@Param("configKeys") List<String> configKeys);
}