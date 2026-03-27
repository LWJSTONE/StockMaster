package com.stockmaster.modules.stock.entity;

import com.stockmaster.common.entity.BaseEntity;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stock_outbound")
public class Outbound extends BaseEntity {

    @Column(name = "outbound_no", unique = true, nullable = false, length = 50)
    private String outboundNo;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "warehouse_code", length = 50)
    private String warehouseCode;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "outbound_time")
    private LocalDateTime outboundTime;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "outbound_type", length = 20)
    private String outboundType;

    @Column(name = "status", columnDefinition = "tinyint default 1")
    private Integer status = 1;
}
