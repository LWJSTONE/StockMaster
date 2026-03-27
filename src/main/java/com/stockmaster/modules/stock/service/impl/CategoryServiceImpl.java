package com.stockmaster.modules.stock.service.impl;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.StockStatus;
import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.stock.dto.CategoryDTO;
import com.stockmaster.modules.stock.dto.CategoryTreeVO;
import com.stockmaster.modules.stock.entity.Category;
import com.stockmaster.modules.stock.entity.Product;
import com.stockmaster.modules.stock.repository.CategoryRepository;
import com.stockmaster.modules.stock.repository.ProductRepository;
import com.stockmaster.modules.stock.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public CategoryDTO getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));
        return convertToDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO create(CategoryDTO categoryDTO) {
        if (categoryDTO.getCategoryCode() != null && categoryRepository.existsByCategoryCode(categoryDTO.getCategoryCode())) {
            throw new BusinessException("分类编码已存在");
        }

        Category category = convertToEntity(categoryDTO);
        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO update(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));

        if (categoryDTO.getCategoryCode() != null && !categoryDTO.getCategoryCode().equals(category.getCategoryCode()) 
                && categoryRepository.existsByCategoryCode(categoryDTO.getCategoryCode())) {
            throw new BusinessException("分类编码已存在");
        }

        category.setParentId(categoryDTO.getParentId());
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setCategoryCode(categoryDTO.getCategoryCode());
        category.setSortOrder(categoryDTO.getSortOrder());
        category.setStatus(categoryDTO.getStatus());
        category.setIcon(categoryDTO.getIcon());

        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        List<Category> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException("存在子分类，无法删除");
        }

        List<Product> products = productRepository.findByCategoryId(id);
        if (!products.isEmpty()) {
            throw new BusinessException("分类下存在商品，无法删除");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));
        category.setDeleted(true);
        categoryRepository.save(category);
    }

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        List<Category> categories = categoryRepository.findAllOrderBySortOrder();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return convertToTree(categoryDTOs);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAllActive();
        return categories.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));
        category.setStatus(status);
        categoryRepository.save(category);
    }

    private List<CategoryTreeVO> convertToTree(List<CategoryDTO> categories) {
        Map<Long, List<CategoryDTO>> groupedByParent = categories.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() != null ? c.getParentId() : 0L));

        List<CategoryTreeVO> roots = new ArrayList<>();
        for (CategoryDTO category : categories) {
            if (category.getParentId() == null || category.getParentId() == 0) {
                CategoryTreeVO node = convertToTreeVO(category);
                buildTree(node, groupedByParent);
                roots.add(node);
            }
        }

        roots.sort((a, b) -> {
            int orderA = a.getSortOrder() != null ? a.getSortOrder() : 0;
            int orderB = b.getSortOrder() != null ? b.getSortOrder() : 0;
            return Integer.compare(orderA, orderB);
        });

        return roots;
    }

    private void buildTree(CategoryTreeVO parent, Map<Long, List<CategoryDTO>> groupedByParent) {
        List<CategoryDTO> children = groupedByParent.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            List<CategoryTreeVO> childNodes = children.stream()
                    .map(this::convertToTreeVO)
                    .sorted((a, b) -> {
                        int orderA = a.getSortOrder() != null ? a.getSortOrder() : 0;
                        int orderB = b.getSortOrder() != null ? b.getSortOrder() : 0;
                        return Integer.compare(orderA, orderB);
                    })
                    .collect(Collectors.toList());

            for (CategoryTreeVO child : childNodes) {
                buildTree(child, groupedByParent);
            }

            parent.setChildren(childNodes);
        }
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setParentId(category.getParentId());
        dto.setCategoryName(category.getCategoryName());
        dto.setCategoryCode(category.getCategoryCode());
        dto.setSortOrder(category.getSortOrder());
        dto.setStatus(category.getStatus());
        dto.setIcon(category.getIcon());
        return dto;
    }

    private Category convertToEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setParentId(dto.getParentId());
        category.setCategoryName(dto.getCategoryName());
        category.setCategoryCode(dto.getCategoryCode());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        category.setIcon(dto.getIcon());
        return category;
    }

    private CategoryTreeVO convertToTreeVO(CategoryDTO dto) {
        CategoryTreeVO vo = new CategoryTreeVO();
        vo.setId(dto.getId());
        vo.setParentId(dto.getParentId());
        vo.setCategoryName(dto.getCategoryName());
        vo.setCategoryCode(dto.getCategoryCode());
        vo.setSortOrder(dto.getSortOrder());
        vo.setStatus(dto.getStatus());
        vo.setIcon(dto.getIcon());
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}
