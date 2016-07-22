package com.yoho.yhorder.order.restapi;


import com.google.common.collect.Lists;
import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.product.model.GoodsBo;
import com.yoho.product.model.ProductBo;
import com.yoho.service.model.order.model.UserOrdersGoodsStatBO;
import com.yoho.service.model.order.request.OrderGoodsRequest;
import com.yoho.service.model.order.request.OrderGoodsUidRequest;
import com.yoho.service.model.order.request.OrderRequest;
import com.yoho.service.model.order.response.OrdersGoods;
import com.yoho.yhorder.common.page.Page;
import com.yoho.yhorder.dal.domain.ShareOrderGoodsInfo;
import com.yoho.yhorder.order.service.IOrderGoodsService;
import com.yoho.yhorder.order.service.impl.ThirdModelServiceImpl;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/orderGoods")
@ServiceDesc(serviceName = "order")
public class OrderGoodsController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IOrderGoodsService orderGoodsService;

    @Autowired
    private ThirdModelServiceImpl thirdModelService;

    /**
     * 查询用户一个月内已发货的商品列表
     *
     * @param uids
     * @return
     */
    @RequestMapping("/findShippedOrdersGoodsWithinAMonth")
    @ResponseBody
    public List<OrdersGoods> findShippedOrdersGoodsWithinAMonth(@RequestBody List<Integer> uids) {
        return orderGoodsService.findShippedOrdersGoodsWithinAMonth(uids);
    }

    /**
     * 统计用户一天前已发货的商品总数
     *
     * @param uids
     * @return
     */
    @RequestMapping("/statUserShippedOrdersGoods")
    @ResponseBody
    public List<UserOrdersGoodsStatBO> statUserShippedOrdersGoods(@RequestBody List<Integer> uids) {
        return orderGoodsService.statUserShippedOrdersGoods(uids);
    }

    @RequestMapping("/selectOrderGoodsListByOrderCode")
    @ResponseBody
    public List<ShareOrderGoodsInfo> selectOrderGoodsListByOrderCode(@RequestBody OrderRequest request) {
        return orderGoodsService.selectOrderGoodsListByOrderCode(request.getUid(),request.getOrderCode());
    }

    @RequestMapping("/selectOtherOrderGoodsList")
    @ResponseBody
    public List<ShareOrderGoodsInfo> selectOtherOrderGoodsList(@RequestBody OrderRequest request) {
        return orderGoodsService.selectOtherOrderGoodsList(request.getUid(),request.getOrderCode());
    }

    @RequestMapping("/selectAllOrderGoodsList")
    @ResponseBody
    public List<ShareOrderGoodsInfo> selectAllOrderGoodsList(@RequestBody OrderRequest request) {
        return orderGoodsService.selectAllOrderGoodsList(request.getUid());
    }

    @RequestMapping("/selectToShareOrderNumByUid")
    @ResponseBody
    public int selectToShareOrderNumByUid(@RequestBody String uid) {
        return orderGoodsService.selectToShareOrderNumByUid(uid);
    }

    @RequestMapping("/selectAllOrderGoodsListByUid")
    @ResponseBody
    public List<ShareOrderGoodsInfo> selectAllOrderGoodsListByUid(@RequestBody String uid) {
        return orderGoodsService.selectAllOrderGoodsListByUid(uid);
    }

    @RequestMapping("/selectOrderByGoodsId")
    @ResponseBody
    public List<OrdersGoods> selectOrderByGoodsId(@RequestBody OrderGoodsUidRequest orderGoodsUidRequest) {
        if (CollectionUtils.isEmpty(orderGoodsUidRequest.getOrderGoodsRequestList()) || orderGoodsUidRequest.getUid() <=0) {
            return Lists.newArrayList();
        }
        List<OrdersGoods> orderGoodsList = orderGoodsService.selectOrderByGoodsId(orderGoodsUidRequest);
        return orderGoodsList;
    }

}
