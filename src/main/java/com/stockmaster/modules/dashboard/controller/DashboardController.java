package com.stockmaster.modules.dashboard.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.modules.dashboard.dto.*;
import com.stockmaster.modules.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @LogOperation(value = OperationType.QUERY, module = "仪表盘", description = "获取仪表盘统计数据")
    public ApiResponse<DashboardStats> getStats() {
        DashboardStats stats = dashboardService.getStats();
        return ApiResponse.success(stats);
    }

    @GetMapping("/trend")
    @LogOperation(value = OperationType.QUERY, module = "仪表盘", description = "获取采购趋势数据")
    public ApiResponse<List<TrendData>> getPurchaseTrend(@RequestParam(defaultValue = "7") Integer days) {
        List<TrendData> trend = dashboardService.getPurchaseTrend(days);
        return ApiResponse.success(trend);
    }

    @GetMapping("/category-distribution")
    @LogOperation(value = OperationType.QUERY, module = "仪表盘", description = "获取分类分布数据")
    public ApiResponse<List<ChartData>> getCategoryDistribution() {
        List<ChartData> distribution = dashboardService.getCategoryDistribution();
        return ApiResponse.success(distribution);
    }

    @GetMapping("/purchase-vs-stock")
    @LogOperation(value = OperationType.QUERY, module = "仪表盘", description = "获取采购与库存对比数据")
    public ApiResponse<List<ChartData>> getPurchaseVsStock() {
        List<ChartData> comparison = dashboardService.getPurchaseVsStock();
        return ApiResponse.success(comparison);
    }

    /**
     * Get sales summary statistics
     */
    @GetMapping("/sales-summary")
    @LogOperation(value = OperationType.QUERY, module = "仪表盘", description = "获取销售汇总数据")
    public ApiResponse<SalesSummary> getSalesSummary() {
        SalesSummary summary = dashboardService.getSalesSummary();
        return ApiResponse.success(summary);
    }

    /**
     * Get inventory summary statistics
     */
    @GetMapping("/inventory-summary")
    @LogOperation(value = OperationType.QUERY, module = "仪表盘", description = "获取库存汇总数据")
    public ApiResponse<InventorySummary> getInventorySummary() {
        InventorySummary summary = dashboardService.getInventorySummary();
        return ApiResponse.success(summary);
    }

    /**
     * Get purchase summary statistics
     */
    @GetMapping("/purchase-summary")
    @LogOperation(value = OperationType.QUERY, module = "仪表盘", description = "获取采购汇总数据")
    public ApiResponse<PurchaseSummary> getPurchaseSummary() {
        PurchaseSummary summary = dashboardService.getPurchaseSummary();
        return ApiResponse.success(summary);
    }

    /**
     * Get alert list (low stock and overstock items)
     */
    @GetMapping("/alert-list")
    @LogOperation(value = OperationType.QUERY, module = "仪表盘", description = "获取库存预警列表")
    public ApiResponse<List<AlertItem>> getAlertList() {
        List<AlertItem> alerts = dashboardService.getAlertList();
        return ApiResponse.success(alerts);
    }
}
