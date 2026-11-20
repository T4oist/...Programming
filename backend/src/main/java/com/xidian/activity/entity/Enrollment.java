package com.xidian.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 报名表：同一活动下姓名或手机号不可重复，(activity_id, name)/(activity_id, phone) 唯一约束兜底 */
@Data
@TableName("enrollment")
public class Enrollment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private String name;
    private String phone;
    private Long collegeId;
    private LocalDateTime createdAt;
}
