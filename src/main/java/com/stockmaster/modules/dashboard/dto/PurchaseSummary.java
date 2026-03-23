package com.stockmaster.modules.dashboard.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Purchase summary statistics
 */
@Data
public class PurchaseSummary {

    /**
     * Current month order count
     */
    private Long monthOrderCount;

    /**
     * Current month purchase amount
     */
    private BigDecimal monthPurchaseAmount;

    /**
     * Pending approval order count
     */
    private Long pendingOrderCount;

    /**
     * Completed order count
     */
    private Long completedOrderCount;

    /**
     * Year-to-date purchase amount
     */
    private BigDecimal yearPurchaseAmount;

    /**
     * Active supplier count
     */
    private Long activeSupplierCount;
}
