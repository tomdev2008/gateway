package com.yoho.yhorder.shopping.event;

import com.yoho.yhorder.shopping.union.UnionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by wujiexiang on 16/4/1.
 */
@Component
public class UnionPushOrderHandler {
    private final static Logger logger = LoggerFactory.getLogger(UnionPushOrderHandler.class);

    @Resource(name = "defaultUnionServiceImpl")
    private UnionService defaultUnionService;

    @Resource(name = "orderPushServiceImpl")
    private UnionService orderPushService;


    /**
     * cart item delete event
     *
     * @param event
     */
    @Async
    @EventListener
    public void handleUnionPushOrderEvent(UnionPushOrderEvent event) {

        logger.info("begin to handle UnionPushOrderEvent,userAgent is {},userInfo is {}",
                event.getUserAgent(),event.getUnionContext().getOrderCreationContext().getUserInfo());


        defaultUnionService.run(event.getUserAgent(), event.getUnionContext());
        //-----4 => 'YOHOCart_Hook_Union_Order'
        orderPushService.run(event.getUserAgent(), event.getUnionContext());

        logger.info("end to handle UnionPushOrderEvent");
    }
}
