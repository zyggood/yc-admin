package com.yc.admin.system.role.dto;

import com.yc.admin.system.role.entity.Role;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色DTO转换器
 *
 * @author YC
 * @since 1.0.0
 */
@UtilityClass
public class RoleDTOConverter {

    /**
     * Entity转DTO
     */
    public RoleDTO toDTO(Role role) {
        if (role == null) {
            return null;
        }
        
        return RoleDTO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleKey(role.getRoleKey())
                .roleSort(role.getRoleSort())
                .dataScope(role.getDataScope())
                .menuCheckStrictly(role.getMenuCheckStrictly())
                .deptCheckStrictly(role.getDeptCheckStrictly())
                .status(role.getStatus())
                .delFlag(role.getDelFlag())
                .createBy(role.getCreateBy())
                .createTime(role.getCreateTime())
                .updateBy(role.getUpdateBy())
                .updateTime(role.getUpdateTime())
                .remark(role.getRemark())
                .build();
    }

    /**
     * DTO转Entity
     */
    public Role toEntity(RoleDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Role role = new Role();
        role.setId(dto.getId());
        role.setRoleName(dto.getRoleName());
        role.setRoleKey(dto.getRoleKey());
        role.setRoleSort(dto.getRoleSort());
        role.setDataScope(dto.getDataScope());
        role.setMenuCheckStrictly(dto.getMenuCheckStrictly());
        role.setDeptCheckStrictly(dto.getDeptCheckStrictly());
        role.setStatus(dto.getStatus());
        role.setDelFlag(dto.getDelFlag());
        role.setCreateBy(dto.getCreateBy());
        role.setCreateTime(dto.getCreateTime());
        role.setUpdateBy(dto.getUpdateBy());
        role.setUpdateTime(dto.getUpdateTime());
        role.setRemark(dto.getRemark());
        return role;
    }

    /**
     * CreateDTO转Entity
     */
    public Role toEntity(RoleDTO.CreateDTO createDTO) {
        if (createDTO == null) {
            return null;
        }
        
        Role role = new Role();
        role.setRoleName(createDTO.getRoleName());
        role.setRoleKey(createDTO.getRoleKey());
        role.setRoleSort(createDTO.getRoleSort());
        role.setDataScope(createDTO.getDataScope());
        role.setMenuCheckStrictly(createDTO.getMenuCheckStrictly());
        role.setDeptCheckStrictly(createDTO.getDeptCheckStrictly());
        role.setStatus(createDTO.getStatus());
        role.setRemark(createDTO.getRemark());
        return role;
    }

    /**
     * 更新Entity（从UpdateDTO）
     */
    public void updateEntity(Role role, RoleDTO.UpdateDTO updateDTO) {
        if (role == null || updateDTO == null) {
            return;
        }
        
        role.setRoleName(updateDTO.getRoleName());
        role.setRoleKey(updateDTO.getRoleKey());
        role.setRoleSort(updateDTO.getRoleSort());
        role.setDataScope(updateDTO.getDataScope());
        role.setMenuCheckStrictly(updateDTO.getMenuCheckStrictly());
        role.setDeptCheckStrictly(updateDTO.getDeptCheckStrictly());
        role.setStatus(updateDTO.getStatus());
        role.setRemark(updateDTO.getRemark());
    }

    /**
     * Entity列表转DTO列表
     */
    public List<RoleDTO> toDTOList(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        
        return roles.stream()
                .map(RoleDTOConverter::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Entity分页转DTO分页
     */
    public Page<RoleDTO> toDTOPage(Page<Role> rolePage) {
        if (rolePage == null) {
            return null;
        }
        
        List<RoleDTO> dtoList = rolePage.getContent().stream()
                .map(RoleDTOConverter::toDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, rolePage.getPageable(), rolePage.getTotalElements());
    }

    /**
     * 转换为选择器DTO
     */
    public RoleDTO.SelectorDTO toSelectorDTO(Role role) {
        if (role == null) {
            return null;
        }
        
        return RoleDTO.SelectorDTO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleKey(role.getRoleKey())
                .build();
    }

    /**
     * 转换为导出DTO
     */
    public RoleDTO.ExportDTO toExportDTO(Role role) {
        if (role == null) {
            return null;
        }
        
        return RoleDTO.ExportDTO.builder()
                .roleName(role.getRoleName())
                .roleKey(role.getRoleKey())
                .roleSort(role.getRoleSort())
                .dataScopeLabel(getDataScopeLabel(role.getDataScope()))
                .statusLabel("0".equals(role.getStatus()) ? "正常" : "停用")
                .createTime(role.getCreateTime())
                .remark(role.getRemark())
                .build();
    }

    /**
     * 获取数据范围标签
     */
    private String getDataScopeLabel(String dataScope) {
        if (dataScope == null) {
            return "未知";
        }

        return switch (dataScope) {
            case "1" -> "全部数据权限";
            case "2" -> "自定数据权限";
            case "3" -> "部门数据权限";
            case "4" -> "部门及以下数据权限";
            case "5" -> "仅本人数据权限";
            default -> "未知";
        };
    }

    /**
     * Entity列表转选择器DTO列表
     */
    public List<RoleDTO.SelectorDTO> toSelectorDTOList(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        
        return roles.stream()
                .map(RoleDTOConverter::toSelectorDTO)
                .collect(Collectors.toList());
    }

    /**
     * Entity列表转导出DTO列表
     */
    public List<RoleDTO.ExportDTO> toExportDTOList(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        
        return roles.stream()
                .map(RoleDTOConverter::toExportDTO)
                .collect(Collectors.toList());
    }
}