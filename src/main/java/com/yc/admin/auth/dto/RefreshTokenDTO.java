package com.yc.admin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 刷新令牌请求DTO
 * @author yc
 */
@Data
@Schema(description = "刷新令牌请求")
public class RefreshTokenDTO {

    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}