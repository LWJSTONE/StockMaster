package com.stockmaster.modules.dashboard.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ChartData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String label;
    private BigDecimal value;
}
