package com.xidian.activity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/** 活动列表多条件筛选：类型 / 状态 / 活动开始时间范围 / 名称关键字 */
@Data
public class ActivityListQuery {

    private String type;
    /** DRAFT / SIGNUP_OPEN / CLOSED / FINISHED */
    private String status;
    /** 活动开始时间范围（前端 Element Plus 使用该格式；不写则 Spring 默认只接受 ISO 的 T 格式） */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private String keyword;
    @Min(value = 1, message = "页码从 1 开始")
    private Integer pageNum = 1;
    @Min(value = 1, message = "每页条数至少为 1")
    @Max(value = 100, message = "每页最多 100 条")
    private Integer pageSize = 10;
}
