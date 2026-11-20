package com.xidian.activity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 活动列表项（含实时计算状态与已报名人数） */
@Data
public class ActivityListItem {

    private Long id;
    private String title;
    private String type;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime signupDeadline;
    private Integer capacity;
    private Boolean published;
    private LocalDateTime firstPublishedAt;
    private LocalDateTime createdAt;
    /** 已报名人数（由报名记录实时统计） */
    private Long enrolledCount;
    /** 展示状态：DRAFT/SIGNUP_OPEN/CLOSED/FINISHED */
    private String status;

    public String getStatusLabel() {
        if (status == null || status.isBlank()) {
            return "未知状态";
        }
        try {
            return com.xidian.activity.common.ActivityStatus.valueOf(status).getLabel();
        } catch (IllegalArgumentException e) {
            return "未知状态";
        }
    }
}
