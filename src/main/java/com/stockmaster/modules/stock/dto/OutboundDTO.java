package com.stockmaster.modules.stock.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OutboundDTO {
    private Long id;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "出库数量不能为空")
    @Min(value = 1, message = "出库数量必须大于0")
    private Integer quantity;

    private BigDecimal unitPrice;

    private String warehouseCode;

    private String batchNo;

    private String outboundType;
}
