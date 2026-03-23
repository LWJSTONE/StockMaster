package com.stockmaster.modules.system.entity;

import com.stockmaster.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * System configuration entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_config")
public class SystemConfig extends BaseEntity {

    /**
     * Configuration key
     */
    @Column(name = "config_key", unique = true, nullable = false, length = 100)
    private String configKey;

    /**
     * Configuration value
     */
    @Column(name = "config_value", columnDefinition = "text")
    private String configValue;

    /**
     * Configuration name (display name)
     */
    @Column(name = "config_name", length = 100)
    private String configName;

    /**
     * Configuration description
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Configuration type: STRING, NUMBER, BOOLEAN, JSON
     */
    @Column(name = "config_type", length = 20)
    private String configType;

    /**
     * Configuration group
     */
    @Column(name = "config_group", length = 50)
    private String configGroup;

    /**
     * Sort order
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * Whether it's a system configuration (cannot be deleted)
     */
    @Column(name = "is_system", columnDefinition = "tinyint default 0")
    private Boolean isSystem = false;

    /**
     * Whether it's enabled
     */
    @Column(name = "is_enabled", columnDefinition = "tinyint default 1")
    private Boolean isEnabled = true;
}
