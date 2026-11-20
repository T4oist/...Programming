package com.xidian.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xidian.activity.entity.College;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 学院 Mapper */
public interface CollegeMapper extends BaseMapper<College> {

    @Select("SELECT * FROM college WHERE enabled = 1 ORDER BY sort_order")
    List<College> selectEnabled();
}
