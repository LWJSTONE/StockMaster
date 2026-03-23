package com.stockmaster.modules.stock.service;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.modules.stock.entity.Warehouse;

import java.util.List;

public interface WarehouseService {

    PageResult<Warehouse> getList(String keyword, Integer status, Integer pageNum, Integer pageSize);

    Warehouse getById(Long id);

    Warehouse getByCode(String code);

    List<Warehouse> getAllActive();

    Warehouse create(Warehouse warehouse);

    Warehouse update(Long id, Warehouse warehouse);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    Long countActiveWarehouses();
}
