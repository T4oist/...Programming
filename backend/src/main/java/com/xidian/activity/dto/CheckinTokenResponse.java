package com.xidian.activity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 动态签到码响应（管理端签到码展示页每 30 秒拉取一次） */
@Data
@Builder
public class CheckinTokenResponse {

    private Long activityId;
    /** HMAC 动态凭证（拼入二维码内容） */
    private String token;
    /** 凭证过期时间（毫秒时间戳） */
    private Long expireAt;
    /** 刷新间隔（秒） */
    private Integer refreshSeconds;
    /** 签到页完整地址（含凭证） */
    private String checkinUrl;
    /** 活动标题（页面展示） */
    private String title;
}
