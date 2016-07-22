package com.yoho.yhorder.shopping.exception;

/**
 * Created by JXWU on 2015/12/3.
 * http 响应 状态码不为200
 */
public class HttpResponseException extends RuntimeException {
    private int code;
    private String message;

    public HttpResponseException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
