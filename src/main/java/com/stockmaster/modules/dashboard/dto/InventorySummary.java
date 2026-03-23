package com.stockmaster.modules.dashboard.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Inventory summary statistics
 */
@Data
public class InventorySummary {

    /**
     * Total stock quantity
     */
    private Long totalQuantity;

    /**
     * Total stock value
     */
    private BigDecimal totalValue;

    /**
     * Low stock product count
     */
    private Long lowStockCount;

    /**
     * Overstock product count
     */
    private Long overStockCount;

    /**
     * Product types count
     */
    private Long productTypeCount;

    /**
     * Average stock value per product
     */
    private BigDecimal avgStockValue;
}
