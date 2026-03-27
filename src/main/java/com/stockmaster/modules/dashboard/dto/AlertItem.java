package com.stockmaster.modules.dashboard.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AlertItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private String productCode;
    private String productName;
    private Integer currentQuantity;
    private String alertType;
    private Integer threshold;
    private String severity;
    private String suggestion;
}
