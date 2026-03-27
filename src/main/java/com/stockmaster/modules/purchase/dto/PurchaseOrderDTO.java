package com.stockmaster.modules.purchase.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderDTO {
    private Long id;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private LocalDateTime expectedDate;

    private String remark;

    @NotNull(message = "订单明细不能为空")
    private List<PurchaseOrderItemDTO> items;
}
