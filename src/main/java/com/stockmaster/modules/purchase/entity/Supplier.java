package com.stockmaster.modules.purchase.entity;

import com.stockmaster.common.entity.BaseEntity;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "purchase_supplier")
public class Supplier extends BaseEntity {

    @Column(name = "supplier_code", unique = true, nullable = false, length = 50)
    private String supplierCode;

    @Column(name = "supplier_name", nullable = false, length = 100)
    private String supplierName;

    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    @Column(name = "tax_number", length = 50)
    private String taxNumber;

    @Column(name = "status", columnDefinition = "tinyint default 1")
    private Integer status = 1;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "description", length = 500)
    private String description;
}
