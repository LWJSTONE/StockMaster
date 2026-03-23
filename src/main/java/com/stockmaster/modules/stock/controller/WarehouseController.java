package com.stockmaster.modules.stock.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.modules.stock.entity.Warehouse;
import com.stockmaster.modules.stock.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    @LogOperation(value = OperationType.QUERY, module = "仓库管理", description = "查询仓库列表")
    public ApiResponse<PageResult<Warehouse>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<Warehouse> result = warehouseService.getList(keyword, status, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/all")
    public ApiResponse<List<Warehouse>> getAllActive() {
        List<Warehouse> warehouses = warehouseService.getAllActive();
        return ApiResponse.success(warehouses);
    }

    @GetMapping("/{id}")
    @LogOperation(value = OperationType.QUERY, module = "仓库管理", description = "查询仓库详情")
    public ApiResponse<Warehouse> getById(@PathVariable Long id) {
        Warehouse warehouse = warehouseService.getById(id);
        return ApiResponse.success(warehouse);
    }

    @GetMapping("/code/{code}")
    @LogOperation(value = OperationType.QUERY, module = "仓库管理", description = "根据编码查询仓库")
    public ApiResponse<Warehouse> getByCode(@PathVariable String code) {
        Warehouse warehouse = warehouseService.getByCode(code);
        return ApiResponse.success(warehouse);
    }

    @PostMapping
    @LogOperation(value = OperationType.CREATE, module = "仓库管理", description = "创建仓库")
    public ApiResponse<Warehouse> create(@RequestBody Warehouse warehouse) {
        Warehouse created = warehouseService.create(warehouse);
        return ApiResponse.success(created);
    }

    @PutMapping("/{id}")
    @LogOperation(value = OperationType.UPDATE, module = "仓库管理", description = "修改仓库")
    public ApiResponse<Warehouse> update(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        Warehouse updated = warehouseService.update(id, warehouse);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    @LogOperation(value = OperationType.DELETE, module = "仓库管理", description = "删除仓库")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    @LogOperation(value = OperationType.UPDATE, module = "仓库管理", description = "修改仓库状态")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        warehouseService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @GetMapping("/count")
    public ApiResponse<Long> countActiveWarehouses() {
        Long count = warehouseService.countActiveWarehouses();
        return ApiResponse.success(count);
    }
}
