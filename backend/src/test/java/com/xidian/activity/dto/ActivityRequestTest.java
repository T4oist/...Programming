package com.xidian.activity.dto;

import com.xidian.activity.common.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** 活动时间合法性校验单元测试 */
class ActivityRequestTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 12, 1, 10, 0);

    private ActivityRequest req(LocalDateTime start, LocalDateTime end, LocalDateTime deadline) {
        ActivityRequest r = new ActivityRequest();
        r.setStartTime(start);
        r.setEndTime(end);
        r.setSignupDeadline(deadline);
        return r;
    }

    @Test
    void validTimelinePasses() {
        assertDoesNotThrow(() -> req(BASE, BASE.plusHours(2), BASE.minusHours(1)).validateTime());
    }

    @Test
    void startAfterEndIsRejected() {
        assertThrows(BusinessException.class,
                () -> req(BASE.plusHours(3), BASE, BASE.minusHours(1)).validateTime());
    }

    @Test
    void deadlineAfterStartIsRejected() {
        assertThrows(BusinessException.class,
                () -> req(BASE, BASE.plusHours(2), BASE.plusMinutes(30)).validateTime());
    }

    @Test
    void deadlineEqualsStartIsAllowed() {
        assertDoesNotThrow(() -> req(BASE, BASE.plusHours(2), BASE).validateTime());
    }
}
