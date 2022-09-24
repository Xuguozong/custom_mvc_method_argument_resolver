package com.example.common.rest;

import com.example.common.constant.ResponseCode;
import lombok.Data;

import java.io.Serializable;

@Data
public class R<T> implements Serializable {

    private T data;

    private String msg;

    private int code;

    public R(T data, String msg, int code) {
        this.data = data;
        this.msg = msg;
        this.code = code;
    }

    public R() {
    }

    public static <T> R<T> ok() {
        return new R("", ResponseCode.SUCCESS, ResponseCode.OK);
    }

    public static <T> R<T> ok(T data) {
        return new R(data, ResponseCode.SUCCESS, ResponseCode.OK);
    }

    public static <T> R<T> ok(T data, String msg) {
        return new R(data, msg, ResponseCode.OK);
    }

    public static <T> R<T> ok(T data, int code) {
        return new R(data, ResponseCode.SUCCESS, code);
    }

    public static <T> R<T> failed(String msg) {
        return new R(null, msg, ResponseCode.FAILED);
    }
}
