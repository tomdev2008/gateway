package com.yoho.yhorder.order.model;

import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.OrdersGoods;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.List;

/**
 * Created by wujiexiang on 16/4/25.
 * 子订单
 */
public class OrderWrapper {
    private long orderCode;
    //订单信息
    private Orders order;

    //订单对应的商品列表
    private List<OrdersGoods> goodsList;

    //订单金额明细
    private OrderAmountDetail orderAmoutDetail;


    public long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(long orderCode) {
        this.orderCode = orderCode;
    }

    public Orders getOrder() {
        return order;
    }

    public void setOrder(Orders order) {
        this.order = order;
    }

    public List<OrdersGoods> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<OrdersGoods> goodsList) {
        this.goodsList = goodsList;
    }

    public OrderAmountDetail getOrderAmoutDetail() {
        return orderAmoutDetail;
    }

    public void setOrderAmoutDetail(OrderAmountDetail orderAmoutDetail) {
        this.orderAmoutDetail = orderAmoutDetail;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
