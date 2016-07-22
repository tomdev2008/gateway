package com.yoho.yhorder.shopping.charge.promotion.impl;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.product.model.ProductGiftBo;
import com.yoho.product.model.ProductPriceBo;
import com.yoho.product.model.StorageBo;
import com.yoho.product.model.promotion.AddCostProductBo;
import com.yoho.product.request.AddCostProductRequest;
import com.yoho.product.request.BatchBaseRequest;
import com.yoho.service.model.order.model.PromotionBO;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.service.model.order.response.shopping.PromotionGift;
import com.yoho.yhorder.dal.IShoppingCartItemsDAO;
import com.yoho.yhorder.dal.model.ShoppingCartItems;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.ChargeContextFactory;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.charge.model.DiscountType;
import com.yoho.yhorder.shopping.charge.promotion.AbstractPromotion;
import com.yoho.yhorder.shopping.service.impl.ShoppingCartAddService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 赠品
 *
 * @author LUOXC
 * @Time 2015/12/07
 */
@Component
public class Gift extends AbstractPromotion {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    @Autowired
    private ShoppingCartAddService shoppingCartAddService;

    @Autowired
    private IShoppingCartItemsDAO shoppingCartItemsDAO;

    @Autowired
    private ServiceCaller serviceCaller;

    @Autowired
    private ChargeContextFactory chargeContextFactory;

    public void doCompute(ChargeContext chargeContext, PromotionInfo promotionInfo) {
        logger.info("Start compute gift promotioin[{}].", promotionInfo);
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
        int userSelectGiftNumber = getUserSelectGiftNumber(promotionGiftNumber, fitGoodsGifts,mainGoodsGifts,promotionId);
        logger.info("userSelectGiftNumber is {}, promotionGiftNumber is {}.", userSelectGiftNumber, promotionGiftNumber);
        //自动添加赠品 必须是送一件
        boolean autoAddGiftToShoppingCartSuccess = true;
        if (promotionGiftNumber == 1 && userSelectGiftNumber == 0) {
            autoAddGiftToShoppingCartSuccess = autoAddGiftToShoppingCart(promotionId, giftList, chargeContext);
        }
        logger.info("autoAddGiftToShoppingCartSuccess is {}.", autoAddGiftToShoppingCartSuccess);
        // 没有赠品自动加入购物车
        if (!autoAddGiftToShoppingCartSuccess) {
            // 用户没有选择结束，查询出促销赠品加入到赠品列表中
            if (userSelectGiftNumber != promotionGiftNumber) {
                // 查询促销赠送的商品
                AddCostProductRequest addCostProductRequest = new AddCostProductRequest();
                addCostProductRequest.setProductSknIds(giftList);

                /**
                 * 从商品服务中，获取所有skn的信息，包括库存
                 */
                // NOTE:goodsList 中每个对象有固定字段last_price=0
                AddCostProductBo[] goodsList = serviceCaller.call("product.queryAddCostProducts", addCostProductRequest, AddCostProductBo[].class);

                if (goodsList != null && goodsList.length > 0) {
                    PromotionGift gift = new PromotionGift(goodsList, promotionInfo, String.valueOf(promotionGiftNumber), 0, null);
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


    /**
     * 获取满足促销的赠品
     *
     * @param promotionId
     * @param mainGoodsGifts
     * @return
     */
    protected List<ChargeGoods> getFitGoodsGifts(int promotionId, List<ChargeGoods> mainGoodsGifts) {
        List<ChargeGoods> hitGoodsGifts = new ArrayList<>();
        for (ChargeGoods mainGoodsGift : mainGoodsGifts) {
            if (mainGoodsGift.getShoppingGoods().getPromotion_id().equals("" + promotionId) && "Y".equals(mainGoodsGift.getShoppingGoods().getSelected())) {
                hitGoodsGifts.add(mainGoodsGift);
            }
        }
        logger.info("Find {} fit goods gifts of promotion[{}].", hitGoodsGifts.size(), promotionId);
        return hitGoodsGifts;
    }

    /**
     * 更新赠品销售价格
     *
     * @param add_cost
     * @param hitGoodsGifts
     */
    protected void updateGiftSalePrice(String add_cost, List<ChargeGoods> hitGoodsGifts) {
        double addCost = 0;
        if (StringUtils.isNotEmpty(add_cost)) {
            addCost = Double.parseDouble(add_cost);
        }
        for (ChargeGoods hitGoodsGift : hitGoodsGifts) {
            hitGoodsGift.getShoppingGoods().setSale_price(addCost);
        }
    }

    /**
     * 获取用户选择的赠品数量，并删除多选的赠品
     * update 删除fitGoodsGifts和mainGoodsGifts 内多选择的赠品
     * @param promotionGiftNumber
     * @param fitGoodsGifts
     * @param mainGoodsGifts
     * @return
     */
    protected int getUserSelectGiftNumber(int promotionGiftNumber, List<ChargeGoods> fitGoodsGifts,List<ChargeGoods> mainGoodsGifts,int promotionId) {
        int userSelectGiftNumber = 0;
        //TODO frwnote 循环里面操作数据库 mark
        int moreSelectGiftNum=0;
        for (ChargeGoods goodsGift : fitGoodsGifts) {
            userSelectGiftNumber += goodsGift.getBuyNumber();
            // 用户选择的比促销赠送的多
            if (userSelectGiftNumber > promotionGiftNumber) {
                // 用户多购买的赠品数量，从购买车删除
                //更改为物理删除
                logger.info("getUserSelectGiftNumber : deleteCartGoodsByPromotionID  {} ",goodsGift);
                shoppingCartItemsDAO.deleteCartGoodsByPromotionID(goodsGift.getShoppingGoods().getShopping_cart_id(),
                        goodsGift.getShoppingGoods().getUid(),
                        String.valueOf(goodsGift.getShoppingGoods().getPromotion_id()),
                        userSelectGiftNumber - promotionGiftNumber);
//                shoppingCartItemsDAO.updateCartGoodsByPromotionID(goodsGift.getShoppingGoods().getShopping_cart_id(),
//                        goodsGift.getShoppingGoods().getUid(),
//                        String.valueOf(goodsGift.getShoppingGoods().getPromotion_id()),
//                        userSelectGiftNumber - promotionGiftNumber, "0");
                //记录需要删除的赠品数量
                moreSelectGiftNum++;
                goodsGift.getShoppingGoods().setBuy_number("" + promotionGiftNumber);
                userSelectGiftNumber = promotionGiftNumber;
            }
        }
        //删除内存列表中多选的赠品
        //这个列表下的商品肯定是符合当前优惠的了，所以不需要再判断promotionId
        if (moreSelectGiftNum > 0) {
            Iterator<ChargeGoods> it = fitGoodsGifts.iterator();
            while (it.hasNext()) {
                ChargeGoods chargeGoods = it.next();
                if (moreSelectGiftNum > 0) {
                    logger.info("moreGiftNum {}  prepare delete gift sku {} ", moreSelectGiftNum, chargeGoods.getShoppingGoods().getProduct_sku());
                    it.remove();
                    moreSelectGiftNum--;
                } else {
                    break;
                }
            }
        }


        // 删除MainGoodsGift多选赠品
        Iterator<ChargeGoods> it=mainGoodsGifts.iterator();
        while (it.hasNext()){
            ChargeGoods chargeGoods=it.next();
            //首先是选中的且为当前优惠活动的赠品
            if(chargeGoods.getShoppingGoods().getPromotion_id().equals("" + promotionId)
                    && chargeGoods.getShoppingGoods().getSelected().equals("Y")) {
                //mainGoods中的赠品不在fitGoodsGifts中，那么需要删除
                if (!fitGoodsGifts.contains(chargeGoods)) {
                    logger.info("delete more select gift {}", chargeGoods);
                    it.remove();
                }
            }
        }
        return userSelectGiftNumber;
    }

    /**
     * 获取赠品
     *
     * @param actionParam
     * @return
     */
    protected List<Integer> getGifts(JSONObject actionParam) {
        String gift_list = actionParam.getString("gift_list");
        if (StringUtils.isEmpty(gift_list)) {
            return Collections.emptyList();
        }
        List<Integer> giftList = new ArrayList<>();
        String[] giftArray = gift_list.split(",");
        for (String productSkn : giftArray) {
            giftList.add(Integer.valueOf(productSkn.trim()));
        }
        return giftList;
    }

    /**
     * 自动添加赠品到购物车
     *
     * @param promotionId
     * @param giftList
     * @param chargeContext
     * @return 是否添加成功
     */
    protected boolean autoAddGiftToShoppingCart(int promotionId, List<Integer> giftList, ChargeContext chargeContext) {
        BatchBaseRequest<Integer> request = new BatchBaseRequest<>();
        request.setParams(giftList);
        ProductGiftBo[] productGiftBos = serviceCaller.call("product.queryProductGiftBySkns", request, ProductGiftBo[].class);
        return this.autoAddGiftToShoppingCart(promotionId,giftList,chargeContext,productGiftBos);
    }


    /**
     * 自动添加赠品到购物车
     *
     * @param promotionId
     * @param giftList
     * @param chargeContext
     * @return 是否添加成功
     */
    protected boolean autoAddGiftToShoppingCart(int promotionId, List<Integer> giftList, ChargeContext chargeContext,ProductGiftBo[] productGiftBos) {
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        // 查找符合自动添加的赠品
        List<Map<String, Object>> fitAutoAddProductGifts = new ArrayList<>();
        for (Integer productSkn : giftList) {
            ProductGiftBo productGiftBo = findProductByProductSkn(productSkn, productGiftBos);
            // 商品不存在
            if (productGiftBo == null) {
                logger.debug("can not find product[skn={}].", productSkn);
                continue;
            }
            // 商品是预售商品
            if ("Y".equals(productGiftBo.getProductBo().getIsAdvance())) {
                logger.info("the product[skn={}] is advance product.", productSkn);
                continue;
            }
            // 商品已下架
            if (productGiftBo.getProductBo().getStatus() == 0) {
                logger.info("the product[skn={}] has taken off shelves.", productSkn);
                continue;
            }
            List<StorageBo> storages = productGiftBo.getStorageBoList();
            // 判断goods数量多于1，则加入到礼品中
            Set<Integer> hitGoods = new HashSet<>();
            for (StorageBo storage : storages) {
                hitGoods.add(storage.getGoodsId());
            }
            if (hitGoods.size() == 0) {
                continue;
            } else if (hitGoods.size() > 1) {
                //这种情况skc有多种，不自动赠送，以免赠送尺码不合适
                logger.warn("hitGoods size is {}", hitGoods.size());
                return false;
            }
            // 判断有没有有没有库存
            int hitStorage = 0;
            int productSku = 0;
            for (StorageBo storage : storages) {
                if (storage.getStorageNum() > 0) {
                    hitStorage++;
                    productSku = storage.getErpSkuId();
                }
            }
            //hitStorage指的是sku数量，只有sku数量为1的时候才自动赠送，并且sku>=1
            if (hitStorage == 0) {
                continue;
            } else if (hitStorage > 1 || productSku < 1) {
                logger.warn("hitStorage size is {}, productSku is {}", hitStorage, productSku);
                return false;
            }
            Map<String, Object> fitAutoAddProductGift = new HashMap<>();
            fitAutoAddProductGift.put("productSkn", productSkn);
            fitAutoAddProductGift.put("productSku", productSku);
            fitAutoAddProductGift.put("productGiftBo", productGiftBo);
            fitAutoAddProductGifts.add(fitAutoAddProductGift);
            //TODO 循环第二次到这的时候 是否可以直接返回 不需要再进行判断
        }
        //符合赠送的商品数目大于1 则自动添加，否则不添加
        if (fitAutoAddProductGifts.size() == 1) {
            int productSkn = (Integer) fitAutoAddProductGifts.get(0).get("productSkn");
            int productSku = (Integer) fitAutoAddProductGifts.get(0).get("productSku");
            ProductGiftBo productGiftBo = (ProductGiftBo) fitAutoAddProductGifts.get(0).get("productGiftBo");
            logger.info("Start AutoAddGiftToShoppingCart: product[skn={},sku={}] ProductGift[{}].", productSkn, productSku, productGiftBo);
            try {
                // 将商品自动加入购物车
                ProductPriceBo productPrice = productGiftBo.getProductPriceBo();
                chargeTotal.setDiscountAmount(chargeTotal.getDiscountAmount() + productPrice.getSalesPrice());
                addToShoppingCart(promotionId, productSku, chargeContext);
                List<ChargeGoods> chargeGoodsList = makeCartGoods(promotionId, productSkn, productSku, chargeContext);
                //对sku进行分类成普通商品、gift商品、pride_gift等
                //TODO 是否可以直接加到mainGiftList
                chargeContext.classfic(chargeGoodsList);
                logger.info("AutoAddGiftToShoppingCart success: product[skn={},sku={}]to shopping cart success of promotion[{}].", productSkn, productSku, promotionId);
                return true;
            } catch (Exception e) {
                logger.warn("AutoAddGiftToShoppingCart fail: add product[skn={},sku={}]to shopping cart fail of promotion[{}].", productSkn, productSku, promotionId, e);
                return false;
            }
        } else {
            logger.info("Find {} fit AutoAddProductGifts.", fitAutoAddProductGifts.size());
            return false;
        }
    }

    private void addToShoppingCart(int promotionId, int productSku, ChargeContext chargeContext) {

        // 添加购物车
        ShoppingCartRequest shoppingCartRequest = new ShoppingCartRequest();
        shoppingCartRequest.setProduct_sku(productSku);
        shoppingCartRequest.setBuy_number(1);
        shoppingCartRequest.setShopping_key(chargeContext.getChargeParam().getShoppingKey());
        shoppingCartRequest.setUid(chargeContext.getChargeParam().getUid());
        shoppingCartRequest.setPromotion_id(promotionId);
        shoppingCartAddService.add(shoppingCartRequest);
    }


    private ProductGiftBo findProductByProductSkn(int productSkn, ProductGiftBo[] productGiftBos) {
        for (ProductGiftBo productGiftBo : productGiftBos) {
            if (productSkn == productGiftBo.getProductBo().getErpProductId()) {
                return productGiftBo;
            }
        }
        return null;
    }

    private List<ChargeGoods> makeCartGoods(int promotionId, int productSkn, int productSku, ChargeContext chargeContext) {
        ShoppingCartItems goods = new ShoppingCartItems();
        //自动赠送的，id为0，下单后，这个赠送sku，不会被逻辑删除，因为逻辑删除是根据id来的，
        //再次进入购物车时，不会显示这个赠送sku
        //因为赠品是刚刚插入表的，并没有获取到赠品在购物车中的id
        goods.setId(0);
        goods.setUid(chargeContext.getChargeParam().getUid());
        goods.setNum(1);
        goods.setShoppingCartId(chargeContext.getChargeParam().getShoppingCartId());
        // goods.put("product_skc", productSkc);
        goods.setProductSkn(productSkn);
        goods.setSkuId(productSku);
        goods.setPromotionId(promotionId);
        goods.setSelected("Y");
        List<ShoppingCartItems> goodses = chargeContextFactory.setupChargeGoods(Collections.singletonList(goods));
        List<ChargeGoods> chargeGoodsList = new LinkedList<>();
        for (ShoppingCartItems shoppingCartGoods : goodses) {
            chargeGoodsList.add(new ChargeGoods(shoppingCartGoods));
        }
        return chargeGoodsList;
    }
}
