package com.stockmaster.modules.system.service;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.modules.system.dto.RoleDTO;
import com.stockmaster.modules.system.entity.Role;

import java.util.List;

public interface RoleService {

    /**
     * 根据ID获取角色详情
     * @param id 角色ID
     * @return 角色DTO
     */
    RoleDTO getById(Long id);

    /**
     * 获取所有角色列表
     * @return 角色DTO列表
     */
    List<RoleDTO> getAllRoles();

    /**
     * 分页查询角色列表
     * @param keyword 关键字
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 角色分页结果
     */
    PageResult<RoleDTO> getList(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 创建角色
     * @param roleDTO 角色DTO
     * @return 创建后的角色DTO
     */
    RoleDTO create(RoleDTO roleDTO);

    /**
     * 更新角色
     * @param id 角色ID
     * @param roleDTO 角色DTO
     * @return 更新后的角色DTO
     */
    RoleDTO update(Long id, RoleDTO roleDTO);

    /**
     * 删除角色
     * @param id 角色ID
     */
    void delete(Long id);

    /**
     * 更新角色状态
     * @param id 角色ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 分配菜单权限
     * @param id 角色ID
     * @param menuIds 菜单ID列表
     */
    void assignMenus(Long id, List<Long> menuIds);

    /**
     * 获取角色菜单ID列表
     * @param id 角色ID
     * @return 菜单ID列表
     */
    List<Long> getRoleMenuIds(Long id);
}
