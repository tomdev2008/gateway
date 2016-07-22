package com.yoho.yhorder.order.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.message.YhMessageConsumer;
import com.yoho.yhorder.order.model.OrderChangeGoodsApplyErpRsp;
import com.yoho.yhorder.order.service.IChangeGoodsService;
import com.yoho.yhorder.order.service.IDeliveryAddressService;
import com.yoho.yhorder.order.service.IOrderCancelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 客服修改收货人地址
 *
 */
@Component
public class ErpDeliveryAddressUpdateMessageConsumer implements YhMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger("mqConsumerLog");

    private static final String TOPIC = "erp.deliveryAddressUpdate";

    @Autowired
    private IDeliveryAddressService deliveryAddressService;

    @Override
    public String getMessageTopic() {
        return TOPIC;
    }

    @Override
    @Database(ForceMaster = true)
    public void handleMessage(Object message) {
        try {
            logger.info("handle erp.deliveryAddressUpdate message, message is {}.", message);
            JSONArray erpResult = JSON.parseArray(String.valueOf(message));
            deliveryAddressService.updateBatchDeliveryAddress(erpResult);
            logger.info("handle erp.deliveryAddressUpdate message success, message is {}.", message);
        } catch (Exception e) {
            logger.warn("handle erp.deliveryAddressUpdate message fail, message is {}.", message, e);
        }
    }
}
