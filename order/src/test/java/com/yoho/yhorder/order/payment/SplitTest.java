package com.yoho.yhorder.order.payment;

import com.yoho.service.model.order.response.Orders;
import com.yoho.yhorder.order.model.OrderAmountDetail;
import com.yoho.yhorder.order.model.OrderWrapper;
import com.yoho.yhorder.order.service.impl.OrderSplitServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wujiexiang on 16/5/25.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/spring-mybatis-datasource.xml",
        "file:src/test/resources/META-INF/spring/spring-test-context.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class SplitTest {

    //@Resource(name="orderSplitServiceImpl")
    OrderSplitServiceImpl orderSplitServiceImpl;

    @Test
    public void test() {

        orderSplitServiceImpl = new OrderSplitServiceImpl();
        Orders parentOrder = new Orders();
        parentOrder.setYohoCoinNum(31398);
        parentOrder.setShippingCost(BigDecimal.valueOf(15));
        parentOrder.setAmount(BigDecimal.valueOf(0));

        List<OrderWrapper> orderList = new ArrayList<>();

        OrderWrapper orderWrapper = new OrderWrapper();
        orderWrapper.setOrderCode(1);
        Orders order = new Orders();
        order.setOrderCode(1L);

        OrderAmountDetail orderAmountDetail = new OrderAmountDetail();
        orderAmountDetail.setLastOrderAmount(139.3);
        orderAmountDetail.setOrderAmount(209.0);
        orderAmountDetail.setCouponsCutAmount(46.58);
        orderAmountDetail.setPromotionCutAmount(23.12);
        orderWrapper.setOrderCode(1);
        orderWrapper.setOrder(order);
        orderWrapper.setOrderAmoutDetail(orderAmountDetail);

        orderList.add(orderWrapper);

        /**
         *  com.yoho.yhorder.order.model.OrderAmountDetail@78294f18[couponsCutAmount=1.65,lastOrderAmount=1.95,orderAmount=4.0,promotionCutAmount=0.0,promotioncodeCutAmount=0.0,shippingCost=0.0,redenvelopesCutAmount=0.0,yohCoinCutAmount=0.0,vipCutAmount=0.4,shoppingOrigCost=0.0,yohoCoinCutNum=0,getYohoCoinNum=0]
         */

        orderWrapper = new OrderWrapper();
        orderWrapper.setOrderCode(2);
         order = new Orders();
        order.setOrderCode(2l);

        orderAmountDetail = new OrderAmountDetail();
        orderAmountDetail.setLastOrderAmount(3.75);
        orderAmountDetail.setOrderAmount(671.0);
        orderAmountDetail.setCouponsCutAmount(1.25);
        orderAmountDetail.setPromotionCutAmount(666.0);
        orderWrapper.setOrderAmoutDetail(orderAmountDetail);

        orderWrapper.setOrder(order);

        orderList.add(orderWrapper);


        orderWrapper = new OrderWrapper();
        orderWrapper.setOrderCode(3);
         order = new Orders();
        order.setOrderCode(3L);


        /**
         * com.yoho.yhorder.order.model.OrderAmountDetail@6925fa79[couponsCutAmount=5.06,lastOrderAmount=5.94,orderAmount=11.0,promotionCutAmount=0.0,promotioncodeCutAmount=0.0,shippingCost=0.0,redenvelopesCutAmount=0.0,yohCoinCutAmount=0.0,vipCutAmount=0.0,shoppingOrigCost=0.0,yohoCoinCutNum=0,getYohoCoinNum=0]
         */
        orderAmountDetail = new OrderAmountDetail();
        orderAmountDetail.setLastOrderAmount(155.96);
        orderAmountDetail.setOrderAmount(234.0);
        orderAmountDetail.setCouponsCutAmount(52.14);
        orderAmountDetail.setPromotionCutAmount(25.9);
        orderWrapper.setOrderAmoutDetail(orderAmountDetail);
        orderWrapper.setOrder(order);

        orderList.add(orderWrapper);

        //orderSplitServiceImpl.splitRedenvelopes(orderList,parentOrder);
//        orderSplitServiceImpl.splitOrderShippingCost(orderList,parentOrder.getShippingCost().doubleValue());
//        orderSplitServiceImpl.splitYohoCoin(orderList,parentOrder);
//        orderSplitServiceImpl.adjustAllSubOrderAmount(orderList,parentOrder);
    }
}
