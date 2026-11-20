package com.xidian.activity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 活动详情（管理端 / 公开宣传页共用，公开页不含敏感字段） */
@Data
public class ActivityDetail {

    private Long id;
    private String title;
    private String type;
    private String intro;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime signupDeadline;
    private Integer capacity;
    private Boolean published;
    private LocalDateTime firstPublishedAt;
    private String status;
    private String statusLabel;
    /** 已报名人数 */
    private Long enrolledCount;
    /** 宣传页分享链接（稳定，编辑后不变） */
    private String shareUrl;
    /** 现场签到页链接 */
    private String checkinUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 当前是否可报名（服务器时间判断） */
    private Boolean signupAvailable;
}
