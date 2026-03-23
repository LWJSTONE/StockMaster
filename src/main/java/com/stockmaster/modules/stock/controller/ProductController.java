package com.stockmaster.modules.stock.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.common.enums.StockStatus;
import com.stockmaster.common.service.FileUploadService;
import com.stockmaster.modules.stock.dto.ProductDTO;
import com.stockmaster.modules.stock.dto.ProductVO;
import com.stockmaster.modules.stock.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileUploadService fileUploadService;

    @GetMapping
    @LogOperation(value = OperationType.QUERY, module = "商品管理", description = "查询商品列表")
    public ApiResponse<PageResult<ProductVO>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) StockStatus status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ProductVO> result = productService.getList(keyword, categoryId, status, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @LogOperation(value = OperationType.QUERY, module = "商品管理", description = "查询商品详情")
    public ApiResponse<ProductVO> getById(@PathVariable Long id) {
        ProductVO product = productService.getById(id);
        return ApiResponse.success(product);
    }

    @GetMapping("/code/{code}")
    @LogOperation(value = OperationType.QUERY, module = "商品管理", description = "根据编码查询商品")
    public ApiResponse<ProductVO> getByCode(@PathVariable String code) {
        ProductVO product = productService.getByCode(code);
        return ApiResponse.success(product);
    }

    @GetMapping("/barcode/{barcode}")
    @LogOperation(value = OperationType.QUERY, module = "商品管理", description = "根据条码查询商品")
    public ApiResponse<ProductVO> getByBarcode(@PathVariable String barcode) {
        ProductVO product = productService.getByBarcode(barcode);
        return ApiResponse.success(product);
    }

    @PostMapping
    @LogOperation(value = OperationType.CREATE, module = "商品管理", description = "创建商品")
    public ApiResponse<ProductVO> create(@Valid @RequestBody ProductDTO productDTO) {
        ProductVO product = productService.create(productDTO);
        return ApiResponse.success(product);
    }

    @PutMapping("/{id}")
    @LogOperation(value = OperationType.UPDATE, module = "商品管理", description = "修改商品")
    public ApiResponse<ProductVO> update(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        ProductVO product = productService.update(id, productDTO);
        return ApiResponse.success(product);
    }

    @DeleteMapping("/{id}")
    @LogOperation(value = OperationType.DELETE, module = "商品管理", description = "删除商品")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.success();
    }

    @DeleteMapping("/batch")
    @LogOperation(value = OperationType.DELETE, module = "商品管理", description = "批量删除商品")
    public ApiResponse<Void> batchDelete(@RequestBody List<Long> ids) {
        productService.batchDelete(ids);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    @LogOperation(value = OperationType.UPDATE, module = "商品管理", description = "修改商品状态(上下架)")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam StockStatus status) {
        productService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/image")
    @LogOperation(value = OperationType.UPDATE, module = "商品管理", description = "上传商品图片")
    public ApiResponse<Map<String, String>> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String imageUrl = fileUploadService.uploadProductImage(file);
        productService.uploadImage(id, imageUrl);
        Map<String, String> result = new HashMap<>();
        result.put("url", imageUrl);
        return ApiResponse.success(result);
    }

    @GetMapping("/low-stock")
    @LogOperation(value = OperationType.QUERY, module = "商品管理", description = "查询低库存商品")
    public ApiResponse<List<ProductVO>> getLowStockProducts() {
        List<ProductVO> products = productService.getLowStockProducts();
        return ApiResponse.success(products);
    }

    @GetMapping("/active")
    public ApiResponse<List<ProductVO>> getActiveProducts() {
        List<ProductVO> products = productService.getActiveProducts();
        return ApiResponse.success(products);
    }

    @GetMapping("/select")
    public ApiResponse<List<ProductVO>> getProductsForSelect(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        List<ProductVO> products = productService.getProductsForSelect(keyword, categoryId);
        return ApiResponse.success(products);
    }
}
