/*
 * Copyright (C), 2016-2016, yoho
 * FileName: ShoppingMqServiceImpl.java
 * Author:   god_liu
 * Date:     2016年5月5日 下午1:53:47
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.shopping.service.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.message.YhProducerTemplate;
import com.yoho.yhorder.shopping.service.IShoppingMqService;

/**
 * shopping-mqService实现类
 *
 * @author maelk_liu
 */
@Component
public class ShoppingMqServiceImpl implements IShoppingMqService{
    
    public final static String topic="order_autoCancel";
    
    /**
     * 两小时自动取消时间  单位:分钟
     */
    public  static int delay_auto_cancel_times=  2*60;
    
    private Logger mqProducerLog = LoggerFactory.getLogger("mqProducerLog");

    @Resource
    private YhProducerTemplate producerTemplate;

    @Override
    public void autoCancelOrder(JSONObject request) {
        try {
            producerTemplate.send(topic, request,null,delay_auto_cancel_times);
            mqProducerLog.info("send autoCancelOrder message to mq success, topic: {}, message: {}", topic, request);
        } catch (Exception e) {
            mqProducerLog.warn("send autoCancelOrder message to mq fail, topic: {},  message is: {}", topic, request, e);
        }
    }

}
