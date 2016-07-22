package com.yoho.yhorder.order.mq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.message.YhMessageConsumer;
import com.yoho.yhorder.order.service.IOrderCancelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by LUOXC on 2016/3/31.
 */
@Component
public class OrderErpStatusMessageConsumer implements YhMessageConsumer {

    private final Logger logger = LoggerFactory.getLogger("mqConsumerLog");

    private final String TOPIC = "order.erpstatus";

    @Autowired
    private IOrderCancelService orderCancelService;

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
        logger.info("begin handle order.erpstatus message, message is {}.", message);
        try {
            JSONObject msg = JSONObject.parseObject(String.valueOf(message));
            //已交寄的订单格式：{ "order_code":100000, "status":600, "data":{"express_id": 23,"express_number":"25276134639"} } 
            //其余格式:：{ "order_code":100000, "status":700}  status: /906自动取消    /901客服取消
            JSONObject data = msg.getJSONObject("data");
            if (data == null) {
                orderCancelService.updateOrderStatus(msg.getLong("order_code"), msg.getInteger("status"), 0, "");
            } else {
                orderCancelService.updateOrderStatus(msg.getLong("order_code"), msg.getInteger("status"), data.getIntValue("express_id"), data.getString("express_number"));
            }
            logger.info("handle order.erpstatus message success, message is {}.", message);
        } catch (Exception e) {
            logger.info("handle order.erpstatus message fail, message is {}.", message, e);
        }
    }
}
