package com.yoho.yhorder.shopping.charge.promotion.impl;

import com.alibaba.fastjson.JSONObject;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.service.model.order.response.shopping.ShoppingGoods;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.DiscountType;
import com.yoho.yhorder.shopping.charge.promotion.AbstractPromotion;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * SpecifiedAmount 促销实现类 x件y元
 *
 * @author 张春华
 * @Time 2015/12/1
 */
@Service
public class SpecifiedAmount extends AbstractPromotion {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    public void doCompute(ChargeContext chargeContext, PromotionInfo promotionInfo) {

        logger.debug("start to calculate. context: {} promotion: {}", chargeContext, promotionInfo);

        //解析number件，amount 元
        Pair<Integer, Integer> num_amount = this.parse(promotionInfo);
        if (num_amount == null) {
            return;
        }
        int number = num_amount.getLeft();
        int amount = num_amount.getRight();


        /****** 0 先验证满足促销条件的商品，是否达到了X件的条件*****/
        int totalFit = 0;
        for (ChargeGoods mainGoods : chargeContext.getMainGoods()) {
            if (mainGoods.isPromotionFit(promotionInfo.getId()) && mainGoods.isSelected()) {
                totalFit = totalFit + mainGoods.getBuyNumber();
            }
        }
        if (totalFit < number) {
            logger.debug("total fit product size is {} less than num {}, do not need to do calculate.", totalFit, number);
            return;
        }
        logger.info("total fit product size is {} larger or equal than num {}, do calculate number-x-price-y", totalFit, number);


        /****** 1 拆分购买数量 > 1 的商品，为多个商品*****/
        List<ChargeGoods> specifiedGoodsList = new LinkedList<>();
        Iterator<ChargeGoods> it = chargeContext.getMainGoods().iterator();
        while (it.hasNext()) {
            ChargeGoods mainGoods = it.next();
            if (mainGoods.isPromotionFit(promotionInfo.getId()) && mainGoods.isSelected()) {
                for (int i = 1; i <= mainGoods.getBuyNumber(); i++) {
                    try {
                        ChargeGoods newChargeGoods = ChargeGoods.clone(mainGoods);
                        newChargeGoods.getShoppingGoods().setBuy_number("" + 1);
                        specifiedGoodsList.add(newChargeGoods);
                    } catch (Exception e) {
                        logger.error("clone beans exception.", e);
                        return;
                    }
                }

                it.remove();
            }
        }

        //通过价格排序： 价格从小到大排序
        specifiedGoodsList.sort(new Comparator<ChargeGoods>() {
            @Override
            public int compare(ChargeGoods o1, ChargeGoods o2) {
                if (o1.getShoppingGoods().getReal_price() > o2.getShoppingGoods().getReal_price()) {
                    return 1;
                } else if (o1.getShoppingGoods().getReal_price() == o2.getShoppingGoods().getReal_price()) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });

        //一共多少组。  例如一共7件商品，2件100元，则 specifiedCollectionTotal :  7/2 = 3
        int groupSize = (int) Math.floor(specifiedGoodsList.size() / number);

        //grouping
        List<List<ChargeGoods>> groupSpecifiedData = new ArrayList<>();
        for (int i = 0; i < groupSize; i++) {
            groupSpecifiedData.add(specifiedGoodsList.subList(i * number, (i + 1) * number));
        }


        /***************************** 分别计算每一个分组  *************************/
        BigDecimal cutAmount = new BigDecimal(0);
        Map<String, ChargeGoods> goodsListRealPriceResult = new HashMap<>();
        for (List<ChargeGoods> specifiedData : groupSpecifiedData) {

            double boxRealPriceTotal = 0;

            /***** 计算group的金额  **/
            double groupAmount = 0;
            for (ChargeGoods chargeGoods : specifiedData) {
                groupAmount += chargeGoods.getShoppingGoods().getReal_price();
            }
            if (groupAmount <= amount) {   //如果组的金额小于限制金额, 不需要计算优惠了
                chargeContext.getMainGoods().addAll(specifiedData);
                continue;
            }

            /****************计算平均每个商品优惠了多少钱  ****************/
            boxRealPriceTotal = boxRealPriceTotal + groupAmount;
            int _groupCount = specifiedData.size();
            double _groupNewGoodsPrice = 0;
            for (ChargeGoods chargeGoods : specifiedData) {
                double newLastPrice;
                //最后一件直接为剩余价钱
                if (_groupCount == 1) {
                    newLastPrice = amount - _groupNewGoodsPrice;
                } else {
                    //折扣价钱=y元*(本件商品售价/本组商品总价[保留两位小数])
                    BigDecimal scale = chargeGoods.getRealPriceBigDecimal().divide(new BigDecimal(boxRealPriceTotal), 2, BigDecimal.ROUND_HALF_EVEN);
                    newLastPrice = Math.floor(BigDecimalHelper.toDouble(new BigDecimal(amount).multiply(scale.abs())));
                    _groupNewGoodsPrice += newLastPrice;
                }

                //cut =  real -newLastPrice
                BigDecimal promotionCutAmount = chargeGoods.getRealPriceBigDecimal().subtract(new BigDecimal(newLastPrice));
                cutAmount = cutAmount.add(promotionCutAmount);
                chargeGoods.getShoppingGoods().setReal_price(newLastPrice);
                chargeGoods.getShoppingGoods().setBuy_number("" + 1);

                chargeGoods.getDiscountPerSku().setDiscountAmount(BigDecimalHelper.upDouble(promotionCutAmount), DiscountType.PROMOTION);

                String goodsKey = chargeGoods.getShoppingGoods().getProduct_sku() + "_" + chargeGoods.getShoppingGoods().getReal_price();

                if (goodsListRealPriceResult.containsKey(goodsKey)) {
                    chargeGoods.getShoppingGoods().setBuy_number(String.valueOf(goodsListRealPriceResult.get(goodsKey).getBuyNumber() + 1));
                }
                goodsListRealPriceResult.put(goodsKey, chargeGoods);
                _groupCount--;

            }
        }


        /************************* 最后加上MainGoods，并且设置促销信息 ****************/
        // 这是不能分组计算的
        List<ChargeGoods> canNotGroupGoods = specifiedGoodsList.subList(number * groupSize, specifiedGoodsList.size());
        for (ChargeGoods chargeGoods : canNotGroupGoods) {
            chargeGoods.getShoppingGoods().setBuy_number("" + 1);
            chargeContext.getMainGoods().add(chargeGoods);
        }

        //group之后的
        for (ChargeGoods chargeGoods : goodsListRealPriceResult.values()) {
            chargeContext.getMainGoods().add(chargeGoods);

            logger.info("[{}] specified amount. skn {} sku {}, Real_price {} ",
                    chargeContext.getChargeParam().getUid(),
                    chargeGoods.getShoppingGoods().getProduct_skn(),
                    chargeGoods.getShoppingGoods().getProduct_sku(),
                    chargeGoods.getShoppingGoods().getReal_price());
        }

        chargeContext.addCutdownAmout(cutAmount);

        chargeContext.setupPromotionInfo(promotionInfo, cutAmount);

        logger.info("[{}] specified amount. cutAmount: {} promotion: {}",
                chargeContext.getChargeParam().getUid(), cutAmount, promotionInfo);
    }


    /**
     * action_param: {"specified_amount_list":"3:499"}
     * 数据库中数据结构
     */
    private Pair<Integer, Integer> parse(PromotionInfo promotionInfo) {
        final String actionParamStr = promotionInfo.getActionParam();
        if (StringUtils.isEmpty(actionParamStr)) {
            logger.info("action param is null: {}", promotionInfo);
            return null;
        }

        String specifiedAmountListStr = JSONObject.parseObject(actionParamStr).getString("specified_amount_list");
        if (StringUtils.isEmpty(specifiedAmountListStr)) {
            logger.info("action param [specified_amount_list] is null: {}", promotionInfo.getActionParam());
            return null;
        }

        String[] specifiedAmountList = specifiedAmountListStr.split(":");
        if (specifiedAmountList.length != 2) {
            logger.info("action param [specified_amount_list] is invalidate: {}", promotionInfo.getActionParam());
            return null;
        }
        //number个商品，一共amout元
        int number = Integer.parseInt(specifiedAmountList[0]);
        int amount = Integer.parseInt(specifiedAmountList[1]);

        return Pair.of(number, amount);
    }
}