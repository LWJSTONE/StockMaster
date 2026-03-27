package com.stockmaster.modules.purchase.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.common.enums.OrderStatus;
import com.stockmaster.modules.purchase.dto.PurchaseOrderDTO;
import com.stockmaster.modules.purchase.dto.PurchaseOrderVO;
import com.stockmaster.modules.purchase.service.PurchaseOrderService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase/orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    @LogOperation(value = OperationType.QUERY, module = "采购订单", description = "查询订单列表")
    public ApiResponse<PageResult<PurchaseOrderVO>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<PurchaseOrderVO> result = purchaseOrderService.getList(keyword, supplierId, status, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @LogOperation(value = OperationType.QUERY, module = "采购订单", description = "查询订单详情")
    public ApiResponse<PurchaseOrderVO> getById(@PathVariable Long id) {
        PurchaseOrderVO order = purchaseOrderService.getById(id);
        return ApiResponse.success(order);
    }

    @PostMapping
    @LogOperation(value = OperationType.CREATE, module = "采购订单", description = "创建采购订单")
    public ApiResponse<PurchaseOrderVO> create(@Valid @RequestBody PurchaseOrderDTO dto) {
        PurchaseOrderVO order = purchaseOrderService.create(dto);
        return ApiResponse.success(order);
    }

    @PutMapping("/{id}")
    @LogOperation(value = OperationType.UPDATE, module = "采购订单", description = "修改采购订单")
    public ApiResponse<PurchaseOrderVO> update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderDTO dto) {
        PurchaseOrderVO order = purchaseOrderService.update(id, dto);
        return ApiResponse.success(order);
    }

    @DeleteMapping("/{id}")
    @LogOperation(value = OperationType.DELETE, module = "采购订单", description = "删除采购订单")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        purchaseOrderService.delete(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/submit")
    @LogOperation(value = OperationType.UPDATE, module = "采购订单", description = "提交审核")
    public ApiResponse<Void> submit(@PathVariable Long id) {
        purchaseOrderService.updateStatus(id, OrderStatus.PENDING);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/approve")
    @LogOperation(value = OperationType.APPROVE, module = "采购订单", description = "审核通过")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        purchaseOrderService.approve(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/reject")
    @LogOperation(value = OperationType.REJECT, module = "采购订单", description = "审核拒绝")
    public ApiResponse<Void> reject(@PathVariable Long id) {
        purchaseOrderService.reject(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/cancel")
    @LogOperation(value = OperationType.UPDATE, module = "采购订单", description = "取消订单")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        purchaseOrderService.updateStatus(id, OrderStatus.CANCELLED);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/receive")
    @LogOperation(value = OperationType.UPDATE, module = "采购订单", description = "订单收货")
    public ApiResponse<Void> receive(@PathVariable Long id, @RequestBody(required = false) List<Long> itemIds) {
        purchaseOrderService.receive(id, itemIds);
        return ApiResponse.success();
    }
}
