package com.stockmaster.modules.purchase.service;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.OrderStatus;
import com.stockmaster.modules.purchase.dto.PurchaseOrderDTO;
import com.stockmaster.modules.purchase.dto.PurchaseOrderVO;

import java.util.List;

public interface PurchaseOrderService {

    PurchaseOrderVO getById(Long id);

    PageResult<PurchaseOrderVO> getList(String keyword, Long supplierId, OrderStatus status, Integer pageNum, Integer pageSize);

    PurchaseOrderVO create(PurchaseOrderDTO dto);

    PurchaseOrderVO update(Long id, PurchaseOrderDTO dto);

    void delete(Long id);

    void updateStatus(Long id, OrderStatus status);

    void approve(Long id);

    void reject(Long id);

    void receive(Long id, List<Long> itemIds);
}
