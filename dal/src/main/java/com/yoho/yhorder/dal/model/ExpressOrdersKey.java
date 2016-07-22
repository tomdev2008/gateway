package com.yoho.yhorder.dal.model;

public class ExpressOrdersKey {
    /**
     * 订单号
     */
    private Long orderCode;

    /**
     * 快递单号
     */
    private String expressNumber;

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public String getExpressNumber() {
        return expressNumber;
    }

    public void setExpressNumber(String expressNumber) {
        this.expressNumber = expressNumber == null ? null : expressNumber.trim();
    }
}