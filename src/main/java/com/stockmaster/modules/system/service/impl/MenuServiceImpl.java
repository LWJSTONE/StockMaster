package com.stockmaster.modules.system.service.impl;

import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.system.dto.MenuDTO;
import com.stockmaster.modules.system.dto.MenuTreeVO;
import com.stockmaster.modules.system.entity.Menu;
import com.stockmaster.modules.system.entity.RoleMenu;
import com.stockmaster.modules.system.entity.UserRole;
import com.stockmaster.modules.system.repository.MenuRepository;
import com.stockmaster.modules.system.repository.RoleMenuRepository;
import com.stockmaster.modules.system.repository.UserRoleRepository;
import com.stockmaster.modules.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleMenuRepository roleMenuRepository;

    @Override
    public MenuDTO getById(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));
        return convertToDTO(menu);
    }

    @Override
    public List<MenuTreeVO> getMenuTree() {
        List<Menu> menus = menuRepository.findAllOrderBySortOrder();
        List<MenuDTO> menuDTOs = menus.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return convertToTree(menuDTOs);
    }

    @Override
    public List<MenuDTO> getAllMenus() {
        List<Menu> menus = menuRepository.findAllOrderBySortOrder();
        return menus.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<MenuTreeVO> getUserMenus(Long userId) {
        // 获取用户的所有角色ID
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有角色的菜单ID
        List<Long> menuIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            List<Long> roleMenuIds = roleMenuRepository.findMenuIdsByRoleId(roleId);
            menuIds.addAll(roleMenuIds);
        }

        // 去重
        final List<Long> finalMenuIds = menuIds.stream().distinct().collect(Collectors.toList());
        if (finalMenuIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取菜单列表
        List<Menu> allMenus = menuRepository.findAllOrderBySortOrder();
        List<MenuDTO> userMenuDTOs = allMenus.stream()
                .filter(menu -> finalMenuIds.contains(menu.getId()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return convertToTree(userMenuDTOs);
    }

    @Override
    @Transactional
    public MenuDTO create(MenuDTO menuDTO) {
        if (menuDTO.getPermission() != null && !menuDTO.getPermission().isEmpty()) {
            if (menuRepository.existsByPermission(menuDTO.getPermission())) {
                throw new BusinessException("权限标识已存在");
            }
        }

        Menu menu = convertToEntity(menuDTO);
        menu = menuRepository.save(menu);
        return convertToDTO(menu);
    }

    @Override
    @Transactional
    public MenuDTO update(Long id, MenuDTO menuDTO) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));

        if (menuDTO.getPermission() != null && !menuDTO.getPermission().isEmpty()
                && !menuDTO.getPermission().equals(menu.getPermission())) {
            if (menuRepository.existsByPermission(menuDTO.getPermission())) {
                throw new BusinessException("权限标识已存在");
            }
        }

        menu.setParentId(menuDTO.getParentId());
        menu.setMenuName(menuDTO.getMenuName());
        menu.setPath(menuDTO.getPath());
        menu.setComponent(menuDTO.getComponent());
        menu.setPermission(menuDTO.getPermission());
        menu.setIcon(menuDTO.getIcon());
        menu.setMenuType(menuDTO.getMenuType());
        menu.setSortOrder(menuDTO.getSortOrder());
        menu.setVisible(menuDTO.getVisible());
        menu.setStatus(menuDTO.getStatus());
        menu.setIsExternal(menuDTO.getIsExternal());
        menu.setIsCached(menuDTO.getIsCached());

        menu = menuRepository.save(menu);
        return convertToDTO(menu);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        List<Menu> children = menuRepository.findByParentIdOrderBySortOrder(id);
        if (!children.isEmpty()) {
            throw new BusinessException("存在子菜单，无法删除");
        }

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));
        menu.setDeleted(true);
        menuRepository.save(menu);

        // 删除角色菜单关联
        roleMenuRepository.deleteByMenuId(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));
        menu.setStatus(status);
        menuRepository.save(menu);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        // 获取用户的所有角色ID
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有角色的菜单ID
        List<Long> menuIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            List<Long> roleMenuIds = roleMenuRepository.findMenuIdsByRoleId(roleId);
            menuIds.addAll(roleMenuIds);
        }

        // 去重
        final List<Long> finalMenuIds = menuIds.stream().distinct().collect(Collectors.toList());
        if (finalMenuIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取菜单列表并提取权限标识
        List<Menu> allMenus = menuRepository.findAllOrderBySortOrder();
        return allMenus.stream()
                .filter(menu -> finalMenuIds.contains(menu.getId()))
                .filter(menu -> menu.getPermission() != null && !menu.getPermission().isEmpty())
                .map(Menu::getPermission)
                .collect(Collectors.toList());
    }

    private List<MenuTreeVO> convertToTree(List<MenuDTO> menus) {
        Map<Long, List<MenuDTO>> groupedByParent = menus.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() != null ? m.getParentId() : 0L));

        List<MenuTreeVO> roots = new ArrayList<>();
        for (MenuDTO menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                MenuTreeVO node = convertToTreeVO(menu);
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

    private void buildTree(MenuTreeVO parent, Map<Long, List<MenuDTO>> groupedByParent) {
        List<MenuDTO> children = groupedByParent.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            List<MenuTreeVO> childNodes = children.stream()
                    .map(this::convertToTreeVO)
                    .sorted((a, b) -> {
                        int orderA = a.getSortOrder() != null ? a.getSortOrder() : 0;
                        int orderB = b.getSortOrder() != null ? b.getSortOrder() : 0;
                        return Integer.compare(orderA, orderB);
                    })
                    .collect(Collectors.toList());

            for (MenuTreeVO child : childNodes) {
                buildTree(child, groupedByParent);
            }

            parent.setChildren(childNodes);
        }
    }

    private MenuDTO convertToDTO(Menu menu) {
        MenuDTO dto = new MenuDTO();
        dto.setId(menu.getId());
        dto.setParentId(menu.getParentId());
        dto.setMenuName(menu.getMenuName());
        dto.setPath(menu.getPath());
        dto.setComponent(menu.getComponent());
        dto.setPermission(menu.getPermission());
        dto.setIcon(menu.getIcon());
        dto.setMenuType(menu.getMenuType());
        dto.setSortOrder(menu.getSortOrder());
        dto.setVisible(menu.getVisible());
        dto.setStatus(menu.getStatus());
        dto.setIsExternal(menu.getIsExternal());
        dto.setIsCached(menu.getIsCached());
        return dto;
    }

    private Menu convertToEntity(MenuDTO dto) {
        Menu menu = new Menu();
        menu.setId(dto.getId());
        menu.setParentId(dto.getParentId());
        menu.setMenuName(dto.getMenuName());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setPermission(dto.getPermission());
        menu.setIcon(dto.getIcon());
        menu.setMenuType(dto.getMenuType() != null ? dto.getMenuType() : 1);
        menu.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        menu.setVisible(dto.getVisible() != null ? dto.getVisible() : true);
        menu.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        menu.setIsExternal(dto.getIsExternal() != null ? dto.getIsExternal() : false);
        menu.setIsCached(dto.getIsCached() != null ? dto.getIsCached() : false);
        return menu;
    }

    private MenuTreeVO convertToTreeVO(MenuDTO dto) {
        MenuTreeVO vo = new MenuTreeVO();
        vo.setId(dto.getId());
        vo.setParentId(dto.getParentId());
        vo.setMenuName(dto.getMenuName());
        vo.setPath(dto.getPath());
        vo.setComponent(dto.getComponent());
        vo.setPermission(dto.getPermission());
        vo.setIcon(dto.getIcon());
        vo.setMenuType(dto.getMenuType());
        vo.setSortOrder(dto.getSortOrder());
        vo.setVisible(dto.getVisible());
        vo.setStatus(dto.getStatus());
        vo.setIsExternal(dto.getIsExternal());
        vo.setIsCached(dto.getIsCached());
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}
