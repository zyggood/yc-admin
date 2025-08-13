package com.yc.admin.system.api.impl;

import com.yc.admin.system.api.UserApiService;
import com.yc.admin.system.user.dto.UserDTO;
import com.yc.admin.system.user.entity.User;
import com.yc.admin.system.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户 API 服务实现类
 * 
 * @author YC
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserApiServiceImpl implements UserApiService {

    private final UserService userService;

    @Override
    public UserDTO findById(Long userId) {
        return userService.findById(userId);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userService.findByUsername(username);
    }

    @Override
    public Optional<User> findByUserName(String userName) {
        return userService.findByUserName(userName);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userService.findByEmail(email);
    }

    @Override
    public Optional<User> findEntityById(Long userId) {
        return userService.findEntityById(userId);
    }
}