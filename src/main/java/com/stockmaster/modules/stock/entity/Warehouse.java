package com.stockmaster.modules.stock.entity;

import com.stockmaster.common.entity.BaseEntity;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stock_warehouse")
public class Warehouse extends BaseEntity {

    @Column(name = "warehouse_code", unique = true, nullable = false, length = 50)
    private String warehouseCode;

    @Column(name = "warehouse_name", nullable = false, length = 100)
    private String warehouseName;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "status", columnDefinition = "tinyint default 1")
    private Integer status = 1;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "description", length = 500)
    private String description;
}
