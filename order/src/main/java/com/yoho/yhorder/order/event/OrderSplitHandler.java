package com.yoho.yhorder.order.event;

import com.alibaba.fastjson.JSONObject;
import com.yoho.yhorder.dal.OrderPromotionInfoMapper;
import com.yoho.yhorder.dal.model.OrderPromotionInfo;
import com.yoho.yhorder.order.service.IOrderSplitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Created by wujiexiang on 16/4/28.
 */
@Component
public class OrderSplitHandler {

    private Logger logger = LoggerFactory.getLogger("orderSplitLog");

    @Autowired
    private IOrderSplitService orderSplitService;

    @Autowired
    private OrderPromotionInfoMapper orderPromotionInfoMapper;

    /**
     * 拆分订单处理，刚发布是需要考虑兼容老订单，等老订单都完成拆单后，可以删除相应检测逻辑
     *
     * @param event
     */
    @Async
    @EventListener
    public void handleOrderSplitEvent(OrderSplitEvent event) {
        logger.info("Begin to handle OrderSplitEvent,uid is {},order code is {}", event.getUid(), event.getOrderCode());

        OrderPromotionInfo promotionInfo = orderPromotionInfoMapper.selectByOrderCode(event.getOrderCode());
        if (null == promotionInfo || promotionInfo.getOrderPromotion() == null) {
            logger.warn("Exception happen when handle OrderSplitEvent : promotionInfo is NULL,uid is {},order code is {}\n\n", event.getUid(), event.getOrderCode());
            return;
        }
        JSONObject jobj = JSONObject.parseObject(promotionInfo.getOrderPromotion());
        try {
            int splitedOrderNum;
            if (jobj.containsKey("use_yoho_coin_shipping_cost")) {
                //存在yoho币抵运费字段，拆单
                splitedOrderNum = orderSplitService.splitOrder(event.getUid(), event.getOrderCode());
                logger.info("End to handle OrderSplitEvent,uid is {},order code is {},split order num is {}\n\n", event.getUid(), event.getOrderCode(), splitedOrderNum);
            } else {
                //不存在yoho币抵运费字段，报错
                logger.warn("End to handle OrderSplitEvent failed not found use_yoho_coin_shipping_cost ,uid is {},order code is {},\n\n", event.getUid(), event.getOrderCode());
            }
        } catch (Exception ex) {
            logger.warn("Exception happen when handle OrderSplitEvent,uid is {},order code is {}\n\n", event.getUid(), event.getOrderCode(), ex);
        }
    }
}
