package com.stockmaster.modules.system.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.modules.system.dto.RoleDTO;
import com.stockmaster.modules.system.service.RoleService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @LogOperation(value = OperationType.QUERY, module = "角色管理", description = "查询角色列表")
    public ApiResponse<PageResult<RoleDTO>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<RoleDTO> result = roleService.getList(keyword, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/all")
    public ApiResponse<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ApiResponse.success(roles);
    }

    @GetMapping("/{id}")
    @LogOperation(value = OperationType.QUERY, module = "角色管理", description = "查询角色详情")
    public ApiResponse<RoleDTO> getById(@PathVariable Long id) {
        RoleDTO role = roleService.getById(id);
        return ApiResponse.success(role);
    }

    @PostMapping
    @LogOperation(value = OperationType.CREATE, module = "角色管理", description = "创建角色")
    public ApiResponse<RoleDTO> create(@Valid @RequestBody RoleDTO roleDTO) {
        RoleDTO role = roleService.create(roleDTO);
        return ApiResponse.success(role);
    }

    @PutMapping("/{id}")
    @LogOperation(value = OperationType.UPDATE, module = "角色管理", description = "修改角色")
    public ApiResponse<RoleDTO> update(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDTO) {
        RoleDTO role = roleService.update(id, roleDTO);
        return ApiResponse.success(role);
    }

    @DeleteMapping("/{id}")
    @LogOperation(value = OperationType.DELETE, module = "角色管理", description = "删除角色")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/menus")
    @LogOperation(value = OperationType.UPDATE, module = "角色管理", description = "分配菜单权限")
    public ApiResponse<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    @LogOperation(value = OperationType.UPDATE, module = "角色管理", description = "修改角色状态")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        roleService.updateStatus(id, status);
        return ApiResponse.success();
    }
}
