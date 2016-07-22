package com.yoho.yhorder.order.restapi;

import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.service.model.order.model.OrdersCouponsBO;
import com.yoho.service.model.order.request.OrdersCouponsRequest;
import com.yoho.yhorder.order.service.IOrdersCouponsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/orderscoupons")
@ServiceDesc(serviceName = "order")
public class OrdersCouponsController {

    Logger logger = LoggerFactory.getLogger(OrdersCouponsController.class);

    @Autowired
    private IOrdersCouponsService ordersCouponsService;


    @RequestMapping("/findOrdersCouponsBO")
    @ResponseBody
    public List<OrdersCouponsBO> findOrdersCouponsBO(@RequestBody OrdersCouponsRequest ordersCouponsRequest) {
        logger.debug("find orders coupons by {}.", ordersCouponsRequest);
        List<OrdersCouponsBO> ordersCouponsBOs = ordersCouponsService.findOrdersCouponsBO(ordersCouponsRequest);
        logger.debug("find orders coupons by {} success.", ordersCouponsRequest);
        return ordersCouponsBOs;
    }


}





















