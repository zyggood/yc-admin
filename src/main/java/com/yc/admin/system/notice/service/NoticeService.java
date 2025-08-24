package com.yc.admin.system.notice.service;

import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.system.notice.dto.NoticeDto;
import com.yc.admin.system.notice.dto.NoticeDtoConverter;
import com.yc.admin.system.notice.entity.Notice;
import com.yc.admin.system.notice.repository.NoticeRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 通知公告服务类
 *
 * @author yc
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeDtoConverter noticeDtoConverter;

    /**
     * 分页查询公告列表
     *
     * @param queryDto 查询条件
     * @param pageable 分页参数
     * @return 公告分页列表
     */
    public Page<NoticeDto> findPage(NoticeDto.Query queryDto, Pageable pageable) {
        Specification<Notice> spec = buildSpecification(queryDto);
        Page<Notice> noticePage = noticeRepository.findAll(spec, pageable);
        return noticePage.map(noticeDtoConverter::toDto);
    }

    /**
     * 查询已发布的公告列表（前台使用）
     *
     * @param pageable 分页参数
     * @return 已发布公告分页列表
     */
    public Page<NoticeDto> findPublishedNotices(Pageable pageable) {
        Page<Notice> noticePage = noticeRepository.findPublishedNotices(Notice.Status.PUBLISHED, pageable);
        return noticePage.map(noticeDtoConverter::toSimpleDto);
    }

    /**
     * 根据ID查询公告详情
     *
     * @param id 公告ID
     * @return 公告详情
     */
    public Optional<NoticeDto> findById(Long id) {
        return noticeRepository.findByIdAndNotDeleted(id)
                .map(noticeDtoConverter::toDto);
    }

    /**
     * 创建公告
     *
     * @param createDto 创建DTO
     * @return 创建的公告
     */
    @Transactional
    public NoticeDto create(NoticeDto.Create createDto) {
        log.info("创建公告: {}", createDto.getNoticeTitle());
        
        // 检查标题是否重复
        if (noticeRepository.existsByNoticeTitleAndIdNot(createDto.getNoticeTitle(), null)) {
            throw new BusinessException("公告标题已存在");
        }
        
        // 转换并保存
        Notice notice = noticeDtoConverter.toEntity(createDto);
        Notice savedNotice = noticeRepository.save(notice);
        
        log.info("公告创建成功，ID: {}", savedNotice.getId());
        return noticeDtoConverter.toDto(savedNotice);
    }

    /**
     * 更新公告
     *
     * @param updateDto 更新DTO
     * @return 更新后的公告
     */
    @Transactional
    public NoticeDto update(NoticeDto.Update updateDto) {
        log.info("更新公告，ID: {}", updateDto.getId());
        
        // 查询现有公告
        Notice existingNotice = noticeRepository.findByIdAndNotDeleted(updateDto.getId())
                .orElseThrow(() -> new BusinessException("公告不存在或已删除"));
        
        // 检查标题是否重复（排除当前公告）
        if (noticeRepository.existsByNoticeTitleAndIdNot(updateDto.getNoticeTitle(), updateDto.getId())) {
            throw new BusinessException("公告标题已存在");
        }
        
        // 更新实体
        Notice updatedNotice = noticeDtoConverter.updateEntity(updateDto, existingNotice);
        Notice savedNotice = noticeRepository.save(updatedNotice);
        
        log.info("公告更新成功，ID: {}", savedNotice.getId());
        return noticeDtoConverter.toDto(savedNotice);
    }

    /**
     * 更新公告状态
     *
     * @param statusUpdateDto 状态更新DTO
     * @return 更新后的公告
     */
    @Transactional
    public NoticeDto updateStatus(NoticeDto.StatusUpdate statusUpdateDto) {
        log.info("更新公告状态，ID: {}, 新状态: {}", statusUpdateDto.getId(), statusUpdateDto.getStatus());
        
        // 查询现有公告
        Notice existingNotice = noticeRepository.findByIdAndNotDeleted(statusUpdateDto.getId())
                .orElseThrow(() -> new BusinessException("公告不存在或已删除"));
        
        // 验证状态转换的合法性
        validateStatusTransition(existingNotice.getStatus(), statusUpdateDto.getStatus());
        
        // 更新状态
        Notice updatedNotice = noticeDtoConverter.updateStatus(statusUpdateDto, existingNotice);
        Notice savedNotice = noticeRepository.save(updatedNotice);
        
        log.info("公告状态更新成功，ID: {}, 状态: {} -> {}", 
                savedNotice.getId(), existingNotice.getStatus(), savedNotice.getStatus());
        return noticeDtoConverter.toDto(savedNotice);
    }

    /**
     * 批量更新公告状态
     *
     * @param ids 公告ID列表
     * @param status 新状态
     * @param updateBy 更新人
     * @return 更新数量
     */
    @Transactional
    public int batchUpdateStatus(List<Long> ids, Integer status, String updateBy) {
        log.info("批量更新公告状态，IDs: {}, 新状态: {}", ids, status);
        
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        int updateCount = noticeRepository.batchUpdateStatus(ids, status, updateBy);
        log.info("批量状态更新完成，更新数量: {}", updateCount);
        return updateCount;
    }

    /**
     * 删除公告（逻辑删除）
     *
     * @param id 公告ID
     */
    @Transactional
    public void delete(Long id) {
        log.info("删除公告，ID: {}", id);
        
        Notice notice = noticeRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new BusinessException("公告不存在或已删除"));
        
        // 执行逻辑删除
        notice.markDeleted();
        noticeRepository.save(notice);
        
        log.info("公告删除成功，ID: {}", id);
    }

    /**
     * 批量删除公告（逻辑删除）
     *
     * @param ids 公告ID列表
     * @param updateBy 更新人
     * @return 删除数量
     */
    @Transactional
    public int batchDelete(List<Long> ids, String updateBy) {
        log.info("批量删除公告，IDs: {}", ids);
        
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        int deleteCount = noticeRepository.batchLogicalDelete(ids, updateBy);
        log.info("批量删除完成，删除数量: {}", deleteCount);
        return deleteCount;
    }

    /**
     * 发布公告
     *
     * @param id 公告ID
     * @return 发布后的公告
     */
    @Transactional
    public NoticeDto publish(Long id) {
        log.info("发布公告，ID: {}", id);
        
        Notice notice = noticeRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new BusinessException("公告不存在或已删除"));
        
        // 只有草稿状态的公告才能发布
        if (!notice.isDraft()) {
            throw new BusinessException("只有草稿状态的公告才能发布");
        }
        
        notice.setStatus(Notice.Status.PUBLISHED);
        Notice savedNotice = noticeRepository.save(notice);
        
        log.info("公告发布成功，ID: {}", savedNotice.getId());
        return noticeDtoConverter.toDto(savedNotice);
    }

    /**
     * 关闭公告
     *
     * @param id 公告ID
     * @return 关闭后的公告
     */
    @Transactional
    public NoticeDto close(Long id) {
        log.info("关闭公告，ID: {}", id);
        
        Notice notice = noticeRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new BusinessException("公告不存在或已删除"));
        
        // 只有发布状态的公告才能关闭
        if (!notice.isPublished()) {
            throw new BusinessException("只有发布状态的公告才能关闭");
        }
        
        notice.setStatus(Notice.Status.CLOSED);
        Notice savedNotice = noticeRepository.save(notice);
        
        log.info("公告关闭成功，ID: {}", savedNotice.getId());
        return noticeDtoConverter.toDto(savedNotice);
    }

    /**
     * 获取公告统计信息
     *
     * @return 统计信息
     */
    public NoticeStatistics getStatistics() {
        long draftCount = noticeRepository.countByStatus(Notice.Status.DRAFT);
        long publishedCount = noticeRepository.countByStatus(Notice.Status.PUBLISHED);
        long closedCount = noticeRepository.countByStatus(Notice.Status.CLOSED);
        long noticeCount = noticeRepository.countByNoticeType(Notice.Type.NOTICE);
        long announcementCount = noticeRepository.countByNoticeType(Notice.Type.ANNOUNCEMENT);
        
        return new NoticeStatistics(draftCount, publishedCount, closedCount, noticeCount, announcementCount);
    }

    /**
     * 构建查询条件
     */
    private Specification<Notice> buildSpecification(NoticeDto.Query queryDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 未删除
            predicates.add(criteriaBuilder.equal(root.get("delFlag"), 0));
            
            // 标题模糊查询
            if (StringUtils.hasText(queryDto.getNoticeTitle())) {
                predicates.add(criteriaBuilder.like(
                        root.get("noticeTitle"), 
                        "%" + queryDto.getNoticeTitle() + "%"
                ));
            }
            
            // 公告类型
            if (queryDto.getNoticeType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("noticeType"), queryDto.getNoticeType()));
            }
            
            // 公告状态
            if (queryDto.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), queryDto.getStatus()));
            }
            
            // 创建时间范围
            if (queryDto.getCreateTimeStart() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createTime"), queryDto.getCreateTimeStart()
                ));
            }
            if (queryDto.getCreateTimeEnd() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createTime"), queryDto.getCreateTimeEnd()
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 验证状态转换的合法性
     */
    private void validateStatusTransition(Integer currentStatus, Integer newStatus) {
        // 草稿 -> 发布、关闭
        if (Notice.Status.DRAFT == currentStatus) {
            if (newStatus != Notice.Status.PUBLISHED && newStatus != Notice.Status.CLOSED) {
                throw new BusinessException("草稿状态只能转换为发布或关闭状态");
            }
        }
        // 发布 -> 关闭
        else if (Notice.Status.PUBLISHED == currentStatus) {
            if (newStatus != Notice.Status.CLOSED) {
                throw new BusinessException("发布状态只能转换为关闭状态");
            }
        }
        // 关闭状态不能转换
        else if (Notice.Status.CLOSED == currentStatus) {
            throw new BusinessException("关闭状态的公告不能再次修改状态");
        }
    }

    /**
     * 公告统计信息
     */
    public record NoticeStatistics(
            long draftCount,
            long publishedCount,
            long closedCount,
            long noticeCount,
            long announcementCount
    ) {}
}