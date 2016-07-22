package com.yoho.yhorder.order.mq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.message.YhMessageConsumer;
import com.yoho.yhorder.order.model.OrderChangeGoodsApplyErpRsp;
import com.yoho.yhorder.order.service.IChangeGoodsService;
import com.yoho.yhorder.order.service.IOrderCancelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 退货异步处理
 *
 * @author LiQZ on 2016/4/11.
 */
@Component
public class ErpCreateChangeOrderMessageConsumer implements YhMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger("mqConsumerLog");

    private static final String TOPIC = "erp.createChangeOrder";

    @Autowired
    private IChangeGoodsService changeGoodsService;

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
        try {
            logger.info("handle erp.createChangeOrder message, message is {}.", message);
            JSONObject erpResult = JSONObject.parseObject(String.valueOf(message));
            OrderChangeGoodsApplyErpRsp erpRsp = JSONObject.toJavaObject(erpResult.getJSONObject("data"), OrderChangeGoodsApplyErpRsp.class);
            if (erpResult.getIntValue("code") == 200) {
                changeGoodsService.asyncUpdateChangeGoodsApply(erpRsp);
            } else {
                logger.warn("erp.createChangeOrder fail, init_order_code is {} and message is {}", erpRsp.getInit_order_code(), message);
                changeGoodsService.asyncUpdateChangeGoodsApply(erpRsp);
                // 取消订单
                logger.warn("erp.createChangeOrder fail, init_order_code is {} to cancel order {}", erpRsp.getNew_order_code());
                orderCancelService.updateOrderStatus(erpRsp.getNew_order_code(), 901, 0, "");
            }
            logger.info("handle erp.createChangeOrder message success, message is {}.", message);
        } catch (Exception e) {
            logger.warn("handle order.createChangeOrder message fail, message is {}.", message, e);
        }
    }
}
