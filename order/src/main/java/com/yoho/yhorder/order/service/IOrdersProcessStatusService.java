package com.yoho.yhorder.order.service;

import com.yoho.service.model.order.response.Orders;
import com.yoho.yhorder.dal.domain.OrdersProcessStatus;

import java.util.List;

/**
 * qianjun 2016/6/13
 */
public interface IOrdersProcessStatusService {

    /**
     * 获取单个订单状态
     *
     */
    OrdersProcessStatus select(Orders orders);

    /**
     * 批量获取订单状态
     *
     */
    List<OrdersProcessStatus> select(List<Orders> orderses);

    /**
     * 保存或更新订单状态
     *
     */
    void save(Orders orders, int status);
}
