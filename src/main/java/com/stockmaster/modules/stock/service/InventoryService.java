package com.stockmaster.modules.stock.service;

import com.stockmaster.modules.stock.dto.InventoryDTO;
import com.stockmaster.modules.stock.entity.Inventory;

import java.util.List;

public interface InventoryService {

    Inventory getById(Long id);

    Inventory getByProductId(Long productId);

    List<Inventory> getLowStock();

    List<InventoryDTO> getAll();

    void adjust(Long productId, Integer quantity, String remark);
}
