package com.xidian.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 扫码签到请求 */
@Data
public class CheckinRequest {

    @NotNull(message = "活动不能为空")
    private Long activityId;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号须为大陆11位手机号")
    private String phone;

    /** 动态签到凭证（必填，由活动签到二维码签发，禁止绕过） */
    @NotBlank(message = "签到凭证不能为空，请扫描活动签到二维码")
    private String token;
}
