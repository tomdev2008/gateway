package com.yoho.yhorder.shopping.charge.promotion.service;

import com.yoho.product.model.ProductGiftBo;
import com.yoho.product.model.promotion.AddCostProductBo;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.service.model.order.response.shopping.PromotionGift;
import com.yoho.yhorder.dal.IShoppingCartItemsDAO;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.promotion.AbstractPromotion;
import com.yoho.yhorder.shopping.charge.promotion.impl.Gift2;
import com.yoho.yhorder.shopping.service.ExternalDegradeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 针对购物车的促销服务
 * Created by chunhua.zhang@yoho.cn on 2015/12/17.
 */
@Service
public class CartPromotionService {

    private final static Logger logger = LoggerFactory.getLogger(CartPromotionService.class);

    @Autowired
    private PromotionInfoRepository promotionInfoRepository;


    // 满减
    @Autowired
    @Qualifier("cashReduce")
    private AbstractPromotion cashReduce;

    @Autowired
    @Qualifier("degressdiscount")
    private AbstractPromotion degressdiscount;

    @Autowired
    @Qualifier("changeshippingfee")
    private AbstractPromotion changeshippingfee;

    @Autowired
    @Qualifier("cheapestfree")
    private AbstractPromotion cheapestfree;

    @Autowired
    @Qualifier("discount")
    private AbstractPromotion discount;

    @Autowired
    @Qualifier("gift2")
    private AbstractPromotion gift;

    //加价购
    @Autowired
    @Qualifier("needpaygift")
    private AbstractPromotion needpaygift;
    @Autowired
    @Qualifier("specifiedAmount")
    private AbstractPromotion specifiedAmount;
    @Autowired
    @Qualifier("vipfixeddiscount")
    private AbstractPromotion vipfixeddiscount;

    @Autowired
    private IShoppingCartItemsDAO shoppingCartItemsDAO;

    @Autowired
    private Gift2 giftImpl;

    @Autowired
    private ExternalDegradeService externalDegradeService;

//    @Autowired
//    private ServiceCaller serviceCaller;

    public int caculatePromotins(ChargeContext chargeContext) {

        logger.info("Start to do promotion caculate for param:{}", chargeContext.getChargeParam());

        List<PromotionInfo> promotionInfos = promotionInfoRepository.getActivePromotionsByFitChannel();

        if (promotionInfos == null) {
            logger.info("can not get any promotion info from database!");
            return 0;
        }


        final List<PromotionInfo> fitPromotion = new LinkedList<>();

        for (PromotionInfo promotionInfo : promotionInfos) {

            if (StringUtils.isEmpty(promotionInfo.getPromotionType())) {
                continue;
            }

            logger.debug("Begin to do promotion caculate. Promotion Title:{},  ChargeContext: {} ", promotionInfo.getTitle(), chargeContext);

            AbstractPromotion promotionIns = null;

            switch (promotionInfo.getPromotionType()) {

                case "Cashreduce": {//满减
                    promotionIns = this.cashReduce;
                    break;
                }
                case "Degressdiscount": {//分件折扣
                    promotionIns = this.degressdiscount;
                    break;
                }
                case "Changeshippingfee": { //调整运费
                    promotionIns = this.changeshippingfee;
                    break;
                }
                case "Cheapestfree": {//满X免1
                    promotionIns = this.cheapestfree;
                    break;
                }
                case "Discount": {//打折
                    promotionIns = this.discount;
                    break;
                }

                case "Gift": {//赠品
                    promotionIns = this.gift;
                    break;
                }
                case "Needpaygift": {//加价购
                    promotionIns = this.needpaygift;
                    break;
                }

                case "SpecifiedAmount": {//X件X元
                    promotionIns = this.specifiedAmount;
                    break;
                }

                case "Vipfixeddiscount": {//VIP固定折扣
                    promotionIns = this.vipfixeddiscount;
                    break;
                }
            }

            //找不到促销实现类。
            if (promotionIns == null) {
                logger.warn("can not find suitable promotion for type: {}", promotionInfo.getPromotionType());
                continue;
            }


            //计算促销
            boolean isMatch = promotionIns.compute(promotionInfo, chargeContext);

            if (isMatch) {
                fitPromotion.add(promotionInfo);
                logger.debug("End to do match promotion caculate. Promotion Type:{},  ChargeContext: {} ", promotionInfo.getTitle(), chargeContext);
            } else {
                logger.debug("End to do un-match promotion caculate. Promotion:{},  ChargeContext: {} ", promotionInfo.getTitle(), chargeContext);
            }

        }

        //获取所有赠品和加价购商品信息
        AddCostProductBo[] allAddCostProductBos = queryAddCostProducts(chargeContext);

        /**
         * 重新处理赠品
         */
        this.processGiftPromotion(chargeContext,allAddCostProductBos);

        /**
         * 最后批量查询加价购
         */
        this.updatePriceGiftStorage(chargeContext,allAddCostProductBos);

        /**
         *  最后检查一下
         */
        this.checkGifts(chargeContext);

        logger.info("Promotion total match  : {} for param:{}", dumpPromotionTitle(fitPromotion), chargeContext.getChargeParam());

        return fitPromotion.size();
    }


    private Set<String> dumpPromotionTitle(List<PromotionInfo> promotionInfos) {
        return promotionInfos.stream().map(PromotionInfo::getTitle).collect(Collectors.toSet());
    }


    private void checkGifts(ChargeContext chargeContext) {

        Map<String, List<Integer>> promotionSKNS = chargeContext.getPromotionIDS();

        logger.info("check gifts: promotion skns: {}", promotionSKNS);

        //mainGoodsPriceGift
        Iterator<ChargeGoods> it = chargeContext.getMainGoodsPriceGift().iterator();
        while (it.hasNext()) {
            ChargeGoods chargeGoods = it.next();
            List<Integer> skns = promotionSKNS.get(String.valueOf(chargeGoods.getShoppingGoods().getPromotion_id()));
            logger.debug("checkGifts [getMainGoodsPriceGift] skns : {}, charGoods:{} ", skns, chargeGoods);
            if (CollectionUtils.isEmpty(skns) || !skns.contains(Integer.parseInt(chargeGoods.getShoppingGoods().getProduct_skn()))) {
                this.deleteChartItem(chargeContext.getChargeParam().getShoppingCartId(), Integer.parseInt(chargeGoods.getShoppingGoods().getProduct_sku()),
                        new Integer(chargeGoods.getShoppingGoods().getPromotion_id()),chargeContext.getChargeParam().getUid());
                it.remove();
            }
        }


        //mainGoodsGift
        it = chargeContext.getMainGoodsGift().iterator();
        while (it.hasNext()) {
            ChargeGoods chargeGoods = it.next();
            List<Integer> skns = promotionSKNS.get(String.valueOf(chargeGoods.getShoppingGoods().getPromotion_id()));
            logger.debug("checkGifts [getMainGoodsGift] skns : {}, charGoods:{} ", skns, chargeGoods);
            if (CollectionUtils.isEmpty(skns) || !skns.contains(Integer.parseInt(chargeGoods.getShoppingGoods().getProduct_skn()))) {
                //TODO 加判断本地购物车查询不需要更新库
                this.deleteChartItem(chargeContext.getChargeParam().getShoppingCartId(), Integer.parseInt(chargeGoods.getShoppingGoods().getProduct_sku()),
                        new Integer(chargeGoods.getShoppingGoods().getPromotion_id()),chargeContext.getChargeParam().getUid());
                it.remove();
            }
        }
    }

    /**
     * deleteChartItem from shopping_cart_items where `sku_id`=:product_sku and `shopping_cart_id`=:shopping_cart_id
     */
    private void deleteChartItem(int shoppingCartId, int skuId,Integer promotionId,int uid) {
        logger.info("[{}] delete the expired promotional sku,skuId: {},shoppingCartId:{},promotionId:{}",uid, skuId, shoppingCartId, promotionId);
        //TODO 更改为delete
      // shoppingCartItemsDAO.updateCartGoodsBySKU(shoppingCartId, skuId, 0,promotionId);
        shoppingCartItemsDAO.deleteCartGoodsBySKU(shoppingCartId, skuId,promotionId,uid);
    }

    private void processGiftPromotion(ChargeContext chargeContext,AddCostProductBo[] allAddCostProductBos) {

        List<PromotionGift> gifts = new ArrayList<>();
        gifts.addAll(chargeContext.getGiftList());
        chargeContext.getGiftList().clear();

        if (CollectionUtils.isNotEmpty(gifts)) {
            ProductGiftBo[] allProductGiftBos = queryProductGift(gifts);

            for (PromotionGift gift : gifts) {
                AddCostProductBo[] addCostProductBos = findAddCostProductBoByProductSkns(gift.getGiftSkns(), allAddCostProductBos);
                ProductGiftBo[] productGiftBos = findProductGiftBoByProductSkns(gift.getGiftSkns(), allProductGiftBos);
                giftImpl.process(chargeContext, gift.getPromotionInfo(), addCostProductBos, productGiftBos);
            }
        }

    }


    /**
     * 查询skn库存及状态,若无库存或已下架,则不添加到最终的gift中
     * @param chargeContext
     * @param allAddCostProductBos
     */
    private void updatePriceGiftStorage(ChargeContext chargeContext,AddCostProductBo[] allAddCostProductBos) {

        List<PromotionGift> tmpGiftList = new ArrayList<>();
        tmpGiftList.addAll(chargeContext.getPriceGiftList());

        List<PromotionGift> finalGifts = new ArrayList<>();

        for (PromotionGift gift : tmpGiftList) {
            AddCostProductBo[] _productBos = findAddCostProductBoByProductSkns(gift.getGiftSkns(), allAddCostProductBos);
            if (_productBos.length > 0) {
                gift.addCostProductBos(_productBos);
            }

            if (gift.getGoodsList().size() > 0) {
                finalGifts.add(gift);
            }
        }


        chargeContext.getPriceGiftList().clear();
        if (CollectionUtils.isNotEmpty(finalGifts)) {
            chargeContext.getPriceGiftList().addAll(finalGifts);
        }
    }

    private AddCostProductBo[] queryAddCostProducts(ChargeContext chargeContext) {
        AddCostProductBo[] productBos = null;
        List<PromotionGift> giftList = meargePromoitonGift(chargeContext.getGiftList(),chargeContext.getPriceGiftList());

        List<Integer> giftSkns = meargeSkn(giftList);

        if (CollectionUtils.isNotEmpty(giftSkns)) {
            productBos = externalDegradeService.queryAddCostProducts(giftSkns);
        }
        return productBos;
    }

    private AddCostProductBo[] findAddCostProductBoByProductSkns(List<Integer> skns, AddCostProductBo[] productBos) {
        List<AddCostProductBo> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(skns) && ArrayUtils.isNotEmpty(productBos)) {

            for (Integer skn : skns) {
                for (AddCostProductBo productBo : productBos) {
                    if (skn.intValue() == productBo.getProductSkn().intValue()) {
                        resultList.add(productBo);
                        break;
                    }
                }
            }
        }
        return resultList.toArray(new AddCostProductBo[resultList.size()]);
    }

    private ProductGiftBo[] findProductGiftBoByProductSkns(List<Integer> skns, ProductGiftBo[] productGiftBos) {
        List<ProductGiftBo> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(skns) && ArrayUtils.isNotEmpty(productGiftBos)) {

            for (Integer skn : skns) {
                for (ProductGiftBo giftBo : productGiftBos) {
                    if (skn.intValue() == giftBo.getProductBo().getErpProductId()) {
                        resultList.add(giftBo);
                        break;
                    }
                }
            }
        }
        return resultList.toArray(new ProductGiftBo[resultList.size()]);
    }

    private ProductGiftBo[] queryProductGift(List<PromotionGift> giftList) {
        ProductGiftBo[] productGiftBos = null;
        List<Integer> giftSkns = meargeSkn(giftList);
        if (CollectionUtils.isNotEmpty(giftSkns)) {
            productGiftBos = externalDegradeService.queryProductGiftBySkns(giftSkns);
        }
        return productGiftBos;
    }

    private List<Integer> meargeSkn(List<PromotionGift> gifts)
    {
        Set<Integer> sets = new HashSet<>();
        for(PromotionGift gift:gifts)
        {
            sets.addAll(gift.getGiftSkns());
        }

        return new ArrayList<>(sets);
     }


    private List<PromotionGift> meargePromoitonGift(List<PromotionGift> giftList,List<PromotionGift> priceGiftList)
    {
        List<PromotionGift> meargedList = new ArrayList<>();
        meargedList.addAll(giftList);
        meargedList.addAll(priceGiftList);
        return  meargedList;
    }
}
