package com.yoho.yhorder.order.service;

import com.yoho.service.model.order.response.Orders;
import com.yoho.yhorder.dal.domain.DeliveryAddress;

import java.util.List;

/**
 * Created by yoho on 2016/6/3.
 */
public interface IOrdersDeliveryAddressRepository {

    DeliveryAddress select(Orders orders);

    void update(Orders orders, DeliveryAddress deliveryAddress);

    void insert(List<Orders> orderses, DeliveryAddress deliveryAddress);

    void update(List<Orders> orderses, DeliveryAddress deliveryAddress);

}
