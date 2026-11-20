package com.xidian.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 修改个人信息请求 */
@Data
public class ProfileUpdateRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名须为2~20个字符")
    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号须为大陆11位手机号")
    private String phone;
}
