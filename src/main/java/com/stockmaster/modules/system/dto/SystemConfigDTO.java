package com.stockmaster.modules.system.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * System configuration DTO
 */
@Data
public class SystemConfigDTO {

    private Long id;

    @NotBlank(message = "配置键不能为空")
    private String configKey;

    private String configValue;

    @NotBlank(message = "配置名称不能为空")
    private String configName;

    private String description;

    private String configType;

    private String configGroup;

    private Integer sortOrder;

    private Boolean isSystem;

    private Boolean isEnabled;
}
