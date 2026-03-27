package com.stockmaster.modules.stock.service;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.StockStatus;
import com.stockmaster.modules.stock.dto.ProductDTO;
import com.stockmaster.modules.stock.dto.ProductVO;

import java.util.List;

public interface ProductService {

    ProductVO getById(Long id);

    ProductVO getByCode(String code);

    ProductVO getByBarcode(String barcode);

    PageResult<ProductVO> getList(String keyword, Long categoryId, StockStatus status, Integer pageNum, Integer pageSize);

    List<ProductVO> getActiveProducts();

    List<ProductVO> getLowStockProducts();

    List<ProductVO> getProductsForSelect(String keyword, Long categoryId);

    ProductVO create(ProductDTO productDTO);

    ProductVO update(Long id, ProductDTO productDTO);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void updateStatus(Long id, StockStatus status);

    void uploadImage(Long id, String imageUrl);
}
