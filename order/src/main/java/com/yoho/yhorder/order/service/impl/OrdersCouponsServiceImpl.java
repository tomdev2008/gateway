package com.yoho.yhorder.order.service.impl;

import com.yoho.service.model.order.model.OrdersCouponsBO;
import com.yoho.service.model.order.request.OrdersCouponsRequest;
import com.yoho.service.model.order.response.Orders;
import com.yoho.yhorder.dal.IOrdersGoodsMapper;
import com.yoho.yhorder.dal.IOrdersMapper;
import com.yoho.yhorder.dal.OrdersCouponsMapper;
import com.yoho.yhorder.dal.domain.OrdersPrice;
import com.yoho.yhorder.dal.model.OrdersCoupons;
import com.yoho.yhorder.order.service.IOrdersCouponsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yoho on 2016/1/12.
 */
@Service
public class OrdersCouponsServiceImpl implements IOrdersCouponsService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IOrdersMapper ordersMapper;

    @Autowired
    private OrdersCouponsMapper ordersCouponsMapper;

    @Autowired
    private IOrdersGoodsMapper ordersGoodsMapper;

    @Override
    public List<OrdersCouponsBO> findOrdersCouponsBO(OrdersCouponsRequest ordersCouponsRequest) {
        if (ordersCouponsRequest.getUid() == null || CollectionUtils.isEmpty(ordersCouponsRequest.getCouponCodes())) {
            return Collections.emptyList();
        }
        List<OrdersCoupons> ordersCouponses = ordersCouponsMapper.selectByUidAndCouponsCodes(ordersCouponsRequest.getUid(), ordersCouponsRequest.getCouponCodes());
        if (ordersCouponses.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> orderIds = new ArrayList<>();
        for (OrdersCoupons ordersCoupons : ordersCouponses) {
            orderIds.add(ordersCoupons.getOrderId());
        }
        List<Orders> orderses = ordersMapper.selectByPrimaryKeys(orderIds);
        List<OrdersPrice> ordersPrices = ordersGoodsMapper.selectOrdersPriceByOrderIds(orderIds);
        List<OrdersCouponsBO> ordersCouponsBOs = new ArrayList<>();
        for (OrdersCoupons ordersCoupons : ordersCouponses) {
            OrdersCouponsBO ordersCouponsBO = new OrdersCouponsBO();
            ordersCouponsBO.setId(ordersCoupons.getId());
            ordersCouponsBO.setCreateTime(ordersCoupons.getCreateTime());
            ordersCouponsBO.setCouponsCode(ordersCoupons.getCouponsCode());
            ordersCouponsBO.setCouponsId(ordersCoupons.getCouponsId());
            ordersCouponsBO.setOrders(findOrdersById(ordersCoupons.getOrderId(), orderses));
            ordersCouponsBO.setOrdersPrice(findOrdersPriceById(ordersCoupons.getOrderId(), ordersPrices));
            ordersCouponsBOs.add(ordersCouponsBO);
        }
        return ordersCouponsBOs;
    }

    private Orders findOrdersById(Integer id, List<Orders> orderses) {
        for (Orders orders : orderses) {
            if (orders.getId().equals(id)) {
                return orders;
            }
        }
        logger.warn("can not find order[{}]", id);
        return null;
    }

    private double findOrdersPriceById(Integer id, List<OrdersPrice> ordersPrices) {
        for (OrdersPrice ordersPrice : ordersPrices) {
            if (ordersPrice.getId().equals(id)) {
                return ordersPrice.getPrice();
            }
        }
        return 0;
    }

}
