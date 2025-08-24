package com.yc.admin.system.notice.dto;

import com.yc.admin.system.notice.entity.Notice;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知公告DTO转换器
 *
 * @author yc
 */
@UtilityClass
public class NoticeDtoConverter {

    /**
     * 实体转DTO
     *
     * @param notice 实体
     * @return DTO
     */
    public NoticeDto toDto(Notice notice) {
        if (notice == null) {
            return null;
        }

        return new NoticeDto()
                .setId(notice.getId())
                .setNoticeTitle(notice.getNoticeTitle())
                .setNoticeType(notice.getNoticeType())
                .setNoticeTypeDesc(notice.getNoticeTypeDesc())
                .setNoticeContent(notice.getNoticeContent())
                .setStatus(notice.getStatus())
                .setStatusDesc(notice.getStatusDesc())
                .setRemark(notice.getRemark())
                .setCreateTime(notice.getCreateTime())
                .setUpdateTime(notice.getUpdateTime())
                .setCreateBy(notice.getCreateBy())
                .setUpdateBy(notice.getUpdateBy());
    }

    /**
     * 实体列表转DTO列表
     *
     * @param notices 实体列表
     * @return DTO列表
     */
    public List<NoticeDto> toDtoList(List<Notice> notices) {
        if (notices == null || notices.isEmpty()) {
            return List.of();
        }

        return notices.stream()
                .map(NoticeDtoConverter::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 创建DTO转实体
     *
     * @param createDto 创建DTO
     * @return 实体
     */
    public Notice toEntity(NoticeDto.Create createDto) {
        if (createDto == null) {
            return null;
        }

        return new Notice()
                .setNoticeTitle(createDto.getNoticeTitle())
                .setNoticeType(createDto.getNoticeType())
                .setNoticeContent(createDto.getNoticeContent())
                .setStatus(createDto.getStatus())
                .setRemark(createDto.getRemark());
    }

    /**
     * 更新DTO转实体（用于更新操作）
     *
     * @param updateDto 更新DTO
     * @param existingNotice 现有实体
     * @return 更新后的实体
     */
    public Notice updateEntity(NoticeDto.Update updateDto, Notice existingNotice) {
        if (updateDto == null || existingNotice == null) {
            return existingNotice;
        }

        return existingNotice
                .setNoticeTitle(updateDto.getNoticeTitle())
                .setNoticeType(updateDto.getNoticeType())
                .setNoticeContent(updateDto.getNoticeContent())
                .setStatus(updateDto.getStatus())
                .setRemark(updateDto.getRemark());
    }

    /**
     * 状态更新DTO转实体（用于状态更新操作）
     *
     * @param statusUpdateDto 状态更新DTO
     * @param existingNotice 现有实体
     * @return 更新后的实体
     */
    public Notice updateStatus(NoticeDto.StatusUpdate statusUpdateDto, Notice existingNotice) {
        if (statusUpdateDto == null || existingNotice == null) {
            return existingNotice;
        }

        return existingNotice.setStatus(statusUpdateDto.getStatus());
    }

    /**
     * 简化转换 - 仅包含基本信息（用于列表显示）
     *
     * @param notice 实体
     * @return 简化DTO
     */
    public NoticeDto toSimpleDto(Notice notice) {
        if (notice == null) {
            return null;
        }

        return new NoticeDto()
                .setId(notice.getId())
                .setNoticeTitle(notice.getNoticeTitle())
                .setNoticeType(notice.getNoticeType())
                .setNoticeTypeDesc(notice.getNoticeTypeDesc())
                .setStatus(notice.getStatus())
                .setStatusDesc(notice.getStatusDesc())
                .setCreateTime(notice.getCreateTime())
                .setCreateBy(notice.getCreateBy());
    }

    /**
     * 简化转换列表
     *
     * @param notices 实体列表
     * @return 简化DTO列表
     */
    public List<NoticeDto> toSimpleDtoList(List<Notice> notices) {
        if (notices == null || notices.isEmpty()) {
            return List.of();
        }

        return notices.stream()
                .map(NoticeDtoConverter::toSimpleDto)
                .collect(Collectors.toList());
    }
}