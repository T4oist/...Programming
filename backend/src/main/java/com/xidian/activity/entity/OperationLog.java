package com.xidian.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 操作日志表：管理员关键操作留痕 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adminId;
    private Long activityId;
    /** LOGIN / CREATE / PUBLISH / UPDATE / DELETE / REGENERATE / PASSWORD */
    private String action;
    private String detail;
    private String ip;
    private LocalDateTime createdAt;
}
