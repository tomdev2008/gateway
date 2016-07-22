package com.yoho.yhorder.order.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Created by JXWU on 2015/11/30.
 * 创建订单需要调用erp的接口，接口参数为json，参数名称不能变，所以属性名采用这种命名方式
 */
public class Coupon {
    /**
     * 优惠券ID
     */
    private Integer coupon_id;

    /**
     * 优惠券码
     */
    private String coupon_code;

    //优惠券使用金额
    private Double coupon_amount;
    /**
     * 优惠券名称
     */
    public String coupon_title;

    public Integer getCoupon_id() {
        return coupon_id;
    }

    public void setCoupon_id(Integer coupon_id) {
        this.coupon_id = coupon_id;
    }

    public String getCoupon_code() {
        return coupon_code;
    }

    public void setCoupon_code(String coupon_code) {
        this.coupon_code = coupon_code;
    }

    public Double getCoupon_amount() {
        return coupon_amount;
    }

    public void setCoupon_amount(Double coupon_amount) {
        this.coupon_amount = coupon_amount;
    }

    public String getCoupon_title() {
        return coupon_title;
    }

    public void setCoupon_title(String coupon_title) {
        this.coupon_title = coupon_title;
    }

    @Override
    public String toString(){
        return ReflectionToStringBuilder.toString(this);
    }
}
