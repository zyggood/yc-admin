package com.yc.admin.system.notice.entity;

import com.yc.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 通知公告实体类
 *
 * @author yc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "sys_notice", indexes = {
    @Index(name = "idx_notice_type", columnList = "notice_type"),
    @Index(name = "idx_notice_status", columnList = "status"),
    @Index(name = "idx_notice_create_time", columnList = "create_time")
})
public class Notice extends BaseEntity {

    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 100, message = "公告标题长度不能超过100个字符")
    @Column(name = "notice_title", nullable = false, length = 100)
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    @NotNull(message = "公告类型不能为空")
    @Column(name = "notice_type", nullable = false)
    private Integer noticeType;

    /**
     * 公告内容
     */
    @NotBlank(message = "公告内容不能为空")
    @Column(name = "notice_content", nullable = false, columnDefinition = "TEXT")
    private String noticeContent;

    /**
     * 公告状态（0草稿 1发布 2关闭）
     */
    @NotNull(message = "公告状态不能为空")
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 公告类型枚举
     */
    public static class Type {
        /** 通知 */
        public static final int NOTICE = 1;
        /** 公告 */
        public static final int ANNOUNCEMENT = 2;
    }

    /**
     * 公告状态枚举
     */
    public static class Status {
        /** 草稿 */
        public static final int DRAFT = 0;
        /** 发布 */
        public static final int PUBLISHED = 1;
        /** 关闭 */
        public static final int CLOSED = 2;
    }

    /**
     * 获取公告类型描述
     */
    public String getNoticeTypeDesc() {
        return switch (this.noticeType) {
            case Type.NOTICE -> "通知";
            case Type.ANNOUNCEMENT -> "公告";
            default -> "未知";
        };
    }

    /**
     * 获取公告状态描述
     */
    public String getStatusDesc() {
        return switch (this.status) {
            case Status.DRAFT -> "草稿";
            case Status.PUBLISHED -> "发布";
            case Status.CLOSED -> "关闭";
            default -> "未知";
        };
    }

    /**
     * 是否为草稿状态
     */
    public boolean isDraft() {
        return Status.DRAFT == this.status;
    }

    /**
     * 是否为发布状态
     */
    public boolean isPublished() {
        return Status.PUBLISHED == this.status;
    }

    /**
     * 是否为关闭状态
     */
    public boolean isClosed() {
        return Status.CLOSED == this.status;
    }
}