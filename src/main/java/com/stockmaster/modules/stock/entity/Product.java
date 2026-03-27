package com.stockmaster.modules.stock.entity;

import com.stockmaster.common.entity.BaseEntity;
import com.stockmaster.common.enums.StockStatus;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stock_product")
public class Product extends BaseEntity {

    @Column(name = "product_code", unique = true, nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "brand", length = 50)
    private String brand;

    @Column(name = "spec", length = 100)
    private String spec;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "barcode", length = 50)
    private String barcode;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "sale_price", precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "min_stock")
    private Integer minStock;

    @Column(name = "max_stock")
    private Integer maxStock;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StockStatus status;

    @Column(name = "image_url", columnDefinition = "text")
    private String imageUrl;

    @Column(name = "description", columnDefinition = "text")
    private String description;
}
