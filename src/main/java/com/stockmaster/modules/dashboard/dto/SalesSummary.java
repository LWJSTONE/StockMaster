package com.stockmaster.modules.dashboard.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Sales summary statistics
 */
@Data
public class SalesSummary {

    /**
     * Current month inbound amount
     */
    private BigDecimal monthInboundAmount;

    /**
     * Current month outbound amount
     */
    private BigDecimal monthOutboundAmount;

    /**
     * Inbound count this month
     */
    private Long monthInboundCount;

    /**
     * Outbound count this month
     */
    private Long monthOutboundCount;

    /**
     * Year-to-date inbound amount
     */
    private BigDecimal yearInboundAmount;

    /**
     * Year-to-date outbound amount
     */
    private BigDecimal yearOutboundAmount;

    /**
     * Month-over-month inbound growth rate
     */
    private BigDecimal inboundGrowthRate;

    /**
     * Month-over-month outbound growth rate
     */
    private BigDecimal outboundGrowthRate;
}
