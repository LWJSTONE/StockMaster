package com.stockmaster.modules.system.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.common.service.FileUploadService;
import com.stockmaster.modules.system.dto.*;
import com.stockmaster.modules.system.entity.User;
import com.stockmaster.modules.system.service.UserService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileUploadService fileUploadService;

    @GetMapping
    @LogOperation(value = OperationType.QUERY, module = "用户管理", description = "查询用户列表")
    public ApiResponse<PageResult<UserInfoVO>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<UserInfoVO> result = userService.getUserList(keyword, status, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @LogOperation(value = OperationType.QUERY, module = "用户管理", description = "查询用户详情")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    @GetMapping("/info")
    public ApiResponse<UserInfoVO> getCurrentUser() {
        UserInfoVO userInfo = userService.getCurrentUser();
        return ApiResponse.success(userInfo);
    }

    @PostMapping
    @LogOperation(value = OperationType.CREATE, module = "用户管理", description = "创建用户")
    public ApiResponse<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        User user = userService.createUser(userDTO);
        return ApiResponse.success(user);
    }

    @PutMapping("/{id}")
    @LogOperation(value = OperationType.UPDATE, module = "用户管理", description = "修改用户")
    public ApiResponse<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        User user = userService.updateUser(id, userDTO);
        return ApiResponse.success(user);
    }

    @DeleteMapping("/{id}")
    @LogOperation(value = OperationType.DELETE, module = "用户管理", description = "删除用户")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    @DeleteMapping("/batch")
    @LogOperation(value = OperationType.DELETE, module = "用户管理", description = "批量删除用户")
    public ApiResponse<Void> batchDeleteUsers(@RequestBody List<Long> ids) {
        userService.batchDeleteUsers(ids);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/password")
    @LogOperation(value = OperationType.UPDATE, module = "用户管理", description = "修改密码")
    public ApiResponse<Void> changePassword(@PathVariable Long id, @Valid @RequestBody PasswordDTO passwordDTO) {
        userService.changePassword(id, passwordDTO);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/avatar")
    @LogOperation(value = OperationType.UPDATE, module = "用户管理", description = "上传头像")
    public ApiResponse<Map<String, String>> uploadAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String avatarUrl = fileUploadService.uploadAvatar(file);
        userService.updateAvatar(id, avatarUrl);
        Map<String, String> result = new HashMap<>();
        result.put("url", avatarUrl);
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}/roles")
    @LogOperation(value = OperationType.UPDATE, module = "用户管理", description = "分配角色")
    public ApiResponse<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    @LogOperation(value = OperationType.UPDATE, module = "用户管理", description = "修改用户状态")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/reset-password")
    @LogOperation(value = OperationType.UPDATE, module = "用户管理", description = "重置密码")
    public ApiResponse<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return ApiResponse.success();
    }

    @PutMapping("/profile")
    @LogOperation(value = OperationType.UPDATE, module = "用户管理", description = "修改个人信息")
    public ApiResponse<User> updateProfile(@Valid @RequestBody UserDTO userDTO) {
        UserInfoVO currentUser = userService.getCurrentUser();
        User user = userService.updateUser(currentUser.getId(), userDTO);
        return ApiResponse.success(user);
    }
}
