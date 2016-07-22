package com.yoho.yhorder.order.service;


/**
 * 赠送有货币
 * @author yoho
 *
 */
public interface DeliverYohoCoinService {
    /**
     * 订单完成7天后，赠送有货币
     */
    void deliverYohoCoin();
}
