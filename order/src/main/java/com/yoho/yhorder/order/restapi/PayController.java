/*
 * Copyright (C), 2016-2016, yoho
 * FileName: PayController.java
 * Author:   god_liu
 * Date:     2016年5月25日 下午2:12:11
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.order.restapi;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.service.model.order.request.RefundGoodsListRequest;
import com.yoho.service.model.order.response.Orders;
import com.yoho.yhorder.order.payment.service.PaymentService;

/**
 * 支付服务
 *
 * @author maelk_liu
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
@Controller
@RequestMapping("/paymentInfo")
@ServiceDesc(serviceName = "order")
public class PayController {
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * 取消订单前获取支付查询结果
     */
    @RequestMapping("/queryTradeStatus")
    @ResponseBody
    public boolean queryTradeStatus(@RequestBody Orders orders){
        return paymentService.queryTradeStatus(orders);
    }

}
