package com.xidian.activity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/** 活动创建 / 编辑请求 */
@Data
public class ActivityRequest {

    @NotBlank(message = "活动名称不能为空")
    @Size(max = 100, message = "活动名称不超过100字符")
    private String title;

    @NotBlank(message = "活动类型不能为空")
    private String type;

    @Size(max = 2000, message = "活动简介不超过2000字符")
    private String intro;

    @NotBlank(message = "活动地点不能为空")
    @Size(max = 100, message = "活动地点不超过100字符")
    private String location;

    @NotNull(message = "活动开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "活动结束时间不能为空")
    private LocalDateTime endTime;

    @NotNull(message = "报名截止时间不能为空")
    private LocalDateTime signupDeadline;

    @NotNull(message = "人数上限不能为空")
    @Min(value = 1, message = "人数上限必须为正整数")
    private Integer capacity;

    /**
     * 时间合法性校验：报名截止 <= 活动开始 < 活动结束
     */
    public void validateTime() {
        if (startTime == null || endTime == null || signupDeadline == null) {
            return;
        }
        if (!startTime.isBefore(endTime)) {
            throw new com.xidian.activity.common.BusinessException(
                    com.xidian.activity.common.ErrorCode.ACTIVITY_TIME_INVALID,
                    "活动开始时间必须早于结束时间");
        }
        if (signupDeadline.isAfter(startTime)) {
            throw new com.xidian.activity.common.BusinessException(
                    com.xidian.activity.common.ErrorCode.ACTIVITY_TIME_INVALID,
                    "报名截止时间不得晚于活动开始时间");
        }
    }
}
