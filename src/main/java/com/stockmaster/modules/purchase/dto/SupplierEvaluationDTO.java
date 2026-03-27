package com.stockmaster.modules.purchase.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SupplierEvaluationDTO {
    private Long id;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private Long orderId;

    @Min(value = 1, message = "评分必须在1-5之间")
    @Max(value = 5, message = "评分必须在1-5之间")
    private Integer qualityScore;

    @Min(value = 1, message = "评分必须在1-5之间")
    @Max(value = 5, message = "评分必须在1-5之间")
    private Integer deliveryScore;

    @Min(value = 1, message = "评分必须在1-5之间")
    @Max(value = 5, message = "评分必须在1-5之间")
    private Integer serviceScore;

    @Min(value = 1, message = "评分必须在1-5之间")
    @Max(value = 5, message = "评分必须在1-5之间")
    private Integer priceScore;

    private String content;
}
