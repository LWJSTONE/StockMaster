package com.stockmaster.modules.dashboard.service.impl;

import com.stockmaster.common.enums.OrderStatus;
import com.stockmaster.modules.dashboard.dto.*;
import com.stockmaster.modules.dashboard.service.DashboardService;
import com.stockmaster.modules.purchase.repository.PurchaseOrderRepository;
import com.stockmaster.modules.purchase.repository.SupplierRepository;
import com.stockmaster.modules.stock.entity.Category;
import com.stockmaster.modules.stock.entity.Inventory;
import com.stockmaster.modules.stock.entity.Product;
import com.stockmaster.modules.stock.repository.CategoryRepository;
import com.stockmaster.modules.stock.repository.InboundRepository;
import com.stockmaster.modules.stock.repository.InventoryRepository;
import com.stockmaster.modules.stock.repository.OutboundRepository;
import com.stockmaster.modules.stock.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final InboundRepository inboundRepository;
    private final OutboundRepository outboundRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        stats.setProductCount(productRepository.countActiveProducts());
        stats.setSupplierCount(supplierRepository.countActiveSuppliers());
        stats.setPurchaseOrderCount(purchaseOrderRepository.count());

        Long lowStockCount = inventoryRepository.countLowStockProducts();
        stats.setLowStockCount(lowStockCount != null ? lowStockCount : 0L);

        Long totalStock = inventoryRepository.getTotalStockQuantity();
        stats.setTotalStockQuantity(totalStock != null ? totalStock : 0L);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        Double purchaseAmount = inboundRepository.sumTotalPriceBetween(todayStart.minusDays(30), todayEnd);
        stats.setTotalPurchaseAmount(purchaseAmount != null ? BigDecimal.valueOf(purchaseAmount) : BigDecimal.ZERO);

        Double outboundAmount = outboundRepository.sumTotalPriceBetween(todayStart.minusDays(30), todayEnd);
        stats.setTotalOutboundAmount(outboundAmount != null ? BigDecimal.valueOf(outboundAmount) : BigDecimal.ZERO);

        // 计算今日入库数量
        Long todayInboundCount = inboundRepository.countByTimeBetween(todayStart, todayEnd);
        stats.setTodayInboundCount(todayInboundCount != null ? todayInboundCount : 0L);

        // 计算今日出库数量
        Long todayOutboundCount = outboundRepository.countByTimeBetween(todayStart, todayEnd);
        stats.setTodayOutboundCount(todayOutboundCount != null ? todayOutboundCount : 0L);

        // 计算库存总价值
        BigDecimal totalInventoryValue = BigDecimal.ZERO;
        List<Inventory> allInventory = inventoryRepository.findAll();
        for (Inventory inv : allInventory) {
            if (inv.getQuantity() != null && inv.getQuantity() > 0) {
                Optional<Product> productOpt = productRepository.findById(inv.getProductId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    BigDecimal costPrice = product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
                    totalInventoryValue = totalInventoryValue.add(costPrice.multiply(BigDecimal.valueOf(inv.getQuantity())));
                }
            }
        }
        stats.setTotalInventoryValue(totalInventoryValue);

        return stats;
    }

    @Override
    public List<TrendData> getPurchaseTrend(Integer days) {
        List<TrendData> trends = new ArrayList<>();
        LocalDateTime endTime = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime startTime = LocalDate.now().minusDays(days - 1).atStartOfDay();

        List<Object[]> purchaseStats = inboundRepository.getDailyStats(startTime, endTime);
        List<Object[]> outboundStats = outboundRepository.getDailyStats(startTime, endTime);

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            TrendData trend = new TrendData();
            trend.setDate(date.toString());
            trend.setPurchaseCount(0);
            trend.setOutboundCount(0);
            trend.setPurchaseAmount(BigDecimal.ZERO);
            trend.setOutboundAmount(BigDecimal.ZERO);

            for (Object[] stat : purchaseStats) {
                if (stat[0] != null && stat[0].toString().equals(date.toString())) {
                    trend.setPurchaseCount(stat[1] != null ? ((Number) stat[1]).intValue() : 0);
                    break;
                }
            }

            for (Object[] stat : outboundStats) {
                if (stat[0] != null && stat[0].toString().equals(date.toString())) {
                    trend.setOutboundCount(stat[1] != null ? ((Number) stat[1]).intValue() : 0);
                    break;
                }
            }

            trends.add(trend);
        }

        return trends;
    }

    @Override
    public List<ChartData> getCategoryDistribution() {
        List<ChartData> distribution = new ArrayList<>();

        // 获取所有分类的商品数量
        List<Category> categories = categoryRepository.findAllActive();
        for (Category category : categories) {
            List<Product> products = productRepository.findByCategoryId(category.getId());
            if (!products.isEmpty()) {
                ChartData data = new ChartData();
                data.setLabel(category.getCategoryName());
                data.setValue(BigDecimal.valueOf(products.size()));
                distribution.add(data);
            }
        }

        return distribution;
    }

    @Override
    public List<ChartData> getPurchaseVsStock() {
        List<ChartData> comparison = new ArrayList<>();

        // 计算采购总额
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = LocalDate.now().atTime(LocalTime.MAX);

        Double purchaseAmount = inboundRepository.sumTotalPriceBetween(monthStart, monthEnd);
        Double outboundAmount = outboundRepository.sumTotalPriceBetween(monthStart, monthEnd);

        ChartData purchaseData = new ChartData();
        purchaseData.setLabel("采购金额");
        purchaseData.setValue(purchaseAmount != null ? BigDecimal.valueOf(purchaseAmount) : BigDecimal.ZERO);
        comparison.add(purchaseData);

        ChartData outboundData = new ChartData();
        outboundData.setLabel("出库金额");
        outboundData.setValue(outboundAmount != null ? BigDecimal.valueOf(outboundAmount) : BigDecimal.ZERO);
        comparison.add(outboundData);

        Long totalStock = inventoryRepository.getTotalStockQuantity();
        ChartData stockData = new ChartData();
        stockData.setLabel("库存数量");
        stockData.setValue(BigDecimal.valueOf(totalStock != null ? totalStock : 0L));
        comparison.add(stockData);

        return comparison;
    }

    @Override
    public SalesSummary getSalesSummary() {
        SalesSummary summary = new SalesSummary();

        // Current month time range
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = LocalDate.now().atTime(LocalTime.MAX);

        // Last month time range
        LocalDateTime lastMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastMonthEnd = LocalDate.now().minusMonths(1).withDayOfMonth(LocalDate.now().minusMonths(1).lengthOfMonth()).atTime(LocalTime.MAX);

        // Year time range
        LocalDateTime yearStart = LocalDate.now().withDayOfYear(1).atStartOfDay();

        // Current month statistics
        Double monthInboundAmount = inboundRepository.sumTotalPriceBetween(monthStart, monthEnd);
        Double monthOutboundAmount = outboundRepository.sumTotalPriceBetween(monthStart, monthEnd);
        Long monthInboundCount = inboundRepository.countByTimeBetween(monthStart, monthEnd);
        Long monthOutboundCount = outboundRepository.countByTimeBetween(monthStart, monthEnd);

        summary.setMonthInboundAmount(monthInboundAmount != null ? BigDecimal.valueOf(monthInboundAmount) : BigDecimal.ZERO);
        summary.setMonthOutboundAmount(monthOutboundAmount != null ? BigDecimal.valueOf(monthOutboundAmount) : BigDecimal.ZERO);
        summary.setMonthInboundCount(monthInboundCount != null ? monthInboundCount : 0L);
        summary.setMonthOutboundCount(monthOutboundCount != null ? monthOutboundCount : 0L);

        // Year-to-date statistics
        Double yearInboundAmount = inboundRepository.sumTotalPriceBetween(yearStart, monthEnd);
        Double yearOutboundAmount = outboundRepository.sumTotalPriceBetween(yearStart, monthEnd);

        summary.setYearInboundAmount(yearInboundAmount != null ? BigDecimal.valueOf(yearInboundAmount) : BigDecimal.ZERO);
        summary.setYearOutboundAmount(yearOutboundAmount != null ? BigDecimal.valueOf(yearOutboundAmount) : BigDecimal.ZERO);

        // Calculate growth rate
        Double lastMonthInboundAmount = inboundRepository.sumTotalPriceBetween(lastMonthStart, lastMonthEnd);
        Double lastMonthOutboundAmount = outboundRepository.sumTotalPriceBetween(lastMonthStart, lastMonthEnd);

        summary.setInboundGrowthRate(calculateGrowthRate(monthInboundAmount, lastMonthInboundAmount));
        summary.setOutboundGrowthRate(calculateGrowthRate(monthOutboundAmount, lastMonthOutboundAmount));

        return summary;
    }

    @Override
    public InventorySummary getInventorySummary() {
        InventorySummary summary = new InventorySummary();

        // Total quantity
        Long totalQuantity = inventoryRepository.getTotalStockQuantity();
        summary.setTotalQuantity(totalQuantity != null ? totalQuantity : 0L);

        // Total value calculation
        BigDecimal totalValue = BigDecimal.ZERO;
        List<Inventory> allInventory = inventoryRepository.findAll();
        for (Inventory inv : allInventory) {
            if (inv.getQuantity() != null && inv.getQuantity() > 0) {
                Optional<Product> productOpt = productRepository.findById(inv.getProductId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    BigDecimal costPrice = product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
                    totalValue = totalValue.add(costPrice.multiply(BigDecimal.valueOf(inv.getQuantity())));
                }
            }
        }
        summary.setTotalValue(totalValue);

        // Low stock and overstock counts
        Long lowStockCount = inventoryRepository.countLowStockProducts();
        List<Inventory> overstockList = inventoryRepository.findOverStock();
        summary.setLowStockCount(lowStockCount != null ? lowStockCount : 0L);
        summary.setOverStockCount((long) overstockList.size());

        // Product type count
        Long productTypeCount = productRepository.countActiveProducts();
        summary.setProductTypeCount(productTypeCount != null ? productTypeCount : 0L);

        // Average stock value
        if (productTypeCount != null && productTypeCount > 0) {
            summary.setAvgStockValue(totalValue.divide(BigDecimal.valueOf(productTypeCount), 2, RoundingMode.HALF_UP));
        } else {
            summary.setAvgStockValue(BigDecimal.ZERO);
        }

        return summary;
    }

    @Override
    public PurchaseSummary getPurchaseSummary() {
        PurchaseSummary summary = new PurchaseSummary();

        // Current month time range
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = LocalDate.now().atTime(LocalTime.MAX);

        // Year time range
        LocalDateTime yearStart = LocalDate.now().withDayOfYear(1).atStartOfDay();

        // Month order count
        Long monthOrderCount = purchaseOrderRepository.countByTimeBetween(monthStart, monthEnd);
        summary.setMonthOrderCount(monthOrderCount != null ? monthOrderCount : 0L);

        // Month purchase amount
        Double monthPurchaseAmount = purchaseOrderRepository.sumAmountBetween(monthStart, monthEnd);
        summary.setMonthPurchaseAmount(monthPurchaseAmount != null ? BigDecimal.valueOf(monthPurchaseAmount) : BigDecimal.ZERO);

        // Pending orders
        Long pendingCount = purchaseOrderRepository.countByStatus(OrderStatus.PENDING);
        summary.setPendingOrderCount(pendingCount != null ? pendingCount : 0L);

        // Completed orders
        Long completedCount = purchaseOrderRepository.countByStatus(OrderStatus.COMPLETED);
        summary.setCompletedOrderCount(completedCount != null ? completedCount : 0L);

        // Year-to-date purchase amount
        Double yearPurchaseAmount = purchaseOrderRepository.sumAmountBetween(yearStart, monthEnd);
        summary.setYearPurchaseAmount(yearPurchaseAmount != null ? BigDecimal.valueOf(yearPurchaseAmount) : BigDecimal.ZERO);

        // Active suppliers
        Long activeSupplierCount = supplierRepository.countActiveSuppliers();
        summary.setActiveSupplierCount(activeSupplierCount != null ? activeSupplierCount : 0L);

        return summary;
    }

    @Override
    public List<AlertItem> getAlertList() {
        List<AlertItem> alerts = new ArrayList<>();

        // Get low stock items
        List<Inventory> lowStockItems = inventoryRepository.findLowStock();
        for (Inventory inv : lowStockItems) {
            if (Boolean.TRUE.equals(inv.getDeleted())) continue;
            
            AlertItem alert = new AlertItem();
            alert.setProductId(inv.getProductId());
            alert.setCurrentQuantity(inv.getQuantity());
            alert.setAlertType("LOW_STOCK");
            alert.setThreshold(inv.getWarningMin());

            Optional<Product> productOpt = productRepository.findById(inv.getProductId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                alert.setProductCode(product.getProductCode());
                alert.setProductName(product.getProductName());
            }

            // Determine severity
            if (inv.getQuantity() == 0) {
                alert.setSeverity("HIGH");
                alert.setSuggestion("立即补货，库存已耗尽");
            } else if (inv.getWarningMin() != null && inv.getQuantity() < inv.getWarningMin() / 2) {
                alert.setSeverity("HIGH");
                alert.setSuggestion("急需补货，库存严重不足");
            } else {
                alert.setSeverity("MEDIUM");
                alert.setSuggestion("建议尽快补货");
            }

            alerts.add(alert);
        }

        // Get overstock items
        List<Inventory> overstockItems = inventoryRepository.findOverStock();
        for (Inventory inv : overstockItems) {
            if (Boolean.TRUE.equals(inv.getDeleted())) continue;
            
            AlertItem alert = new AlertItem();
            alert.setProductId(inv.getProductId());
            alert.setCurrentQuantity(inv.getQuantity());
            alert.setAlertType("OVERSTOCK");
            alert.setThreshold(inv.getWarningMax());

            Optional<Product> productOpt = productRepository.findById(inv.getProductId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                alert.setProductCode(product.getProductCode());
                alert.setProductName(product.getProductName());
            }

            // Determine severity
            if (inv.getWarningMax() != null && inv.getQuantity() > inv.getWarningMax() * 2) {
                alert.setSeverity("HIGH");
                alert.setSuggestion("严重积压，建议促销处理");
            } else {
                alert.setSeverity("MEDIUM");
                alert.setSuggestion("库存偏高，建议控制采购");
            }

            alerts.add(alert);
        }

        // Sort by severity: HIGH first
        alerts.sort((a, b) -> {
            int severityOrder = severityOrder(a.getSeverity()) - severityOrder(b.getSeverity());
            return severityOrder != 0 ? severityOrder : a.getProductName().compareToIgnoreCase(b.getProductName());
        });

        return alerts;
    }

    private int severityOrder(String severity) {
        if ("HIGH".equals(severity)) return 1;
        if ("MEDIUM".equals(severity)) return 2;
        return 3;
    }

    private BigDecimal calculateGrowthRate(Double current, Double previous) {
        if (previous == null || previous == 0) {
            if (current != null && current > 0) {
                return BigDecimal.valueOf(100); // 100% growth from zero
            }
            return BigDecimal.ZERO;
        }
        if (current == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf((current - previous) / previous * 100)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
