package com.yoho.yhorder.order.event;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Created by wujiexiang on 16/4/28.
 */
public class OrderSplitEvent {
    //用户uid
    private int uid;
    //订单code
    private long orderCode;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(long orderCode) {
        this.orderCode = orderCode;
    }

    @Override
    public String toString(){
        return ReflectionToStringBuilder.toString(this);
    }
}
