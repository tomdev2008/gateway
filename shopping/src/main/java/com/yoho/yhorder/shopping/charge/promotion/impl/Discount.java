package com.yoho.yhorder.shopping.charge.promotion.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.yoho.service.model.order.model.PromotionBO;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.charge.model.DiscountType;
import com.yoho.yhorder.shopping.charge.promotion.AbstractPromotion;
import com.yoho.yhorder.shopping.utils.MathUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 打折
 *
 * @author LUOXC
 * @Time 2015/12/03
 */
@Service
public class Discount extends AbstractPromotion {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    public void doCompute(ChargeContext chargeContext, PromotionInfo promotionInfo) {
        logger.debug("Start compute discount promotioin[{}].", promotionInfo);
        JSONObject actionParam = JSONObject.parseObject(promotionInfo.getActionParam());
        // 获取折扣信息
        String discountData = actionParam.getString("discount");
        if (StringUtils.isEmpty(discountData)) {
            logger.warn("discount param is empty of promotion[{}].", promotionInfo);
            return;
        }
        BigDecimal discount = new BigDecimal(discountData).abs();
        // 设置打折商品价格
        BigDecimal cutdownAmount = setDiscountGoodsPrice(promotionInfo.getId(), chargeContext.getMainGoods(), discount, chargeContext);
        // 更新促销信息
        updatePromotionInfo(chargeContext, promotionInfo, cutdownAmount);
    }

    /**
     * 设置打折商品价格
     *
     * @param promotionId
     * @param goodses
     * @param discount
     * @return 折扣金额
     */
    protected BigDecimal setDiscountGoodsPrice(String promotionId, List<ChargeGoods> goodses, BigDecimal discount,ChargeContext chargeContext) {
        BigDecimal cutdownAmount = new BigDecimal(0);
        for (ChargeGoods chargeGoods : goodses) {
            if (chargeGoods.isPromotionFit(promotionId)) {
                BigDecimal realPrice = MathUtils.roundPrice(new BigDecimal(chargeGoods.getShoppingGoods().getReal_price()));
                BigDecimal newRealPrice = MathUtils.roundPrice(realPrice.multiply(discount));
                BigDecimal cutdownPrice = MathUtils.roundPrice(realPrice.subtract(newRealPrice).multiply(new BigDecimal(chargeGoods.getBuyNumber())));
                chargeGoods.getShoppingGoods().setReal_price(newRealPrice.doubleValue());

                chargeGoods.getDiscountPerSku().setDiscountAmount(BigDecimalHelper.upDouble(realPrice.subtract(newRealPrice)), DiscountType.PROMOTION);

                cutdownAmount = MathUtils.roundPrice(cutdownAmount.add(cutdownPrice));

                logger.info("[{}] discount promotioin, skn {} sku {}, real price {}, after buy num {}, cutdownAmount {}",
                        chargeContext.getChargeParam().getUid(),
                        chargeGoods.getShoppingGoods().getProduct_skn(),
                        chargeGoods.getShoppingGoods().getProduct_sku(),
                        realPrice,
                        chargeGoods.getShoppingGoods().getReal_price(),
                        cutdownAmount);

                logger.debug("realPrice is {}, newRealPrice is {}, cutdownPrice is {}.", realPrice.doubleValue(), newRealPrice.doubleValue(), cutdownPrice.doubleValue());
            }
        }
        return cutdownAmount;
    }

    /**
     * 更新促销信息
     *
     * @param chargeContext
     * @param promotionInfo
     * @param cutdownAmount
     */
    protected void updatePromotionInfo(ChargeContext chargeContext, PromotionInfo promotionInfo, BigDecimal cutdownAmount) {
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        BigDecimal oldDiscountAmount = MathUtils.roundPrice(BigDecimal.valueOf(chargeTotal.getDiscountAmount()));
        BigDecimal newDiscountAmount = MathUtils.roundPrice(oldDiscountAmount.add(cutdownAmount));
        chargeTotal.setDiscountAmount(newDiscountAmount.doubleValue());
        logger.debug("DiscountAmount:{} + {} = {}", oldDiscountAmount.doubleValue(), cutdownAmount.doubleValue(), newDiscountAmount.doubleValue());
        // 设置促销IDS
        chargeContext.setupPromotionIDS(promotionInfo.getId(), Lists.newArrayList());
        // 设置促销信息
//        PromotionBO promotion = new PromotionBO();
//        promotion.setPromotion_id(Integer.parseInt(promotionInfo.getId()));
//        promotion.setPromotion_title(promotionInfo.getTitle());
//        promotion.setCutdown_amount(cutdownAmount.doubleValue());
//        promotion.setPromotion_type(promotionInfo.getPromotionType());
//        chargeContext.setupPromotionInfo(promotion);

        chargeContext.setupPromotionInfo(promotionInfo,cutdownAmount);
    }

}
