package com.stockmaster.modules.dashboard.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class InventorySummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long totalQuantity;
    private BigDecimal totalValue;
    private Long lowStockCount;
    private Long overStockCount;
    private Long productTypeCount;
    private BigDecimal avgStockValue;
}
