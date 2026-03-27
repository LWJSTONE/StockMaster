package com.stockmaster.modules.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SupplierVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String supplierCode;
    private String supplierName;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String bankName;
    private String bankAccount;
    private String taxNumber;
    private Integer status;
    private BigDecimal rating;
    private String description;
    private BigDecimal totalPurchaseAmount;
    private Integer totalOrderCount;
    private Double averageScore;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
