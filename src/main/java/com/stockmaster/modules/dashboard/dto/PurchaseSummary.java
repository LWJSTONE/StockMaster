package com.stockmaster.modules.dashboard.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PurchaseSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long monthOrderCount;
    private BigDecimal monthPurchaseAmount;
    private Long pendingOrderCount;
    private Long completedOrderCount;
    private BigDecimal yearPurchaseAmount;
    private Long activeSupplierCount;
}
