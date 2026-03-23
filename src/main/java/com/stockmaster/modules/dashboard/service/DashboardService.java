package com.stockmaster.modules.dashboard.service;

import com.stockmaster.modules.dashboard.dto.*;

import java.util.List;

public interface DashboardService {

    DashboardStats getStats();

    List<TrendData> getPurchaseTrend(Integer days);

    List<ChartData> getCategoryDistribution();

    List<ChartData> getPurchaseVsStock();

    /**
     * Get sales summary (monthly inbound/outbound statistics)
     */
    SalesSummary getSalesSummary();

    /**
     * Get inventory summary
     */
    InventorySummary getInventorySummary();

    /**
     * Get purchase summary
     */
    PurchaseSummary getPurchaseSummary();

    /**
     * Get alert list (low stock and overstock items)
     */
    List<AlertItem> getAlertList();
}
