package com.stockmaster.modules.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PurchaseOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long supplierId;
    private String remark;
    private String expectedDate;
    private List<PurchaseOrderItemDTO> items;

    @Data
    public static class PurchaseOrderItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long productId;
        private Integer quantity;
        private java.math.BigDecimal unitPrice;
    }
}
