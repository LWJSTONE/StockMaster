package com.stockmaster.modules.system.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuDTO {
    private Long id;

    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    private String path;

    private String component;

    private String permission;

    private String icon;

    private Integer menuType;

    private Integer sortOrder;

    private Boolean visible;

    private Integer status;

    private Boolean isExternal;

    private Boolean isCached;
}
