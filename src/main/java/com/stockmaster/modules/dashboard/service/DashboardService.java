package com.stockmaster.modules.dashboard.service;

import com.stockmaster.modules.dashboard.dto.AlertItem;
import com.stockmaster.modules.dashboard.dto.ChartData;
import com.stockmaster.modules.dashboard.dto.DashboardStats;
import com.stockmaster.modules.dashboard.dto.InventorySummary;
import com.stockmaster.modules.dashboard.dto.PurchaseSummary;
import com.stockmaster.modules.dashboard.dto.SalesSummary;
import com.stockmaster.modules.dashboard.dto.TrendData;

import java.util.List;

public interface DashboardService {

    DashboardStats getStats();

    SalesSummary getSalesSummary();

    InventorySummary getInventorySummary();

    PurchaseSummary getPurchaseSummary();

    List<TrendData> getPurchaseTrend(Integer days);

    List<ChartData> getCategoryDistribution();

    List<ChartData> getPurchaseVsStock();

    List<AlertItem> getAlertList();
}
