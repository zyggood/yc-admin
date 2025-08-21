package com.yc.admin.system.dict.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class DictDto {

    @Schema(description = "字典类型")
    private String type;

    @Schema(description = "字典编码")
    private String code;

    @Schema(description = "字典值")
    private String value;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态")
    private Integer status;

    @Data
    @Schema(description = "字典查询参数")
    public static class QueryDto {

        @Schema(description = "字典类型")
        private String type;

        @Schema(description = "字典编码")
        private String code;

        @Schema(description = "字典值")
        private String value;

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "字典创建参数")
    public static class CreateDto extends DictDto {
    }


    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "字典更新参数")
    public static class UpdateDto extends DictDto {

        @Schema(description = "字典ID")
        private Long id;
    }
}
