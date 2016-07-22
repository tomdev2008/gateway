package com.yoho.yhorder.shopping.charge.promotion.impl;

import com.alibaba.fastjson.JSONObject;
import com.yoho.product.model.ProductGiftBo;
import com.yoho.product.model.promotion.AddCostProductBo;
import com.yoho.service.model.order.model.PromotionBO;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.service.model.order.response.shopping.PromotionGift;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.charge.model.DiscountType;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wujiexiang on 16/3/2.
 */
@Component
public class Gift2 extends Gift {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    @Override
    public void doCompute(ChargeContext chargeContext, PromotionInfo promotionInfo) {
        logger.info("Start compute gift promotioin[{}].", promotionInfo);
        JSONObject actionParam = JSONObject.parseObject(promotionInfo.getActionParam());
        String[] skns = promotionInfo.getActionSKNList();
        if (ArrayUtils.isEmpty(skns)) {
            logger.info("can not find giftList of promotion[{}].", promotionInfo);
            return;
        }
        List<Integer> giftSkns = new ArrayList<>();
        for (String productSkn : skns) {
            giftSkns.add(Integer.parseInt(productSkn));
        }

        int promotionGiftNumber = actionParam.getIntValue("num");
        PromotionGift gift = new PromotionGift(giftSkns, promotionInfo, String.valueOf(promotionGiftNumber), 0, null);
        chargeContext.setupGift(gift);
    }


    /**
     * 先根据促销查询所有的skn数量,然后在逐一处理
     * @param chargeContext
     * @param promotionInfo
     * @param addCostproductBos
     * @param productGiftBos
     */
        public void process(ChargeContext chargeContext, PromotionInfo promotionInfo,AddCostProductBo[] addCostproductBos,ProductGiftBo[] productGiftBos)
    {
        logger.info("start process gift,\npromotion is {},\naddCostproductBos is {},\nproductGiftBos is {}", promotionInfo, addCostproductBos, productGiftBos);
        JSONObject actionParam = JSONObject.parseObject(promotionInfo.getActionParam());
        List<Integer> giftList = getGifts(actionParam);
        if (giftList.isEmpty()) {
            logger.info("can not find giftList of promotion[{}].", promotionInfo);
            return;
        }
        int promotionId = Integer.parseInt(promotionInfo.getId());
        List<ChargeGoods> mainGoodsGifts = chargeContext.getMainGoodsGift();
        // 获取满足促销的赠品
        List<ChargeGoods> fitGoodsGifts = getFitGoodsGifts(promotionId, mainGoodsGifts);
        // 更新赠品销售价格
        updateGiftSalePrice(actionParam.getString("add_cost"), fitGoodsGifts);
        // 删除选择多选的赠品
        int promotionGiftNumber = actionParam.getIntValue("num");
        // 获取用户选择的赠品数量，并删除多选的赠品
        //TODO 删除多选的商品后 需要更新chargeContext内商品数量?!
        int userSelectGiftNumber = getUserSelectGiftNumber(promotionGiftNumber, fitGoodsGifts,mainGoodsGifts,promotionId);
        logger.info("userSelectGiftNumber is {}, promotionGiftNumber is {}.", userSelectGiftNumber, promotionGiftNumber);
        //自动添加赠品 必须是送一件
        boolean autoAddGiftToShoppingCartSuccess = true;
        if (promotionGiftNumber == 1 && userSelectGiftNumber == 0) {
            autoAddGiftToShoppingCartSuccess = autoAddGiftToShoppingCart(promotionId, giftList, chargeContext,productGiftBos);
        }
        logger.info("autoAddGiftToShoppingCartSuccess is {}.", autoAddGiftToShoppingCartSuccess);
        // 没有赠品自动加入购物车
        if (!autoAddGiftToShoppingCartSuccess) {
            // 用户没有选择结束，查询出促销赠品加入到赠品列表中
            if (userSelectGiftNumber != promotionGiftNumber) {
                if (addCostproductBos != null && addCostproductBos.length > 0) {
                    PromotionGift gift = new PromotionGift(addCostproductBos, promotionInfo, String.valueOf(promotionGiftNumber), 0, null);

                    /**
                     * PromotionGift中判断库存和状态，如果没有库存不会添加到goodsList中
                     */
                    if (gift.getGoodsList().size() > 0) {
                        chargeContext.setupGift(gift);
                    } else {
                        logger.info("auto add gift failed, git storage or status is error,  promotion[{}].", promotionInfo);
                    }
                } else {
                    logger.info("auto add gift failed ,can not find gift info from product service, promotion[{}].", promotionInfo);
                }
            } else {
                logger.info("user has select end.");
            }

        }
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        //计算优惠价格
        double discountAmount = 0;
        for (ChargeGoods goodsGift : fitGoodsGifts) {
            double salePrice = goodsGift.getShoppingGoods().getSales_price();
            discountAmount += salePrice;
            goodsGift.getDiscountPerSku().setDiscountAmount(salePrice, DiscountType.PROMOTION);
        }

        chargeTotal.setDiscountAmount(chargeTotal.getDiscountAmount() + discountAmount);
        // 设置促销IDS

        // 设置促销信息
        PromotionBO promotion = new PromotionBO();
        chargeContext.setupPromotionIDS(promotionInfo.getId(), giftList);
        if (!fitGoodsGifts.isEmpty() || autoAddGiftToShoppingCartSuccess) {
//            promotion.setPromotion_id(Integer.parseInt(promotionInfo.getId()));
//            promotion.setPromotion_title(promotionInfo.getTitle());
//            promotion.setCutdown_amount(0);
//            promotion.setPromotion_type(promotionInfo.getPromotionType());
//            chargeContext.setupPromotionInfo(promotion);
            chargeContext.setupPromotionInfo(promotionInfo,0.00);
        }

        logger.info("[{}] gift promotion, discount {}, chargeTotal.DiscountAmount {}, promotion {}  ",
                chargeContext.getChargeParam().getUid(),
                discountAmount,
                chargeTotal.getDiscountAmount(),promotion
        );
    }
}
