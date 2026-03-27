package com.stockmaster.modules.system.service.impl;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.system.dto.RoleDTO;
import com.stockmaster.modules.system.entity.Role;
import com.stockmaster.modules.system.entity.RoleMenu;
import com.stockmaster.modules.system.entity.UserRole;
import com.stockmaster.modules.system.repository.RoleMenuRepository;
import com.stockmaster.modules.system.repository.RoleRepository;
import com.stockmaster.modules.system.repository.UserRoleRepository;
import com.stockmaster.modules.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public RoleDTO getById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        RoleDTO dto = convertToDTO(role);
        // 获取角色的菜单ID列表
        List<Long> menuIds = roleMenuRepository.findMenuIdsByRoleId(id);
        dto.setMenuIds(menuIds);
        return dto;
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAllActive();
        return roles.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public PageResult<RoleDTO> getList(String keyword, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by("sortOrder").ascending());
        
        Page<Role> page;
        if (StringUtils.hasText(keyword)) {
            page = roleRepository.findByKeyword(keyword, pageable);
        } else {
            page = roleRepository.findAllByDeletedFalse(pageable);
        }

        List<RoleDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(records, page.getTotalElements(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public RoleDTO create(RoleDTO roleDTO) {
        if (roleRepository.existsByRoleCode(roleDTO.getRoleCode())) {
            throw new BusinessException("角色编码已存在");
        }

        Role role = convertToEntity(roleDTO);
        role = roleRepository.save(role);

        // 分配菜单权限
        if (roleDTO.getMenuIds() != null && !roleDTO.getMenuIds().isEmpty()) {
            assignMenusInternal(role.getId(), roleDTO.getMenuIds());
        }

        return convertToDTO(role);
    }

    @Override
    @Transactional
    public RoleDTO update(Long id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));

        if (roleDTO.getRoleCode() != null && !roleDTO.getRoleCode().equals(role.getRoleCode())) {
            if (roleRepository.existsByRoleCodeAndIdNot(roleDTO.getRoleCode(), id)) {
                throw new BusinessException("角色编码已存在");
            }
        }

        role.setRoleCode(roleDTO.getRoleCode());
        role.setRoleName(roleDTO.getRoleName());
        role.setDescription(roleDTO.getDescription());
        role.setSortOrder(roleDTO.getSortOrder());
        role.setStatus(roleDTO.getStatus());

        role = roleRepository.save(role);

        // 更新菜单权限
        if (roleDTO.getMenuIds() != null) {
            assignMenusInternal(id, roleDTO.getMenuIds());
        }

        RoleDTO result = convertToDTO(role);
        result.setMenuIds(roleDTO.getMenuIds());
        return result;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 检查是否有用户关联此角色
        List<UserRole> userRoles = userRoleRepository.findByRoleId(id);
        if (!userRoles.isEmpty()) {
            throw new BusinessException("角色已分配给用户，无法删除");
        }

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        role.setDeleted(true);
        roleRepository.save(role);

        // 删除角色菜单关联
        roleMenuRepository.deleteByRoleId(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        role.setStatus(status);
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void assignMenus(Long id, List<Long> menuIds) {
        // 验证角色存在
        roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        
        assignMenusInternal(id, menuIds);
    }

    @Override
    public List<Long> getRoleMenuIds(Long id) {
        return roleMenuRepository.findMenuIdsByRoleId(id);
    }

    private void assignMenusInternal(Long roleId, List<Long> menuIds) {
        // 先删除旧的关联
        roleMenuRepository.deleteByRoleId(roleId);

        // 创建新的关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenuRepository.save(roleMenu);
            }
        }
    }

    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setRoleCode(role.getRoleCode());
        dto.setRoleName(role.getRoleName());
        dto.setDescription(role.getDescription());
        dto.setSortOrder(role.getSortOrder());
        dto.setStatus(role.getStatus());
        return dto;
    }

    private Role convertToEntity(RoleDTO dto) {
        Role role = new Role();
        role.setId(dto.getId());
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        role.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        return role;
    }
}
