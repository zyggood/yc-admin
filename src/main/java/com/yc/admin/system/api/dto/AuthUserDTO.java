package com.yc.admin.system.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证用户DTO
 * 用于auth模块内部的用户认证信息传递，避免直接依赖system模块的User entity
 *
 * @author yc
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserDTO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 用户状态
     */
    private String status;
    
    /**
     * 昵称
     */
    private String nickName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 用户状态枚举
     */
    public enum Status {
        NORMAL("0", "正常"),
        DISABLED("1", "停用");
        
        private final String code;
        private final String desc;
        
        Status(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
        
        public static Status fromCode(String code) {
            Status[] values = values();
            for (Status status : values) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return DISABLED;
        }
    }
    
    /**
     * 检查用户是否启用
     */
    public boolean isEnabled() {
        return Status.NORMAL.getCode().equals(this.status);
    }
    
    /**
     * 检查用户是否禁用
     */
    public boolean isDisabled() {
        return Status.DISABLED.getCode().equals(this.status);
    }
}