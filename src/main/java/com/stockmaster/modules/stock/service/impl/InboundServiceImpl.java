package com.stockmaster.modules.stock.service.impl;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.stock.dto.InboundDTO;
import com.stockmaster.modules.stock.entity.Inbound;
import com.stockmaster.modules.stock.entity.Inventory;
import com.stockmaster.modules.stock.entity.Product;
import com.stockmaster.modules.stock.repository.InboundRepository;
import com.stockmaster.modules.stock.repository.InventoryRepository;
import com.stockmaster.modules.stock.repository.ProductRepository;
import com.stockmaster.modules.stock.service.InboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundRepository inboundRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public Inbound getById(Long id) {
        return inboundRepository.findById(id)
                .orElseThrow(() -> new BusinessException("入库记录不存在"));
    }

    @Override
    @Transactional
    public Inbound create(InboundDTO inboundDTO) {
        // 校验入库数量必须大于0
        if (inboundDTO.getQuantity() == null || inboundDTO.getQuantity() <= 0) {
            throw new BusinessException("入库数量必须大于0");
        }
        
        Product product = productRepository.findById(inboundDTO.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        Inbound inbound = new Inbound();
        inbound.setInboundNo(generateInboundNo());
        inbound.setProductId(inboundDTO.getProductId());
        inbound.setQuantity(inboundDTO.getQuantity());
        inbound.setUnitPrice(inboundDTO.getUnitPrice() != null ? inboundDTO.getUnitPrice() : product.getCostPrice());
        inbound.setTotalPrice(inbound.getUnitPrice().multiply(BigDecimal.valueOf(inboundDTO.getQuantity())));
        inbound.setSupplierId(inboundDTO.getSupplierId());
        inbound.setWarehouseCode(inboundDTO.getWarehouseCode());
        inbound.setBatchNo(inboundDTO.getBatchNo());
        inbound.setInboundTime(LocalDateTime.now());
        inbound.setStatus(1);

        inbound = inboundRepository.save(inbound);

        // 更新库存
        Inventory inventory = inventoryRepository.findByProductId(inboundDTO.getProductId())
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setProductId(inboundDTO.getProductId());
                    inv.setQuantity(0);
                    inv.setFrozenQuantity(0);
                    inv.setAvailableQuantity(0);
                    return inv;
                });

        inventory.setQuantity(inventory.getQuantity() + inboundDTO.getQuantity());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + inboundDTO.getQuantity());
        inventoryRepository.save(inventory);

        return inbound;
    }

    @Override
    @Transactional
    public Inbound update(Long id, InboundDTO inboundDTO) {
        // 校验入库数量必须大于0
        if (inboundDTO.getQuantity() == null || inboundDTO.getQuantity() <= 0) {
            throw new BusinessException("入库数量必须大于0");
        }
        
        Inbound inbound = inboundRepository.findById(id)
                .orElseThrow(() -> new BusinessException("入库记录不存在"));

        if (inbound.getStatus() != 1) {
            throw new BusinessException("只能修改待处理的入库记录");
        }

        Product product = productRepository.findById(inboundDTO.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        Long oldProductId = inbound.getProductId();
        Long newProductId = inboundDTO.getProductId();
        int oldQuantity = inbound.getQuantity();
        int newQuantity = inboundDTO.getQuantity();

        // 处理库存更新
        if (!oldProductId.equals(newProductId)) {
            // 产品ID改变了，需要回退原产品库存并增加新产品库存
            Inventory oldInventory = inventoryRepository.findByProductId(oldProductId)
                    .orElseThrow(() -> new BusinessException("原产品库存记录不存在"));
            oldInventory.setQuantity(oldInventory.getQuantity() - oldQuantity);
            oldInventory.setAvailableQuantity(oldInventory.getAvailableQuantity() - oldQuantity);
            inventoryRepository.save(oldInventory);

            Inventory newInventory = inventoryRepository.findByProductId(newProductId)
                    .orElseGet(() -> {
                        Inventory inv = new Inventory();
                        inv.setProductId(newProductId);
                        inv.setQuantity(0);
                        inv.setFrozenQuantity(0);
                        inv.setAvailableQuantity(0);
                        return inv;
                    });
            newInventory.setQuantity(newInventory.getQuantity() + newQuantity);
            newInventory.setAvailableQuantity(newInventory.getAvailableQuantity() + newQuantity);
            inventoryRepository.save(newInventory);
        } else {
            // 产品ID未变，只更新数量差异
            int diff = newQuantity - oldQuantity;
            if (diff != 0) {
                Inventory inventory = inventoryRepository.findByProductId(newProductId)
                        .orElseThrow(() -> new BusinessException("库存记录不存在"));
                inventory.setQuantity(inventory.getQuantity() + diff);
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() + diff);
                inventoryRepository.save(inventory);
            }
        }

        inbound.setProductId(inboundDTO.getProductId());
        inbound.setQuantity(newQuantity);
        inbound.setUnitPrice(inboundDTO.getUnitPrice() != null ? inboundDTO.getUnitPrice() : product.getCostPrice());
        inbound.setTotalPrice(inbound.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity)));
        inbound.setSupplierId(inboundDTO.getSupplierId());
        inbound.setWarehouseCode(inboundDTO.getWarehouseCode());
        inbound.setBatchNo(inboundDTO.getBatchNo());

        inbound = inboundRepository.save(inbound);
        return inbound;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Inbound inbound = inboundRepository.findById(id)
                .orElseThrow(() -> new BusinessException("入库记录不存在"));

        if (inbound.getStatus() != 1) {
            throw new BusinessException("只能删除待处理的入库记录");
        }

        // 回退库存
        Inventory inventory = inventoryRepository.findByProductId(inbound.getProductId())
                .orElseThrow(() -> new BusinessException("库存记录不存在"));
        inventory.setQuantity(inventory.getQuantity() - inbound.getQuantity());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - inbound.getQuantity());
        inventoryRepository.save(inventory);

        inbound.setDeleted(true);
        inboundRepository.save(inbound);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            delete(id);
        }
    }

    @Override
    public PageResult<Inbound> getList(String keyword, Long productId, Long supplierId, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Inbound> page = inboundRepository.findByConditions(keyword, productId, supplierId, pageable);
        return PageResult.of(page.getContent(), page.getTotalElements(), pageNum, pageSize);
    }

    @Override
    public String generateInboundNo() {
        String prefix = "IN";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fullPrefix = prefix + dateStr;
        
        // 查询当天已有的入库单数量
        Long count = inboundRepository.countByInboundNoPrefix(fullPrefix);
        if (count == null) {
            count = 0L;
        }
        
        // 生成单号并确保唯一性
        String inboundNo;
        int seq = count.intValue() + 1;
        do {
            inboundNo = fullPrefix + String.format("%06d", seq);
            seq++;
        } while (inboundRepository.existsByInboundNo(inboundNo));
        
        return inboundNo;
    }
}
