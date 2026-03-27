package com.stockmaster.modules.system.service.impl;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.common.security.JwtTokenProvider;
import com.stockmaster.common.security.SecurityUtils;
import com.stockmaster.modules.system.dto.*;
import com.stockmaster.modules.system.entity.User;
import com.stockmaster.modules.system.entity.UserRole;
import com.stockmaster.modules.system.repository.UserRepository;
import com.stockmaster.modules.system.repository.UserRoleRepository;
import com.stockmaster.modules.system.repository.RoleRepository;
import com.stockmaster.modules.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 验证用户名密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 获取用户信息
        User user = userRepository.findByUsernameAndDeletedFalse(loginDTO.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        // 获取用户角色
        List<String> roles = getUserRoles(user.getId());

        // 生成token
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        Long expireTime = jwtTokenProvider.getExpirationFromToken(token);

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        // 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setExpireTime(expireTime);
        loginVO.setUserInfo(convertToUserInfoVO(user));

        return loginVO;
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    public UserInfoVO getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new BusinessException("用户未登录");
        }

        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        return convertToUserInfoVO(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    @Override
    public PageResult<UserInfoVO> getUserList(String keyword, Integer status, Integer pageNum, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<User> page = userRepository.findByConditions(keyword, status, pageRequest);

        List<UserInfoVO> voList = page.getContent().stream()
                .map(this::convertToUserInfoVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotalElements(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public User createUser(UserDTO userDTO) {
        // 检查用户名是否为空
        if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        
        // 检查密码是否为空
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        
        // 检查真实姓名是否为空
        if (userDTO.getRealName() == null || userDTO.getRealName().trim().isEmpty()) {
            throw new BusinessException("真实姓名不能为空");
        }
        
        // 检查用户名是否存在
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否存在
        if (StringUtils.hasText(userDTO.getEmail()) && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }

        User user = convertToEntity(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setStatus(userDTO.getStatus() != null ? userDTO.getStatus() : 1);
        user.setAdmin(userDTO.getAdmin() != null ? userDTO.getAdmin() : false);

        user = userRepository.save(user);

        // 分配角色
        if (userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            assignRolesInternal(user.getId(), userDTO.getRoleIds());
        }

        return user;
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查用户名是否重复
        if (!user.getUsername().equals(userDTO.getUsername())) {
            if (userRepository.existsByUsernameAndIdNot(userDTO.getUsername(), id)) {
                throw new BusinessException("用户名已存在");
            }
        }

        // 检查邮箱是否重复
        if (StringUtils.hasText(userDTO.getEmail()) && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(userDTO.getEmail(), id)) {
                throw new BusinessException("邮箱已存在");
            }
        }

        user.setUsername(userDTO.getUsername());
        user.setRealName(userDTO.getRealName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setGender(userDTO.getGender());
        user.setAdmin(userDTO.getAdmin());
        user.setDeptId(userDTO.getDeptId());

        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }

        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        user = userRepository.save(user);

        // 更新角色
        if (userDTO.getRoleIds() != null) {
            assignRolesInternal(id, userDTO.getRoleIds());
        }

        return user;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        user.setDeleted(true);
        userRepository.save(user);

        // 删除用户角色关联
        userRoleRepository.deleteByUserId(id);
    }

    @Override
    @Transactional
    public void batchDeleteUsers(List<Long> ids) {
        for (Long id : ids) {
            deleteUser(id);
        }
    }

    @Override
    @Transactional
    public void changePassword(Long id, PasswordDTO passwordDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateAvatar(Long id, String avatarUrl) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        user.setAvatar(avatarUrl);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void assignRoles(Long id, List<Long> roleIds) {
        // 验证用户存在
        userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        assignRolesInternal(id, roleIds);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 重置为默认密码
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
    }

    private void assignRolesInternal(Long userId, List<Long> roleIds) {
        // 先删除旧的角色关联
        userRoleRepository.deleteByUserId(userId);

        // 创建新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole(userId, roleId);
                userRoleRepository.save(userRole);
            }
        }
    }

    private List<String> getUserRoles(Long userId) {
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.singletonList("ROLE_USER");
        }

        return roleRepository.findAllById(roleIds).stream()
                .filter(role -> !Boolean.TRUE.equals(role.getDeleted()))
                .map(role -> "ROLE_" + role.getRoleCode())
                .collect(Collectors.toList());
    }

    private UserInfoVO convertToUserInfoVO(User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        vo.setStatus(user.getStatus());
        vo.setAdmin(user.getAdmin());
        vo.setDeptId(user.getDeptId());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setLastLoginIp(user.getLastLoginIp());

        // 获取用户角色
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(user.getId());
        vo.setRoleIds(roleIds);

        List<String> roles = getUserRoles(user.getId());
        vo.setRoles(roles);

        return vo;
    }

    private User convertToEntity(UserDTO dto) {
        User entity = new User();
        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername());
        entity.setRealName(dto.getRealName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAvatar(dto.getAvatar());
        entity.setGender(dto.getGender());
        entity.setAdmin(dto.getAdmin());
        entity.setDeptId(dto.getDeptId());
        return entity;
    }
}
