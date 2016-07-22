package com.yoho.yhorder.order.service;


import com.yoho.service.model.order.model.UserOrdersGoodsStatBO;
import com.yoho.service.model.order.request.OrderGoodsRequest;
import com.yoho.service.model.order.request.OrderGoodsUidRequest;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.OrdersGoods;
import com.yoho.yhorder.common.page.Page;
import com.yoho.yhorder.dal.domain.ShareOrderGoodsInfo;

import java.util.List;
import java.util.Map;

public interface IOrderGoodsService {

    /**
     * 查询订单中所有商品信息
     *
     * @param orderIdList
     * @return
     */
    List<OrdersGoods> selectOrderGoodsByOrderId(List<Integer> orderIdList);


    /**
     * 查询订单中某一商品的信息
     *
     * @param orderGoodsUidRequest
     * @return
     */
    List<OrdersGoods> selectOrderByGoodsId(OrderGoodsUidRequest orderGoodsUidRequest);

    /**
     * 查询订单中所有商品信息
     *
     * @param order
     * @return
     */
    List<OrdersGoods> selectOrderGoodsByOrder(Orders order);

    /**
     * 查询用户一个月内已发货的商品列表
     *
     * @param uids
     * @return
     */
    List<OrdersGoods> findShippedOrdersGoodsWithinAMonth(List<Integer> uids);

    /**
     * 统计用户一天前已发货的商品总数
     *
     * @param uids
     * @return
     */
    List<UserOrdersGoodsStatBO> statUserShippedOrdersGoods(List<Integer> uids);

    List<ShareOrderGoodsInfo> selectOrderGoodsListByOrderCode(int uid,long orderCode);

    List<ShareOrderGoodsInfo> selectOtherOrderGoodsList(int uid,long orderCode);

    List<ShareOrderGoodsInfo> selectAllOrderGoodsList(int uid);

    int selectToShareOrderNumByUid(String uid);

    List<ShareOrderGoodsInfo> selectAllOrderGoodsListByUid(String uid);
}
