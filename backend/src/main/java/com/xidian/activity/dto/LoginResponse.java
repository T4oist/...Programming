package com.xidian.activity.dto;

import lombok.Builder;
import lombok.Data;

/** 登录响应：JWT 令牌 + 管理员基本信息 */
@Data
@Builder
public class LoginResponse {

    private String token;
    private Long id;
    private String username;
    private String realName;
    private String phone;
}
