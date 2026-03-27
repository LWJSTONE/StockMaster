package com.stockmaster.modules.system.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserDTO {
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String email;

    private String phone;

    private String avatar;

    private Integer gender;

    private Integer status;

    private Boolean admin;

    private Long deptId;

    private List<Long> roleIds;
}
