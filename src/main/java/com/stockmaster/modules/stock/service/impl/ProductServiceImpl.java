package com.stockmaster.modules.stock.service.impl;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.StockStatus;
import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.stock.dto.ProductDTO;
import com.stockmaster.modules.stock.dto.ProductVO;
import com.stockmaster.modules.stock.entity.Inventory;
import com.stockmaster.modules.stock.entity.Product;
import com.stockmaster.modules.stock.repository.CategoryRepository;
import com.stockmaster.modules.stock.repository.InventoryRepository;
import com.stockmaster.modules.stock.repository.ProductRepository;
import com.stockmaster.modules.stock.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public ProductVO getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        return convertToVO(product);
    }

    @Override
    public ProductVO getByCode(String productCode) {
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        return convertToVO(product);
    }

    @Override
    public ProductVO getByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        return convertToVO(product);
    }

    @Override
    @Transactional
    public ProductVO create(ProductDTO productDTO) {
        if (productRepository.existsByProductCode(productDTO.getProductCode())) {
            throw new BusinessException("商品编码已存在");
        }

        Product product = convertToEntity(productDTO);
        product.setStatus(productDTO.getStatus() != null ? productDTO.getStatus() : StockStatus.ACTIVE);
        product = productRepository.save(product);

        // 创建库存记录
        Inventory inventory = new Inventory();
        inventory.setProductId(product.getId());
        inventory.setQuantity(0);
        inventory.setFrozenQuantity(0);
        inventory.setAvailableQuantity(0);
        inventory.setWarningMin(product.getMinStock());
        inventory.setWarningMax(product.getMaxStock());
        inventoryRepository.save(inventory);

        return convertToVO(product);
    }

    @Override
    @Transactional
    public ProductVO update(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (!product.getProductCode().equals(productDTO.getProductCode()) 
                && productRepository.existsByProductCode(productDTO.getProductCode())) {
            throw new BusinessException("商品编码已存在");
        }

        product.setProductCode(productDTO.getProductCode());
        product.setProductName(productDTO.getProductName());
        product.setCategoryId(productDTO.getCategoryId());
        product.setBrand(productDTO.getBrand());
        product.setSpec(productDTO.getSpec());
        product.setUnit(productDTO.getUnit());
        product.setBarcode(productDTO.getBarcode());
        product.setCostPrice(productDTO.getCostPrice());
        product.setSalePrice(productDTO.getSalePrice());
        product.setMinStock(productDTO.getMinStock());
        product.setMaxStock(productDTO.getMaxStock());
        product.setStatus(productDTO.getStatus());
        product.setImageUrl(productDTO.getImageUrl());
        product.setDescription(productDTO.getDescription());

        product = productRepository.save(product);
        return convertToVO(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        product.setDeleted(true);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            delete(id);
        }
    }

    @Override
    public PageResult<ProductVO> getList(String keyword, Long categoryId, StockStatus status, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Product> page = productRepository.findByConditions(keyword, categoryId, status, pageable);

        List<ProductVO> list = page.getContent().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(list, page.getTotalElements(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, StockStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        product.setStatus(status);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void uploadImage(Long id, String imageUrl) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        product.setImageUrl(imageUrl);
        productRepository.save(product);
    }

    @Override
    public List<ProductVO> getLowStockProducts() {
        List<Inventory> lowStockInventories = inventoryRepository.findLowStock();
        return lowStockInventories.stream()
                .map(inv -> {
                    Product product = productRepository.findById(inv.getProductId()).orElse(null);
                    if (product != null) {
                        ProductVO vo = convertToVO(product);
                        vo.setStockQuantity(inv.getQuantity());
                        return vo;
                    }
                    return null;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getActiveProducts() {
        List<Product> products = productRepository.findByStatus(StockStatus.ACTIVE);
        return products.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getProductsForSelect(String keyword, Long categoryId) {
        List<Product> products;
        if (keyword != null && !keyword.isEmpty()) {
            Pageable pageable = PageRequest.of(0, 20);
            products = productRepository.findByConditions(keyword, categoryId, StockStatus.ACTIVE, pageable).getContent();
        } else {
            products = productRepository.findByStatus(StockStatus.ACTIVE);
            if (products.size() > 50) {
                products = products.subList(0, 50);
            }
        }
        return products.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setProductCode(product.getProductCode());
        vo.setProductName(product.getProductName());
        vo.setCategoryId(product.getCategoryId());
        vo.setBrand(product.getBrand());
        vo.setSpec(product.getSpec());
        vo.setUnit(product.getUnit());
        vo.setBarcode(product.getBarcode());
        vo.setCostPrice(product.getCostPrice());
        vo.setSalePrice(product.getSalePrice());
        vo.setMinStock(product.getMinStock());
        vo.setMaxStock(product.getMaxStock());
        vo.setStatus(product.getStatus() != null ? product.getStatus().name() : StockStatus.ACTIVE.name());
        vo.setImageUrl(product.getImageUrl());
        vo.setDescription(product.getDescription());
        vo.setCreateTime(product.getCreateTime());

        if (product.getCategoryId() != null) {
            categoryRepository.findById(product.getCategoryId())
                    .ifPresent(category -> vo.setCategoryName(category.getCategoryName()));
        }

        inventoryRepository.findByProductId(product.getId())
                .ifPresent(inventory -> vo.setStockQuantity(inventory.getQuantity()));

        return vo;
    }

    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setProductCode(dto.getProductCode());
        product.setProductName(dto.getProductName());
        product.setCategoryId(dto.getCategoryId());
        product.setBrand(dto.getBrand());
        product.setSpec(dto.getSpec());
        product.setUnit(dto.getUnit());
        product.setBarcode(dto.getBarcode());
        product.setCostPrice(dto.getCostPrice());
        product.setSalePrice(dto.getSalePrice());
        product.setMinStock(dto.getMinStock());
        product.setMaxStock(dto.getMaxStock());
        product.setImageUrl(dto.getImageUrl());
        product.setDescription(dto.getDescription());
        return product;
    }
}
