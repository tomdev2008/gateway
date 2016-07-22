package com.yoho.yhorder.order.restapi;


import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.service.model.order.request.OrderPromotionInfoReq;
import com.yoho.service.model.order.response.OrderPromotionInfoBo;
import com.yoho.yhorder.order.service.IOrderPromotionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orderPromotion")
@ServiceDesc(serviceName = "order")
public class OrderPromotionController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IOrderPromotionService iOrderPromotionService;

    /**
     * 获取订单中优惠券信息
     * param：OrderGoodsRequest
     *
     * @param orderPromotionInfoReq
     * @return
     */

    @RequestMapping("/getOrdPromotionByUserInfo")
    @ResponseBody
    public List<OrderPromotionInfoBo> getOrderPromotion(@RequestBody OrderPromotionInfoReq orderPromotionInfoReq) {
        List<OrderPromotionInfoBo> orderPromotionInfoBoList = new ArrayList<OrderPromotionInfoBo>();
        orderPromotionInfoBoList = iOrderPromotionService.selectOrdPromotionListByUserInfo(orderPromotionInfoReq);
        return orderPromotionInfoBoList;

    }


}
