package com.yc.admin.system.api.impl;

import com.yc.admin.system.api.RoleApiService;
import com.yc.admin.system.role.entity.Role;
import com.yc.admin.system.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色 API 服务实现类
 * 
 * @author YC
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class RoleApiServiceImpl implements RoleApiService {

    private final RoleService roleService;

    @Override
    public List<Role> findByUserId(Long userId) {
        return roleService.findByUserId(userId);
    }
}