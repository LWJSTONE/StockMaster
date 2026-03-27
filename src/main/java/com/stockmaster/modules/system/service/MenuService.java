package com.stockmaster.modules.system.service;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.modules.system.dto.MenuDTO;
import com.stockmaster.modules.system.dto.MenuTreeVO;
import com.stockmaster.modules.system.entity.Menu;

import java.util.List;

public interface MenuService {

    /**
     * 根据ID获取菜单详情
     * @param id 菜单ID
     * @return 菜单DTO
     */
    MenuDTO getById(Long id);

    /**
     * 获取菜单树
     * @return 菜单树列表
     */
    List<MenuTreeVO> getMenuTree();

    /**
     * 获取所有菜单列表
     * @return 菜单DTO列表
     */
    List<MenuDTO> getAllMenus();

    /**
     * 获取用户菜单
     * @param userId 用户ID
     * @return 用户菜单树
     */
    List<MenuTreeVO> getUserMenus(Long userId);

    /**
     * 创建菜单
     * @param menuDTO 菜单DTO
     * @return 创建后的菜单DTO
     */
    MenuDTO create(MenuDTO menuDTO);

    /**
     * 更新菜单
     * @param id 菜单ID
     * @param menuDTO 菜单DTO
     * @return 更新后的菜单DTO
     */
    MenuDTO update(Long id, MenuDTO menuDTO);

    /**
     * 删除菜单
     * @param id 菜单ID
     */
    void delete(Long id);

    /**
     * 更新菜单状态
     * @param id 菜单ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 获取用户权限列表
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> getUserPermissions(Long userId);
}
