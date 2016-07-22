package com.yoho.yhorder.shopping.charge.promotion.impl;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.product.model.promotion.AddCostProductBo;
import com.yoho.product.request.AddCostProductRequest;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.service.model.order.response.shopping.PromotionGift;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.DiscountType;
import com.yoho.yhorder.shopping.charge.promotion.AbstractPromotion;
import com.yoho.yhorder.shopping.charge.promotion.service.handler.ShoppingChartDeleteEvent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 加价购买
 *
 * @author zhangchunhua@yoho.cn
 * @Time 2015/12/19
 */
@Service
public class Needpaygift extends AbstractPromotion {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    private final ServiceCaller serviceCaller;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public Needpaygift(ApplicationEventPublisher publisher, ServiceCaller serviceCaller) {
        this.publisher = publisher;
        this.serviceCaller = serviceCaller;
    }


    public void doCompute(ChargeContext chargeContext, PromotionInfo promotionInfo) {

        logger.debug("begin to do cash need-pay-gift promotion. promotion info: {}, context: {}", promotionInfo, chargeContext);

        if (chargeContext.getMainGoodsPriceGift() == null) {
            logger.info("can not find any mainGoodsPriceGift from context:{}", chargeContext);
            return;
        }

        //{"add_cost":"1222121","goods_list":"12,12212","num":"3"}
        JSONObject actionParam = JSONObject.parseObject(promotionInfo.getActionParam());

        //add cost
        String _addCost = actionParam.getString("add_cost");
        _addCost = StringUtils.isEmpty(_addCost) ? "0" : _addCost;
        BigDecimal addCost = new BigDecimal(_addCost);

        //max num
        String num = actionParam.getString("num");
        if (StringUtils.isEmpty(num)) {
            logger.warn("action param {} has no num.", promotionInfo.getActionParam());
            return;
        }
        //you can choose at most num gifts for all gifts
        int gitMaxBuy = Integer.parseInt(num);

        //goods list
        String[] giftList = promotionInfo.getActionSKNList();
        if (ArrayUtils.isEmpty(giftList)) {
            logger.warn("action param {} has no goods list.", promotionInfo.getActionParam());
            return;
        }

        //user total chosen gift num
        int selTotalNum = 0;
        //是否匹配促销
        boolean hitPromotion = false;
        //是否能选择另外的加价购商品。如果是，则用户不能再选择其他加价购商品了，否则，需要返回可以选择的加价购商品
        boolean canNotSelectOtherGifts = false;
        //记录是否有加价购需要删除
        int moreSelectPriceGiftNum=0;
        int mainGoodsPriceGiftSize=chargeContext.getMainGoodsPriceGift().size();
        //逆序遍历
        for(int i=mainGoodsPriceGiftSize-1;i>=0;i--){
            ChargeGoods chargeGoods=chargeContext.getMainGoodsPriceGift().get(i);
            //不是该活动下的加价购，略过
            if(Integer.parseInt(chargeGoods.getShoppingGoods().getPromotion_id()) != Integer.parseInt(promotionInfo.getId())){
                continue;
            }
            if (StringUtils.equals("N", chargeGoods.getShoppingGoods().getSelected())) {
                //如果有没选择的该活动下的加价购商品，直接设置客户端无法再选择该加价购
                canNotSelectOtherGifts = true;
                continue;
            }

            hitPromotion = true;
            //统计加价购商品购买总数 ，不会发生同一个sku>1
            selTotalNum = selTotalNum + chargeGoods.getBuyNumber();

            //正常情况下，发生超卖的话，这个商品肯定会被删除,一个都不留(不可能出现buynum>1)
            if (selTotalNum > gitMaxBuy) {  //如果选择的商品数量大于设定数量, 需要删除购物车中多余的
                int rowNum = selTotalNum - gitMaxBuy;
                selTotalNum = selTotalNum - rowNum;

                String cartId = chargeGoods.getShoppingGoods().getShopping_cart_id();
                String uid = chargeGoods.getShoppingGoods().getUid();
                logger.info("publishEvent ShoppingChartDeleteEvent cartid:{}, uid:{}, promotionId:{}, promotionTitle:{}, rowNum:{}",
                        cartId,uid,promotionInfo.getId(),promotionInfo.getTitle(),rowNum);
                this.publisher.publishEvent(new ShoppingChartDeleteEvent(cartId, promotionInfo.getId(), uid, rowNum));
                //需要删除的加价购商品数量增加
                moreSelectPriceGiftNum++;
            } else {
                //没有超卖情况，需要设置折扣优惠价钱
                // set total discount
                BigDecimal disCount = chargeGoods.getSalesPriceBigDecimal().subtract(addCost).abs();
                chargeGoods.getDiscountPerSku().setDiscountAmount(BigDecimalHelper.upDouble(disCount), DiscountType.PROMOTION);
                chargeContext.addCutdownAmout(disCount);
                //set sale price
                chargeGoods.getShoppingGoods().setSale_price(BigDecimalHelper.toDouble(addCost));
                logger.info("[{}] need pay gift promotion, skn {} sku {}, buy num {}, disCount {}, total discount amount {}, selTotalNum {} ",
                        chargeContext.getChargeParam().getUid(),
                        chargeGoods.getShoppingGoods().getProduct_skn(),
                        chargeGoods.getShoppingGoods().getProduct_sku(),
                        chargeGoods.getBuyNumber(),
                        disCount,
                        chargeContext.getChargeTotal().getDiscountAmount(),
                        selTotalNum);

                if (selTotalNum == gitMaxBuy) {  //如果选择商品等于设定数量
                    canNotSelectOtherGifts = true;
                }
            }
        }

        //有多余加价购
        if(moreSelectPriceGiftNum>0){
            Iterator<ChargeGoods> iteratorPriceGift=chargeContext.getMainGoodsPriceGift().iterator();
            while (iteratorPriceGift.hasNext()) {
                ChargeGoods tmpChargeGoods=iteratorPriceGift.next();
                //只处理当前活动下的加价购商品
                if (Integer.parseInt(tmpChargeGoods.getShoppingGoods().getPromotion_id()) == Integer.parseInt(promotionInfo.getId()) &&
                        StringUtils.equals(tmpChargeGoods.getShoppingGoods().getSelected(), "Y")) {
                    //是否需要删除
                    if(moreSelectPriceGiftNum>0) {
                        //删除当前加价购
                        logger.info("moreSelectPriceGiftNum:{}  remove ChargeGoods:{} ",moreSelectPriceGiftNum,tmpChargeGoods);
                        iteratorPriceGift.remove();
                        moreSelectPriceGiftNum--;
                    }else{ //无需再删除，退出循环
                        break;
                    }
                }
            }
        }

        // 加价购商品信息完善
        this.setupGiftInfos(chargeContext, promotionInfo, num, addCost, canNotSelectOtherGifts);

        //setup promotion IDS
        int[] giftIds = new int[giftList.length];
        for (int i = 0; i < giftList.length; i++) {
            if (giftList[i] != null) {
                giftIds[i] = Integer.parseInt(giftList[i]);
            }
        }
        chargeContext.setupPromotionIDS(promotionInfo.getId(), giftIds);

        //如果匹配到活动
        if (hitPromotion) {
            chargeContext.setupPromotionInfo(promotionInfo, addCost);
        }

        logger.info("[{}] need pay gift promotion, hitPromotion {}, promotionInfo {}, addCost {}, num {}  ",
                chargeContext.getChargeParam().getUid(),hitPromotion,
                promotionInfo, addCost, num
        );
        logger.debug("done with need-pay-gift. hit promotion:{},   after chargecontext: {}", hitPromotion, chargeContext);

    }


    /**
     * 加价购商品信息完善
     * 从product模块查询加价购商品的详细信息
     * 查询出商品的storage status sales_price market_price product_skn  storage_number  图片 product_id  product_name
     */
    private void setupGiftInfos(ChargeContext chargeContext, PromotionInfo promotionInfo, String num, BigDecimal addCost, boolean canNotSelectOtherGifts) {
        //AddCostProductRequest addCostProductRequest = new AddCostProductRequest();
        List<Integer> giftSKNs = new ArrayList<>();
        for (String productSkn : promotionInfo.getActionSKNList()) {
            giftSKNs.add(Integer.parseInt(productSkn));
        }

        if (canNotSelectOtherGifts == false) {
            PromotionGift gift = new PromotionGift(giftSKNs, promotionInfo, num, addCost.doubleValue(), addCost);
            chargeContext.setupPriceGift(gift);
        }

        //--------------------放到最后批量查询-----------------------
//        addCostProductRequest.setProductSknIds(giftSKNs);
//        AddCostProductBo[] responseArray = serviceCaller.call("product.queryAddCostProducts", addCostProductRequest, AddCostProductBo[].class);
//        if (ArrayUtils.isNotEmpty(responseArray) && canNotSelectOtherGifts == false) {
//            PromotionGift gift = new PromotionGift(responseArray, promotionInfo, num, addCost.doubleValue(), addCost);
//            logger.debug("query gift {} from product success. and add to gift: {} gift success", responseArray, gift);
//            if (CollectionUtils.isNotEmpty(gift.getGoodsList())) {
//                chargeContext.setupPriceGift(gift);
//            }
//        }
        //----------------------放到最后批量查询-----------------------------------
    }
}
