package com.yoho.yhorder.shopping.charge.promotion.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.common.utils.YHMath;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.DiscountType;
import com.yoho.yhorder.shopping.charge.promotion.AbstractPromotion;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.yoho.yhorder.shopping.charge.promotion.impl.BigDecimalHelper.toDouble;
import static com.yoho.yhorder.shopping.charge.promotion.impl.BigDecimalHelper.up;

/**
 * 满减： promotion_type是[Cashreduce]的促销计算类
 * 数据库中配置如下： action_param: {"reduce":"1000"}
 *
 * @author chzhang@yoho.cn
 * @Time 2015/11/25
 */
@Service
public class CashReduce extends AbstractPromotion {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");


    @Override
    public void doCompute(ChargeContext chargeContext, PromotionInfo promotionInfo) {


        logger.debug("begin to do cash reduce promotion. promotion info: {}, context: {}", promotionInfo, chargeContext);

        //折扣多少钱
        String actionParam = promotionInfo.getActionParam();
        if (StringUtils.isEmpty(actionParam)) {
            logger.warn("reduce param is empty.");
            return;
        }
        JSONObject object = JSON.parseObject(actionParam);
        String strReduce = object.getString("reduce");
        BigDecimal cutdownAmount = new BigDecimal(strReduce);

        boolean hitPromotion = false;

        //符合条件的商品的总金额
        BigDecimal originalAmount = new BigDecimal(0);

        for (ChargeGoods chargeGoods : chargeContext.getMainGoods()) {

            //判断当前的活动在不在商品的能参与的活动列表中
            if (chargeGoods.isPromotionFit(promotionInfo.getId())) {
                //total = price * num
                BigDecimal total = chargeGoods.getRealPriceBigDecimal().multiply(new BigDecimal(chargeGoods.getBuyNumber()));
                originalAmount = originalAmount.add(total);
                hitPromotion = true;
            }
        }


        //计算满减金额折算到每件商品之后，每件商品的实际价格。用于ERP计算每件商品的毛利
        BigDecimal realCutdown = new BigDecimal("0");

        ChargeGoods minNumChargeGoods =null;

        for (ChargeGoods chargeGoods: chargeContext.getMainGoods()) {

            if (chargeGoods.isPromotionFit(promotionInfo.getId())) {
                //折扣分摊到每件商品，需要减去的价格: downAmout * realPrice / originamAmout
                BigDecimal realPrice = chargeGoods.getRealPriceBigDecimal();
                BigDecimal avgcut = realPrice.multiply(cutdownAmount).abs().divide(originalAmount, 4, BigDecimal.ROUND_HALF_EVEN);

                //这里在精度上，加大平均折扣
                avgcut = up(avgcut);

                logger.debug("calculate cashreduce realPrice:{}, avgcut:{}", realPrice, avgcut);

                // if avgCut <= real_Price, 则设置real_price = real_Price - avgCut
                if (avgcut.compareTo(realPrice) < 1) {

                    double newRealPrice = toDouble(realPrice.subtract(avgcut));
                    logger.debug("calculate cashreduce newRealPrice:{} ", newRealPrice);
                    chargeGoods.getShoppingGoods().setReal_price(newRealPrice);
                }

                //total && last
                BigDecimal priceCut = realPrice.subtract(chargeGoods.getRealPriceBigDecimal());
                realCutdown = realCutdown.add(priceCut.multiply(new BigDecimal(chargeGoods.getBuyNumber())));

                chargeGoods.getDiscountPerSku().setDiscountAmount(BigDecimalHelper.upDouble(priceCut), DiscountType.PROMOTION);

                if (minNumChargeGoods == null) {    //记录单独一件
                    minNumChargeGoods = chargeGoods;
                } else {
                    if (minNumChargeGoods.getBuyNumber() > chargeGoods.getBuyNumber()) {
                        minNumChargeGoods = chargeGoods;
                    }
                }


                logger.info("[{}]calculate cashreduce, realPrice:{}, newRealPrice:{}, Cutdown per goods:{}, total Cutdown:{}, avgcut: {} ",
                        chargeContext.getChargeParam().getUid(),
                        realPrice, chargeGoods.getShoppingGoods().getReal_price(), priceCut, realCutdown, avgcut);
            }
        }

        //优惠多了，需要减去,优先从单件商品减，没有就从购买数量最少的优惠商品拆分出单独一件再减·
        if (realCutdown.compareTo(cutdownAmount) > 0) {
            double moreCutdownAmout = YHMath.sub(realCutdown.doubleValue(), cutdownAmount.doubleValue());
            if (minNumChargeGoods != null) {
                ChargeGoods singleChargeGoods;
                if (minNumChargeGoods.getBuyNumber() == 1) {
                    singleChargeGoods = minNumChargeGoods;
                } else {//拆分商品
                    singleChargeGoods = ChargeGoods.clone(minNumChargeGoods);
                    singleChargeGoods.getShoppingGoods().setBuy_number("1");
                    minNumChargeGoods.getShoppingGoods().setBuy_number("" + (minNumChargeGoods.getBuyNumber() - 1));
                    //添加商品到普通商品列表
                    chargeContext.getMainGoods().add(singleChargeGoods);
                }
                singleChargeGoods.getDiscountPerSku().setDiscountAmount(YHMath.sub(0, moreCutdownAmout), DiscountType.PROMOTION);
                double realPrice = singleChargeGoods.getRealPriceBigDecimal().doubleValue();
                singleChargeGoods.getShoppingGoods().setReal_price(YHMath.add(realPrice, moreCutdownAmout));
                logger.info("[{}]calculate cashreduce, moreCutdownAmout :{} goodsId: {} sku:{} ,old realPrice {} new realPrice: {} ", chargeContext.getChargeParam().getUid(), moreCutdownAmout,
                        singleChargeGoods.getShoppingGoods().getGoods_id(), singleChargeGoods.getShoppingGoods().getProduct_sku(),
                        realPrice, singleChargeGoods.getShoppingGoods().getReal_price());
            } else {
                logger.warn("[{}]calculate cashreduce, moreCutdownAmout :{}, but no goods ! ", chargeContext.getChargeParam().getUid(), moreCutdownAmout);
            }
        }

        //设置一共优惠了多少钱
        chargeContext.addCutdownAmout(cutdownAmount);
//        chargeContext.addCutdownAmout(realCutdown);

        // 设置额外的信息
        chargeContext.setupPromotionIDS(promotionInfo.getId());

        //设置promotion信息
        if (hitPromotion) {
            chargeContext.setupPromotionInfo(promotionInfo, cutdownAmount);
        }

        logger.info("done with cash reduce, uid {}, hit promotion:{}, total cut: {}, promotionInfo: {}", chargeContext.getChargeParam().getUid(), hitPromotion, cutdownAmount, promotionInfo);
    }

}
