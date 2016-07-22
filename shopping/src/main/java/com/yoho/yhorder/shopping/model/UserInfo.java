package com.yoho.yhorder.shopping.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Created by JXWU on 2015/11/27.
 * 用户信息
 */
public class UserInfo {

    /**
     * 结算使用默认使用yoho币,1 使用
     */
    public final static int DEFAULT_USE_YOHO_COIN = 1;

    /**
     * 结算不使用yoho币
     */
    public final static int DEFAULT_NOTUSE_YOHO_COIN = 0;

    /**
     * VIP 级别
     *
     * @var int
     */
    public int userLevel = 0;

    /**
     * 用户当月订单数量
     */
    private int monthOrderCount = 0;

    /**
     * 用户实际拥有的YOHO币总数，还有稀释单位
     */
    private OrderYohoCoin orderYohoCoin = new OrderYohoCoin();

    /**
     * 用户红包总数
     */
    private double redEnvelopes = 0;


    /**
     *  uid
     */
    private int uid;

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public int getMonthOrderCount() {
        return monthOrderCount;
    }

    public void setMonthOrderCount(int monthOrderCount) {
        this.monthOrderCount = monthOrderCount;
    }

    public OrderYohoCoin getOrderYohoCoin() {
        return orderYohoCoin;
    }

    public void setOrderYohoCoin(OrderYohoCoin orderYohoCoin) {
        this.orderYohoCoin = orderYohoCoin;
    }

    public double getRedEnvelopes() {
        return redEnvelopes;
    }

    public void setRedEnvelopes(double redEnvelopes) {
        this.redEnvelopes = redEnvelopes;
    }

    /**
     * 返回yoho币 金额
     *
     * @param yohoCoinMode
     * @return
     */
    public double getRatedYohoCoinFor(int yohoCoinMode) {
        if (yohoCoinMode == DEFAULT_USE_YOHO_COIN) {
            return getRatedYohoCoin();
        } else {
            return 0.00;
        }
    }

    public double getRatedYohoCoin() {
        return this.getOrderYohoCoin().ratedYohoCoin();
    }


    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
