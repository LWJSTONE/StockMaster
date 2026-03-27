package com.stockmaster.modules.purchase.entity;

import com.stockmaster.common.entity.BaseEntity;
import com.stockmaster.common.enums.OrderStatus;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "purchase_order")
public class PurchaseOrder extends BaseEntity {

    @Column(name = "order_no", unique = true, nullable = false, length = 50)
    private String orderNo;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "expected_date")
    private LocalDateTime expectedDate;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "buyer", length = 50)
    private String buyer;

    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    @Column(name = "approver", length = 50)
    private String approver;

    @Column(name = "remark", length = 500)
    private String remark;
}
