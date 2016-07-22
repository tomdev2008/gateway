package com.yoho.yhorder.order.event;

import com.yoho.yhorder.order.service.ITicketShoppingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * qianjun 2016/5/31
 */
@Component
public class OrderGoodsCommentHandler {
    private Logger logger = LoggerFactory.getLogger(OrderGoodsCommentHandler.class);

    @Autowired
    private ITicketShoppingService ticketShoppingService;

    @Async
    @EventListener
    public void handleOrderGoodsCommentEvent(OrderGoodsCommentEvent orderGoodsCommentEvent){
        logger.info("begin to handle TicketIssueEvent, orders is {},uid is {}, orderCode is {}", orderGoodsCommentEvent.getOrders(),
                orderGoodsCommentEvent.getOrders().getUid(), orderGoodsCommentEvent.getOrders().getOrderCode());
        try{
            ticketShoppingService.addOrderGoodsToComment(orderGoodsCommentEvent.getOrders());
        }catch (Exception ex){
            logger.warn("exception happen when handle TicketIssueEvent,orders is {},uid is {}, orderCode is {} and exception is {}", orderGoodsCommentEvent.getOrders(),
                    orderGoodsCommentEvent.getOrders().getUid(),orderGoodsCommentEvent.getOrders().getOrderCode(), ex);
        }
    }
}
