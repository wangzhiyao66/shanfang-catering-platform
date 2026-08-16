package com.ordering.common.result;

import lombok.Getter;

/**
 * 业务异常：抛出后由 GlobalExceptionHandler 统一转成 { code, msg }。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BizException(CodeEnum e) {
        this(e.getCode(), e.getMsg());
    }

    public BizException(String msg) {
        this(CodeEnum.BIZ_ERROR.getCode(), msg);
    }
}
