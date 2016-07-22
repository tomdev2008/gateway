package com.yoho.yhorder.dal.model;

/**
 * Created by JXWU on 2016/2/25.
 */
public class OrderExtAttribute {
    private long orderCode;
    private String extAttribute;

    public long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(long orderCode) {
        this.orderCode = orderCode;
    }

    public String getExtAttribute() {
        return extAttribute;
    }

    public void setExtAttribute(String extAttribute) {
        this.extAttribute = extAttribute;
    }
}
