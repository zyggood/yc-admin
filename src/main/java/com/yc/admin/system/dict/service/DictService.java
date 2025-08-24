package com.yc.admin.system.dict.service;

import com.yc.admin.system.dict.dto.DictDto;
import com.yc.admin.system.dict.dto.DictDtoConverter;
import com.yc.admin.system.dict.entity.Dict;
import com.yc.admin.system.dict.repository.DictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "dict:type", key = "#type")
    public List<Dict> type(String type) {
        return dictRepository.findByType(type);
    }

    @CacheEvict(value = {"dict:type", "dict:all"}, allEntries = true)
    public Dict save(DictDto saveDto) {
        return dictRepository.save(DictDtoConverter.toEntity(saveDto));
    }

    @CacheEvict(value = {"dict:type", "dict:all"}, allEntries = true)
    public void delete(Long id) {
        dictRepository.deleteById(id);
    }

    @CacheEvict(value = {"dict:type", "dict:all"}, allEntries = true)
    public void deleteBatch(List<Long> ids) {
        dictRepository.deleteAllById(ids);
    }

    /**
     * 获取所有字典数据（用于缓存预热）
     * @return 所有字典数据
     */
    @Cacheable(value = "dict:all")
    public List<Dict> findAll() {
        return dictRepository.findAll();
    }

    /**
     * 清除字典缓存
     */
    @CacheEvict(value = {"dict:type", "dict:all"}, allEntries = true)
    public void clearCache() {
        // 方法体为空，仅用于清除缓存
    }
}
