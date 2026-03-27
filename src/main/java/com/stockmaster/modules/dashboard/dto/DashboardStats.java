package com.stockmaster.modules.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardStats {
    private Long productCount;
    private Long supplierCount;
    
    @JsonProperty("orderCount")
    private Long purchaseOrderCount;
    
    @JsonProperty("warningCount")
    private Long lowStockCount;
    
    private Long totalStockQuantity;
    private BigDecimal totalPurchaseAmount;
    private BigDecimal totalOutboundAmount;
    
    // 前端期望的额外字段
    @JsonProperty("todayInbound")
    private Long todayInboundCount;
    
    @JsonProperty("todayOutbound")
    private Long todayOutboundCount;
    
    @JsonProperty("totalInventoryValue")
    private BigDecimal totalInventoryValue;
}
