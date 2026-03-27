package com.stockmaster.modules.purchase.entity;

import com.stockmaster.common.entity.BaseEntity;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "purchase_supplier_evaluation")
public class SupplierEvaluation extends BaseEntity {

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "quality_score")
    private Integer qualityScore;

    @Column(name = "delivery_score")
    private Integer deliveryScore;

    @Column(name = "service_score")
    private Integer serviceScore;

    @Column(name = "price_score")
    private Integer priceScore;

    @Column(name = "total_score", precision = 3, scale = 2)
    private BigDecimal totalScore;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "evaluator", length = 50)
    private String evaluator;
}
