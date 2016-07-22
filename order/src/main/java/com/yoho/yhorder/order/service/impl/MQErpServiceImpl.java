package com.yoho.yhorder.order.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.message.YhProducerTemplate;
import com.yoho.yhorder.order.service.IErpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author LiQZ on 2016/3/16.
 */
@Service("mqErpService")
public class MQErpServiceImpl implements IErpService {

    public static final String SET_CHANGE_REFUND_EXPRESS_TOPIC = "order.updateChangeRefundOrderExpressInfo";

    public static final String ORDER_CANCEL_TOPIC = "order.orderStatus";

    public static final String CREATE_CHANGE_ORDER = "order.createChangeOrder";

    public static final String ORDER_REFUND_TOPIC = "order.createRefundOrder";

    public final static String ORDER_SUBMIT_TOPIC = "order.submit";

    public final static String ORDER_DELIVERY_ADDRESS_TOPIC = "order.deliveryAddress";

    private Logger orderCloseLogger = LoggerFactory.getLogger("orderCloseLog");

    private Logger mqProducerLog = LoggerFactory.getLogger("mqProducerLog");

    @Resource
    private YhProducerTemplate producerTemplate;

    @Override
    public void cancelOrder(JSONObject request) {
        try {
            producerTemplate.send(ORDER_CANCEL_TOPIC, request);
            orderCloseLogger.info("send cancel order message to mq success, order {}.", request.getString("orderCode"));
            mqProducerLog.info("send order.orderStatus message success, message is {}.", request.toJSONString());
        } catch (Exception e) {
            orderCloseLogger.warn("send cancel order message to mq fail, order {}.", request.getString("orderCode"), e);
            mqProducerLog.warn("send order.orderStatus message fail, message is {}.", request.toJSONString(), e);
        }
    }

    @Override
    public void confirmOrder(JSONObject request, Map<String, Object> map) {
        try {
            producerTemplate.send(ORDER_CANCEL_TOPIC, request, map);
            mqProducerLog.info("send order.orderStatus message success, message is {}.", request.toJSONString());
        } catch (Exception e) {
            mqProducerLog.warn("send order.orderStatus message fail, message is {}.", request.toJSONString(), e);
        }
    }

    @Override
    public JSONObject refundGoods(JSONObject request) {
        JSONObject sendResult = new JSONObject();
        try {
            producerTemplate.send(ORDER_REFUND_TOPIC, request);
            mqProducerLog.info("send refund order message to mq success, topic: {}, message: {}", ORDER_REFUND_TOPIC, request.toJSONString());
            sendResult.put("sendResult", "success");
        } catch (Exception e) {
            mqProducerLog.warn("send refund order message to mq fail, topic: {},  message is: {}", ORDER_REFUND_TOPIC, request.toJSONString(), e);
            sendResult.put("sendResult", "fail");
        }
        return sendResult;
    }

    @Override
    public void setRefundExpressData(JSONObject request) {
        try {
            producerTemplate.send(SET_CHANGE_REFUND_EXPRESS_TOPIC, request);
            mqProducerLog.info("send updateRefundOrderExpressInfo message to mq success, topic: {}, message: {}", SET_CHANGE_REFUND_EXPRESS_TOPIC, request.toJSONString());
        } catch (Exception e) {
            mqProducerLog.warn("send updateRefundOrderExpressInfo message to mq fail, topic: {}, message: {}", SET_CHANGE_REFUND_EXPRESS_TOPIC, request.toJSONString());
            //throw new ServiceException(ServiceError.ORDER_REFUND_EXPRESS_DATA_FAIL);
        }
    }

    @Override
    public JSONObject createChangeOrder(JSONObject request) {
        try {
            producerTemplate.send(CREATE_CHANGE_ORDER, request);
            mqProducerLog.info("send createChangeOrder message to mq success, topic: {}, message: {}", CREATE_CHANGE_ORDER, request.toJSONString());
        } catch (Exception e) {
            mqProducerLog.warn("send createChangeOrder message to mq fail, topic: {}, message: {}", CREATE_CHANGE_ORDER, request.toJSONString());
        }
        return null;
    }

    @Override
    public void updateChangeOrderExpressInfo(Map<String, Object> express) {
        try {
            producerTemplate.send(SET_CHANGE_REFUND_EXPRESS_TOPIC, express);
            mqProducerLog.info("send updateChangeOrderExpressInfo message to mq success, topic: {}, message: {}", SET_CHANGE_REFUND_EXPRESS_TOPIC, JSONObject.toJSONString(express));
        } catch (Exception e) {
            mqProducerLog.warn("send updateChangeOrderExpressInfo message to mq fail, topic: {}, message: {}", SET_CHANGE_REFUND_EXPRESS_TOPIC, JSONObject.toJSONString(express));
        }
    }

    /**
     * 修改订单收货地址
     */
    @Override
    public void updateDeliveryAddress(JSONArray request) {
        if (request == null || request.isEmpty()) {
            return;
        }
        try {
            producerTemplate.send(ORDER_DELIVERY_ADDRESS_TOPIC, request);
            mqProducerLog.info("send order.deliveryAddress message success, topic: {}, message: {}.", ORDER_DELIVERY_ADDRESS_TOPIC, request.toJSONString());
        } catch (Exception e) {
            mqProducerLog.warn("send order.deliveryAddress message fail,  topic: {}, message: {}.", ORDER_DELIVERY_ADDRESS_TOPIC, request.toJSONString());
        }
    }
}
