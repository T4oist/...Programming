package com.xidian.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 学院字典表 */
@Data
@TableName("college")
public class College {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Boolean enabled;
    private Integer sortOrder;
}
