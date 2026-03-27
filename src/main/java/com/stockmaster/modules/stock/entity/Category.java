package com.stockmaster.modules.stock.entity;

import com.stockmaster.common.entity.BaseEntity;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stock_category")
public class Category extends BaseEntity {

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    @Column(name = "category_code", unique = true, length = 50)
    private String categoryCode;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "status", columnDefinition = "tinyint default 1")
    private Integer status = 1;

    @Column(name = "icon", length = 100)
    private String icon;
}
