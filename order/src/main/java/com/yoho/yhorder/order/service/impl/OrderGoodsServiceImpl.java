package com.yoho.yhorder.order.service.impl;


import com.yoho.service.model.order.model.UserOrdersGoodsStatBO;
import com.yoho.service.model.order.request.OrderGoodsRequest;
import com.yoho.service.model.order.request.OrderGoodsUidRequest;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.OrdersGoods;
import com.yoho.yhorder.common.page.Page;
import com.yoho.yhorder.common.utils.DateUtil;
import com.yoho.yhorder.dal.IOrdersGoodsMapper;
import com.yoho.yhorder.dal.IOrdersMapper;
import com.yoho.yhorder.dal.domain.ShareOrderGoodsInfo;
import com.yoho.yhorder.dal.domain.UserOrdersGoodsStat;
import com.yoho.yhorder.order.service.IOrderGoodsService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * OrderGoodsServiceImpl
 * 类描述
 *
 * @author zhangyonghui
 * @date 2015/11/4
 */
@Service
public class OrderGoodsServiceImpl implements IOrderGoodsService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private IOrdersMapper ordersMapper;

    @Autowired
    private IOrdersGoodsMapper ordersGoodsMapper;

    @Override
    public List<OrdersGoods> selectOrderGoodsByOrderId(List<Integer> orderIdList) {
        return ordersGoodsMapper.selectOrderGoodsByOrderId(orderIdList);
    }

    @Override
    public List<OrdersGoods> selectOrderByGoodsId(OrderGoodsUidRequest orderGoodsUidRequest) {
        return ordersGoodsMapper.selectOrderByGoodsId(orderGoodsUidRequest);
    }

    @Override
    public List<OrdersGoods> selectOrderGoodsByOrder(Orders order) {
        return ordersGoodsMapper.selectOrderGoodsByOrder(order);
    }

    @Override
    public List<OrdersGoods> findShippedOrdersGoodsWithinAMonth(List<Integer> uids) {
        if (CollectionUtils.isEmpty(uids)) {
            return Collections.emptyList();
        }
        logger.info("FindShippedOrdersGoodsWithinAMonth start, request uids is {}.", uids);
        int now = (int) (System.currentTimeMillis() / 1000);
        List<Orders> orderses = ordersMapper.selectByUidsAndStatusGreaterThanAndShipmentTimeBetween(uids, 3, now - 2592000, now - 86400);
        if (CollectionUtils.isEmpty(orderses)) {
            logger.info("can not find any orders for users {}.", uids);
            return Collections.emptyList();
        }
        List<Integer> orderIds = new ArrayList<>(orderses.size());
        for (Orders orders : orderses) {
            orderIds.add(orders.getId());
        }
        List<OrdersGoods> ordersGoodses = ordersGoodsMapper.selectByOrderIdInAndGoodsTypeIn(orderIds, Arrays.asList(1, 4));
        for (Orders orders : orderses) {
            for (OrdersGoods ordersGoods : ordersGoodses) {
                if (orders.getId().equals(ordersGoods.getOrderId())) {
                    ordersGoods.setOrderCode(orders.getOrderCode());
                }
            }
        }
        logger.info("FindShippedOrdersGoodsWithinAMonth success, find {} goods for users {}.", ordersGoodses.size(), uids);
        return ordersGoodses;
    }

    @Override
    public List<UserOrdersGoodsStatBO> statUserShippedOrdersGoods(List<Integer> uids) {
        if (CollectionUtils.isEmpty(uids)) {
            return Collections.emptyList();
        }
        logger.info("StatUserShippedOrdersGoods start, request uids is {}.", uids);
        int now = (int) (System.currentTimeMillis() / 1000);
        // 查询最多50个用户一天前已发货的商品总数
        List<UserOrdersGoodsStat> userShippedOrdersGoodsStats = ordersGoodsMapper.selectUserOrdersGoodsStatByUidsAndStatusGreaterThanAndShipmentTimeLessThan(uids, 3, now - 86400);
        List<UserOrdersGoodsStatBO> userOrdersGoodsStatBOs = new ArrayList<>();
        for (UserOrdersGoodsStat userShippedOrdersGoodsStat : userShippedOrdersGoodsStats) {
            UserOrdersGoodsStatBO userOrdersGoodsStatBO = new UserOrdersGoodsStatBO();
            userOrdersGoodsStatBO.setUid(userShippedOrdersGoodsStat.getUid());
            userOrdersGoodsStatBO.setTotal(userShippedOrdersGoodsStat.getTotal());
            userOrdersGoodsStatBOs.add(userOrdersGoodsStatBO);
        }
        logger.info("StatUserShippedOrdersGoods success, stat result is {} for users {}.", userOrdersGoodsStatBOs, uids);
        return userOrdersGoodsStatBOs;
    }

    @Override
    public List<ShareOrderGoodsInfo> selectOrderGoodsListByOrderCode(int uid,long orderCode) {
        return ordersGoodsMapper.selectOrderGoodsListByOrderCode(uid,orderCode);
    }

    @Override
    public List<ShareOrderGoodsInfo> selectOtherOrderGoodsList(int uid,long orderCode) {
        return ordersGoodsMapper.selectOtherOrderGoodsList(uid,orderCode);
    }

    @Override
    public List<ShareOrderGoodsInfo> selectAllOrderGoodsList(int uid) {
        return ordersGoodsMapper.selectAllOrderGoodsList(uid);
    }

    @Override
    public int selectToShareOrderNumByUid(String uid) {
        return ordersGoodsMapper.selectToShareOrderNumByUid(uid);
    }

    @Override
    public List<ShareOrderGoodsInfo> selectAllOrderGoodsListByUid(String uid) {
        return ordersGoodsMapper.selectAllOrderGoodsListByUid(uid);
    }
}
