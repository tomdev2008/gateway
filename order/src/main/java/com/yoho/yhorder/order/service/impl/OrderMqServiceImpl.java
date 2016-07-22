/*
 * Copyright (C), 2016-2016, yoho
 * FileName: OrderMqServiceImpl.java
 * Author:   god_liu
 * Date:     2016年4月22日 下午2:27:49
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.message.YhProducerTemplate;
import com.yoho.yhorder.common.message.WechatProducerTemplate;
import com.yoho.yhorder.order.service.IOrderMqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 前台mq-service  发送者实现类
 * @author maelk_liu
 */
@Service
public class OrderMqServiceImpl implements IOrderMqService{
    
    public final static String SEND_ORDER_CONFIRM_COUPON="send_order_confirm_coupon";
    
    public final static String SEND_WECHAT="wechat.send_wechat_msg";
    
    public final static String CHANGE_REFUND_CANCEL="order.changeRefundCancel";
    
    /**
     * 7天发券     单位：分钟
     */
    public  static int delay_send_coupon_times=  7*24*60;
    
    private Logger mqProducerLog = LoggerFactory.getLogger("mqProducerLog");

    @Resource
    private YhProducerTemplate producerTemplate;
    
    @Resource
    private WechatProducerTemplate wechatProducerTemplate;
    
    /**
     * 7天首单自动发送优惠券
     * 
     * @param request
     */
    public void sendOrderConfirmCoupon(JSONObject request) {
        try {
            producerTemplate.send(SEND_ORDER_CONFIRM_COUPON, request,null,delay_send_coupon_times);
            mqProducerLog.info("send send_order_confirm_coupon message to mq success, topic: {}, message: {}", SEND_ORDER_CONFIRM_COUPON, request);
        } catch (Exception e) {
            mqProducerLog.warn("send send_order_confirm_coupon message to mq fail, topic: {},  message is: {}", SEND_ORDER_CONFIRM_COUPON, request, e);
        }
    }

    /**
     * 发送给微信,推送消息
     */
    @Override
    public void sendWechatPushMessage(JSONObject request) {
        try {
            wechatProducerTemplate.send(SEND_WECHAT, request);
            mqProducerLog.info("send wechat message to mq success, topic: {}, message: {}", SEND_WECHAT, request);
        } catch (Exception e) {
            mqProducerLog.warn("send wechat message to mq fail, topic: {},  message is: {}", SEND_WECHAT, request, e);
        }
    }
    
    /**
     * 功能描述: <br>取消退换货申请--用户主动发起的
     * 〈功能详细描述〉
     *
     * @param request
     */
    @Override
    public void sendChangeRefundCancelMessage(JSONObject request) {
        try {
            producerTemplate.send(CHANGE_REFUND_CANCEL, request);
            mqProducerLog.info("sendChangeRefundCancelMessage  to mq success, topic: {}, message: {}", CHANGE_REFUND_CANCEL, request);
        } catch (Exception e) {
            mqProducerLog.warn("sendChangeRefundCancelMessage  to mq fail, topic: {},  message is: {}", CHANGE_REFUND_CANCEL, request, e);
        }
    }
}
