package com.yoho.yhorder.shopping.event;

import com.yoho.yhorder.dal.IShoppingCartItemsDAO;
import com.yoho.yhorder.dal.model.ShoppingCartItems;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wujiexiang on 16/3/14.
 */
@Component
public class ShoppingCartItmeHandler {

    private final static Logger logger = LoggerFactory.getLogger(ShoppingCartItmeHandler.class);

    @Autowired
    private IShoppingCartItemsDAO shoppingCartItemsDAO;


    /**
     * cart item delete event
     * @param event
     */
    @Async
    @EventListener
    public void handlerItemDeleteEvent(ShoppingCartItemDelEvent event) {

        logger.info("begin to handle ShoppingCartItemDelEvent: {}", event);

        if (CollectionUtils.isNotEmpty(event.getItems())) {
            List<Integer> itemIds = new ArrayList<>();
            int uid = 0;
            for (ShoppingCartItems item : event.getItems()) {
                uid = item.getUid();
                itemIds.add(item.getId());
            }

            //更改为直接删除
            //shoppingCartItemsDAO.updateShoppingCartGoodsStatusByCartID(uid, StringUtils.join(itemIds, ","));
            shoppingCartItemsDAO.deleteShoppingCartGoodsByCartID(uid, StringUtils.join(itemIds, ","));
        }

        logger.info("end to handle ShoppingCartItemDelEvent");
    }
}
