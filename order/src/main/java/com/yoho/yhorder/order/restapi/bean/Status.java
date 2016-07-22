package com.yoho.yhorder.order.restapi.bean;

/**
 * Created by CaoQi on 2015/10/28.
 * 状态的枚举类
 * SUCCESS 成功 值：0
 */
public enum Status {

    SUCCESS("200","操作成功"),ERROR("300","操作失败");
    private Status(String value, String msg){
        this.value=value;
        this.msg= msg;
    }

    public String getValue() {
        return value;
    }

    public String getMsg() {
        return msg;
    }
    private String msg;
    private String value;
}
