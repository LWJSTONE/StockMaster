package com.stockmaster.modules.dashboard.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TrendData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String date;
    private Integer purchaseCount;
    private Integer outboundCount;
    private BigDecimal purchaseAmount;
    private BigDecimal outboundAmount;
}
