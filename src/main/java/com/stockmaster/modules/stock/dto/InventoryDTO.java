package com.stockmaster.modules.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryDTO {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private String warehouseCode;
    private Integer quantity;
    private Integer frozenQuantity;
    private Integer availableQuantity;
    private String batchNo;
    private String shelfLocation;
    
    @JsonProperty("warningQuantity")
    private Integer warningMin;
    
    private Integer warningMax;
    private BigDecimal unitPrice;
}
