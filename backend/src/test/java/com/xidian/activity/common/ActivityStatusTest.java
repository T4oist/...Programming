package com.xidian.activity.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** 活动展示状态实时计算单元测试 */
class ActivityStatusTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 20, 12, 0);

    @Test
    void unpublishedIsAlwaysDraft() {
        assertEquals(ActivityStatus.DRAFT,
                ActivityStatus.of(false, NOW.minusDays(1), NOW.plusDays(1), NOW));
    }

    @Test
    void publishedBeforeDeadlineIsSignupOpen() {
        assertEquals(ActivityStatus.SIGNUP_OPEN,
                ActivityStatus.of(true, NOW.plusHours(1), NOW.plusDays(1), NOW));
    }

    @Test
    void afterDeadlineBeforeEndIsClosed() {
        assertEquals(ActivityStatus.CLOSED,
                ActivityStatus.of(true, NOW.minusHours(1), NOW.plusHours(1), NOW));
    }

    @Test
    void afterEndIsFinished() {
        assertEquals(ActivityStatus.FINISHED,
                ActivityStatus.of(true, NOW.minusDays(1), NOW.minusHours(1), NOW));
    }

    @Test
    void exactDeadlineBoundaryIsClosed() {
        // 报名截止的瞬间即视为“已截止”，与报名校验 !now.isBefore(deadline) 语义一致
        assertEquals(ActivityStatus.CLOSED,
                ActivityStatus.of(true, NOW, NOW.plusHours(1), NOW));
    }
}
