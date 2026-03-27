package com.stockmaster.modules.stock.service;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.StockStatus;
import com.stockmaster.modules.stock.dto.CategoryDTO;
import com.stockmaster.modules.stock.dto.CategoryTreeVO;

import java.util.List;

public interface CategoryService {

    CategoryDTO getById(Long id);

    List<CategoryTreeVO> getCategoryTree();

    List<CategoryDTO> getAllCategories();

    CategoryDTO create(CategoryDTO categoryDTO);

    CategoryDTO update(Long id, CategoryDTO categoryDTO);

    void delete(Long id);

    void updateStatus(Long id, Integer status);
}
