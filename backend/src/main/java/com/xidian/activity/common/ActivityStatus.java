package com.xidian.activity.common;

/**
 * 活动展示状态（由系统按服务器时间实时计算，不落库）：
 * 未发布 / 报名中 / 已截止 / 已结束
 */
public enum ActivityStatus {

    DRAFT("未发布"),
    SIGNUP_OPEN("报名中"),
    CLOSED("已截止"),
    FINISHED("已结束");

    private final String label;

    ActivityStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 依据发布标记与时间计算展示状态
     *
     * @param published       是否已发布
     * @param signupDeadline  报名截止时间
     * @param endTime         活动结束时间
     * @param now             当前服务器时间
     */
    public static ActivityStatus of(boolean published, java.time.LocalDateTime signupDeadline,
                                    java.time.LocalDateTime endTime, java.time.LocalDateTime now) {
        if (!published) {
            return DRAFT;
        }
        if (now.isBefore(signupDeadline)) {
            return SIGNUP_OPEN;
        }
        if (now.isBefore(endTime)) {
            return CLOSED;
        }
        return FINISHED;
    }
}
