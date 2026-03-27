package com.stockmaster.modules.stock.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.modules.stock.dto.InboundDTO;
import com.stockmaster.modules.stock.entity.Inbound;
import com.stockmaster.modules.stock.service.InboundService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock/inbound")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;

    @GetMapping
    @LogOperation(value = OperationType.QUERY, module = "入库管理", description = "查询入库列表")
    public ApiResponse<PageResult<Inbound>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<Inbound> result = inboundService.getList(keyword, productId, supplierId, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @LogOperation(value = OperationType.QUERY, module = "入库管理", description = "查询入库详情")
    public ApiResponse<Inbound> getById(@PathVariable Long id) {
        Inbound inbound = inboundService.getById(id);
        return ApiResponse.success(inbound);
    }

    @PostMapping
    @LogOperation(value = OperationType.CREATE, module = "入库管理", description = "创建入库记录")
    public ApiResponse<Inbound> create(@Valid @RequestBody InboundDTO inboundDTO) {
        Inbound inbound = inboundService.create(inboundDTO);
        return ApiResponse.success(inbound);
    }

    @PutMapping("/{id}")
    @LogOperation(value = OperationType.UPDATE, module = "入库管理", description = "修改入库记录")
    public ApiResponse<Inbound> update(@PathVariable Long id, @Valid @RequestBody InboundDTO inboundDTO) {
        Inbound inbound = inboundService.update(id, inboundDTO);
        return ApiResponse.success(inbound);
    }

    @DeleteMapping("/{id}")
    @LogOperation(value = OperationType.DELETE, module = "入库管理", description = "删除入库记录")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        inboundService.delete(id);
        return ApiResponse.success();
    }

    @DeleteMapping("/batch")
    @LogOperation(value = OperationType.DELETE, module = "入库管理", description = "批量删除入库记录")
    public ApiResponse<Void> batchDelete(@RequestBody List<Long> ids) {
        inboundService.batchDelete(ids);
        return ApiResponse.success();
    }

    @GetMapping("/generate-no")
    public ApiResponse<String> generateInboundNo() {
        String no = inboundService.generateInboundNo();
        return ApiResponse.success(no);
    }
}
