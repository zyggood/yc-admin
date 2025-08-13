package com.yc.admin.system.api;

import com.yc.admin.system.api.dto.AuthUserDTO;

import java.util.Optional;

/**
 * 用户 API 服务接口
 * 提供给其他模块使用的用户相关功能
 * 
 * @author YC
 * @since 1.0.0
 */
public interface UserApiService {

    /**
     * 根据ID查询用户DTO
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    Optional<AuthUserDTO> findById(Long userId);

    /**
     * 根据用户名查找认证用户信息
     * @param username 用户名
     * @return 认证用户信息
     */
    Optional<AuthUserDTO> findAuthUserByUsername(String username);

    /**
     * 根据用户ID查找认证用户信息
     * @param userId 用户ID
     * @return 认证用户信息
     */
    Optional<AuthUserDTO> findAuthUserById(Long userId);

}