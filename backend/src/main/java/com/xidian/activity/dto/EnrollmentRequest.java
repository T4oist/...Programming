package com.xidian.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 参与人报名请求 */
@Data
public class EnrollmentRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名须为2~20个字符")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号须为大陆11位手机号")
    private String phone;

    @NotNull(message = "学院不能为空")
    private Long collegeId;
}
