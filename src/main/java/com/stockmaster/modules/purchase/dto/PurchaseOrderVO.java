package com.stockmaster.modules.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private Long supplierId;
    private String supplierName;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    private Long approveBy;
    private String approveByName;
    private LocalDateTime approveTime;
    private LocalDateTime expectedDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<PurchaseOrderItemVO> items;

    @Data
    public static class PurchaseOrderItemVO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private Long productId;
        private String productCode;
        private String productName;
        private String unit;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private Integer receivedQuantity;
    }
}
