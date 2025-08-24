package com.yc.admin.system.user.dto;

import com.yc.admin.system.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户DTO转换器工具类
 *
 * @author YC
 * @since 1.0.0
 */
public final class UserDTOConverter {

    private UserDTOConverter() {
        // 工具类不允许实例化
    }

    /**
     * Entity转DTO
     */
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .nickName(user.getNickName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .sex(user.getSex())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .delFlag(user.getDelFlag())
                .createBy(user.getCreateBy())
                .createTime(user.getCreateTime())
                .updateBy(user.getUpdateBy())
                .updateTime(user.getUpdateTime())
                .remark(user.getRemark())
                .build();
    }

    /**
     * DTO转Entity
     */
    public static User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        
        User user = new User();
        user.setId(dto.getId());
        user.setUserName(dto.getUserName());
        user.setNickName(dto.getNickName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setSex(dto.getSex());
        user.setAvatar(dto.getAvatar());
        user.setStatus(dto.getStatus());
        user.setDelFlag(dto.getDelFlag());
        user.setCreateBy(dto.getCreateBy());
        user.setCreateTime(dto.getCreateTime());
        user.setUpdateBy(dto.getUpdateBy());
        user.setUpdateTime(dto.getUpdateTime());
        user.setRemark(dto.getRemark());
        return user;
    }

    /**
     * CreateDTO转Entity
     */
    public static User toEntity(UserDTO.CreateDTO createDTO) {
        if (createDTO == null) {
            return null;
        }
        
        User user = new User();
        user.setUserName(createDTO.getUserName());
        user.setNickName(createDTO.getNickName());
        user.setEmail(createDTO.getEmail());
        user.setPhone(createDTO.getPhone());
        user.setSex(createDTO.getSex());
        user.setAvatar(createDTO.getAvatar());
        user.setStatus(createDTO.getStatus());
        user.setRemark(createDTO.getRemark());
        user.setPassword(createDTO.getPassword());
        return user;
    }

    /**
     * 更新Entity
     */
    public static void updateEntity(User user, UserDTO.UpdateDTO updateDTO) {
        if (user == null || updateDTO == null) {
            return;
        }
        
        user.setUserName(updateDTO.getUserName());
        user.setNickName(updateDTO.getNickName());
        user.setEmail(updateDTO.getEmail());
        user.setPhone(updateDTO.getPhone());
        user.setSex(updateDTO.getSex());
        user.setAvatar(updateDTO.getAvatar());
        user.setStatus(updateDTO.getStatus());
        user.setRemark(updateDTO.getRemark());
    }

    /**
     * Entity列表转DTO列表
     */
    public static List<UserDTO> toDTOList(List<User> users) {
        if (users == null) {
            return null;
        }
        
        return users.stream()
                .map(UserDTOConverter::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Entity分页转DTO分页
     */
    public static Page<UserDTO> toDTOPage(Page<User> userPage) {
        if (userPage == null) {
            return null;
        }
        
        List<UserDTO> dtoList = userPage.getContent().stream()
                .map(UserDTOConverter::toDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, userPage.getPageable(), userPage.getTotalElements());
    }

    /**
     * Entity转SelectorDTO
     */
    public static UserDTO.SelectorDTO toSelectorDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.SelectorDTO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .nickName(user.getNickName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .build();
    }

    /**
     * Entity转ExportDTO
     */
    public static UserDTO.ExportDTO toExportDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.ExportDTO.builder()
                .userName(user.getUserName())
                .nickName(user.getNickName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .sexLabel("1".equals(user.getSex()) ? "男" : "0".equals(user.getSex()) ? "女" : "未知")
                .statusLabel("0".equals(user.getStatus()) ? "正常" : "停用")
                .createTime(user.getCreateTime())
                .remark(user.getRemark())
                .build();
    }

    /**
     * Entity列表转SelectorDTO列表
     */
    public static List<UserDTO.SelectorDTO> toSelectorDTOList(List<User> users) {
        if (users == null) {
            return null;
        }
        
        return users.stream()
                .map(UserDTOConverter::toSelectorDTO)
                .collect(Collectors.toList());
    }

    /**
     * Entity列表转ExportDTO列表
     */
    public static List<UserDTO.ExportDTO> toExportDTOList(List<User> users) {
        if (users == null) {
            return null;
        }
        
        return users.stream()
                .map(UserDTOConverter::toExportDTO)
                .collect(Collectors.toList());
    }
}