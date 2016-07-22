package com.yoho.yhorder.dal.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class OrdersCoupons {
    private Integer id;

    private Integer uid;

    private Long orderCode;

    private Integer orderId;

    private Integer couponsId;

    private String couponsCode;

    private Integer createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getCouponsId() {
        return couponsId;
    }

    public void setCouponsId(Integer couponsId) {
        this.couponsId = couponsId;
    }

    public String getCouponsCode() {
        return couponsCode;
    }

    public void setCouponsCode(String couponsCode) {
        this.couponsCode = couponsCode == null ? null : couponsCode.trim();
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}