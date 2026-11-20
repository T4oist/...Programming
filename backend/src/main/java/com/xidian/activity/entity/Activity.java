package com.xidian.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/** 活动表（展示状态由业务层按时间计算） */
@Data
@TableName("activity")
public class Activity {

    @TableId(type = IdType.AUTO)
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
    /** 逻辑删除标记，由 MyBatis-Plus @TableLogic 维护 */
    @TableLogic
    private Boolean deleted;
    private LocalDateTime deletedAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** 乐观锁版本号 */
    @Version
    private Integer version;
}
