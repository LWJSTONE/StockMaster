package com.stockmaster.modules.system.entity;

import com.stockmaster.common.entity.BaseEntity;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_role")
public class Role extends BaseEntity {

    @Column(nullable = false, length = 50, unique = true)
    private String roleCode;

    @Column(nullable = false, length = 50)
    private String roleName;

    @Column(length = 100)
    private String description;

    @Column(name = "sort_order", columnDefinition = "int default 0")
    private Integer sortOrder = 0;

    @Column(name = "status", columnDefinition = "tinyint default 1")
    private Integer status = 1;
}
