package com.xidian.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 报名表：(activityId, phone) 数据库唯一约束防重复报名 */
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
