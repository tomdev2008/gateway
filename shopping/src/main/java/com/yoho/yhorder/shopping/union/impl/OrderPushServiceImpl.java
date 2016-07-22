package com.yoho.yhorder.shopping.union.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.yhorder.shopping.model.Order;
import com.yoho.yhorder.shopping.model.OrderGoods;
import com.yoho.yhorder.shopping.union.UnionContext;
import com.yoho.yhorder.shopping.utils.MyStringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by JXWU on 2015/12/6.
 * 订单创建完成后推送
 */
@Component
public class OrderPushServiceImpl extends AbstractPushService {

    private final Logger logger = LoggerFactory.getLogger("unionPushLog");

    @Override
    public void run(String userAgent, UnionContext unionContext) {
        logger.info("push order to union,userAgent is {}", userAgent);
        Map<String, String> userAgentMap = MyStringUtils.asStringToMap(userAgent);
        if (userAgentMap.isEmpty()) {
            logger.info("userAgentMap is empty,nothing todo");
            return;
        }
        Order order = unionContext.getOrderCreationContext().getOrder();
        if (order == null) {
            return;
        }
        logger.info("push order to union,order code is {}", order.getOrderCode());
        if (!"iphone".equals(order.getClientType())) {
            logger.info("order client type is not iphone,order code is {}", order.getOrderCode());
            return;
        }

        try {
            List<OrderGoods> goodsList = order.getGoodsList();
            if (CollectionUtils.isNotEmpty(goodsList)) {
                JSONArray buyProductArray = new JSONArray();
                for (OrderGoods orderGoods : goodsList) {
                    JSONObject buyProductJSON = new JSONObject();
                    buyProductJSON.put("id", orderGoods.getProduct_skn());
                    buyProductJSON.put("price", orderGoods.getSales_price());
                    buyProductJSON.put("quantity", orderGoods.getBuy_number());
                    buyProductArray.add(buyProductJSON);
                }
                JSONObject eventJSON = new JSONObject();
                eventJSON.put("event", "trackTransaction");
                eventJSON.put("id", order.getOrderCode());
                eventJSON.put("currency", "CNY");
                eventJSON.put("product", buyProductArray);
                String idfa = userAgentMap.containsKey("ifa") ? userAgentMap.get("ifa") : "";
                super.push(idfa, eventJSON);
            }

        } catch (Exception e) {
            //
            logger.warn("push order to union end", e);
        }
        logger.info("push order to union end");
    }
}