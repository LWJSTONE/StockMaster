package com.stockmaster.modules.purchase.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Data
public class PurchaseOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    private String expectedDate;

    @Valid
    private List<PurchaseOrderItemDTO> items;

    @Data
    public static class PurchaseOrderItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;

        private java.math.BigDecimal unitPrice;
    }
}
