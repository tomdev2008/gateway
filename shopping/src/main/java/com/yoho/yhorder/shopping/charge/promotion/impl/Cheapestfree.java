package com.yoho.yhorder.shopping.charge.promotion.impl;

import com.google.common.collect.Lists;
import com.yoho.service.model.order.model.PromotionBO;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.charge.model.DiscountType;
import com.yoho.yhorder.shopping.charge.promotion.AbstractPromotion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 当购买数量达到X件商品时，最便宜的一件免单（不包含赠品，加价购商品）；
 *
 * @author LUOXC
 * @Time 2015/12/03
 */
@Service
public class Cheapestfree extends AbstractPromotion {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    public void doCompute(ChargeContext chargeContext, PromotionInfo promotionInfo) {
        logger.debug("Start compute cheapestfree promotioin[{}].", promotionInfo);
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        //获取免单商品
        ChargeGoods fitGoods = getFitGoods(promotionInfo.getId(), chargeContext.getMainGoods());
        if (fitGoods == null) {
            logger.warn("can not find fit goods for the promotion[{}]", promotionInfo);
            return;
        }
        logger.debug("find fit goods[{}] for the promotion[{}]", fitGoods, promotionInfo);
        //获取免单金额
        Double cutdownAmount = fitGoods.getShoppingGoods().getReal_price();
        //买了一个商品，直接免单
        if (fitGoods.getBuyNumber() == 1) {
            fitGoods.getShoppingGoods().setReal_price(0d);
            fitGoods.getDiscountPerSku().setDiscountAmount(cutdownAmount, DiscountType.PROMOTION);
        }
        //相同价格的大于两件
        else {
            fitGoods.getShoppingGoods().setBuy_number(String.valueOf(fitGoods.getBuyNumber() - 1));
            fitGoods.getShoppingGoods().setStorage_number(String.valueOf(Integer.parseInt(fitGoods.getShoppingGoods().getStorage_number()) - 1));
            ChargeGoods freeGoods = ChargeGoods.clone(fitGoods);
            freeGoods.getShoppingGoods().setBuy_number(String.valueOf(1));
            freeGoods.getShoppingGoods().setReal_price(0d);
            freeGoods.getShoppingGoods().setStorage_number("" + 1);
            freeGoods.getShoppingGoods().setIs_cheapest_free("" + 1);
            fitGoods.getDiscountPerSku().setDiscountAmount(cutdownAmount, DiscountType.PROMOTION);
            chargeContext.getMainGoods().add(freeGoods);
        }
        chargeTotal.setDiscountAmount(chargeTotal.getDiscountAmount() + cutdownAmount);
        // 设置促销IDS
        chargeContext.setupPromotionIDS(promotionInfo.getId(), Lists.newArrayList());
        // 设置促销信息
//        PromotionBO promotion = new PromotionBO();
//        promotion.setPromotion_id(Integer.parseInt(promotionInfo.getId()));
//        promotion.setPromotion_title(promotionInfo.getTitle());
//        promotion.setCutdown_amount(cutdownAmount);
//        promotion.setPromotion_type(promotionInfo.getPromotionType());
//        chargeContext.setupPromotionInfo(promotion);

        chargeContext.setupPromotionInfo(promotionInfo,cutdownAmount);

        logger.info("[{}] cheapest free promotioin, skn {} sku {}, real price {}, after buy num {}, after storage {}, promotion {}",
                chargeContext.getChargeParam().getUid(),
                fitGoods.getShoppingGoods().getProduct_skn(),
                fitGoods.getShoppingGoods().getProduct_sku(),
                fitGoods.getShoppingGoods().getReal_price(),
                fitGoods.getShoppingGoods().getBuy_number(),
                fitGoods.getShoppingGoods().getStorage_number(),
                promotionInfo);
    }

    private ChargeGoods getFitGoods(String promotionId, List<ChargeGoods> goodses) {
        double minRealPrice = Double.MAX_VALUE;
        ChargeGoods fitGoods = null;
        for (ChargeGoods goods : goodses) {
            if (goods.isPromotionFit(promotionId)) {
                if (minRealPrice > goods.getShoppingGoods().getReal_price()) {
                    minRealPrice = goods.getShoppingGoods().getReal_price();
                    fitGoods = goods;
                }
            }
        }
        return fitGoods;
    }
}
