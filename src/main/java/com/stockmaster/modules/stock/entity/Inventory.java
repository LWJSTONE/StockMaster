package com.stockmaster.modules.stock.entity;

import com.stockmaster.common.entity.BaseEntity;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stock_inventory")
public class Inventory extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "warehouse_code", length = 50)
    private String warehouseCode;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "frozen_quantity")
    private Integer frozenQuantity = 0;

    @Column(name = "available_quantity")
    private Integer availableQuantity = 0;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "shelf_location", length = 50)
    private String shelfLocation;

    @Column(name = "warning_min")
    private Integer warningMin;

    @Column(name = "warning_max")
    private Integer warningMax;
}
