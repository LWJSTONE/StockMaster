package com.stockmaster.modules.stock.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "商品编码不能为空")
    @Size(max = 50, message = "商品编码长度不能超过50个字符")
    private String productCode;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称长度不能超过100个字符")
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
