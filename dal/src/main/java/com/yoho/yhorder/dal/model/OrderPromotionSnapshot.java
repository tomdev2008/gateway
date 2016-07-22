package com.yoho.yhorder.dal.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Created by wujiexiang on 16/4/22.
 */
public class OrderPromotionSnapshot {

    //订单编号
    private long orderCode;

    //促销id
    private String promotionId;

    //促销类型
    private String promotionType;

    //优先级
    private int priority;

    private String startTime;

    private String endTime;

    //促销条件
    private String conditionParam;

    //促销操作
    private String actionParam;

    //促销限制
    private String limitParam;

    //互斥促销
    private String rejectParam;

    public long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(long orderCode) {
        this.orderCode = orderCode;
    }

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionType() {
        return promotionType;
    }

    public void setPromotionType(String promotionType) {
        this.promotionType = promotionType;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getConditionParam() {
        return conditionParam;
    }

    public void setConditionParam(String conditionParam) {
        this.conditionParam = conditionParam;
    }

    public String getActionParam() {
        return actionParam;
    }

    public void setActionParam(String actionParam) {
        this.actionParam = actionParam;
    }

    public String getLimitParam() {
        return limitParam;
    }

    public void setLimitParam(String limitParam) {
        this.limitParam = limitParam;
    }

    public String getRejectParam() {
        return rejectParam;
    }

    public void setRejectParam(String rejectParam) {
        this.rejectParam = rejectParam;
    }

    public String toString(){
        return ReflectionToStringBuilder.toString(this);
    }
}
