package com.yoho.yhorder.shopping.event;

import com.yoho.yhorder.shopping.service.ExternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Created by wujiexiang on 16/7/12.
 */
@Component
public class ShoppingTicketIssueHandler {

    private final static Logger logger = LoggerFactory.getLogger("ticketLog");

    @Autowired
    private ExternalService externalService;

    /**
     * @param event
     */
    @Async
    @EventListener
    public void handleTicketIssueEvent(ShoppingTicketIssueEvent event) {

        logger.info("begin to handle ShoppingTicketIssueEvent,event",
                event);
        try {
            externalService.issueTicket(event.getUid(), event.getOrderCode());
            logger.info("end to handle ShoppingTicketIssueEvent");
        } catch (Exception ex) {
            logger.warn("handle ShoppingTicketIssueEvent error", ex);
        }
    }
}
