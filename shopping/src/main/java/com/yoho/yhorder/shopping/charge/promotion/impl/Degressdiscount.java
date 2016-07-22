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
import java.util.*;

/**
 * 分件数促销折扣
 * <p>
 * 1:1;2:0.7     第二件7折
 * 1:0.8;2:0.6   第一件8折  第二件 6折扣
 *
 * @author LUOXC
 * @Time 2015/12/03
 */
@Service
public class Degressdiscount extends AbstractPromotion {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void doCompute(ChargeContext chargeContext, PromotionInfo promotionInfo) {
        logger.debug("Start compute degressdiscount promotioin[{}].", promotionInfo);
        JSONObject actionParam = JSONObject.parseObject(promotionInfo.getActionParam());
        String degress_discount_list = actionParam.getString("degress_discount_list");
        if (StringUtils.isEmpty(degress_discount_list)) {
            logger.warn("can not find degress_discount_list.");
            return;
        }
        Map<Integer, BigDecimal> discounts = parseDiscounts(degress_discount_list);
        // 获取满足促销的商品
        List<ChargeGoods> fitGoodses = getFitGoods(promotionInfo.getId(), chargeContext.getMainGoods());
        // 设置打折商品价格
        BigDecimal discountPrice = setDiscountGoodsPrice(discounts, fitGoodses,chargeContext);

        // 设置促销信息
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        chargeTotal.setDiscountAmount(chargeTotal.getDiscountAmount() + discountPrice.doubleValue());
        if (discountPrice.doubleValue() > 0) {
            // 设置促销IDS
            chargeContext.setupPromotionIDS(promotionInfo.getId(), Lists.newArrayList());
            // 设置促销信息
//            PromotionBO promotion = new PromotionBO();
//            promotion.setPromotion_id(Integer.parseInt(promotionInfo.getId()));
//            promotion.setPromotion_title(promotionInfo.getTitle());
//            promotion.setCutdown_amount(discountPrice.doubleValue());
//            promotion.setPromotion_type(promotionInfo.getPromotionType());
//            chargeContext.setupPromotionInfo(promotion);

            chargeContext.setupPromotionInfo(promotionInfo,discountPrice);
        }
    }


    /**
     * 解析折扣数据
     *
     * @param degress_discount_list
     * @return
     */
    private Map<Integer, BigDecimal> parseDiscounts(String degress_discount_list) {
        Map<Integer, BigDecimal> discounts = new HashMap<>();
        String[] degressDiscounts = degress_discount_list.split(";");
        for (String degressDiscount : degressDiscounts) {
            String[] temp = degressDiscount.split(":");
            if (temp != null && temp.length == 2) {
                discounts.put(Integer.valueOf(temp[0]), BigDecimal.valueOf(Math.abs(Double.valueOf(temp[1]))));
            }
        }
        return discounts;
    }

    /**
     * 获取满足促销的商品（如果数量大于1，则进行拆分）
     *
     * @param promotionId 促销编号
     * @param goodses     商品
     * @return
     */
    private List<ChargeGoods> getFitGoods(String promotionId, List<ChargeGoods> goodses) {
        List<ChargeGoods> fitGoodses = new ArrayList<>();
        int mainGoodsSize = goodses.size();
        for (int i = 0; i < mainGoodsSize; i++) {
            ChargeGoods chargeGoods = goodses.get(i);
            if ("Y".equals(chargeGoods.getShoppingGoods().getSelected()) && chargeGoods.isPromotionFit(promotionId)) {
                fitGoodses.add(chargeGoods);
                if (chargeGoods.getBuyNumber() > 1) {
                    //需要拆分商品
                    int buyNumber = chargeGoods.getBuyNumber();
                    chargeGoods.getShoppingGoods().setBuy_number("" + 1);
                    for (int j = 1; j < buyNumber; j++) {
                        ChargeGoods cg = ChargeGoods.clone(chargeGoods);
                        goodses.add(cg);
                        fitGoodses.add(cg);
                    }
                }
            }
        }

        //TODO 添加排序，否则优惠与列表内商品顺序有关
        //商品按价格排序，高->低
        fitGoodses.sort(new java.util.Comparator<ChargeGoods>() {
            @Override
            public int compare(ChargeGoods o1, ChargeGoods o2) {
                if (o1.getShoppingGoods().getReal_price() > o2.getShoppingGoods().getReal_price()) {
                    return -1;
                } else if (o1.getShoppingGoods().getReal_price() == o2.getShoppingGoods().getReal_price()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        return fitGoodses;
    }

    /**
     * 设置打折商品价格
     *
     * @param discounts  折扣信息
     * @param fitGoodses 本次促销的打折商品
     * @return 折扣金额
     */
    private BigDecimal setDiscountGoodsPrice(Map<Integer, BigDecimal> discounts, List<ChargeGoods> fitGoodses, ChargeContext chargeContext) {
        int indexKey = 1;
        // 折扣金额
        BigDecimal discountPrice = BigDecimal.valueOf(0);
        int maxDiscountsNum = 0;
        for (Integer discountsNum : discounts.keySet()) {
            maxDiscountsNum = Math.max(maxDiscountsNum, discountsNum);
        }
        for (ChargeGoods fitGoods : fitGoodses) {
            // 获取当前折扣价格
            BigDecimal curDiscount;
            /**
             * 规则为： 1件0.8，2件0.6
             * 当购买第三件时，第二件的折扣
             */
            if (indexKey > maxDiscountsNum && discounts.get(maxDiscountsNum) != null) {
                curDiscount = discounts.get(maxDiscountsNum);
            } else if (discounts.get(indexKey) != null) {
                curDiscount = discounts.get(indexKey);
            } else {
                continue;
            }
            BigDecimal realPrice = MathUtils.roundPrice(BigDecimal.valueOf(fitGoods.getShoppingGoods().getReal_price()));
            BigDecimal newLastPrice = MathUtils.roundPrice(realPrice.multiply(curDiscount));
            discountPrice = MathUtils.roundPrice(discountPrice.add(realPrice.subtract(newLastPrice)));
            fitGoods.getShoppingGoods().setReal_price(newLastPrice.doubleValue());

            fitGoods.getDiscountPerSku().setDiscountAmount(BigDecimalHelper.upDouble(realPrice.subtract(newLastPrice)), DiscountType.PROMOTION);

            indexKey++;


            logger.info("[{}] degress discount promotioin, skn {} sku {}, real price {}, after buy num {}, total discount {}",
                    chargeContext.getChargeParam().getUid(),
                    fitGoods.getShoppingGoods().getProduct_skn(),
                    fitGoods.getShoppingGoods().getProduct_sku(),
                    realPrice,
                    fitGoods.getShoppingGoods().getReal_price(),
                    discountPrice);

        }
        return discountPrice;
    }

}
