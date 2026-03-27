package com.stockmaster.modules.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String productCode;
    private String productName;
    private Long categoryId;
    private String brand;
    private String spec;
    private String unit;
    private String barcode;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
    private Integer minStock;
    private Integer maxStock;
    private String status;
    private String imageUrl;
    private String description;
}
