package com.yc.admin.system.dict.service;

import com.yc.admin.system.dict.dto.DictDto;
import com.yc.admin.system.dict.dto.DictDtoConverter;
import com.yc.admin.system.dict.entity.Dict;
import com.yc.admin.system.dict.repository.DictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictService {

    private final DictRepository dictRepository;

    public Page<Dict> page(DictDto.QueryDto pageQuery, PageRequest pageRequest) {

        return dictRepository.findByQueryDto(pageQuery, pageRequest);

    }

    public List<Dict> type(String type) {
        return dictRepository.findByType(type);
    }

    public Dict save(DictDto saveDto) {

        return dictRepository.save(DictDtoConverter.toEntity(saveDto));
    }

    public void delete(Long id) {
        dictRepository.deleteById(id);

    }

    public void deleteBatch(List<Long> ids) {
        dictRepository.deleteAllById(ids);
    }
}
