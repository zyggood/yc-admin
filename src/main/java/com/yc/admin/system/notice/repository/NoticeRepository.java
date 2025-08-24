package com.yc.admin.system.notice.repository;

import com.yc.admin.system.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 通知公告数据访问接口
 *
 * @author yc
 */
@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long>, JpaSpecificationExecutor<Notice> {

    /**
     * 根据状态查询公告列表
     *
     * @param status 状态
     * @param pageable 分页参数
     * @return 公告分页列表
     */
    Page<Notice> findByStatusAndDelFlagOrderByCreateTimeDesc(Integer status, Integer delFlag, Pageable pageable);

    /**
     * 根据类型查询公告列表
     *
     * @param noticeType 公告类型
     * @param pageable 分页参数
     * @return 公告分页列表
     */
    Page<Notice> findByNoticeTypeAndDelFlagOrderByCreateTimeDesc(Integer noticeType, Integer delFlag, Pageable pageable);

    /**
     * 根据类型和状态查询公告列表
     *
     * @param noticeType 公告类型
     * @param status 状态
     * @param pageable 分页参数
     * @return 公告分页列表
     */
    Page<Notice> findByNoticeTypeAndStatusAndDelFlagOrderByCreateTimeDesc(
            Integer noticeType, Integer status, Integer delFlag, Pageable pageable);

    /**
     * 根据标题模糊查询公告列表
     *
     * @param noticeTitle 公告标题
     * @param pageable 分页参数
     * @return 公告分页列表
     */
    Page<Notice> findByNoticeTitleContainingAndDelFlagOrderByCreateTimeDesc(
            String noticeTitle, Integer delFlag, Pageable pageable);

    /**
     * 查询已发布的公告列表（用于前台展示）
     *
     * @param pageable 分页参数
     * @return 已发布公告分页列表
     */
    @Query("SELECT n FROM Notice n WHERE n.status = :status AND n.delFlag = 0 ORDER BY n.createTime DESC")
    Page<Notice> findPublishedNotices(@Param("status") Integer status, Pageable pageable);

    /**
     * 查询指定时间范围内的公告
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 公告分页列表
     */
    @Query("SELECT n FROM Notice n WHERE n.createTime >= :startTime AND n.createTime <= :endTime " +
           "AND n.delFlag = 0 ORDER BY n.createTime DESC")
    Page<Notice> findByCreateTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 统计各状态的公告数量
     *
     * @param status 状态
     * @return 数量
     */
    @Query("SELECT COUNT(n) FROM Notice n WHERE n.status = :status AND n.delFlag = 0")
    long countByStatus(@Param("status") Integer status);

    /**
     * 统计各类型的公告数量
     *
     * @param noticeType 公告类型
     * @return 数量
     */
    @Query("SELECT COUNT(n) FROM Notice n WHERE n.noticeType = :noticeType AND n.delFlag = 0")
    long countByNoticeType(@Param("noticeType") Integer noticeType);

    /**
     * 批量更新公告状态
     *
     * @param ids 公告ID列表
     * @param status 新状态
     * @param updateBy 更新人
     * @return 更新数量
     */
    @Modifying
    @Query("UPDATE Notice n SET n.status = :status, n.updateBy = :updateBy, n.updateTime = CURRENT_TIMESTAMP " +
           "WHERE n.id IN :ids AND n.delFlag = 0")
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status, @Param("updateBy") String updateBy);

    /**
     * 逻辑删除公告
     *
     * @param ids 公告ID列表
     * @param updateBy 更新人
     * @return 删除数量
     */
    @Modifying
    @Query("UPDATE Notice n SET n.delFlag = 1, n.updateBy = :updateBy, n.updateTime = CURRENT_TIMESTAMP " +
           "WHERE n.id IN :ids AND n.delFlag = 0")
    int batchLogicalDelete(@Param("ids") List<Long> ids, @Param("updateBy") String updateBy);

    /**
     * 查询未删除的公告（重写父类方法）
     *
     * @param id 主键ID
     * @return 公告实体
     */
    @Query("SELECT n FROM Notice n WHERE n.id = :id AND n.delFlag = 0")
    Optional<Notice> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * 查询所有未删除的公告
     *
     * @param pageable 分页参数
     * @return 公告分页列表
     */
    @Query("SELECT n FROM Notice n WHERE n.delFlag = 0 ORDER BY n.createTime DESC")
    Page<Notice> findAllNotDeleted(Pageable pageable);

    /**
     * 检查公告标题是否已存在（排除指定ID）
     *
     * @param noticeTitle 公告标题
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(n) > 0 FROM Notice n WHERE n.noticeTitle = :noticeTitle " +
           "AND (:excludeId IS NULL OR n.id != :excludeId) AND n.delFlag = 0")
    boolean existsByNoticeTitleAndIdNot(@Param("noticeTitle") String noticeTitle, @Param("excludeId") Long excludeId);
}