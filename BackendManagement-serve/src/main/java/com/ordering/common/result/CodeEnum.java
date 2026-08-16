package com.ordering.common.result;

import lombok.Getter;

/**
 * 常用返回码。401 与小程序 request.js 的未登录分支对齐。
 */
@Getter
public enum CodeEnum {

    SUCCESS(0, "ok"),
    UNAUTHORIZED(401, "未登录或登录失效"),
    FORBIDDEN(403, "无权限"),
    PARAM_ERROR(400, "参数错误"),
    BIZ_ERROR(500, "业务异常"),
    SYSTEM_ERROR(500, "系统异常");

    private final int code;
    private final String msg;

    CodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
