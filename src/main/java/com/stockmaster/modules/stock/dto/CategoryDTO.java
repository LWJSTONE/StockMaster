package com.stockmaster.modules.stock.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class CategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long parentId;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String categoryName;

    @Size(max = 50, message = "分类编码长度不能超过50个字符")
    private String categoryCode;

    private Integer sortOrder;

    private Integer status;

    private String icon;
}
