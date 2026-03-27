package com.stockmaster.modules.stock.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InboundDTO {
    private Long id;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "入库数量不能为空")
    @Min(value = 1, message = "入库数量必须大于0")
    private Integer quantity;

    private BigDecimal unitPrice;

    private Long supplierId;

    private String warehouseCode;

    private String batchNo;
}
