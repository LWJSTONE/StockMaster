package com.stockmaster.modules.dashboard.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DashboardStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productCount;
    private Long supplierCount;
    private Long purchaseOrderCount;
    private Long lowStockCount;
    private Long totalStockQuantity;
    private BigDecimal totalPurchaseAmount;
    private BigDecimal totalOutboundAmount;
    private Long todayInboundCount;
    private Long todayOutboundCount;
    private BigDecimal totalInventoryValue;
}
