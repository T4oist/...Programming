package com.xidian.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xidian.activity.dto.EnrollmentListItem;
import com.xidian.activity.entity.Enrollment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 报名 Mapper */
public interface EnrollmentMapper extends BaseMapper<Enrollment> {

    /** 报名名单（含学院名称与签到状态，供管理端展示） */
    @Select("""
            SELECT e.id, e.name, e.phone, c.name AS college, e.created_at,
                   (ci.id IS NOT NULL) AS checked_in, ci.checked_at, ci.method
            FROM enrollment e
            LEFT JOIN college c ON c.id = e.college_id
            LEFT JOIN check_in ci ON ci.enrollment_id = e.id
            WHERE e.activity_id = #{activityId}
            ORDER BY e.created_at
            """)
    List<EnrollmentListItem> selectListWithCheckin(@Param("activityId") Long activityId);

    /** 按活动 + 手机号查询报名（签到核验用） */
    @Select("SELECT * FROM enrollment WHERE activity_id = #{activityId} AND phone = #{phone} LIMIT 1")
    Enrollment selectByActivityAndPhone(@Param("activityId") Long activityId, @Param("phone") String phone);

    /**
     * 重复报名预检：同一活动下「姓名相同」或「手机号相同」均视为重复。
     * 在锁定活动行之后执行，同一活动的报名已串行化，快照读即可看到已提交记录；
     * 数据库唯一约束 (activity_id, name) / (activity_id, phone) 作为并发兜底。
     */
    @Select("""
            SELECT COUNT(*) FROM enrollment
            WHERE activity_id = #{activityId} AND (name = #{name} OR phone = #{phone})
            """)
    long countDuplicate(@Param("activityId") Long activityId,
                        @Param("name") String name,
                        @Param("phone") String phone);

    /**
     * 当前读统计名额占用（FOR SHARE）：在 FOR UPDATE 锁定活动行之后调用，
     * 保证读到的是已提交的最新报名数，避免 RR 下快照读复用旧 read view 导致超卖。
     */
    @Select("SELECT COUNT(*) FROM enrollment WHERE activity_id = #{activityId} FOR SHARE")
    long countForUpdate(@Param("activityId") Long activityId);

    /** 某活动的有效签到人数 */
    @Select("""
            SELECT COUNT(*) FROM check_in ci
            JOIN enrollment e ON e.id = ci.enrollment_id
            WHERE e.activity_id = #{activityId}
            """)
    long countChecked(@Param("activityId") Long activityId);
}
