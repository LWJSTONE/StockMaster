package com.stockmaster.modules.system.entity;

import com.stockmaster.common.entity.BaseEntity;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_menu")
public class Menu extends BaseEntity {

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 50)
    private String menuName;

    @Column(length = 100)
    private String path;

    @Column(length = 100)
    private String component;

    @Column(length = 100)
    private String permission;

    @Column(length = 50)
    private String icon;

    @Column(name = "menu_type", columnDefinition = "tinyint default 1")
    private Integer menuType = 1;

    @Column(name = "sort_order", columnDefinition = "int default 0")
    private Integer sortOrder = 0;

    @Column(name = "visible", columnDefinition = "tinyint(1) default 1")
    private Boolean visible = true;

    @Column(name = "status", columnDefinition = "tinyint default 1")
    private Integer status = 1;

    @Column(name = "is_external", columnDefinition = "tinyint(1) default 0")
    private Boolean isExternal = false;

    @Column(name = "is_cached", columnDefinition = "tinyint(1) default 0")
    private Boolean isCached = false;
}
