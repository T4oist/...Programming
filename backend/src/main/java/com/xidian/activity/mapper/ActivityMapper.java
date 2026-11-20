package com.xidian.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xidian.activity.dto.ActivityListItem;
import com.xidian.activity.dto.ActivityListQuery;
import com.xidian.activity.entity.Activity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** 活动 Mapper */
public interface ActivityMapper extends BaseMapper<Activity> {

    /** 报名事务内悲观锁锁定活动行，保证并发名额控制 */
    @Select("SELECT * FROM activity WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    Activity selectByIdForUpdate(@Param("id") Long id);

    /** 分页查询（状态由 SQL 按服务器时间实时计算，支持多条件筛选） */
    IPage<ActivityListItem> selectPageWithStatus(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<ActivityListItem> page,
            @Param("q") ActivityListQuery query);

    /** 逻辑删除：公开入口立即失效，历史数据保留 */
    @Update("UPDATE activity SET deleted = 1, deleted_at = NOW() WHERE id = #{id} AND deleted = 0")
    int deleteLogically(@Param("id") Long id);
}
