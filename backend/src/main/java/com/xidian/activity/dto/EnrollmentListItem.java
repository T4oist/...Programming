package com.xidian.activity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 报名名单项（含签到状态） */
@Data
public class EnrollmentListItem {

    private Long id;
    private String name;
    private String phone;
    private String college;
    private LocalDateTime createdAt;
    private Boolean checkedIn;
    private LocalDateTime checkedAt;
    /** static / dynamic */
    private String method;
}
