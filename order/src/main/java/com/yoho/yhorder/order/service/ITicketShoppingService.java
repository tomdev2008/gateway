package com.yoho.yhorder.order.service;

import com.yoho.service.model.order.response.Orders;

/**
 * qianjun 2016/5/31
 */
public interface ITicketShoppingService {

    /**
     *  发放电子门票
     */
    void issueTicket(int uid,long orderCode);

    /**
     *  插入待评价记录
     */
    void addOrderGoodsToComment(Orders order);
}
