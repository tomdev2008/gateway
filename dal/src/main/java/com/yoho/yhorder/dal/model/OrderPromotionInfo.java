package com.yoho.yhorder.dal.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class OrderPromotionInfo {
    private Integer id;

    private Long orderCode;

    private Integer uid;

    private String orderPromotion;

    private Integer createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getOrderPromotion() {
        return orderPromotion;
    }

    public void setOrderPromotion(String orderPromotion) {
        this.orderPromotion = orderPromotion == null ? null : orderPromotion.trim();
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public String toString(){
        return ReflectionToStringBuilder.toString(this);
    }
}