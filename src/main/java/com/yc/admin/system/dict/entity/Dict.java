package com.yc.admin.system.dict.entity;


import com.yc.admin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 系统字典类
 */
@Entity
@Table(name = "sys_dict", indexes = {
        @Index(name = "idx_sys_dict_type", columnList = "type"),
        @Index(name = "idx_sys_dict_code", columnList = "code"),
        @Index(name = "idx_sys_dict_value", columnList = "value")
})
@Getter
@Setter
@ToString(callSuper = true)
@AttributeOverride(name = "id", column = @Column(name = "dict_id"))
public class Dict extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典类型
     */
    @Column(name = "type", nullable = false, length = 30)
    private String type;

    /**
     * 字典编码
     */
    @Column(name = "code", nullable = false, length = 30)
    private String code;

    /**
     * 字典值
     */
    @Column(name = "value", nullable = false, length = 30)
    private String value;

    /**
     * 排序
     */
    @Column(name = "sort", nullable = false)
    private Integer sort = 0;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 状态（0正常 1停用）
     */
    @Column(name = "status", nullable = false, length = 1)
    private Integer status = 0;

}
