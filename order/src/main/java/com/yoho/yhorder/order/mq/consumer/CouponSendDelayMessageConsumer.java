/*
 * Copyright (C), 2016-2016, yoho
 * FileName: AutoSendCouponWithFirstOrderConsumer.java
 * Author:   god_liu
 * Date:     2016年4月22日 下午2:34:11
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.order.mq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.message.YhDelayMessageConsumer;
import com.yoho.yhorder.dal.model.CouponSend;
import com.yoho.yhorder.order.service.CouponSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 首单发送优惠券
 *
 * @author maelk_liu
 */
@Component
public class CouponSendDelayMessageConsumer implements YhDelayMessageConsumer {

    private final Logger logger = LoggerFactory.getLogger("mqConsumerLog");

    private final String topic = "send_order_confirm_coupon";

    @Autowired
    private CouponSendService couponSendService;


    @Override
    public int getDelayInMinutes() {
        // 七天
        return 7 * 24 * 60;
    }

    @Override
    public String getMessageTopic() {
        return topic;
    }

    @Override
    public void handleMessage(Object message) {
        logger.info("begin handle order_auto_send_coupon message, message is {}.", message);
        try {
            JSONObject js = JSONObject.parseObject(message.toString());
            Integer uid = js.getInteger("uid");
            Long orderCode = js.getLong("orderCode");
            CouponSend couponSend = new CouponSend();
            couponSend.setUid(uid);
            couponSend.setOrderCode(orderCode);
            couponSendService.sendOrderConfirmCoupon(couponSend);
        } catch (Exception e) {
            logger.warn("handle order_auto_send_coupon message fail, message is {} ", message);
            return;
        }
        logger.info("handle order_auto_send_coupon message success, message is {}.", message);
    }

}
