package com.yc.admin.common.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 包含所有实体的公共字段：主键、创建时间、更新时间、创建人、更新人、删除标志
 * 
 * @author YC
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @CreatedBy
    @Column(name = "create_by", length = 64, updatable = false)
    private String createBy;

    /**
     * 更新人
     */
    @LastModifiedBy
    @Column(name = "update_by", length = 64)
    private String updateBy;

    /**
     * 删除标志（0：未删除，1：已删除）
     */
    @Column(name = "del_flag", nullable = false)
    private Integer delFlag = 0;

    /**
     * 预持久化操作
     * 设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) {
            createTime = now;
        }
        if (updateTime == null) {
            updateTime = now;
        }
        if (delFlag == null) {
            delFlag = 0;
        }
    }

    /**
     * 预更新操作
     * 设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    /**
     * 判断是否为新实体
     * @return true：新实体，false：已存在实体
     */
    public boolean isNew() {
        return id == null;
    }

    /**
     * 判断是否已删除
     * @return true：已删除，false：未删除
     */
    public boolean isDeleted() {
        return delFlag != null && delFlag == 1;
    }

    /**
     * 标记为已删除
     */
    public void markDeleted() {
        this.delFlag = 1;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记为未删除
     */
    public void markUndeleted() {
        this.delFlag = 0;
        this.updateTime = LocalDateTime.now();
    }
}