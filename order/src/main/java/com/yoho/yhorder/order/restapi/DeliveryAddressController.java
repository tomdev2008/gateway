package com.yoho.yhorder.order.restapi;

import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.service.model.order.request.DeliveryAddressRequest;
import com.yoho.yhorder.order.service.IDeliveryAddressService;
import com.yoho.yhorder.order.service.IYohoOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * qianjun 2016/6/14
 */
@Controller
@RequestMapping("/deliveryAddressInfo")
@ServiceDesc(serviceName = "order")
public class DeliveryAddressController {
    private Logger logger = LoggerFactory.getLogger(DeliveryAddressController.class);

    @Autowired
    private IDeliveryAddressService deliveryAddressService;
    /**
     * 修改订单收货地址
     */
    @RequestMapping("/updateDeliveryAddress")
    @ResponseBody
    @Database(ForceMaster = true)
    public void updateDeliveryAddress(@RequestBody DeliveryAddressRequest deliveryAddressRequest) {
        logger.info("updateDeliveryAddress by orderCode {} and addressId is {}", deliveryAddressRequest.getOrderCode(), deliveryAddressRequest.getAddressId());
        deliveryAddressService.updateDeliveryAddress(deliveryAddressRequest);
        logger.info("updateDeliveryAddress by orderCode {} and addressId is {} success.", deliveryAddressRequest.getOrderCode(), deliveryAddressRequest.getAddressId());
    }

}
