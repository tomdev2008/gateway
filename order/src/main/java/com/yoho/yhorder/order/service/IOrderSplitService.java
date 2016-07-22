package com.yoho.yhorder.order.service;

/**
 * Created by wujiexiang on 16/4/24.
 */
public interface IOrderSplitService {
    /**
     * 拆分订单
     *
     * @param orderCode
     * @return 拆分后的子订单数
     */
    int splitOrder(int uid, long orderCode);
}
