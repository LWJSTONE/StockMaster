package com.stockmaster.modules.stock.service.impl;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.stock.dto.OutboundDTO;
import com.stockmaster.modules.stock.entity.Inventory;
import com.stockmaster.modules.stock.entity.Outbound;
import com.stockmaster.modules.stock.entity.Product;
import com.stockmaster.modules.stock.repository.InventoryRepository;
import com.stockmaster.modules.stock.repository.OutboundRepository;
import com.stockmaster.modules.stock.repository.ProductRepository;
import com.stockmaster.modules.stock.service.OutboundService;
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

@Service
@RequiredArgsConstructor
public class OutboundServiceImpl implements OutboundService {

    private final OutboundRepository outboundRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public Outbound getById(Long id) {
        return outboundRepository.findById(id)
                .orElseThrow(() -> new BusinessException("出库记录不存在"));
    }

    @Override
    @Transactional
    public Outbound create(OutboundDTO outboundDTO) {
        // 校验出库数量必须大于0
        if (outboundDTO.getQuantity() == null || outboundDTO.getQuantity() <= 0) {
            throw new BusinessException("出库数量必须大于0");
        }
        
        Product product = productRepository.findById(outboundDTO.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        Inventory inventory = inventoryRepository.findByProductId(outboundDTO.getProductId())
                .orElseThrow(() -> new BusinessException("库存记录不存在"));

        if (inventory.getAvailableQuantity() < outboundDTO.getQuantity()) {
            throw new BusinessException("库存不足，当前可用库存：" + inventory.getAvailableQuantity());
        }

        Outbound outbound = new Outbound();
        outbound.setOutboundNo(generateOutboundNo());
        outbound.setProductId(outboundDTO.getProductId());
        outbound.setQuantity(outboundDTO.getQuantity());
        outbound.setUnitPrice(outboundDTO.getUnitPrice() != null ? outboundDTO.getUnitPrice() : product.getSalePrice());
        outbound.setTotalPrice(outbound.getUnitPrice().multiply(BigDecimal.valueOf(outboundDTO.getQuantity())));
        outbound.setWarehouseCode(outboundDTO.getWarehouseCode());
        outbound.setBatchNo(outboundDTO.getBatchNo());
        outbound.setOutboundType(outboundDTO.getOutboundType());
        outbound.setOutboundTime(LocalDateTime.now());
        outbound.setStatus(1);

        outbound = outboundRepository.save(outbound);

        // 更新库存
        inventory.setQuantity(inventory.getQuantity() - outboundDTO.getQuantity());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - outboundDTO.getQuantity());
        inventoryRepository.save(inventory);

        return outbound;
    }

    @Override
    @Transactional
    public Outbound update(Long id, OutboundDTO outboundDTO) {
        // 校验出库数量必须大于0
        if (outboundDTO.getQuantity() == null || outboundDTO.getQuantity() <= 0) {
            throw new BusinessException("出库数量必须大于0");
        }
        
        Outbound outbound = outboundRepository.findById(id)
                .orElseThrow(() -> new BusinessException("出库记录不存在"));

        if (outbound.getStatus() != 1) {
            throw new BusinessException("只能修改待处理的出库记录");
        }

        Long oldProductId = outbound.getProductId();
        Long newProductId = outboundDTO.getProductId();
        int oldQuantity = outbound.getQuantity();
        int newQuantity = outboundDTO.getQuantity();

        // 处理库存更新
        if (!oldProductId.equals(newProductId)) {
            // 产品ID改变了，需要回退原产品库存并扣减新产品库存
            Inventory oldInventory = inventoryRepository.findByProductId(oldProductId)
                    .orElseThrow(() -> new BusinessException("原产品库存记录不存在"));
            oldInventory.setQuantity(oldInventory.getQuantity() + oldQuantity);
            oldInventory.setAvailableQuantity(oldInventory.getAvailableQuantity() + oldQuantity);
            inventoryRepository.save(oldInventory);

            Inventory newInventory = inventoryRepository.findByProductId(newProductId)
                    .orElseThrow(() -> new BusinessException("新产品库存记录不存在"));
            
            if (newInventory.getAvailableQuantity() < newQuantity) {
                throw new BusinessException("新产品库存不足，当前可用库存：" + newInventory.getAvailableQuantity());
            }
            newInventory.setQuantity(newInventory.getQuantity() - newQuantity);
            newInventory.setAvailableQuantity(newInventory.getAvailableQuantity() - newQuantity);
            inventoryRepository.save(newInventory);
        } else {
            // 产品ID未变，只更新数量差异
            Inventory inventory = inventoryRepository.findByProductId(newProductId)
                    .orElseThrow(() -> new BusinessException("库存记录不存在"));
            
            int diff = newQuantity - oldQuantity;
            if (diff > 0 && inventory.getAvailableQuantity() < diff) {
                throw new BusinessException("库存不足，当前可用库存：" + inventory.getAvailableQuantity());
            }
            
            if (diff != 0) {
                inventory.setQuantity(inventory.getQuantity() - diff);
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() - diff);
                inventoryRepository.save(inventory);
            }
        }

        Product product = productRepository.findById(newProductId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        outbound.setProductId(newProductId);
        outbound.setQuantity(newQuantity);
        outbound.setUnitPrice(outboundDTO.getUnitPrice() != null ? outboundDTO.getUnitPrice() : product.getSalePrice());
        outbound.setTotalPrice(outbound.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity)));
        outbound.setWarehouseCode(outboundDTO.getWarehouseCode());
        outbound.setBatchNo(outboundDTO.getBatchNo());
        outbound.setOutboundType(outboundDTO.getOutboundType());

        outbound = outboundRepository.save(outbound);
        return outbound;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Outbound outbound = outboundRepository.findById(id)
                .orElseThrow(() -> new BusinessException("出库记录不存在"));

        if (outbound.getStatus() != 1) {
            throw new BusinessException("只能删除待处理的出库记录");
        }

        // 回退库存
        Inventory inventory = inventoryRepository.findByProductId(outbound.getProductId())
                .orElseThrow(() -> new BusinessException("库存记录不存在"));
        inventory.setQuantity(inventory.getQuantity() + outbound.getQuantity());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + outbound.getQuantity());
        inventoryRepository.save(inventory);

        outbound.setDeleted(true);
        outboundRepository.save(outbound);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            delete(id);
        }
    }

    @Override
    public PageResult<Outbound> getList(String keyword, Long productId, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Outbound> page = outboundRepository.findByConditions(keyword, productId, pageable);
        return PageResult.of(page.getContent(), page.getTotalElements(), pageNum, pageSize);
    }

    @Override
    public String generateOutboundNo() {
        String prefix = "OUT";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fullPrefix = prefix + dateStr;
        
        // 查询当天已有的出库单数量
        Long count = outboundRepository.countByOutboundNoPrefix(fullPrefix);
        if (count == null) {
            count = 0L;
        }
        
        // 生成单号并确保唯一性
        String outboundNo;
        int seq = count.intValue() + 1;
        do {
            outboundNo = fullPrefix + String.format("%06d", seq);
            seq++;
        } while (outboundRepository.existsByOutboundNo(outboundNo));
        
        return outboundNo;
    }
}
