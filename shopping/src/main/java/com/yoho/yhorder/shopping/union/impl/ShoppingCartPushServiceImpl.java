package com.yoho.yhorder.shopping.union.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.union.UnionContext;
import com.yoho.yhorder.shopping.utils.MyStringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by JXWU on 2015/12/8.
 * 购物车提交算费完成后推送
 */
@Component
public class ShoppingCartPushServiceImpl extends AbstractPushService {

    private final Logger logger = LoggerFactory.getLogger("unionPushLog");

    @Override
    public void run(String userAgent, UnionContext unionContext) {
        logger.info("push shopping cart to union,userAgent is {}", userAgent);
        Map<String, String> userAgentMap = MyStringUtils.asStringToMap(userAgent);
        if (userAgentMap.isEmpty()) {
            logger.info("userAgentMap is empty,nothing todo");
            return;
        }

        ChargeParam chargeParam = unionContext.getChargeContext().getChargeParam();
        if (chargeParam == null || !"iphone".equals(chargeParam.getClientType())) {
            logger.info("order client type is not iphone, charge param is {}", chargeParam);
            return;
        }
        try {
            List<ChargeGoods> goodsList = unionContext.getChargeContext().getMainGoods();
            if (CollectionUtils.isNotEmpty(goodsList)) {
                JSONArray buyProductArray = new JSONArray();
                for (ChargeGoods chargeGoods : goodsList) {
                    JSONObject buyProductJSON = new JSONObject();
                    buyProductJSON.put("id", chargeGoods.getShoppingGoods().getProduct_skn());
                    buyProductJSON.put("price", chargeGoods.getShoppingGoods().getSales_price());
                    buyProductJSON.put("quantity", chargeGoods.getShoppingGoods().getBuy_number());
                    buyProductArray.add(buyProductJSON);
                }
                JSONObject eventJSON = new JSONObject();
                eventJSON.put("event", "viewBasket");
                eventJSON.put("currency", "CNY");
                eventJSON.put("product", buyProductArray);
                String idfa = userAgentMap.containsKey("ifa") ? userAgentMap.get("ifa") : "";
                super.push(idfa, eventJSON);
            }

        } catch (Exception e) {
            //
            logger.warn("push shopping cart to union error", e);
        }
        logger.info("push shopping cart to union end");
    }
}
