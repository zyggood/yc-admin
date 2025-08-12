package com.yc.admin.menu.service;

import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.menu.entity.Menu;
import com.yc.admin.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单业务逻辑层
 * 
 * @author YC
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    // ==================== 查询方法 ====================

    /**
     * 根据ID查询菜单
     * @param id 菜单ID
     * @return 菜单信息
     */
    public Optional<Menu> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return menuRepository.findById(id).filter(menu -> menu.getDelFlag() == 0);
    }

    /**
     * 根据权限标识查询菜单
     * @param perms 权限标识
     * @return 菜单信息
     */
    public Optional<Menu> findByPerms(String perms) {
        if (!StringUtils.hasText(perms)) {
            return Optional.empty();
        }
        return menuRepository.findByPermsAndDelFlag(perms, 0);
    }

    /**
     * 查询所有菜单列表
     * @return 菜单列表
     */
    public List<Menu> findAll() {
        return menuRepository.findByDelFlagOrderByOrderNumAsc(0);
    }

    /**
     * 根据父菜单ID查询子菜单列表
     * @param parentId 父菜单ID
     * @return 子菜单列表
     */
    public List<Menu> findByParentId(Long parentId) {
        if (parentId == null) {
            parentId = Menu.TOP_PARENT_ID;
        }
        return menuRepository.findByParentIdAndDelFlagOrderByOrderNumAsc(parentId, 0);
    }

    /**
     * 根据菜单类型查询菜单列表
     * @param menuType 菜单类型
     * @return 菜单列表
     */
    public List<Menu> findByMenuType(String menuType) {
        if (!StringUtils.hasText(menuType)) {
            return Collections.emptyList();
        }
        return menuRepository.findByMenuTypeAndDelFlagOrderByOrderNumAsc(menuType, 0);
    }

    /**
     * 根据状态查询菜单列表
     * @param status 状态
     * @return 菜单列表
     */
    public List<Menu> findByStatus(Integer status) {
        if (status == null) {
            return Collections.emptyList();
        }
        return menuRepository.findByStatusAndDelFlagOrderByOrderNumAsc(status, 0);
    }

    /**
     * 分页查询菜单
     * @param menuName 菜单名称关键字
     * @param menuType 菜单类型
     * @param status 状态
     * @param visible 可见性
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页结果
     */
    public Page<Menu> findByConditions(String menuName, String menuType, Integer status, Integer visible, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return menuRepository.findByConditions(
            StringUtils.hasText(menuName) ? menuName.trim() : null,
            StringUtils.hasText(menuType) ? menuType.trim() : null,
            status,
            visible,
            0,
            pageable
        );
    }

    /**
     * 构建菜单树
     * @return 菜单树列表
     */
    public List<MenuTreeNode> buildMenuTree() {
        List<Menu> allMenus = findAll();
        return buildMenuTree(allMenus, Menu.TOP_PARENT_ID);
    }

    /**
     * 构建菜单树（指定根节点）
     * @param menus 菜单列表
     * @param parentId 父菜单ID
     * @return 菜单树列表
     */
    public List<MenuTreeNode> buildMenuTree(List<Menu> menus, Long parentId) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<Menu>> menuMap = menus.stream()
            .collect(Collectors.groupingBy(Menu::getParentId));

        return buildMenuTreeRecursive(menuMap, parentId);
    }

    /**
     * 递归构建菜单树
     * @param menuMap 菜单映射
     * @param parentId 父菜单ID
     * @return 菜单树节点列表
     */
    private List<MenuTreeNode> buildMenuTreeRecursive(Map<Long, List<Menu>> menuMap, Long parentId) {
        List<Menu> children = menuMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return Collections.emptyList();
        }

        return children.stream()
            .sorted(Comparator.comparing(Menu::getOrderNum))
            .map(menu -> {
                MenuTreeNode node = new MenuTreeNode();
                node.setId(menu.getId());
                node.setMenuName(menu.getMenuName());
                node.setParentId(menu.getParentId());
                node.setOrderNum(menu.getOrderNum());
                node.setPath(menu.getPath());
                node.setComponent(menu.getComponent());
                node.setQuery(menu.getQuery());
                node.setIsFrame(menu.getIsFrame());
                node.setIsCache(menu.getIsCache());
                node.setMenuType(menu.getMenuType());
                node.setVisible(menu.getVisible());
                node.setStatus(menu.getStatus());
                node.setPerms(menu.getPerms());
                node.setIcon(menu.getIcon());
                node.setRemark(menu.getRemark());
                node.setCreateTime(menu.getCreateTime());
                
                // 递归构建子菜单
                List<MenuTreeNode> childNodes = buildMenuTreeRecursive(menuMap, menu.getId());
                node.setChildren(childNodes);
                
                return node;
            })
            .collect(Collectors.toList());
    }

    /**
     * 根据用户ID查询菜单权限
     * @param userId 用户ID
     * @return 菜单列表
     */
    public List<Menu> findByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return menuRepository.findByUserId(userId);
    }

    /**
     * 根据角色ID查询菜单权限
     * @param roleId 角色ID
     * @return 菜单列表
     */
    public List<Menu> findByRoleId(Long roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }
        return menuRepository.findByRoleId(roleId);
    }

    // ==================== 创建和更新方法 ====================

    /**
     * 创建菜单
     * @param menu 菜单信息
     * @return 创建的菜单
     */
    @Transactional
    public Menu createMenu(Menu menu) {
        validateMenuForCreate(menu);
        
        // 设置默认值
        if (menu.getParentId() == null) {
            menu.setParentId(Menu.TOP_PARENT_ID);
        }
        if (menu.getOrderNum() == null) {
            Integer maxOrderNum = menuRepository.findMaxOrderNumByParentId(menu.getParentId(), 0);
            menu.setOrderNum(maxOrderNum + 1);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(Menu.Status.NORMAL);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(Menu.Visible.SHOW);
        }
        if (menu.getIsFrame() == null) {
            menu.setIsFrame(Menu.Frame.NO);
        }
        if (menu.getIsCache() == null) {
            menu.setIsCache(Menu.Cache.YES);
        }
        
        Menu savedMenu = menuRepository.save(menu);
        log.info("创建菜单成功: {}", savedMenu.getMenuName());
        return savedMenu;
    }

    /**
     * 更新菜单
     * @param menu 菜单信息
     * @return 更新的菜单
     */
    @Transactional
    public Menu updateMenu(Menu menu) {
        validateMenuForUpdate(menu);
        
        Menu existingMenu = findById(menu.getId())
            .orElseThrow(() -> new BusinessException("菜单不存在"));
        
        // 检查是否修改了父菜单，避免形成循环引用
        if (!Objects.equals(existingMenu.getParentId(), menu.getParentId())) {
            validateParentMenu(menu.getId(), menu.getParentId());
        }
        
        // 更新字段
        existingMenu.setMenuName(menu.getMenuName());
        existingMenu.setParentId(menu.getParentId());
        existingMenu.setOrderNum(menu.getOrderNum());
        existingMenu.setPath(menu.getPath());
        existingMenu.setComponent(menu.getComponent());
        existingMenu.setQuery(menu.getQuery());
        existingMenu.setIsFrame(menu.getIsFrame());
        existingMenu.setIsCache(menu.getIsCache());
        existingMenu.setMenuType(menu.getMenuType());
        existingMenu.setVisible(menu.getVisible());
        existingMenu.setStatus(menu.getStatus());
        existingMenu.setPerms(menu.getPerms());
        existingMenu.setIcon(menu.getIcon());
        existingMenu.setRemark(menu.getRemark());
        
        Menu savedMenu = menuRepository.save(existingMenu);
        log.info("更新菜单成功: {}", savedMenu.getMenuName());
        return savedMenu;
    }

    // ==================== 删除方法 ====================

    /**
     * 删除菜单
     * @param id 菜单ID
     */
    @Transactional
    public void deleteMenu(Long id) {
        Menu menu = findById(id)
            .orElseThrow(() -> new BusinessException("菜单不存在"));
        
        // 检查是否存在子菜单
        if (menuRepository.existsByParentIdAndDelFlag(id, 0)) {
            throw new BusinessException("存在子菜单，无法删除");
        }
        
        // TODO: 检查是否有角色关联此菜单
        
        // 逻辑删除
        menu.setDelFlag(1);
        menuRepository.save(menu);
        
        log.info("删除菜单成功: {}", menu.getMenuName());
    }

    /**
     * 批量删除菜单
     * @param ids 菜单ID列表
     * @return 删除的记录数
     */
    @Transactional
    public int deleteMenus(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        // 检查每个菜单是否可以删除
        for (Long id : ids) {
            Menu menu = findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在: " + id));
            
            if (menuRepository.existsByParentIdAndDelFlag(id, 0)) {
                throw new BusinessException("菜单 [" + menu.getMenuName() + "] 存在子菜单，无法删除");
            }
        }
        
        int deletedCount = menuRepository.deleteByIds(ids);
        log.info("批量删除菜单成功，删除数量: {}", deletedCount);
        return deletedCount;
    }

    // ==================== 状态管理方法 ====================

    /**
     * 启用菜单
     * @param id 菜单ID
     */
    @Transactional
    public void enableMenu(Long id) {
        updateMenuStatus(id, Menu.Status.NORMAL);
    }

    /**
     * 停用菜单
     * @param id 菜单ID
     */
    @Transactional
    public void disableMenu(Long id) {
        updateMenuStatus(id, Menu.Status.DISABLED);
    }

    /**
     * 更新菜单状态
     * @param id 菜单ID
     * @param status 新状态
     */
    @Transactional
    public void updateMenuStatus(Long id, Integer status) {
        Menu menu = findById(id)
            .orElseThrow(() -> new BusinessException("菜单不存在"));
        
        menuRepository.updateStatusById(id, status);
        log.info("更新菜单状态成功: {} -> {}", menu.getMenuName(), status == 0 ? "正常" : "停用");
    }

    /**
     * 批量更新菜单状态
     * @param ids 菜单ID列表
     * @param status 新状态
     * @return 更新的记录数
     */
    @Transactional
    public int updateMenuStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        int updatedCount = menuRepository.updateStatusByIds(ids, status);
        log.info("批量更新菜单状态成功，更新数量: {}, 新状态: {}", updatedCount, status == 0 ? "正常" : "停用");
        return updatedCount;
    }

    // ==================== 统计方法 ====================

    /**
     * 获取菜单统计信息
     * @return 统计信息
     */
    public Map<String, Object> getMenuStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总菜单数
        long totalCount = menuRepository.countByMenuTypeAndStatusAndDelFlag(null, null, 0);
        stats.put("totalCount", totalCount);
        
        // 目录数量
        long directoryCount = menuRepository.countByMenuTypeAndStatusAndDelFlag(Menu.Type.DIRECTORY, null, 0);
        stats.put("directoryCount", directoryCount);
        
        // 菜单数量
        long menuCount = menuRepository.countByMenuTypeAndStatusAndDelFlag(Menu.Type.MENU, null, 0);
        stats.put("menuCount", menuCount);
        
        // 按钮数量
        long buttonCount = menuRepository.countByMenuTypeAndStatusAndDelFlag(Menu.Type.BUTTON, null, 0);
        stats.put("buttonCount", buttonCount);
        
        // 正常状态数量
        long normalCount = menuRepository.countByMenuTypeAndStatusAndDelFlag(null, Menu.Status.NORMAL, 0);
        stats.put("normalCount", normalCount);
        
        // 停用状态数量
        long disabledCount = menuRepository.countByMenuTypeAndStatusAndDelFlag(null, Menu.Status.DISABLED, 0);
        stats.put("disabledCount", disabledCount);
        
        return stats;
    }

    // ==================== 私有验证方法 ====================

    /**
     * 验证创建菜单的参数
     * @param menu 菜单信息
     */
    private void validateMenuForCreate(Menu menu) {
        if (menu == null) {
            throw new IllegalArgumentException("菜单信息不能为空");
        }
        
        validateMenuCommon(menu);
        
        // 检查同级菜单名称是否重复
        Long parentId = menu.getParentId() != null ? menu.getParentId() : Menu.TOP_PARENT_ID;
        if (menuRepository.existsByMenuNameAndParentIdAndIdNotAndDelFlag(menu.getMenuName(), parentId, -1L, 0)) {
            throw new IllegalArgumentException("同级菜单名称已存在: " + menu.getMenuName());
        }
        
        // 检查权限标识是否重复
        if (StringUtils.hasText(menu.getPerms()) && 
            menuRepository.existsByPermsAndIdNotAndDelFlag(menu.getPerms(), -1L, 0)) {
            throw new IllegalArgumentException("权限标识已存在: " + menu.getPerms());
        }
    }

    /**
     * 验证更新菜单的参数
     * @param menu 菜单信息
     */
    private void validateMenuForUpdate(Menu menu) {
        if (menu == null) {
            throw new IllegalArgumentException("菜单信息不能为空");
        }
        
        if (menu.getId() == null) {
            throw new IllegalArgumentException("菜单ID不能为空");
        }
        
        validateMenuCommon(menu);
        
        // 检查同级菜单名称是否重复
        Long parentId = menu.getParentId() != null ? menu.getParentId() : Menu.TOP_PARENT_ID;
        if (menuRepository.existsByMenuNameAndParentIdAndIdNotAndDelFlag(menu.getMenuName(), parentId, menu.getId(), 0)) {
            throw new IllegalArgumentException("同级菜单名称已存在: " + menu.getMenuName());
        }
        
        // 检查权限标识是否重复
        if (StringUtils.hasText(menu.getPerms()) && 
            menuRepository.existsByPermsAndIdNotAndDelFlag(menu.getPerms(), menu.getId(), 0)) {
            throw new IllegalArgumentException("权限标识已存在: " + menu.getPerms());
        }
    }

    /**
     * 通用菜单验证
     * @param menu 菜单信息
     */
    private void validateMenuCommon(Menu menu) {
        if (!StringUtils.hasText(menu.getMenuName())) {
            throw new IllegalArgumentException("菜单名称不能为空");
        }
        
        if (menu.getMenuName().length() > 50) {
            throw new IllegalArgumentException("菜单名称长度不能超过50个字符");
        }
        
        if (!StringUtils.hasText(menu.getMenuType())) {
            throw new IllegalArgumentException("菜单类型不能为空");
        }
        
        if (!Arrays.asList(Menu.Type.DIRECTORY, Menu.Type.MENU, Menu.Type.BUTTON).contains(menu.getMenuType())) {
            throw new IllegalArgumentException("菜单类型只能是M、C、F");
        }
        
        // 按钮类型必须有权限标识
        if (Menu.Type.BUTTON.equals(menu.getMenuType()) && !StringUtils.hasText(menu.getPerms())) {
            throw new IllegalArgumentException("按钮类型菜单必须设置权限标识");
        }
        
        // 菜单类型必须有路由地址
        if (Menu.Type.MENU.equals(menu.getMenuType()) && !StringUtils.hasText(menu.getPath())) {
            throw new IllegalArgumentException("菜单类型必须设置路由地址");
        }
    }

    /**
     * 验证父菜单，避免循环引用
     * @param menuId 当前菜单ID
     * @param parentId 父菜单ID
     */
    private void validateParentMenu(Long menuId, Long parentId) {
        if (parentId == null || parentId.equals(Menu.TOP_PARENT_ID)) {
            return;
        }
        
        if (menuId.equals(parentId)) {
            throw new IllegalArgumentException("不能选择自己作为父菜单");
        }
        
        // 检查是否会形成循环引用
        List<Long> childrenIds = menuRepository.findAllChildrenIds(menuId, 0);
        if (childrenIds.contains(parentId)) {
            throw new IllegalArgumentException("不能选择自己的子菜单作为父菜单");
        }
    }

    // ==================== 内部类 ====================

    /**
     * 菜单树节点
     */
    @lombok.Data
    public static class MenuTreeNode {
        private Long id;
        private String menuName;
        private Long parentId;
        private Integer orderNum;
        private String path;
        private String component;
        private String query;
        private Integer isFrame;
        private Integer isCache;
        private String menuType;
        private Integer visible;
        private Integer status;
        private String perms;
        private String icon;
        private String remark;
        private java.time.LocalDateTime createTime;
        private List<MenuTreeNode> children = new ArrayList<>();
    }
}