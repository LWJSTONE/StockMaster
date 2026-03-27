package com.stockmaster.modules.stock.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.modules.stock.dto.CategoryDTO;
import com.stockmaster.modules.stock.dto.CategoryTreeVO;
import com.stockmaster.modules.stock.service.CategoryService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @LogOperation(value = OperationType.QUERY, module = "分类管理", description = "查询分类树")
    public ApiResponse<List<CategoryTreeVO>> getCategoryTree() {
        List<CategoryTreeVO> tree = categoryService.getCategoryTree();
        return ApiResponse.success(tree);
    }

    @GetMapping("/all")
    public ApiResponse<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ApiResponse.success(categories);
    }

    @GetMapping("/{id}")
    @LogOperation(value = OperationType.QUERY, module = "分类管理", description = "查询分类详情")
    public ApiResponse<CategoryDTO> getById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getById(id);
        return ApiResponse.success(category);
    }

    @PostMapping
    @LogOperation(value = OperationType.CREATE, module = "分类管理", description = "创建分类")
    public ApiResponse<CategoryDTO> create(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO category = categoryService.create(categoryDTO);
        return ApiResponse.success(category);
    }

    @PutMapping("/{id}")
    @LogOperation(value = OperationType.UPDATE, module = "分类管理", description = "修改分类")
    public ApiResponse<CategoryDTO> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO category = categoryService.update(id, categoryDTO);
        return ApiResponse.success(category);
    }

    @DeleteMapping("/{id}")
    @LogOperation(value = OperationType.DELETE, module = "分类管理", description = "删除分类")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    @LogOperation(value = OperationType.UPDATE, module = "分类管理", description = "修改分类状态")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        categoryService.updateStatus(id, status);
        return ApiResponse.success();
    }
}
