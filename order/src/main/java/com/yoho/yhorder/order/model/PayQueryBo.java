package com.yoho.yhorder.order.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Created by JXWU on 2016/1/22.
 */
public class PayQueryBo {

    public boolean valid = false;
    
    public boolean paymentSupport = true;//是否支持主动确认的支付方式

    //支付方式
    public PayType payType = PayType.NULLPAY;
    //订单编号
    public String orderCode = "";
    //订单金额
    public double amount;
    //
    public String bankCode = "";

    public String bankName = "";

    public String payOrderCode = "";

    public String tradeNo = "";

    public String bankBillNo = "";

    /**
     * 回调时间
     * @return
     */
    public String callbackTime;

    /**
     * 付款时间
     */
    public String paymentTime;

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
