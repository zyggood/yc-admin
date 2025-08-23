package com.yc.admin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


/**
 * 登录请求DTO
 * @author yc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录请求")
public class LoginDTO {

    @Schema(description = "用户名", example = "admin")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
    private String username;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    @Schema(description = "验证码", example = "1234")
    private String captcha;

    @Schema(description = "验证码UUID", example = "uuid-1234")
    private String uuid;

    @Schema(description = "记住我", example = "true")
    @Builder.Default
    private Boolean rememberMe = false;
}