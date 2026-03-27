package com.stockmaster.modules.system.service;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.modules.system.dto.RoleDTO;
import com.stockmaster.modules.system.entity.Role;

import java.util.List;

public interface RoleService {

    Role getById(Long id);

    List<Role> getAllRoles();

    PageResult<Role> getList(String keyword, Integer status, Integer pageNum, Integer pageSize);

    Role create(RoleDTO roleDTO);

    Role update(Long id, RoleDTO roleDTO);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    void assignMenus(Long id, List<Long> menuIds);

    List<Long> getRoleMenuIds(Long id);
}
