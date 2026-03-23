package com.stockmaster.modules.dashboard.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Alert item for inventory warnings
 */
@Data
public class AlertItem {

    /**
     * Product ID
     */
    private Long productId;

    /**
     * Product code
     */
    private String productCode;

    /**
     * Product name
     */
    private String productName;

    /**
     * Current quantity
     */
    private Integer currentQuantity;

    /**
     * Warning type: LOW_STOCK, OVERSTOCK
     */
    private String alertType;

    /**
     * Threshold value
     */
    private Integer threshold;

    /**
     * Severity: HIGH, MEDIUM, LOW
     */
    private String severity;

    /**
     * Suggested action
     */
    private String suggestion;
}
