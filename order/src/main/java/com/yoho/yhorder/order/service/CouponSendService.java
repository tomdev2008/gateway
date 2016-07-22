package com.yoho.yhorder.order.service;

import com.yoho.yhorder.dal.model.CouponSend;

/**
 * Created by yoho on 2016/1/7.
 */
public interface CouponSendService {
    /**
     * 发送订单确认优惠券
     */
    void sendOrderConfirmCoupon(CouponSend couponSend);
}
