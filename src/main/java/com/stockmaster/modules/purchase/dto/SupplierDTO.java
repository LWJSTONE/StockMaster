package com.stockmaster.modules.purchase.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SupplierDTO {
    private Long id;

    @NotBlank(message = "供应商编码不能为空")
    private String supplierCode;

    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    private String contactPerson;

    private String contactPhone;

    private String email;

    private String address;

    private String bankName;

    private String bankAccount;

    private String taxNumber;

    private Integer status;

    private BigDecimal rating;

    private String description;
}
