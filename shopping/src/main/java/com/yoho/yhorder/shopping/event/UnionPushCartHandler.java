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
public class UnionPushCartHandler {

    private final static Logger logger = LoggerFactory.getLogger(UnionPushCartHandler.class);

    @Resource(name = "shoppingCartPushServiceImpl")
    private UnionService shoppingCartPushService;


    /**
     * cart item delete event
     *
     * @param event
     */
    @Async
    @EventListener
    public void handleUnionPushCartEvent(UnionPushCartEvent event) {

        logger.info("begin to handle UnionPushCartEvent,userAgent is {},userInfo is {}",
                event.getUserAgent(),event.getUnionContext().getChargeContext().getUserInfo());

        shoppingCartPushService.run(event.getUserAgent(), event.getUnionContext());

        logger.info("end to handle UnionPushCartEvent");
    }
}
