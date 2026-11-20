package com.xidian.activity.common;

import lombok.Data;

/**
 * 统一响应包装类
 */
@Data
public class Result<T> {

    /** 业务码：0 成功，非 0 失败 */
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 0;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static Result<Void> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        Result<T> r = new Result<>();
        r.code = errorCode.getCode();
        r.message = errorCode.getMessage();
        return r;
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        Result<T> r = new Result<>();
        r.code = errorCode.getCode();
        r.message = message;
        return r;
    }
}
