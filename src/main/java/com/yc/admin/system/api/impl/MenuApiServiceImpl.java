package com.yc.admin.system.api.impl;

import com.yc.admin.system.api.MenuApiService;
import com.yc.admin.system.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜单 API 服务实现类
 * 
 * @author YC
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class MenuApiServiceImpl implements MenuApiService {

    private final MenuService menuService;

    @Override
    public List<String> findPermissionsByUserId(Long userId) {
        return menuService.findPermissionsByUserId(userId);
    }
}