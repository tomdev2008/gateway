/*
 * Copyright (C), 2016-2016, yoho
 * FileName: ErpSyncOrderRefundStatusMsgConsumer.java
 * Author:   maelk_liu
 * Date:     2016年4月11日 下午5:47:07
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.order.mq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.message.YhMessageConsumer;
import com.yoho.yhorder.order.service.IRefundService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 退货订单处理 ：erp端发来的mq请求
 *
 * @author maelk_liu
 */
@Component
public class ErpCreateRefundOrderMessageConsumer implements YhMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger("mqConsumerLog");

    private static final String TOPIC = "erp.createRefundOrder";

    @Autowired
    private IRefundService refundService;

    @Override
    public String getMessageTopic() {
        return TOPIC;
    }

    /**
     * @param message
     * @see <a href="http://git.dev.yoho.cn/yoho-documents/api-interfaces/blob/master/%E8%AE%A2%E5%8D%95/%E8%AE%A2%E5%8D%95%E5%89%8D%E5%90%8E%E5%8F%B0%E5%88%86%E7%A6%BB.md">订单前后台分离</a>
     */
    @Override
    @Database(ForceMaster = true)
    public void handleMessage(Object message) {
        logger.info("begin handle erp.createRefundOrder message, message is {}.", message);
        try {
            JSONObject msg = JSONObject.parseObject(String.valueOf(message));
            if (msg.getIntValue("code") == 200) {
                JSONObject data = msg.getJSONObject("data");
                refundService.syncRefundStatus(data.getIntValue("id"),
                        data.getIntValue("returned_id"),
                        data.getInteger("returned_status"),
                        data.getDoubleValue("real_returned_amount"),
                        data.getString("is_return_coupon"),
                        data.getInteger("return_yoho_coin"));
            } else {
                JSONObject data = msg.getJSONObject("data");
                logger.warn("erp.createRefundOrder fail, order_returned_id is {} and message is {}", data.getIntValue("order_returned_id"), msg);
                refundService.syncRefundStatus(data.getIntValue("id"), data.getIntValue("returned_id"), data.getInteger("returned_status"), 0, "N", 0);
            }
            logger.info("handle erp.createRefundOrder message success, message is {}.", message);
        } catch (Exception e) {
            logger.warn("handle erp.createRefundOrder message fail, message is {}.", message, e);
        }
    }

}
