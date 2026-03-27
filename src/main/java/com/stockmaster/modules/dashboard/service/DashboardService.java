package com.stockmaster.modules.dashboard.service;

import com.stockmaster.modules.dashboard.dto.DashboardStats;

public interface DashboardService {

    DashboardStats getDashboardStats();

    DashboardStats.SalesSummary getSalesSummary();

    DashboardStats.InventorySummary getInventorySummary();

    DashboardStats.PurchaseSummary getPurchaseSummary();

    java.util.List<DashboardStats.TrendData> getSalesTrend(String startDate, String endDate);

    java.util.List<DashboardStats.ChartData> getCategoryDistribution();

    java.util.List<DashboardStats.AlertItem> getAlerts();
}
