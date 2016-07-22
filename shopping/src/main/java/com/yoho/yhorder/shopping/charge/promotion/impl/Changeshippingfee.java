package com.yoho.yhorder.shopping.charge.promotion.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.yoho.service.model.order.model.PromotionBO;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.charge.promotion.AbstractPromotion;
import com.yoho.yhorder.shopping.utils.MathUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 运费调整
 *
 * @author LUOXC
 * @Time 2015/12/02
 */
@Service
public class Changeshippingfee extends AbstractPromotion {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    public void doCompute(ChargeContext chargeContext, PromotionInfo promotionInfo) {
        logger.debug("Start compute changeshippingfee promotioin[{}].", promotionInfo);
        // 获取运费
        Double shippingCost = parseShippingCost(JSON.parseObject(promotionInfo.getActionParam()));
        if (shippingCost == null) {
            return;
        }
        // 运费不为空
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        // 设置运费
        chargeTotal.setShippingCost(shippingCost);
        // 设置运费促销信息
        Map<String, Object> shippingCostPromotion = new HashMap<>();
        shippingCostPromotion.put("promotion_shipping_cost", shippingCost);
        shippingCostPromotion.put("promotion_id", Integer.valueOf(promotionInfo.getId()));
        chargeTotal.setShippingCostPromotion(shippingCostPromotion);
        // 设置促销IDS
        chargeContext.setupPromotionIDS(promotionInfo.getId(), Lists.newArrayList());
        // 设置促销信息
//        PromotionBO promotion = new PromotionBO();
//        promotion.setPromotion_id(Integer.parseInt(promotionInfo.getId()));
//        promotion.setPromotion_title(promotionInfo.getTitle());
//        promotion.setCutdown_amount(shippingCost);
//        promotion.setPromotion_type(promotionInfo.getPromotionType());
//        chargeContext.setupPromotionInfo(promotion);
        chargeContext.setupPromotionInfo(promotionInfo, shippingCost);

        logger.info("[{}] Change shipping fee , promotion info {},chargeTotal {}",
                chargeContext.getChargeParam().getUid(),promotionInfo,chargeTotal);

        logger.debug("Compute changeshippingfee promotioin[{}] success.", promotionInfo);
    }

    /**
     * 解析运费信息
     *
     * @return
     */
    private Double parseShippingCost(JSONObject actionParam) {
        String shippingCost = actionParam.getString("shipping_cost");
        if (StringUtils.isNotEmpty(shippingCost)) {
            return MathUtils.round(Double.valueOf(shippingCost));
        } else {
            logger.warn("ParseShippingCost:shipping_cost is empty.");
            return null;
        }
    }
}