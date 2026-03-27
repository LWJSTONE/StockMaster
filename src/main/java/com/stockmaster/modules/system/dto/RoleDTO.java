package com.stockmaster.modules.system.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleDTO {
    private Long id;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    private String description;

    private Integer sortOrder;

    private Integer status;

    private java.util.List<Long> menuIds;
}
