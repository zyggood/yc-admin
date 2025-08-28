package com.yc.admin.system.permission.entity;

import com.yc.admin.common.entity.BaseEntity;
import com.yc.admin.system.permission.enums.DataScope;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据权限配置实体
 * 用于存储用户的自定义数据权限配置
 * 
 * @author YC
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "sys_user_data_permission", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id", unique = true),
    @Index(name = "idx_data_scope", columnList = "data_scope"),
    @Index(name = "idx_status", columnList = "status")
})
@AttributeOverride(name = "id", column = @Column(name = "permission_id"))
public class UserDataPermission extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 数据权限范围
     * 1：全部数据权限
     * 2：自定数据权限
     * 3：本部门数据权限
     * 4：本部门及以下数据权限
     * 5：仅本人数据权限
     */
    @NotNull(message = "数据权限范围不能为空")
    @Column(name = "data_scope", nullable = false, length = 1)
    private String dataScope = DataScope.SELF.getCode();

    /**
     * 自定义部门权限（当data_scope=2时使用）
     * 存储部门ID列表，用逗号分隔
     */
    @Column(name = "custom_dept_ids", length = 1000)
    private String customDeptIds;

    /**
     * 权限状态（0：正常，1：停用）
     */
    @Column(name = "status", nullable = false, length = 1)
    private String status = "0";

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    // ==================== 业务方法 ====================

    /**
     * 获取数据权限范围枚举
     * 
     * @return 数据权限范围枚举
     */
    public DataScope getDataScopeEnum() {
        return DataScope.fromCode(this.dataScope);
    }

    /**
     * 设置数据权限范围枚举
     * 
     * @param dataScope 数据权限范围枚举
     */
    public void setDataScopeEnum(DataScope dataScope) {
        this.dataScope = dataScope.getCode();
    }

    /**
     * 获取自定义部门ID列表
     * 
     * @return 部门ID列表
     */
    public List<Long> getCustomDeptIdList() {
        List<Long> deptIds = new ArrayList<>();
        if (customDeptIds != null && !customDeptIds.trim().isEmpty()) {
            String[] ids = customDeptIds.split(",");
            for (String id : ids) {
                try {
                    deptIds.add(Long.parseLong(id.trim()));
                } catch (NumberFormatException e) {
                    // 忽略无效的ID
                }
            }
        }
        return deptIds;
    }

    /**
     * 设置自定义部门ID列表
     * 
     * @param deptIds 部门ID列表
     */
    public void setCustomDeptIdList(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            this.customDeptIds = null;
        } else {
            this.customDeptIds = String.join(",", deptIds.stream().map(String::valueOf).toArray(String[]::new));
        }
    }

    /**
     * 判断权限是否正常
     * 
     * @return true：正常，false：停用
     */
    public boolean isNormal() {
        return "0".equals(this.status);
    }

    /**
     * 判断权限是否停用
     * 
     * @return true：停用，false：正常
     */
    public boolean isDisabled() {
        return "1".equals(this.status);
    }

    /**
     * 启用权限
     */
    public void enable() {
        this.status = "0";
    }

    /**
     * 停用权限
     */
    public void disable() {
        this.status = "1";
    }

    /**
     * 判断是否为自定义权限
     * 
     * @return true：自定义权限，false：非自定义权限
     */
    public boolean isCustomScope() {
        return DataScope.CUSTOM.getCode().equals(this.dataScope);
    }

    /**
     * 判断是否需要部门权限过滤
     * 
     * @return true：需要部门权限过滤，false：不需要
     */
    public boolean needDeptFilter() {
        return getDataScopeEnum().needDeptFilter();
    }

    /**
     * 判断是否需要用户权限过滤
     * 
     * @return true：需要用户权限过滤，false：不需要
     */
    public boolean needUserFilter() {
        return getDataScopeEnum().needUserFilter();
    }
}