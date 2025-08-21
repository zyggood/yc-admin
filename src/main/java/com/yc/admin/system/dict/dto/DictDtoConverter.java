package com.yc.admin.system.dict.dto;

import com.yc.admin.system.dict.entity.Dict;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DictDtoConverter {

    public static Dict toEntity(DictDto dictDto) {

        Dict dict = new Dict();
        dict.setType(dictDto.getType());
        dict.setCode(dictDto.getCode());
        dict.setValue(dictDto.getValue());
        dict.setSort(dictDto.getSort());
        dict.setRemark(dictDto.getRemark());
        dict.setStatus(dictDto.getStatus());
        if (dictDto instanceof DictDto.UpdateDto dto) {
            dict.setId(dto.getId());
        }
        return dict;
    }
}
