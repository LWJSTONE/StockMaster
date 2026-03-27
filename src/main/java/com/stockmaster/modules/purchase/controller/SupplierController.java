package com.stockmaster.modules.purchase.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.modules.purchase.dto.SupplierDTO;
import com.stockmaster.modules.purchase.dto.SupplierVO;
import com.stockmaster.modules.purchase.service.SupplierService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @LogOperation(value = OperationType.QUERY, module = "供应商管理", description = "查询供应商列表")
    public ApiResponse<PageResult<SupplierVO>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<SupplierVO> result = supplierService.getList(keyword, status, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/all")
    public ApiResponse<List<SupplierVO>> getAllSuppliers() {
        List<SupplierVO> suppliers = supplierService.getAllSuppliers();
        return ApiResponse.success(suppliers);
    }

    @GetMapping("/{id}")
    @LogOperation(value = OperationType.QUERY, module = "供应商管理", description = "查询供应商详情")
    public ApiResponse<SupplierVO> getById(@PathVariable Long id) {
        SupplierVO supplier = supplierService.getById(id);
        return ApiResponse.success(supplier);
    }

    @PostMapping
    @LogOperation(value = OperationType.CREATE, module = "供应商管理", description = "创建供应商")
    public ApiResponse<SupplierVO> create(@Valid @RequestBody SupplierDTO supplierDTO) {
        SupplierVO supplier = supplierService.create(supplierDTO);
        return ApiResponse.success(supplier);
    }

    @PutMapping("/{id}")
    @LogOperation(value = OperationType.UPDATE, module = "供应商管理", description = "修改供应商")
    public ApiResponse<SupplierVO> update(@PathVariable Long id, @Valid @RequestBody SupplierDTO supplierDTO) {
        SupplierVO supplier = supplierService.update(id, supplierDTO);
        return ApiResponse.success(supplier);
    }

    @DeleteMapping("/{id}")
    @LogOperation(value = OperationType.DELETE, module = "供应商管理", description = "删除供应商")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    @LogOperation(value = OperationType.UPDATE, module = "供应商管理", description = "修改供应商状态")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        supplierService.updateStatus(id, status);
        return ApiResponse.success();
    }
}
