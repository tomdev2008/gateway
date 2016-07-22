package com.yoho.yhorder.order.event;

import com.yoho.service.model.order.response.Orders;
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
public class TicketIssueHandler {
    private Logger logger = LoggerFactory.getLogger(TicketIssueHandler.class);

    @Autowired
    private ITicketShoppingService ticketShoppingService;

    @Async
    @EventListener
    public void handleTicketIssueEvent(TicketIssueEvent ticketIssueEvent) {
        Orders orders = ticketIssueEvent.getOrders();
        logger.info("begin to handle TicketIssueEvent, orders is {},uid is {}, orderCode is {}", ticketIssueEvent.getOrders(),
                orders.getUid(), orders.getOrderCode());
        try {
            ticketShoppingService.issueTicket(orders.getUid(), orders.getOrderCode());
        } catch (Exception ex) {
            logger.warn("exception happen when handle TicketIssueEvent,orders is {},uid is {}, orderCode is {} and exception is {}", ticketIssueEvent.getOrders(),
                    orders.getUid(), orders.getOrderCode(), ex);
        }
    }
}
