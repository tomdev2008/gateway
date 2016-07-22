package com.yoho.yhorder.shopping.charge.promotion.service.handler;

import com.yoho.service.model.order.model.PromotionBO;
import com.yoho.yhorder.dal.IShoppingCartItemsDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.yoho.yhorder.shopping.charge.promotion.impl.BigDecimalHelper.toDouble;

/**
 * Created by chunhua.zhang@yoho.cn on 2015/12/18.
 */
@Component
public class PromotionHandler {

    private final static Logger logger = LoggerFactory.getLogger(PromotionHandler.class);

    @Autowired
    private IShoppingCartItemsDAO iShoppingCartItemsDAO;


    /**
     *  cart delete event
     * @param event   cart delete event
     */
    @EventListener
    public void handlerCartDeleteEvent(ShoppingChartDeleteEvent event) {

        logger.info("start to process ShoppingChartDeleteEvent: {}" , event);
        //更改为物理删除加价购商品
        iShoppingCartItemsDAO.deleteCartGoodsByPromotionID(event.getShoppingChartId(),
                event.getUid(), event.getPromotionId(), event.getDeleteNum());
//        iShoppingCartItemsDAO.updateCartGoodsByPromotionID(event.getShoppingChartId(),
//                event.getUid(), event.getPromotionId(), event.getDeleteNum(), "0");
    }




}
