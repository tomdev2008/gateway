package com.yoho.yhorder.shopping.event;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Created by wujiexiang on 16/7/12.
 */
public class ShoppingTicketIssueEvent {

    private int uid;
    private Long orderCode;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
