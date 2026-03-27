package com.stockmaster.modules.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stockmaster.common.enums.StockStatus;
import javax.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;

    @NotBlank(message = "商品编码不能为空")
    @JsonProperty("code")
    private String productCode;

    @NotBlank(message = "商品名称不能为空")
    @JsonProperty("name")
    private String productName;

    private Long categoryId;

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

    private StockStatus status;

    @JsonProperty("image")
    private String imageUrl;

    private String description;
}
