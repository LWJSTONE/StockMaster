package com.stockmaster.modules.system.service;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.modules.system.dto.*;
import com.stockmaster.modules.system.entity.User;

import java.util.List;

public interface UserService {

    LoginVO login(LoginDTO loginDTO);

    void logout();

    UserInfoVO getCurrentUser();

    User getUserById(Long id);

    PageResult<UserInfoVO> getUserList(String keyword, Integer status, Integer pageNum, Integer pageSize);

    User createUser(UserDTO userDTO);

    User updateUser(Long id, UserDTO userDTO);

    void deleteUser(Long id);

    void batchDeleteUsers(List<Long> ids);

    void changePassword(Long id, PasswordDTO passwordDTO);

    void updateAvatar(Long id, String avatarUrl);

    void assignRoles(Long id, List<Long> roleIds);

    void updateStatus(Long id, Integer status);

    void resetPassword(Long id);
}
