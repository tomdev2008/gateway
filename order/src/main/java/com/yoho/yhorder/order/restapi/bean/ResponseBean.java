package com.yoho.yhorder.order.restapi.bean;

/**
 * Created by CaoQi on 2015/10/29.
 * 统一controller返回格式
 */
public class ResponseBean {

    /**
     * 当前请求的状态
     */
    private String code;

    /**
     * 当前请求的信息
     */
    private String message;

    /**
     * 当前请求的返回数据
     */
    private Object data;

    public ResponseBean() {
        this.data = new String[]{};
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
