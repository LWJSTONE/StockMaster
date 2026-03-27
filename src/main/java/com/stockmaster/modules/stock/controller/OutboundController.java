package com.stockmaster.modules.stock.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.modules.stock.dto.OutboundDTO;
import com.stockmaster.modules.stock.entity.Outbound;
import com.stockmaster.modules.stock.service.OutboundService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock/outbound")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;

    @GetMapping
    @LogOperation(value = OperationType.QUERY, module = "出库管理", description = "查询出库列表")
    public ApiResponse<PageResult<Outbound>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<Outbound> result = outboundService.getList(keyword, productId, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @LogOperation(value = OperationType.QUERY, module = "出库管理", description = "查询出库详情")
    public ApiResponse<Outbound> getById(@PathVariable Long id) {
        Outbound outbound = outboundService.getById(id);
        return ApiResponse.success(outbound);
    }

    @PostMapping
    @LogOperation(value = OperationType.CREATE, module = "出库管理", description = "创建出库记录")
    public ApiResponse<Outbound> create(@Valid @RequestBody OutboundDTO outboundDTO) {
        Outbound outbound = outboundService.create(outboundDTO);
        return ApiResponse.success(outbound);
    }

    @PutMapping("/{id}")
    @LogOperation(value = OperationType.UPDATE, module = "出库管理", description = "修改出库记录")
    public ApiResponse<Outbound> update(@PathVariable Long id, @Valid @RequestBody OutboundDTO outboundDTO) {
        Outbound outbound = outboundService.update(id, outboundDTO);
        return ApiResponse.success(outbound);
    }

    @DeleteMapping("/{id}")
    @LogOperation(value = OperationType.DELETE, module = "出库管理", description = "删除出库记录")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        outboundService.delete(id);
        return ApiResponse.success();
    }

    @DeleteMapping("/batch")
    @LogOperation(value = OperationType.DELETE, module = "出库管理", description = "批量删除出库记录")
    public ApiResponse<Void> batchDelete(@RequestBody List<Long> ids) {
        outboundService.batchDelete(ids);
        return ApiResponse.success();
    }

    @GetMapping("/generate-no")
    public ApiResponse<String> generateOutboundNo() {
        String no = outboundService.generateOutboundNo();
        return ApiResponse.success(no);
    }
}
