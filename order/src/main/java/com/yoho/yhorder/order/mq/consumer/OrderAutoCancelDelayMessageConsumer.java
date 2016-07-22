/*
 * Copyright (C), 2016-2016, yoho
 * FileName: AutoCancelOrderConsumer.java
 * Author:   god_liu
 * Date:     2016年4月19日 下午7:18:50
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.order.mq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.message.YhDelayMessageConsumer;
import com.yoho.yhorder.order.service.IOrderCancelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 自动取消订单consumer
 *
 * @author maelk_liu
 */
@Component
public class OrderAutoCancelDelayMessageConsumer implements YhDelayMessageConsumer {

    private final Logger logger = LoggerFactory.getLogger("mqConsumerLog");

    private final String topic = "order_autoCancel";

    @Autowired
    private IOrderCancelService orderCancelService;

    @Override
    public int getDelayInMinutes() {
        // 2小时为120分钟
        return 120;
    }

    @Override
    public String getMessageTopic() {
        return topic;
    }

    @Override
    public void handleMessage(Object message) {
        logger.info("handle order_autoCancel message, message is {}.", message);
        try {
            JSONObject js = JSONObject.parseObject(message.toString());
            long orderCode = js.getLongValue("orderCode");
            orderCancelService.cancelBySystemAuto(orderCode);
            logger.info("handle order_autoCancel message success, message is {}.", message);
        } catch (Exception e) {
            logger.warn("handle order_autoCancel message fail, message is {} ", message);
        }
    }

}
