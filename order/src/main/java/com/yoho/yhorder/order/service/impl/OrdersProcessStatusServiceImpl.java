package com.yoho.yhorder.order.service.impl;

import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.response.Orders;
import com.yoho.yhorder.dal.IOrdersMetaDAO;
import com.yoho.yhorder.dal.domain.OrdersProcessStatus;
import com.yoho.yhorder.dal.model.OrdersMeta;
import com.yoho.yhorder.order.service.IOrdersProcessStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * qianjun 2016/6/13
 */
@Service
public class OrdersProcessStatusServiceImpl implements IOrdersProcessStatusService {

    private Logger logger = LoggerFactory.getLogger(OrdersProcessStatusServiceImpl.class);

    @Autowired
    private IOrdersMetaDAO ordersMetaDAO;

    @Override
    public OrdersProcessStatus select(Orders orders) {
        if (orders == null) {
            logger.warn("select fail , the orders is null");
            throw new ServiceException(ServiceError.ORDER_NOT_EXIST);
        }
        return select(Collections.singletonList(orders)).get(0);
    }

    @Override
    public List<OrdersProcessStatus> select(List<Orders> orderses) {
        if (orderses == null) {
            logger.warn("select fail , the orderses is null");
            throw new ServiceException(ServiceError.ORDER_NOT_EXIST);
        }
        if (orderses.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> ordersIds = new ArrayList<>();
        for (Orders orders : orderses) {
            ordersIds.add(orders.getId());
        }
        List<OrdersMeta> ordersMetas = ordersMetaDAO.selectByOrdersIdsAndMetaKey(ordersIds, OrdersMeta.PROCESS_STATUS);
        List<OrdersProcessStatus> ordersProcessStatuses = new ArrayList<>();
        for (Orders orders : orderses) {
            OrdersMeta ordersMeta = findOrdersMetaByOrdersId(orders.getId(), ordersMetas);
            OrdersProcessStatus ordersProcessStatus = new OrdersProcessStatus();
            ordersProcessStatus.setOrdersId(orders.getId());
            if (ordersMeta == null) {
                ordersProcessStatus.setValue(orders.getStatus().intValue());
            } else if (orders.getStatus().intValue() >= 4) {
                logger.info("OrdersMeta has existed , ordersProcessStatus value based on order status");
                ordersProcessStatus.setId(ordersMeta.getId());
                ordersProcessStatus.setValue(orders.getStatus().intValue());
            } else {
                ordersProcessStatus.setId(ordersMeta.getId());
                ordersProcessStatus.setValue(Integer.parseInt(ordersMeta.getMetaValue()));
            }
            ordersProcessStatuses.add(ordersProcessStatus);
        }
        return ordersProcessStatuses;
    }

    private OrdersMeta findOrdersMetaByOrdersId(Integer ordersId, List<OrdersMeta> ordersMetas) {
        for (OrdersMeta ordersMeta : ordersMetas) {
            if (ordersId.equals(ordersMeta.getOrdersId())) {
                return ordersMeta;
            }
        }
        return null;
    }


    @Override
    public void save(Orders orders, int status) {
        if (orders == null || orders.getId() == null) {
            logger.warn("save fail , the orders is null");
            throw new ServiceException(ServiceError.ORDER_NOT_EXIST);
        }
        OrdersMeta ordersMeta = ordersMetaDAO.selectByOrdersIdAndMetaKey(orders.getId(), OrdersMeta.PROCESS_STATUS);
        if (ordersMeta == null) {
            ordersMeta = new OrdersMeta();
            ordersMeta.setUid(orders.getUid());
            ordersMeta.setOrderCode(orders.getOrderCode());
            ordersMeta.setOrdersId(orders.getId());
            ordersMeta.setMetaKey(OrdersMeta.PROCESS_STATUS);
            ordersMeta.setMetaValue(String.valueOf(status));
            ordersMetaDAO.insert(ordersMeta);
        } else {
            ordersMeta.setMetaValue(String.valueOf(status));
            ordersMetaDAO.updateByPrimaryKey(ordersMeta);
        }
    }

}
