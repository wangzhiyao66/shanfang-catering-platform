package com.ordering.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结构：{ code, msg, data }，与小程序 request.js 契约一致（code=0 成功）。
 */
@Data
public class R<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(0);
        r.setMsg("ok");
        r.setData(data);
        return r;
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(null);
        return r;
    }
}
