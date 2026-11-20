package com.xidian.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 签到表：一条报名最多一条有效签到（enrollment_id 唯一） */
@Data
@TableName("check_in")
public class CheckIn {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long enrollmentId;
    /** static 静态签到 / dynamic 动态二维码签到 */
    private String method;
    private LocalDateTime checkedAt;
}
