package com.stockmaster.modules.system.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.common.security.SecurityUtils;
import com.stockmaster.modules.system.dto.*;
import com.stockmaster.modules.system.entity.User;
import com.stockmaster.modules.system.service.UserService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    @LogOperation(value = OperationType.LOGIN, module = "认证", description = "用户登录")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return ApiResponse.success(loginVO);
    }

    @PostMapping("/logout")
    @LogOperation(value = OperationType.LOGOUT, module = "认证", description = "用户登出")
    public ApiResponse<Void> logout() {
        userService.logout();
        return ApiResponse.success();
    }

    @GetMapping("/info")
    public ApiResponse<UserInfoVO> getUserInfo() {
        UserInfoVO userInfo = userService.getCurrentUser();
        return ApiResponse.success(userInfo);
    }

    @PostMapping("/register")
    @LogOperation(value = OperationType.CREATE, module = "认证", description = "用户注册")
    public ApiResponse<User> register(@Valid @RequestBody UserDTO userDTO) {
        User user = userService.createUser(userDTO);
        return ApiResponse.success(user);
    }
}
