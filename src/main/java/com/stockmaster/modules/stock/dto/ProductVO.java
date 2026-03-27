package com.stockmaster.modules.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
    private Long id;
    
    @JsonProperty("code")
    private String productCode;
    
    @JsonProperty("name")
    private String productName;
    
    private Long categoryId;
    private String categoryName;
    private String brand;
    
    @JsonProperty("specification")
    private String spec;
    
    private String unit;
    private String barcode;
    
    @JsonProperty("purchasePrice")
    private BigDecimal costPrice;
    
    private BigDecimal salePrice;
    private Integer minStock;
    private Integer maxStock;
    private String status;
    
    @JsonProperty("image")
    private String imageUrl;
    
    private String description;
    private Integer stockQuantity;
    private LocalDateTime createTime;
}
