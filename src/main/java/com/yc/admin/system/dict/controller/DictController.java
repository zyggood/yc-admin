package com.yc.admin.system.dict.controller;

import com.yc.admin.common.core.Result;
import com.yc.admin.system.dict.dto.DictDto;
import com.yc.admin.system.dict.entity.Dict;
import com.yc.admin.system.dict.service.DictService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class DictController {

    private DictService dictService;


    @GetMapping("/page")
    public Result<Page<Dict>> page(DictDto.QueryDto pageQuery,
                                   @Parameter(description = "页码（从0开始）")
                                   @RequestParam(defaultValue = "0") int page,
                                   @Parameter(description = "每页大小")
                                   @RequestParam(defaultValue = "10") int size) {
        return Result.success(dictService.page(pageQuery, PageRequest.of(page, size)));
    }

    @GetMapping("/type")
    public Result<List<Dict>> type(
            @RequestParam String type
    ) {
        return Result.success(dictService.type(type));
    }


    // ==================== 创建和更新接口 ====================

    @PostMapping
    public Result<Dict> create(@RequestBody DictDto.CreateDto createDto) {
        return Result.success(dictService.save(createDto));
    }

    @PutMapping
    public Result<Dict> update(@RequestBody DictDto.UpdateDto updateDto) {
        return Result.success(dictService.save(updateDto));
    }


    // ==================== 删除接口 ====================
    @DeleteMapping
    public Result<String> delete(Long id) {
        dictService.delete(id);
        return Result.success("删除成功");
    }

    @DeleteMapping("/batch")
    public Result<String> deleteBatch(@RequestParam List<Long> ids) {
        dictService.deleteBatch(ids);
        return Result.success("删除成功");
    }


}
