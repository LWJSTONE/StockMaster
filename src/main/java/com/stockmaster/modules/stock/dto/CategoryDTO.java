package com.stockmaster.modules.stock.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;

    private Long parentId;

    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    private String categoryCode;

    private Integer sortOrder;

    private Integer status;

    private String icon;
}
