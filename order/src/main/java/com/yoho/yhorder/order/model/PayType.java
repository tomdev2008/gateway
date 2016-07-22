package com.yoho.yhorder.order.model;

/**
 * Created by JXWU on 2016/1/23.
 */
public enum PayType {
    NULLPAY(0), ALIPAY_PC(2), ALIPAY_BANK(12), UNIONPAY(14), ALIPAY(15), ALIPAYWAP(18), WECHATAPP(19), WECHAT_QRCODE(21), WECHATWAP(22), UNIONPAY_WEB(25),QQ_WALLET(26);
    private int payId = 0;

    PayType(int payId) {
        this.payId = payId;
    }

    public int getPayId() {
        return payId;
    }

    /**
     *
     * 支付方式
     * 14:银联手机支付
     * 15:支付宝手机
     * 18:支付宝wap
     * 19:微信支付
     * 22:微信wap
     * @param payId
     * @return
     */
    public static PayType valueOf(int payId) {
        switch (payId) {
            case 0:
                return NULLPAY;
            case 2:
            	return ALIPAY_PC;
            case 12:	
            	return ALIPAY_BANK;
            case 14:
                return UNIONPAY;
            case 15:
                return ALIPAY;
            case 18:
                return ALIPAYWAP;
            case 19:
                return WECHATAPP;
            case 21:
            	return WECHAT_QRCODE;
            case 22:
                return WECHATWAP;
            case 25:
                return UNIONPAY_WEB; 
            case 26:
                return QQ_WALLET;
            default:
                return NULLPAY;
        }
    }
}
