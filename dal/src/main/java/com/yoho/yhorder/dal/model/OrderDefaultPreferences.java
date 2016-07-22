package com.yoho.yhorder.dal.model;

public class OrderDefaultPreferences {
    private Integer uid;

    private String orderDefaultPreferences;

    private Integer updateTime;

    private Integer createTime;

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getOrderDefaultPreferences() {
        return orderDefaultPreferences;
    }

    public void setOrderDefaultPreferences(String orderDefaultPreferences) {
        this.orderDefaultPreferences = orderDefaultPreferences == null ? null : orderDefaultPreferences.trim();
    }

    public Integer getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Integer updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }
}