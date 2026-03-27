package com.stockmaster.modules.system.service;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.modules.system.dto.MenuDTO;
import com.stockmaster.modules.system.dto.MenuTreeVO;
import com.stockmaster.modules.system.entity.Menu;

import java.util.List;

public interface MenuService {

    Menu getById(Long id);

    List<MenuTreeVO> getMenuTree();

    List<MenuDTO> getAllMenus();

    List<MenuDTO> getUserMenus(Long userId);

    Menu create(MenuDTO menuDTO);

    Menu update(Long id, MenuDTO menuDTO);

    void delete(Long id);

    void updateStatus(Long id, Integer status);
}
