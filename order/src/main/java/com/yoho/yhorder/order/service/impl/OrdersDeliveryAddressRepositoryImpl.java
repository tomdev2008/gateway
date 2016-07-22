package com.yoho.yhorder.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.response.Orders;
import com.yoho.yhorder.dal.IOrdersMetaDAO;
import com.yoho.yhorder.dal.domain.DeliveryAddress;
import com.yoho.yhorder.dal.model.OrdersMeta;
import com.yoho.yhorder.order.service.IOrdersDeliveryAddressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yoho on 2016/6/3.
 */
@Repository
public class OrdersDeliveryAddressRepositoryImpl implements IOrdersDeliveryAddressRepository {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IOrdersMetaDAO ordersMetaDAO;

    @Override
    public DeliveryAddress select(Orders orders) {
        if (orders == null) {
            logger.warn("select fail , the order is null");
            throw new ServiceException(ServiceError.ORDER_NOT_EXIST);
        }
        OrdersMeta ordersMeta = ordersMetaDAO.selectByOrdersIdAndMetaKey(orders.getId(), OrdersMeta.DELIVERY_ADDRESS);
        if (ordersMeta != null) {
            try {
                DeliveryAddress deliveryAddress = JSONObject.parseObject(ordersMeta.getMetaValue(), DeliveryAddress.class);
                return deliveryAddress;
            } catch (Exception e) {
                // 删除垃圾数据
                DeliveryAddress deliveryAddress = new DeliveryAddress();
                deliveryAddress.setAddressId(0);
                deliveryAddress.setDeliveryAddressUpdateTimes(0);
                deliveryAddress.setOrderCode(orders.getOrderCode());
                update(orders, deliveryAddress);
                return deliveryAddress;
            }
        } else {
            DeliveryAddress deliveryAddress = new DeliveryAddress();
            deliveryAddress.setAddressId(0);
            deliveryAddress.setDeliveryAddressUpdateTimes(0);
            deliveryAddress.setOrderCode(orders.getOrderCode());
            ordersMeta = new OrdersMeta();
            ordersMeta.setUid(orders.getUid());
            ordersMeta.setOrderCode(orders.getOrderCode());
            ordersMeta.setOrdersId(orders.getId());
            ordersMeta.setMetaKey(OrdersMeta.DELIVERY_ADDRESS);
            ordersMeta.setMetaValue(JSONObject.toJSONString(deliveryAddress));
            ordersMetaDAO.insert(ordersMeta);
            return deliveryAddress;
        }
    }


    public void update(Orders orders, DeliveryAddress deliveryAddress) {
        if (orders == null) {
            logger.warn("update fail , the order is null");
            throw new ServiceException(ServiceError.ORDER_NOT_EXIST);
        }
        if (deliveryAddress == null) {
            logger.warn("update fail , the deliveryAddress is null");
            throw new ServiceException(ServiceError.ORDER_DELIVERY_ADDRESSID_IS_EMPTY);
        }
        update(Collections.singletonList(orders), deliveryAddress);
    }


    public void update(List<Orders> orderses, DeliveryAddress deliveryAddress) {
        if (orderses == null) {
            logger.warn("update fail , the orderses is null");
            throw new ServiceException(ServiceError.ORDER_NOT_EXIST);
        }
        if (deliveryAddress == null) {
            logger.warn("update fail , the deliveryAddress is null");
            throw new ServiceException(ServiceError.ORDER_DELIVERY_ADDRESSID_IS_EMPTY);
        }
        if (orderses.isEmpty()) {
            return;
        }
        List<Integer> ordersIds = new ArrayList<>();
        for (Orders orders : orderses) {
            ordersIds.add(orders.getId());
        }
        ordersMetaDAO.updateMetaValueByOrdersIdsAndMetaKey(ordersIds, OrdersMeta.DELIVERY_ADDRESS, JSONObject.toJSONString(deliveryAddress));
    }

    @Override
    public void insert(List<Orders> orderses, DeliveryAddress deliveryAddress) {
        if (orderses == null) {
            logger.warn("insert fail , the orderses is null");
            throw new ServiceException(ServiceError.ORDER_NOT_EXIST);
        }
        if (deliveryAddress == null) {
            logger.warn("insert fail , the deliveryAddress is null");
            throw new ServiceException(ServiceError.ORDER_DELIVERY_ADDRESSID_IS_EMPTY);
        }
        if (orderses.isEmpty()) {
            return;
        }
        List<OrdersMeta> ordersMetas = new ArrayList<>();
        for (Orders orders : orderses) {
            OrdersMeta ordersMeta = new OrdersMeta();
            ordersMeta.setUid(orders.getUid());
            ordersMeta.setOrderCode(orders.getOrderCode());
            ordersMeta.setOrdersId(orders.getId());
            ordersMeta.setMetaKey(OrdersMeta.DELIVERY_ADDRESS);
            ordersMeta.setMetaValue(JSONObject.toJSONString(deliveryAddress));
            ordersMetas.add(ordersMeta);
        }
        ordersMetaDAO.insertBatch(ordersMetas);
    }

}
