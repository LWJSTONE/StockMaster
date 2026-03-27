package com.stockmaster.modules.stock.service.impl;

import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.stock.dto.InventoryDTO;
import com.stockmaster.modules.stock.entity.Inventory;
import com.stockmaster.modules.stock.entity.Product;
import com.stockmaster.modules.stock.repository.InventoryRepository;
import com.stockmaster.modules.stock.repository.ProductRepository;
import com.stockmaster.modules.stock.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Override
    public Inventory getById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("库存记录不存在"));
    }

    @Override
    public Inventory getByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException("库存记录不存在"));
    }

    @Override
    public List<Inventory> getLowStock() {
        return inventoryRepository.findLowStock();
    }

    @Override
    public List<InventoryDTO> getAll() {
        List<Inventory> inventories = inventoryRepository.findAllNotDeleted();
        return inventories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void adjust(Long productId, Integer quantity, String remark) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException("库存记录不存在"));

        int newQuantity = inventory.getQuantity() + quantity;
        if (newQuantity < 0) {
            throw new BusinessException("库存不足，无法调整");
        }

        inventory.setQuantity(newQuantity);
        inventory.setAvailableQuantity(newQuantity - (inventory.getFrozenQuantity() != null ? inventory.getFrozenQuantity() : 0));
        inventoryRepository.save(inventory);
    }

    private InventoryDTO convertToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setProductId(inventory.getProductId());
        dto.setWarehouseCode(inventory.getWarehouseCode());
        dto.setQuantity(inventory.getQuantity());
        dto.setFrozenQuantity(inventory.getFrozenQuantity());
        dto.setAvailableQuantity(inventory.getAvailableQuantity());
        dto.setBatchNo(inventory.getBatchNo());
        dto.setShelfLocation(inventory.getShelfLocation());
        dto.setWarningMin(inventory.getWarningMin());
        dto.setWarningMax(inventory.getWarningMax());

        // 获取商品信息
        productRepository.findById(inventory.getProductId()).ifPresent(product -> {
            dto.setProductCode(product.getProductCode());
            dto.setProductName(product.getProductName());
            dto.setUnitPrice(product.getSalePrice());
        });

        return dto;
    }
}
