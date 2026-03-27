package com.stockmaster.modules.dashboard.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SalesSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal monthInboundAmount;
    private BigDecimal monthOutboundAmount;
    private Long monthInboundCount;
    private Long monthOutboundCount;
    private BigDecimal yearInboundAmount;
    private BigDecimal yearOutboundAmount;
    private BigDecimal inboundGrowthRate;
    private BigDecimal outboundGrowthRate;
}
