package com.stockmaster.modules.system.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.modules.system.dto.MenuDTO;
import com.stockmaster.modules.system.dto.UserInfoVO;
import com.stockmaster.modules.system.dto.MenuTreeVO;
import com.stockmaster.modules.system.service.MenuService;
import com.stockmaster.modules.system.service.UserService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final UserService userService;

    @GetMapping
    @LogOperation(value = OperationType.QUERY, module = "菜单管理", description = "查询菜单树")
    public ApiResponse<List<MenuTreeVO>> getMenuTree() {
        List<MenuTreeVO> tree = menuService.getMenuTree();
        return ApiResponse.success(tree);
    }

    @GetMapping("/user-menus")
    public ApiResponse<List<MenuTreeVO>> getUserMenus() {
        UserInfoVO userInfo = userService.getCurrentUser();
        List<MenuTreeVO> menus = menuService.getUserMenus(userInfo.getId());
        return ApiResponse.success(menus);
    }

    @GetMapping("/{id}")
    @LogOperation(value = OperationType.QUERY, module = "菜单管理", description = "查询菜单详情")
    public ApiResponse<MenuDTO> getById(@PathVariable Long id) {
        MenuDTO menu = menuService.getById(id);
        return ApiResponse.success(menu);
    }

    @PostMapping
    @LogOperation(value = OperationType.CREATE, module = "菜单管理", description = "创建菜单")
    public ApiResponse<MenuDTO> create(@Valid @RequestBody MenuDTO menuDTO) {
        MenuDTO menu = menuService.create(menuDTO);
        return ApiResponse.success(menu);
    }

    @PutMapping("/{id}")
    @LogOperation(value = OperationType.UPDATE, module = "菜单管理", description = "修改菜单")
    public ApiResponse<MenuDTO> update(@PathVariable Long id, @Valid @RequestBody MenuDTO menuDTO) {
        MenuDTO menu = menuService.update(id, menuDTO);
        return ApiResponse.success(menu);
    }

    @DeleteMapping("/{id}")
    @LogOperation(value = OperationType.DELETE, module = "菜单管理", description = "删除菜单")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/permissions")
    public ApiResponse<List<String>> getUserPermissions() {
        UserInfoVO userInfo = userService.getCurrentUser();
        List<String> permissions = menuService.getUserPermissions(userInfo.getId());
        return ApiResponse.success(permissions);
    }
}
