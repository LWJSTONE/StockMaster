package com.stockmaster.modules.dashboard.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long totalProducts;
    private Long totalInventory;
    private Long lowStockCount;
    private Long totalSuppliers;
    private BigDecimal totalPurchaseAmount;
    private BigDecimal totalSalesAmount;
    private Integer pendingOrders;
    private Integer completedOrders;
    private List<TrendData> salesTrend;
    private List<ChartData> categoryDistribution;
    private SalesSummary salesSummary;
    private InventorySummary inventorySummary;
    private PurchaseSummary purchaseSummary;
    private List<AlertItem> alerts;

    @Data
    public static class TrendData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String date;
        private BigDecimal amount;
        private Integer quantity;
    }

    @Data
    public static class ChartData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private BigDecimal value;
        private Integer count;
    }

    @Data
    public static class SalesSummary implements Serializable {
        private static final long serialVersionUID = 1L;
        private BigDecimal todayAmount;
        private BigDecimal weekAmount;
        private BigDecimal monthAmount;
        private Integer todayOrders;
        private Integer weekOrders;
        private Integer monthOrders;
    }

    @Data
    public static class InventorySummary implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long totalQuantity;
        private Long totalProducts;
        private Long lowStockProducts;
        private Long overStockProducts;
    }

    @Data
    public static class PurchaseSummary implements Serializable {
        private static final long serialVersionUID = 1L;
        private BigDecimal todayAmount;
        private BigDecimal weekAmount;
        private BigDecimal monthAmount;
        private Integer todayOrders;
        private Integer weekOrders;
        private Integer monthOrders;
    }

    @Data
    public static class AlertItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String type;
        private String title;
        private String message;
        private String level;
        private String createTime;
    }
}
