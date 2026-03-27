package com.stockmaster.modules.stock.service;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.modules.stock.entity.Warehouse;

import java.util.List;

public interface WarehouseService {

    Warehouse getById(Long id);

    Warehouse getByCode(String code);

    List<Warehouse> getAll();

    List<Warehouse> getAllActive();

    PageResult<Warehouse> getList(String keyword, Integer status, Integer pageNum, Integer pageSize);

    Warehouse create(Warehouse warehouse);

    Warehouse update(Long id, Warehouse warehouse);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    Long countActiveWarehouses();
}
