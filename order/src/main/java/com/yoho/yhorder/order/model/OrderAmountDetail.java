package com.yoho.yhorder.order.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Created by wujiexiang on 16/4/25.
 */
public class OrderAmountDetail {
    //优惠券减免金额
    @JSONField(name="coupons_amount")
    private double couponsCutAmount;

    //订单实付金额
    @JSONField(name="last_order_amount")
    private double lastOrderAmount;

    //订单商品金额
    @JSONField(name="order_amount")
    private double orderAmount;

    //促销减免金额
    @JSONField(name="promotion_amount")
    private double promotionCutAmount;

    //优惠码减免金额
    @JSONField(name="promotioncode_discount_amount")
    private double promotioncodeCutAmount;

    //运费
    @JSONField(name="shipping_cost")
    private double shippingCost;

    //使用的红包
    @JSONField(name="use_red_envelopes")
    private double redenvelopesCutAmount;

    //yoho币
    @JSONField(name="use_yoho_coin")
    private double yohCoinCutAmount;

    //vip
    @JSONField(name="vip_cutdown_amount")
    private double vipCutAmount;

    //运费原价
    @JSONField(name="shopping_orig_cost")
    private double shoppingOrigCost;

    //yoho币减免数量
    @JSONField(serialize = false)
    private int yohoCoinCutNum = 0;

    //商品返yoho币
    @JSONField(serialize = false)
    private int getYohoCoinNum;

    //使用yoho币抵扣运费
    @JSONField(name="use_yoho_coin_shipping_cost")
    private double yohoCoinShippingCost=0;


    public double getCouponsCutAmount() {
        return couponsCutAmount;
    }

    public void setCouponsCutAmount(double couponsCutAmount) {
        this.couponsCutAmount = couponsCutAmount;
    }

    public double getLastOrderAmount() {
        return lastOrderAmount;
    }

    public void setLastOrderAmount(double lastOrderAmount) {
        this.lastOrderAmount = lastOrderAmount;
    }

    public double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(double orderAmount) {
        this.orderAmount = orderAmount;
    }

    public double getPromotionCutAmount() {
        return promotionCutAmount;
    }

    public void setPromotionCutAmount(double promotionCutAmount) {
        this.promotionCutAmount = promotionCutAmount;
    }

    public double getPromotioncodeCutAmount() {
        return promotioncodeCutAmount;
    }

    public void setPromotioncodeCutAmount(double promotioncodeCutAmount) {
        this.promotioncodeCutAmount = promotioncodeCutAmount;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public double getRedenvelopesCutAmount() {
        return redenvelopesCutAmount;
    }

    public void setRedenvelopesCutAmount(double redenvelopesCutAmount) {
        this.redenvelopesCutAmount = redenvelopesCutAmount;
    }

    public double getYohCoinCutAmount() {
        return yohCoinCutAmount;
    }

    public void setYohCoinCutAmount(double yohCoinCutAmount) {
        this.yohCoinCutAmount = yohCoinCutAmount;
    }

    public double getVipCutAmount() {
        return vipCutAmount;
    }

    public void setVipCutAmount(double vipCutAmount) {
        this.vipCutAmount = vipCutAmount;
    }

    public double getShoppingOrigCost() {
        return shoppingOrigCost;
    }

    public void setShoppingOrigCost(double shoppingOrigCost) {
        this.shoppingOrigCost = shoppingOrigCost;
    }

    public int getGetYohoCoinNum() {
        return getYohoCoinNum;
    }

    public void setGetYohoCoinNum(int getYohoCoinNum) {
        this.getYohoCoinNum = getYohoCoinNum;
    }

    public int getYohoCoinCutNum() {
        return yohoCoinCutNum;
    }

    public void setYohoCoinCutNum(int yohoCoinCutNum) {
        this.yohoCoinCutNum = yohoCoinCutNum;
    }


    public double getYohoCoinShippingCost() {
        return yohoCoinShippingCost;
    }

    public void setYohoCoinShippingCost(double yohoCoinShippingCost) {
        this.yohoCoinShippingCost = yohoCoinShippingCost;
    }


    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
