package com.yc.admin.system.menu.dto;

import com.yc.admin.system.menu.entity.Menu;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单DTO转换器
 *
 * @author YC
 * @since 1.0.0
 */
@UtilityClass
public class MenuDTOConverter {

    /**
     * Entity转DTO
     */
    public MenuDTO toDTO(Menu menu) {
        if (menu == null) {
            return null;
        }
        
        return MenuDTO.builder()
                .id(menu.getId())
                .menuName(menu.getMenuName())
                .parentId(menu.getParentId())
                .orderNum(menu.getOrderNum())
                .path(menu.getPath())
                .component(menu.getComponent())
                .query(menu.getQuery())
                .isFrame(menu.getIsFrame())
                .isCache(menu.getIsCache())
                .menuType(menu.getMenuType())
                .visible(menu.getVisible())
                .status(menu.getStatus())
                .perms(menu.getPerms())
                .icon(menu.getIcon())
                .createBy(menu.getCreateBy())
                .createTime(menu.getCreateTime())
                .updateBy(menu.getUpdateBy())
                .updateTime(menu.getUpdateTime())
                .remark(menu.getRemark())
                .build();
    }

    /**
     * DTO转Entity
     */
    public Menu toEntity(MenuDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Menu menu = new Menu();
        menu.setId(dto.getId());
        menu.setMenuName(dto.getMenuName());
        menu.setParentId(dto.getParentId());
        menu.setOrderNum(dto.getOrderNum());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setQuery(dto.getQuery());
        menu.setIsFrame(dto.getIsFrame());
        menu.setIsCache(dto.getIsCache());
        menu.setMenuType(dto.getMenuType());
        menu.setVisible(dto.getVisible());
        menu.setStatus(dto.getStatus());
        menu.setPerms(dto.getPerms());
        menu.setIcon(dto.getIcon());
        menu.setCreateBy(dto.getCreateBy());
        menu.setCreateTime(dto.getCreateTime());
        menu.setUpdateBy(dto.getUpdateBy());
        menu.setUpdateTime(dto.getUpdateTime());
        menu.setRemark(dto.getRemark());
        return menu;
    }

    /**
     * CreateDTO转Entity
     */
    public Menu toEntity(MenuDTO.CreateDTO createDTO) {
        if (createDTO == null) {
            return null;
        }
        
        Menu menu = new Menu();
        menu.setMenuName(createDTO.getMenuName());
        menu.setParentId(createDTO.getParentId());
        menu.setOrderNum(createDTO.getOrderNum());
        menu.setPath(createDTO.getPath());
        menu.setComponent(createDTO.getComponent());
        menu.setQuery(createDTO.getQuery());
        menu.setIsFrame(createDTO.getIsFrame());
        menu.setIsCache(createDTO.getIsCache());
        menu.setMenuType(createDTO.getMenuType());
        menu.setVisible(createDTO.getVisible());
        menu.setStatus(createDTO.getStatus());
        menu.setPerms(createDTO.getPerms());
        menu.setIcon(createDTO.getIcon());
        menu.setRemark(createDTO.getRemark());
        return menu;
    }

    /**
     * 更新Entity（从UpdateDTO）
     */
    public void updateEntity(Menu menu, MenuDTO.UpdateDTO updateDTO) {
        if (menu == null || updateDTO == null) {
            return;
        }
        
        menu.setMenuName(updateDTO.getMenuName());
        menu.setParentId(updateDTO.getParentId());
        menu.setOrderNum(updateDTO.getOrderNum());
        menu.setPath(updateDTO.getPath());
        menu.setComponent(updateDTO.getComponent());
        menu.setQuery(updateDTO.getQuery());
        menu.setIsFrame(updateDTO.getIsFrame());
        menu.setIsCache(updateDTO.getIsCache());
        menu.setMenuType(updateDTO.getMenuType());
        menu.setVisible(updateDTO.getVisible());
        menu.setStatus(updateDTO.getStatus());
        menu.setPerms(updateDTO.getPerms());
        menu.setIcon(updateDTO.getIcon());
        menu.setRemark(updateDTO.getRemark());
    }

    /**
     * Entity列表转DTO列表
     */
    public List<MenuDTO> toDTOList(List<Menu> menus) {
        if (menus == null) {
            return null;
        }
        
        return menus.stream()
                .map(MenuDTOConverter::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Entity分页转DTO分页
     */
    public Page<MenuDTO> toDTOPage(Page<Menu> menuPage) {
        if (menuPage == null) {
            return null;
        }
        
        List<MenuDTO> dtoList = menuPage.getContent().stream()
                .map(MenuDTOConverter::toDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, menuPage.getPageable(), menuPage.getTotalElements());
    }

    /**
     * Entity转TreeNodeDTO
     */
    public MenuDTO.TreeNodeDTO toTreeNodeDTO(Menu menu) {
        if (menu == null) {
            return null;
        }
        
        return MenuDTO.TreeNodeDTO.builder()
                .id(menu.getId())
                .label(menu.getMenuName())
                .parentId(menu.getParentId())
                .value(menu.getId())
                .orderNum(menu.getOrderNum())
                .path(menu.getPath())
                .icon(menu.getIcon())
                .disabled(menu.getStatus() != null && menu.getStatus() == 1)
                .type(menu.getMenuType())
                .isLeaf(menu.getMenuType().equals("F"))
                .build();
    }

    /**
     * Entity列表转TreeNodeDTO列表
     */
    public List<MenuDTO.TreeNodeDTO> toTreeNodeDTOList(List<Menu> menus) {
        if (menus == null) {
            return null;
        }
        
        return menus.stream()
                .map(MenuDTOConverter::toTreeNodeDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为权限DTO
     */
    public MenuDTO.PermissionDTO toPermissionDTO(Menu menu) {
        if (menu == null) {
            return null;
        }
        
        return MenuDTO.PermissionDTO.builder()
                .id(menu.getId())
                .menuId(menu.getId())
                .menuName(menu.getMenuName())
                .menuType(menu.getMenuType())
                .perms(menu.getPerms())
                .path(menu.getPath())
                .hasPermission(menu.getStatus() == 0)
                .build();
    }

    /**
     * Entity列表转权限DTO列表
     */
    public List<MenuDTO.PermissionDTO> toPermissionDTOList(List<Menu> menus) {
        if (menus == null) {
            return null;
        }
        
        return menus.stream()
                .map(MenuDTOConverter::toPermissionDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为导出DTO
     */
    public MenuDTO.ExportDTO toExportDTO(Menu menu) {
        if (menu == null) {
            return null;
        }
        
        return MenuDTO.ExportDTO.builder()
                .menuName(menu.getMenuName())
                .parentName(getParentName(menu.getParentId()))
                .menuTypeName(getMenuTypeLabel(menu.getMenuType()))
                .orderNum(menu.getOrderNum())
                .path(menu.getPath())
                .perms(menu.getPerms())
                .statusName(menu.getStatus() == 0 ? "正常" : "停用")
                .visibleName(menu.getVisible() == 0 ? "显示" : "隐藏")
                .createTime(menu.getCreateTime() != null ? menu.getCreateTime().toString() : "")
                .remark(menu.getRemark())
                .build();
    }

    /**
     * 获取菜单类型标签
     */
    private String getMenuTypeLabel(String menuType) {
        if (menuType == null) {
            return "未知";
        }

        return switch (menuType) {
            case "M" -> "目录";
            case "C" -> "菜单";
            case "F" -> "按钮";
            default -> "未知";
        };
    }

    /**
     * 获取父菜单名称（这里简化处理，实际应该查询数据库）
     */
    private String getParentName(Long parentId) {
        if (parentId == null || parentId == 0) {
            return "主类目";
        }
        // 实际应该查询数据库获取父菜单名称
        return "父菜单-" + parentId;
    }

    /**
     * Entity列表转导出DTO列表
     */
    public List<MenuDTO.ExportDTO> toExportDTOList(List<Menu> menus) {
        if (menus == null) {
            return null;
        }
        
        return menus.stream()
                .map(MenuDTOConverter::toExportDTO)
                .collect(Collectors.toList());
    }
}