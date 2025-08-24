package com.yc.admin.system.notice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 通知公告DTO
 *
 * @author yc
 */
@Data
@Accessors(chain = true)
public class NoticeDto {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 100, message = "公告标题长度不能超过100个字符")
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    @NotNull(message = "公告类型不能为空")
    private Integer noticeType;

    /**
     * 公告类型描述
     */
    private String noticeTypeDesc;

    /**
     * 公告内容
     */
    @NotBlank(message = "公告内容不能为空")
    private String noticeContent;

    /**
     * 公告状态（0草稿 1发布 2关闭）
     */
    @NotNull(message = "公告状态不能为空")
    private Integer status;

    /**
     * 公告状态描述
     */
    private String statusDesc;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 更新者
     */
    private String updateBy;



    /**
     * 查询DTO - 用于列表查询
     */
    @Data
    @Accessors(chain = true)
    public static class Query {
        /**
         * 公告标题（模糊查询）
         */
        private String noticeTitle;

        /**
         * 公告类型
         */
        private Integer noticeType;

        /**
         * 公告状态
         */
        private Integer status;

        /**
         * 创建时间范围 - 开始时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTimeStart;

        /**
         * 创建时间范围 - 结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTimeEnd;
    }

    /**
     * 创建DTO - 用于新增公告
     */
    @Data
    @Accessors(chain = true)
    public static class Create {
        /**
         * 公告标题
         */
        @NotBlank(message = "公告标题不能为空")
        @Size(max = 100, message = "公告标题长度不能超过100个字符")
        private String noticeTitle;

        /**
         * 公告类型（1通知 2公告）
         */
        @NotNull(message = "公告类型不能为空")
        private Integer noticeType;

        /**
         * 公告内容
         */
        @NotBlank(message = "公告内容不能为空")
        private String noticeContent;

        /**
         * 公告状态（0草稿 1发布 2关闭）
         */
        @NotNull(message = "公告状态不能为空")
        private Integer status;

        /**
         * 备注
         */
        @Size(max = 500, message = "备注长度不能超过500个字符")
        private String remark;
    }

    /**
     * 更新DTO - 用于修改公告
     */
    @Data
    @Accessors(chain = true)
    public static class Update {
        /**
         * 主键ID
         */
        @NotNull(message = "公告ID不能为空")
        private Long id;

        /**
         * 公告标题
         */
        @NotBlank(message = "公告标题不能为空")
        @Size(max = 100, message = "公告标题长度不能超过100个字符")
        private String noticeTitle;

        /**
         * 公告类型（1通知 2公告）
         */
        @NotNull(message = "公告类型不能为空")
        private Integer noticeType;

        /**
         * 公告内容
         */
        @NotBlank(message = "公告内容不能为空")
        private String noticeContent;

        /**
         * 公告状态（0草稿 1发布 2关闭）
         */
        @NotNull(message = "公告状态不能为空")
        private Integer status;

        /**
         * 备注
         */
        @Size(max = 500, message = "备注长度不能超过500个字符")
        private String remark;


    }

    /**
     * 状态更新DTO - 用于状态变更
     */
    @Data
    @Accessors(chain = true)
    public static class StatusUpdate {
        /**
         * 主键ID
         */
        @NotNull(message = "公告ID不能为空")
        private Long id;

        /**
         * 公告状态（0草稿 1发布 2关闭）
         */
        @NotNull(message = "公告状态不能为空")
        private Integer status;


    }
}