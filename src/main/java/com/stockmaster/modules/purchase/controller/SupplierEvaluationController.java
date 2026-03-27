package com.stockmaster.modules.purchase.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.modules.purchase.dto.SupplierEvaluationDTO;
import com.stockmaster.modules.purchase.entity.SupplierEvaluation;
import com.stockmaster.modules.purchase.service.SupplierEvaluationService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase/evaluations")
@RequiredArgsConstructor
public class SupplierEvaluationController {

    private final SupplierEvaluationService evaluationService;

    @GetMapping
    @LogOperation(value = OperationType.QUERY, module = "供应商评价", description = "查询评价列表")
    public ApiResponse<PageResult<SupplierEvaluation>> getList(
            @RequestParam Long supplierId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<SupplierEvaluation> result = evaluationService.getList(supplierId, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @LogOperation(value = OperationType.QUERY, module = "供应商评价", description = "查询评价详情")
    public ApiResponse<SupplierEvaluation> getById(@PathVariable Long id) {
        SupplierEvaluation evaluation = evaluationService.getById(id);
        return ApiResponse.success(evaluation);
    }

    @PostMapping
    @LogOperation(value = OperationType.CREATE, module = "供应商评价", description = "创建评价")
    public ApiResponse<SupplierEvaluation> create(@Valid @RequestBody SupplierEvaluationDTO dto) {
        SupplierEvaluation evaluation = evaluationService.create(dto);
        return ApiResponse.success(evaluation);
    }

    @PutMapping("/{id}")
    @LogOperation(value = OperationType.UPDATE, module = "供应商评价", description = "修改评价")
    public ApiResponse<SupplierEvaluation> update(@PathVariable Long id, @Valid @RequestBody SupplierEvaluationDTO dto) {
        SupplierEvaluation evaluation = evaluationService.update(id, dto);
        return ApiResponse.success(evaluation);
    }

    @DeleteMapping("/{id}")
    @LogOperation(value = OperationType.DELETE, module = "供应商评价", description = "删除评价")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        evaluationService.delete(id);
        return ApiResponse.success();
    }
}
