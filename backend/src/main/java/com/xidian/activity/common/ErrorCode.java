package com.xidian.activity.common;

import lombok.Getter;

/**
 * 系统错误码：5 位业务码，与 HTTP 状态码对应
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, 200, "成功"),

    PARAM_ERROR(40001, 400, "参数错误"),
    UNAUTHORIZED(40101, 401, "未登录或会话已过期"),
    FORBIDDEN(40301, 403, "无权访问"),
    NOT_FOUND(40401, 404, "资源不存在"),

    // 登录 / 账号 41xxx
    LOGIN_FAILED(41101, 400, "账号或密码错误"),
    OLD_PASSWORD_WRONG(41102, 400, "原密码错误"),
    USERNAME_EXISTS(41103, 400, "账号已存在"),

    // 活动 42xxx
    ACTIVITY_NOT_FOUND(42001, 404, "活动不存在或已删除"),
    ACTIVITY_TIME_INVALID(42002, 400, "活动时间设置不合法"),
    ACTIVITY_CAPACITY_INVALID(42003, 400, "人数上限必须为正整数"),
    ACTIVITY_PUBLISH_NOT_ALLOWED(42004, 400, "活动已截止或已结束，不满足发布条件"),
    ACTIVITY_CAPACITY_LESS_THAN_ENROLLED(42005, 400, "人数上限不能小于已报名人数"),

    // 报名 43xxx
    SIGNUP_NOT_AVAILABLE(43001, 400, "活动未发布或已删除，暂不可报名"),
    SIGNUP_ENDED(43002, 400, "报名已结束，感谢关注"),
    SIGNUP_DUPLICATE(43003, 400, "该手机号已报名，请勿重复提交"),
    SIGNUP_FULL(43004, 400, "报名人数已满"),
    SIGNUP_NAME_INVALID(43005, 400, "姓名须为2~20个字符"),
    SIGNUP_PHONE_INVALID(43006, 400, "手机号格式不正确"),
    SIGNUP_COLLEGE_INVALID(43007, 400, "学院不在可选列表中"),

    // 签到 44xxx
    CHECKIN_NOT_OPEN(44001, 400, "当前不在签到时间窗内"),
    CHECKIN_NOT_ENROLLED(44002, 400, "未查询到报名记录，请先报名或联系管理员"),
    CHECKIN_TOKEN_INVALID(44003, 400, "签到入口无效"),
    CHECKIN_TOKEN_EXPIRED(44004, 400, "二维码已刷新，请重新扫码"),

    SYSTEM_ERROR(50000, 500, "系统内部错误");

    private final int code;
    private final int httpStatus;
    private final String message;

    ErrorCode(int code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
