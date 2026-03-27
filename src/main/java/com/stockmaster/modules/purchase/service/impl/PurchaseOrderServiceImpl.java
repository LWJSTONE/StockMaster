package com.stockmaster.modules.purchase.service.impl;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.OrderStatus;
import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.purchase.dto.PurchaseOrderDTO;
import com.stockmaster.modules.purchase.dto.PurchaseOrderVO;
import com.stockmaster.modules.purchase.entity.PurchaseOrder;
import com.stockmaster.modules.purchase.entity.PurchaseOrderItem;
import com.stockmaster.modules.purchase.entity.Supplier;
import com.stockmaster.modules.purchase.repository.PurchaseOrderItemRepository;
import com.stockmaster.modules.purchase.repository.PurchaseOrderRepository;
import com.stockmaster.modules.purchase.repository.SupplierRepository;
import com.stockmaster.modules.purchase.service.PurchaseOrderService;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InboundRepository inboundRepository;
    private final InventoryRepository inventoryRepository;
    private final InboundService inboundService;

    @Override
    public PurchaseOrderVO getById(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购订单不存在"));
        return convertToVO(order);
    }

    @Override
    public PageResult<PurchaseOrderVO> getList(String keyword, Long supplierId, OrderStatus status, Integer pageNum, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<PurchaseOrder> page = purchaseOrderRepository.findByConditions(keyword, supplierId, status, pageRequest);

        List<PurchaseOrderVO> voList = page.getContent().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotalElements(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public PurchaseOrderVO create(PurchaseOrderDTO dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new BusinessException("供应商不存在"));

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(generateOrderNo());
        order.setSupplierId(dto.getSupplierId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setRemark(dto.getRemark());

        if (dto.getExpectedDate() != null && !dto.getExpectedDate().isEmpty()) {
            order.setExpectedDate(LocalDateTime.parse(dto.getExpectedDate() + "T00:00:00"));
        }

        order = purchaseOrderRepository.save(order);

        // 保存订单明细
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<PurchaseOrderItem> items = new ArrayList<>();

            for (PurchaseOrderDTO.PurchaseOrderItemDTO itemDTO : dto.getItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new BusinessException("商品不存在: " + itemDTO.getProductId()));

                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setOrderId(order.getId());
                item.setProductId(itemDTO.getProductId());
                item.setQuantity(itemDTO.getQuantity());
                item.setUnitPrice(itemDTO.getUnitPrice());
                item.setReceivedQuantity(0);

                BigDecimal totalPrice = itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
                item.setTotalPrice(totalPrice);
                totalAmount = totalAmount.add(totalPrice);

                items.add(item);
            }

            purchaseOrderItemRepository.saveAll(items);
            order.setTotalAmount(totalAmount);
            order = purchaseOrderRepository.save(order);
        }

        return convertToVO(order);
    }

    @Override
    @Transactional
    public PurchaseOrderVO update(Long id, PurchaseOrderDTO dto) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购订单不存在"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("只有待审核状态的订单可以修改");
        }

        order.setSupplierId(dto.getSupplierId());
        order.setRemark(dto.getRemark());

        if (dto.getExpectedDate() != null && !dto.getExpectedDate().isEmpty()) {
            order.setExpectedDate(LocalDateTime.parse(dto.getExpectedDate() + "T00:00:00"));
        }

        // 删除原有明细
        purchaseOrderItemRepository.deleteByOrderId(id);

        // 保存新明细
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<PurchaseOrderItem> items = new ArrayList<>();

            for (PurchaseOrderDTO.PurchaseOrderItemDTO itemDTO : dto.getItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new BusinessException("商品不存在: " + itemDTO.getProductId()));

                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setOrderId(order.getId());
                item.setProductId(itemDTO.getProductId());
                item.setQuantity(itemDTO.getQuantity());
                item.setUnitPrice(itemDTO.getUnitPrice());
                item.setReceivedQuantity(0);

                BigDecimal totalPrice = itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
                item.setTotalPrice(totalPrice);
                totalAmount = totalAmount.add(totalPrice);

                items.add(item);
            }

            purchaseOrderItemRepository.saveAll(items);
            order.setTotalAmount(totalAmount);
        }

        order = purchaseOrderRepository.save(order);
        return convertToVO(order);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购订单不存在"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("已完成的订单不能删除");
        }

        order.setDeleted(true);
        purchaseOrderRepository.save(order);
        purchaseOrderItemRepository.deleteByOrderId(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, OrderStatus status) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购订单不存在"));

        order.setStatus(status);
        purchaseOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void approve(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购订单不存在"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("只有待审核状态的订单可以审批");
        }

        order.setStatus(OrderStatus.APPROVED);
        order.setApproveTime(LocalDateTime.now());
        purchaseOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void reject(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购订单不存在"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("只有待审核状态的订单可以拒绝");
        }

        order.setStatus(OrderStatus.REJECTED);
        purchaseOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void receive(Long id, List<Long> itemIds) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购订单不存在"));

        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BusinessException("只有已审批的订单可以收货");
        }

        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByOrderId(id);
        if (items == null || items.isEmpty()) {
            throw new BusinessException("订单明细为空，无法收货");
        }
        
        boolean allReceived = true;

        for (PurchaseOrderItem item : items) {
            // 如果itemIds为null或空，则收货所有待收货的项目；否则只收货指定的项目
            boolean shouldReceive = itemIds == null || itemIds.isEmpty() || itemIds.contains(item.getId());
            
            if (shouldReceive && item.getReceivedQuantity() < item.getQuantity()) {
                // 计算本次收货数量（收货剩余未收货的数量）
                int receiveQty = item.getQuantity() - item.getReceivedQuantity();
                if (receiveQty > 0) {
                    item.setReceivedQuantity(item.getReceivedQuantity() + receiveQty);
                    
                    // 创建入库记录并更新库存
                    createInboundRecord(order, item, receiveQty);
                }
            }
            
            if (item.getReceivedQuantity() < item.getQuantity()) {
                allReceived = false;
            }
        }

        purchaseOrderItemRepository.saveAll(items);

        if (allReceived) {
            order.setStatus(OrderStatus.COMPLETED);
            purchaseOrderRepository.save(order);
        }
    }

    private void createInboundRecord(PurchaseOrder order, PurchaseOrderItem item, int quantity) {
        // 创建入库记录
        Inbound inbound = new Inbound();
        inbound.setInboundNo(inboundService.generateInboundNo());
        inbound.setProductId(item.getProductId());
        inbound.setQuantity(quantity);
        inbound.setUnitPrice(item.getUnitPrice());
        inbound.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
        inbound.setSupplierId(order.getSupplierId());
        inbound.setInboundTime(LocalDateTime.now());
        inbound.setStatus(1);
        inboundRepository.save(inbound);

        // 更新库存
        Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setProductId(item.getProductId());
                    inv.setQuantity(0);
                    inv.setFrozenQuantity(0);
                    inv.setAvailableQuantity(0);
                    return inv;
                });
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventoryRepository.save(inventory);
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PO" + date + random;
    }

    private PurchaseOrderVO convertToVO(PurchaseOrder order) {
        PurchaseOrderVO vo = new PurchaseOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setSupplierId(order.getSupplierId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus() != null ? order.getStatus().getDescription() : "");
        vo.setRemark(order.getRemark());
        vo.setApproveTime(order.getApproveTime());
        vo.setExpectedDate(order.getExpectedDate());
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());

        // 获取供应商名称
        supplierRepository.findById(order.getSupplierId()).ifPresent(supplier -> 
            vo.setSupplierName(supplier.getSupplierName())
        );

        // 获取订单明细
        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByOrderId(order.getId());
        List<PurchaseOrderVO.PurchaseOrderItemVO> itemVOs = new ArrayList<>();

        for (PurchaseOrderItem item : items) {
            PurchaseOrderVO.PurchaseOrderItemVO itemVO = new PurchaseOrderVO.PurchaseOrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setProductId(item.getProductId());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setUnitPrice(item.getUnitPrice());
            itemVO.setTotalPrice(item.getTotalPrice());
            itemVO.setReceivedQuantity(item.getReceivedQuantity());

            productRepository.findById(item.getProductId()).ifPresent(product -> {
                itemVO.setProductCode(product.getProductCode());
                itemVO.setProductName(product.getProductName());
                itemVO.setUnit(product.getUnit());
            });

            itemVOs.add(itemVO);
        }

        vo.setItems(itemVOs);
        return vo;
    }
}
