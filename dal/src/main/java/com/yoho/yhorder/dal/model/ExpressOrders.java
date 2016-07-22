package com.yoho.yhorder.dal.model;

public class ExpressOrders extends ExpressOrdersKey {
    /**
     * sms类型
     * 1 正常订单
     */
    public static final Byte SMS_TYPE_NORMAL = 1;

    /**
     * sms类型
     * 2 退货订单
     */
    public static final Byte SMS_TYPE_REFUND = 2;

    /**
     * sms类型
     * 3 换货订单
     */
    public static final Byte SMS_TYPE_CHANGE = 3;

    /**
     * 快递ID
     */
    private Byte expressId;

    /**
     * 物流订单创建时间
     */
    private Long createTime;

    /**
     * 订单创建时间
     */
    private Integer orderCreateTime;

    /**
     * 数量
     */
    private Byte num;

    /**
     * 标志
     */
    private Byte flag;

    /**
     * sms类型
     * 1 正常订单
     * 2 退货订单
     * 3 换货订单
     */
    private Byte smsType;

    public Byte getExpressId() {
        return expressId;
    }

    public void setExpressId(Byte expressId) {
        this.expressId = expressId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getOrderCreateTime() {
        return orderCreateTime;
    }

    public void setOrderCreateTime(Integer orderCreateTime) {
        this.orderCreateTime = orderCreateTime;
    }

    public Byte getNum() {
        return num;
    }

    public void setNum(Byte num) {
        this.num = num;
    }

    public Byte getFlag() {
        return flag;
    }

    public void setFlag(Byte flag) {
        this.flag = flag;
    }

    public Byte getSmsType() {
        return smsType;
    }

    public void setSmsType(Byte smsType) {
        this.smsType = smsType;
    }
}