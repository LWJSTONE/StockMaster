package com.stockmaster.common.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.common.service.impl.ExcelExportServiceImpl;
import com.stockmaster.common.service.impl.PdfExportServiceImpl;
import com.stockmaster.modules.purchase.entity.PurchaseOrder;
import com.stockmaster.modules.purchase.entity.Supplier;
import com.stockmaster.modules.purchase.repository.PurchaseOrderRepository;
import com.stockmaster.modules.purchase.repository.SupplierRepository;
import com.stockmaster.modules.stock.entity.*;
import com.stockmaster.modules.stock.repository.*;
import com.stockmaster.modules.system.entity.SysLog;
import com.stockmaster.modules.system.repository.SysLogRepository;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Export controller for generating reports
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ExportController {

    private final ExcelExportServiceImpl excelExportService;
    private final PdfExportServiceImpl pdfExportService;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InboundRepository inboundRepository;
    private final OutboundRepository outboundRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final SysLogRepository sysLogRepository;

    /**
     * Export products list
     */
    @GetMapping("/stock/products/export")
    @LogOperation(value = OperationType.EXPORT, module = "商品管理", description = "导出商品列表")
    public void exportProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletResponse response) {

        List<Product> products = productRepository.findByConditions(keyword, categoryId, null,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        List<String> headers = Arrays.asList("商品编码", "商品名称", "品牌", "规格", "单位", "成本价", "销售价", "最低库存", "最高库存", "状态");
        List<List<Object>> dataList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Product product : products) {
            List<Object> row = new ArrayList<>();
            row.add(product.getProductCode());
            row.add(product.getProductName());
            row.add(product.getBrand() != null ? product.getBrand() : "");
            row.add(product.getSpec() != null ? product.getSpec() : "");
            row.add(product.getUnit() != null ? product.getUnit() : "");
            row.add(product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO);
            row.add(product.getSalePrice() != null ? product.getSalePrice() : BigDecimal.ZERO);
            row.add(product.getMinStock() != null ? product.getMinStock() : 0);
            row.add(product.getMaxStock() != null ? product.getMaxStock() : 0);
            row.add(product.getStatus() != null ? product.getStatus().getDescription() : "");
            dataList.add(row);
        }

        String fileName = "商品列表_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        if ("pdf".equalsIgnoreCase(format)) {
            pdfExportService.exportToPdf(fileName, "商品列表", headers, dataList, response);
        } else {
            excelExportService.exportToExcel(fileName, "商品列表", headers, dataList, response);
        }
    }

    /**
     * Export inventory list
     */
    @GetMapping("/stock/inventory/export")
    @LogOperation(value = OperationType.EXPORT, module = "库存管理", description = "导出库存列表")
    public void exportInventory(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletResponse response) {

        List<Inventory> inventories = inventoryRepository.findByKeyword(keyword,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        List<String> headers = Arrays.asList("商品编码", "商品名称", "仓库编码", "库存数量", "冻结数量", "可用数量", "批次号", "货架位置", "预警下限", "预警上限");
        List<List<Object>> dataList = new ArrayList<>();

        for (Inventory inventory : inventories) {
            List<Object> row = new ArrayList<>();
            Product product = productRepository.findById(inventory.getProductId()).orElse(null);
            row.add(product != null ? product.getProductCode() : "");
            row.add(product != null ? product.getProductName() : "");
            row.add(inventory.getWarehouseCode() != null ? inventory.getWarehouseCode() : "");
            row.add(inventory.getQuantity() != null ? inventory.getQuantity() : 0);
            row.add(inventory.getFrozenQuantity() != null ? inventory.getFrozenQuantity() : 0);
            row.add(inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0);
            row.add(inventory.getBatchNo() != null ? inventory.getBatchNo() : "");
            row.add(inventory.getShelfLocation() != null ? inventory.getShelfLocation() : "");
            row.add(inventory.getWarningMin() != null ? inventory.getWarningMin() : 0);
            row.add(inventory.getWarningMax() != null ? inventory.getWarningMax() : 0);
            dataList.add(row);
        }

        String fileName = "库存列表_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        if ("pdf".equalsIgnoreCase(format)) {
            pdfExportService.exportToPdf(fileName, "库存列表", headers, dataList, response);
        } else {
            excelExportService.exportToExcel(fileName, "库存列表", headers, dataList, response);
        }
    }

    /**
     * Export inbound records
     */
    @GetMapping("/stock/inbound/export")
    @LogOperation(value = OperationType.EXPORT, module = "入库管理", description = "导出入库记录")
    public void exportInbound(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletResponse response) {

        List<Inbound> inboundList = inboundRepository.findByConditions(keyword, productId, supplierId,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        List<String> headers = Arrays.asList("入库单号", "商品编码", "商品名称", "数量", "单价", "总价", "供应商", "仓库编码", "批次号", "入库时间", "操作员", "状态");
        List<List<Object>> dataList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Inbound inbound : inboundList) {
            List<Object> row = new ArrayList<>();
            Product product = productRepository.findById(inbound.getProductId()).orElse(null);
            Supplier supplier = inbound.getSupplierId() != null ? supplierRepository.findById(inbound.getSupplierId()).orElse(null) : null;

            row.add(inbound.getInboundNo());
            row.add(product != null ? product.getProductCode() : "");
            row.add(product != null ? product.getProductName() : "");
            row.add(inbound.getQuantity() != null ? inbound.getQuantity() : 0);
            row.add(inbound.getUnitPrice() != null ? inbound.getUnitPrice() : BigDecimal.ZERO);
            row.add(inbound.getTotalPrice() != null ? inbound.getTotalPrice() : BigDecimal.ZERO);
            row.add(supplier != null ? supplier.getSupplierName() : "");
            row.add(inbound.getWarehouseCode() != null ? inbound.getWarehouseCode() : "");
            row.add(inbound.getBatchNo() != null ? inbound.getBatchNo() : "");
            row.add(inbound.getInboundTime() != null ? inbound.getInboundTime().format(formatter) : "");
            row.add(inbound.getOperator() != null ? inbound.getOperator() : "");
            row.add(inbound.getStatus() != null && inbound.getStatus() == 1 ? "已完成" : "已取消");
            dataList.add(row);
        }

        String fileName = "入库记录_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        if ("pdf".equalsIgnoreCase(format)) {
            pdfExportService.exportToPdf(fileName, "入库记录", headers, dataList, response);
        } else {
            excelExportService.exportToExcel(fileName, "入库记录", headers, dataList, response);
        }
    }

    /**
     * Export outbound records
     */
    @GetMapping("/stock/outbound/export")
    @LogOperation(value = OperationType.EXPORT, module = "出库管理", description = "导出出库记录")
    public void exportOutbound(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletResponse response) {

        List<Outbound> outboundList = outboundRepository.findByConditions(keyword, productId,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        List<String> headers = Arrays.asList("出库单号", "商品编码", "商品名称", "数量", "单价", "总价", "仓库编码", "批次号", "出库时间", "操作员", "出库类型", "状态");
        List<List<Object>> dataList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Outbound outbound : outboundList) {
            List<Object> row = new ArrayList<>();
            Product product = productRepository.findById(outbound.getProductId()).orElse(null);

            row.add(outbound.getOutboundNo());
            row.add(product != null ? product.getProductCode() : "");
            row.add(product != null ? product.getProductName() : "");
            row.add(outbound.getQuantity() != null ? outbound.getQuantity() : 0);
            row.add(outbound.getUnitPrice() != null ? outbound.getUnitPrice() : BigDecimal.ZERO);
            row.add(outbound.getTotalPrice() != null ? outbound.getTotalPrice() : BigDecimal.ZERO);
            row.add(outbound.getWarehouseCode() != null ? outbound.getWarehouseCode() : "");
            row.add(outbound.getBatchNo() != null ? outbound.getBatchNo() : "");
            row.add(outbound.getOutboundTime() != null ? outbound.getOutboundTime().format(formatter) : "");
            row.add(outbound.getOperator() != null ? outbound.getOperator() : "");
            row.add(outbound.getOutboundType() != null ? outbound.getOutboundType() : "");
            row.add(outbound.getStatus() != null && outbound.getStatus() == 1 ? "已完成" : "已取消");
            dataList.add(row);
        }

        String fileName = "出库记录_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        if ("pdf".equalsIgnoreCase(format)) {
            pdfExportService.exportToPdf(fileName, "出库记录", headers, dataList, response);
        } else {
            excelExportService.exportToExcel(fileName, "出库记录", headers, dataList, response);
        }
    }

    /**
     * Export purchase orders
     */
    @GetMapping("/purchase/orders/export")
    @LogOperation(value = OperationType.EXPORT, module = "采购管理", description = "导出采购订单")
    public void exportPurchaseOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletResponse response) {

        List<PurchaseOrder> orders = purchaseOrderRepository.findByConditions(keyword, supplierId, null,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        List<String> headers = Arrays.asList("订单编号", "供应商名称", "订单日期", "期望日期", "总金额", "订单状态", "采购员", "审批人", "审批时间", "备注");
        List<List<Object>> dataList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (PurchaseOrder order : orders) {
            List<Object> row = new ArrayList<>();
            Supplier supplier = supplierRepository.findById(order.getSupplierId()).orElse(null);

            row.add(order.getOrderNo());
            row.add(supplier != null ? supplier.getSupplierName() : "");
            row.add(order.getOrderDate() != null ? order.getOrderDate().format(formatter) : "");
            row.add(order.getExpectedDate() != null ? order.getExpectedDate().format(formatter) : "");
            row.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
            row.add(order.getStatus() != null ? order.getStatus().getDescription() : "");
            row.add(order.getBuyer() != null ? order.getBuyer() : "");
            row.add(order.getApprover() != null ? order.getApprover() : "");
            row.add(order.getApproveTime() != null ? order.getApproveTime().format(formatter) : "");
            row.add(order.getRemark() != null ? order.getRemark() : "");
            dataList.add(row);
        }

        String fileName = "采购订单_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        if ("pdf".equalsIgnoreCase(format)) {
            pdfExportService.exportToPdf(fileName, "采购订单", headers, dataList, response);
        } else {
            excelExportService.exportToExcel(fileName, "采购订单", headers, dataList, response);
        }
    }

    /**
     * Export suppliers list
     */
    @GetMapping("/purchase/suppliers/export")
    @LogOperation(value = OperationType.EXPORT, module = "供应商管理", description = "导出供应商列表")
    public void exportSuppliers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletResponse response) {

        List<Supplier> suppliers = supplierRepository.findByConditions(keyword, null,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        List<String> headers = Arrays.asList("供应商编码", "供应商名称", "联系人", "联系电话", "邮箱", "地址", "开户银行", "银行账号", "税号", "状态", "评分");
        List<List<Object>> dataList = new ArrayList<>();

        for (Supplier supplier : suppliers) {
            List<Object> row = new ArrayList<>();
            row.add(supplier.getSupplierCode());
            row.add(supplier.getSupplierName());
            row.add(supplier.getContactPerson() != null ? supplier.getContactPerson() : "");
            row.add(supplier.getContactPhone() != null ? supplier.getContactPhone() : "");
            row.add(supplier.getEmail() != null ? supplier.getEmail() : "");
            row.add(supplier.getAddress() != null ? supplier.getAddress() : "");
            row.add(supplier.getBankName() != null ? supplier.getBankName() : "");
            row.add(supplier.getBankAccount() != null ? supplier.getBankAccount() : "");
            row.add(supplier.getTaxNumber() != null ? supplier.getTaxNumber() : "");
            row.add(supplier.getStatus() != null && supplier.getStatus() == 1 ? "启用" : "禁用");
            row.add(supplier.getRating() != null ? supplier.getRating() : BigDecimal.ZERO);
            dataList.add(row);
        }

        String fileName = "供应商列表_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        if ("pdf".equalsIgnoreCase(format)) {
            pdfExportService.exportToPdf(fileName, "供应商列表", headers, dataList, response);
        } else {
            excelExportService.exportToExcel(fileName, "供应商列表", headers, dataList, response);
        }
    }

    /**
     * Export system logs
     */
    @GetMapping("/system/logs/export")
    @LogOperation(value = OperationType.EXPORT, module = "系统管理", description = "导出操作日志")
    public void exportLogs(
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletResponse response) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = startTime != null ? LocalDate.parse(startTime).atStartOfDay() : null;
        LocalDateTime endDateTime = endTime != null ? LocalDate.parse(endTime).atTime(LocalTime.MAX) : null;

        List<SysLog> logs = sysLogRepository.findByConditions(operationType, module, username, null, startDateTime, endDateTime,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        List<String> headers = Arrays.asList("操作类型", "模块", "描述", "请求方法", "请求URL", "IP地址", "操作用户", "执行时间(ms)", "状态", "创建时间");
        List<List<Object>> dataList = new ArrayList<>();

        for (SysLog log : logs) {
            List<Object> row = new ArrayList<>();
            row.add(log.getOperationType() != null ? log.getOperationType() : "");
            row.add(log.getModule() != null ? log.getModule() : "");
            row.add(log.getDescription() != null ? log.getDescription() : "");
            row.add(log.getRequestMethod() != null ? log.getRequestMethod() : "");
            row.add(log.getRequestUrl() != null ? log.getRequestUrl() : "");
            row.add(log.getIp() != null ? log.getIp() : "");
            row.add(log.getUsername() != null ? log.getUsername() : "");
            row.add(log.getTime() != null ? log.getTime() : 0);
            row.add(log.getStatus() != null && log.getStatus() == 1 ? "成功" : "失败");
            row.add(log.getCreateTime() != null ? log.getCreateTime().format(formatter) : "");
            dataList.add(row);
        }

        String fileName = "操作日志_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        if ("pdf".equalsIgnoreCase(format)) {
            pdfExportService.exportToPdf(fileName, "操作日志", headers, dataList, response);
        } else {
            excelExportService.exportToExcel(fileName, "操作日志", headers, dataList, response);
        }
    }
}
