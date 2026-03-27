package com.stockmaster.modules.stock.service.impl;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.StockStatus;
import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.stock.dto.ProductDTO;
import com.stockmaster.modules.stock.dto.ProductVO;
import com.stockmaster.modules.stock.entity.Category;
import com.stockmaster.modules.stock.entity.Inventory;
import com.stockmaster.modules.stock.entity.Product;
import com.stockmaster.modules.stock.repository.CategoryRepository;
import com.stockmaster.modules.stock.repository.InventoryRepository;
import com.stockmaster.modules.stock.repository.ProductRepository;
import com.stockmaster.modules.stock.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public ProductVO getByCode(String code) {
        Product product = productRepository.findByProductCode(code)
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
    public PageResult<ProductVO> getList(String keyword, Long categoryId, StockStatus status, Integer pageNum, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Product> page = productRepository.findByConditions(keyword, categoryId, status, pageRequest);

        List<ProductVO> voList = page.getContent().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotalElements(), pageNum, pageSize);
    }

    @Override
    public List<ProductVO> getActiveProducts() {
        List<Product> products = productRepository.findAllActive(StockStatus.ACTIVE);
        return products.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getLowStockProducts() {
        List<Inventory> lowStockInventories = inventoryRepository.findLowStock();
        return lowStockInventories.stream()
                .filter(inv -> !Boolean.TRUE.equals(inv.getDeleted()))
                .map(inv -> {
                    Product product = productRepository.findById(inv.getProductId()).orElse(null);
                    if (product != null) {
                        ProductVO vo = convertToVO(product);
                        vo.setInventoryQuantity(inv.getQuantity());
                        return vo;
                    }
                    return null;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getProductsForSelect(String keyword, Long categoryId) {
        PageRequest pageRequest = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Product> page = productRepository.findByConditions(keyword, categoryId, StockStatus.ACTIVE, pageRequest);
        return page.getContent().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductVO create(ProductDTO productDTO) {
        if (productRepository.existsByProductCode(productDTO.getProductCode())) {
            throw new BusinessException("商品编码已存在");
        }

        if (productDTO.getBarcode() != null && !productDTO.getBarcode().isEmpty()) {
            if (productRepository.findByBarcode(productDTO.getBarcode()).isPresent()) {
                throw new BusinessException("条码已存在");
            }
        }

        Product product = convertToEntity(productDTO);
        if (product.getStatus() == null) {
            product.setStatus(StockStatus.ACTIVE);
        }
        product = productRepository.save(product);

        // 创建库存记录
        Inventory inventory = new Inventory();
        inventory.setProductId(product.getId());
        inventory.setQuantity(0);
        inventory.setAvailableQuantity(0);
        inventory.setFrozenQuantity(0);
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

        if (!product.getProductCode().equals(productDTO.getProductCode())) {
            if (productRepository.existsByProductCodeAndIdNot(productDTO.getProductCode(), id)) {
                throw new BusinessException("商品编码已存在");
            }
        }

        if (productDTO.getBarcode() != null && !productDTO.getBarcode().isEmpty()) {
            if (!productDTO.getBarcode().equals(product.getBarcode())) {
                if (productRepository.findByBarcode(productDTO.getBarcode()).isPresent()) {
                    throw new BusinessException("条码已存在");
                }
            }
        }

        product.setProductCode(productDTO.getProductCode());
        product.setProductName(productDTO.getProductName());
        product.setBarcode(productDTO.getBarcode());
        product.setBrand(productDTO.getBrand());
        product.setSpec(productDTO.getSpec());
        product.setUnit(productDTO.getUnit());
        product.setCostPrice(productDTO.getCostPrice());
        product.setSalePrice(productDTO.getSalePrice());
        product.setMinStock(productDTO.getMinStock());
        product.setMaxStock(productDTO.getMaxStock());
        product.setCategoryId(productDTO.getCategoryId());
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

    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setProductCode(product.getProductCode());
        vo.setProductName(product.getProductName());
        vo.setBarcode(product.getBarcode());
        vo.setBrand(product.getBrand());
        vo.setSpec(product.getSpec());
        vo.setUnit(product.getUnit());
        vo.setCostPrice(product.getCostPrice());
        vo.setSalePrice(product.getSalePrice());
        vo.setMinStock(product.getMinStock());
        vo.setMaxStock(product.getMaxStock());
        vo.setCategoryId(product.getCategoryId());
        vo.setStatus(product.getStatus() != null ? product.getStatus().getDescription() : "");
        vo.setDescription(product.getDescription());
        vo.setImageUrl(product.getImageUrl());
        vo.setCreateTime(product.getCreateTime());
        vo.setUpdateTime(product.getUpdateTime());

        // 获取分类名称
        if (product.getCategoryId() != null) {
            Category category = categoryRepository.findById(product.getCategoryId()).orElse(null);
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }

        // 获取库存数量
        inventoryRepository.findByProductId(product.getId()).ifPresent(inventory -> {
            vo.setInventoryQuantity(inventory.getQuantity());
            vo.setAvailableQuantity(inventory.getAvailableQuantity());
        });

        return vo;
    }

    private Product convertToEntity(ProductDTO dto) {
        Product entity = new Product();
        entity.setId(dto.getId());
        entity.setProductCode(dto.getProductCode());
        entity.setProductName(dto.getProductName());
        entity.setBarcode(dto.getBarcode());
        entity.setBrand(dto.getBrand());
        entity.setSpec(dto.getSpec());
        entity.setUnit(dto.getUnit());
        entity.setCostPrice(dto.getCostPrice());
        entity.setSalePrice(dto.getSalePrice());
        entity.setMinStock(dto.getMinStock());
        entity.setMaxStock(dto.getMaxStock());
        entity.setCategoryId(dto.getCategoryId());
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            entity.setStatus(StockStatus.valueOf(dto.getStatus()));
        }
        entity.setDescription(dto.getDescription());
        return entity;
    }
}
