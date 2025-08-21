package com.yc.admin.system.log.entity;

import com.yc.admin.common.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "sys_log", indexes = {
})
@AttributeOverride(name = "id", column = @Column(name = "log_id"))
public class SysLog extends BaseEntity {

    /**
     * 操作类型
     */
    private String type;

    /**
     * 操作标题
     */
    private String title;

    /**
     * 操作IP地址
     */
    private String remoteAddr;
    /**
     * 用户代理
     */
    private String userAgent;
    /**
     * 请求URI
     */
    private String requestUri;
    /**
     * 操作方式
     */
    private String method;
    /**
     * 操作提交的数据
     */
    private String params;
    /**
     * 执行时间
     */
    private Long time;

    /**
     * 异常信息
     */
    private String exception;

}
