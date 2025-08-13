package com.yc.admin.system.api;

import com.yc.admin.system.user.dto.UserDTO;
import com.yc.admin.system.user.entity.User;

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
     * @param userId 用户ID
     * @return 用户信息
     */
    UserDTO findById(Long userId);

    /**
     * 根据用户名查询用户实体
     * @param username 用户名
     * @return 用户实体
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据用户名查询用户实体
     * @param userName 用户名
     * @return 用户实体
     */
    Optional<User> findByUserName(String userName);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户实体
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据ID查询用户实体（内部使用）
     * @param userId 用户ID
     * @return 用户实体
     */
    Optional<User> findEntityById(Long userId);
}