package com.stockmaster.modules.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;
    private String refreshToken;
    private Long expireTime;
    private UserInfoVO userInfo;
}
