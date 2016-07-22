package com.yoho.yhorder.shopping.charge;


import com.alibaba.fastjson.JSONObject;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.product.model.*;
import com.yoho.product.model.shopcart.ProductBasicInfoBo;
import com.yoho.product.model.shopcart.ProductShopCartBo;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.service.model.promotion.ProductBuyLimitBo;
import com.yoho.service.model.vip.VipInfo;
import com.yoho.yhorder.common.bean.ShoppingItem;
import com.yoho.yhorder.common.bean.ShoppingItemReq;
import com.yoho.yhorder.common.convert.BeanConvert;
import com.yoho.yhorder.common.convert.Convert;
import com.yoho.yhorder.dal.IShoppingCartDAO;
import com.yoho.yhorder.dal.IShoppingCartItemsDAO;
import com.yoho.yhorder.dal.model.ShoppingCart;
import com.yoho.yhorder.dal.model.ShoppingCartItems;
import com.yoho.yhorder.shopping.cache.GateCacheService;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.promotion.service.PromotionInfoRepository;
import com.yoho.yhorder.shopping.event.ShoppingCartItemDelEvent;
import com.yoho.yhorder.shopping.model.UserInfo;
import com.yoho.yhorder.shopping.service.ExternalDegradeService;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 构造changetext
 * Created by jipeng on 2015/12/11.
 */
@Component
public class ChargeContextFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected IShoppingCartDAO shoppingCartMapper;

    @Autowired
    protected IShoppingCartItemsDAO shoppingCartItemsDAO;

    /**
     * 支持降级
     */
    @Autowired
    private ExternalDegradeService externalDegradeService;

    @Autowired
    private PromotionInfoRepository promotionInfoRepository;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private GateCacheService gateCacheManager;


    public ChargeContext build(boolean selected, ChargeParam chargeParam) {
        logger.info("start to build charge context for request, selected {},chargeParam {}", selected, chargeParam);

        ChargeContext chargeContext = null;

        List<ShoppingItem> items = chargeParam.getShoppingItemList();

        if (CollectionUtils.isEmpty(items)) {
            //通过购物车购买
            chargeContext = buildByShoppingCart(selected, chargeParam);
            //TODO 5-30 version
        /*
            //uid 或者 shopping不为空
            if(chargeParam.getUid()>0||StringUtils.isNotEmpty(chargeParam.getShoppingKey())){
                //  if(CollectionUtils.isEmpty(reqItems)) {
                //通过购物车购买
                chargeContext = buildByShoppingCart(selected, chargeParam);
            }else
            {
                //TODO check local null
                //客户端发送过来的购物车信息
                List<ShoppingItemReq> reqItems = chargeParam.getReqShopCartItems();
                chargeContext = buildByShoppingCartFromReq(chargeParam, reqItems);
            }
			*/
        } else {
            chargeContext = buildByShoppingItems(chargeParam, items);
        }

        return chargeContext;
    }


    private ChargeContext buildByShoppingCart(boolean selected, ChargeParam chargeParam) {

        //logger.info("start to build chart for request: {} ", chargeParam);
        //0. 找到购物车
        ShoppingCart shoppingCart = this.getShoppingCart(chargeParam.getUid(), chargeParam.getShoppingKey());
        if (shoppingCart == null) {
            throw new ServiceException(ServiceError.SHOPPING_CART_IS_EMPTY);
        }
        //0.1 uid、shoppingkey可能有一个为空，所以查询到购物车后统一更新这两个值。
        chargeParam.setUid(shoppingCart.getUid());
        chargeParam.setShoppingKey(shoppingCart.getShoppingKey());
        //设置购物车id
        chargeParam.setShoppingCartId(shoppingCart.getId());

        ChargeContext chargeContext = new ChargeContext();
        //1.设置请求参数
        chargeContext.setChargeParam(chargeParam);

        //2.0查询购物车中商品的详细信息
        List<ShoppingCartItems> shoppingCartGoodsList = this.getChargeShoppingList(shoppingCart.getId(), selected, chargeParam.getUid());

        return doBuild(chargeParam, shoppingCartGoodsList);
    }

    /**
     * 非购物车
     *
     * @param chargeParam
     * @return
     */
    private ChargeContext buildByShoppingItems(ChargeParam chargeParam, final List<ShoppingItem> items) {

        // logger.info("start to build non car for request: {},items: {}", chargeParam, items);

        List<ShoppingCartItems> goodsList = transToShoppingCartItems(items);

        //TODO 删除啥？
        List<ShoppingCartItems> shoppingCartGoodsList = setupChargeGoods(goodsList);

        ChargeContext chargeContext = doBuild(chargeParam, shoppingCartGoodsList);

        return chargeContext;
    }

    /**
     * 客户端本地购物车
     *
     * @param chargeParam
     * @return
     */
    private ChargeContext buildByShoppingCartFromReq(ChargeParam chargeParam, List<ShoppingItemReq> shoppingItemsReq) {
        logger.info("start to build chart #buildByShoppingCartFromReq# for request: {} ", shoppingItemsReq);


        ChargeContext chargeContext = new ChargeContext();
        //1.设置请求参数
        chargeContext.setChargeParam(chargeParam);

        //2.0查询购物车中商品的详细信息
        Convert convert = new BeanConvert();
        List<ShoppingCartItems> shoppingCartGoodsList = new ArrayList<ShoppingCartItems>();
        shoppingCartGoodsList = convert.convertFromBatch(shoppingItemsReq, shoppingCartGoodsList, ShoppingCartItems.class);

        //获取详细信息
        final List<ShoppingCartItems> toChargeList = setupChargeGoods(shoppingCartGoodsList);


        return doBuild(chargeParam, toChargeList);
    }


    private List<ShoppingCartItems> transToShoppingCartItems(List<ShoppingItem> items) {
        List<ShoppingCartItems> goodsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(items)) {
            for (ShoppingItem item : items) {
                ShoppingCartItems _item = new ShoppingCartItems();
                _item.setId(0);
                _item.setShoppingCartId(0);
                _item.setPromotionId(0);
                _item.setStatus((byte) 1);
                _item.setSkuId(item.getSku());
                _item.setProductSkn(item.getSkn());
                _item.setNum(item.getBuyNumber());
                _item.setSelected(Constants.IS_SELECTED_Y);
                goodsList.add(_item);
            }
        }
        return goodsList;
    }

    private List<ChargeGoods> transToChargeGoods(List<ShoppingCartItems> shoppingCartGoodsList) {
        //2.1转化bean
        List<ChargeGoods> chargeGoodseList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(shoppingCartGoodsList)) {
            for (ShoppingCartItems shoppingCartGoods : shoppingCartGoodsList) {
                chargeGoodseList.add(new ChargeGoods(shoppingCartGoods));
            }
        }

        return chargeGoodseList;
    }

    private ChargeContext doBuild(ChargeParam chargeParam, List<ShoppingCartItems> shoppingCartGoodsList) {

        logger.debug("find charge items success: {} ", shoppingCartGoodsList);

        ChargeContext chargeContext = new ChargeContext();
        //1.设置请求参数
        chargeContext.setChargeParam(chargeParam);

        List<ChargeGoods> chargeGoodseList = transToChargeGoods(shoppingCartGoodsList);

        chargeContext.setChargeGoodsList(chargeGoodseList);

        //3.设置用户信息
        chargeContext.setUserInfo(this.getUserInfo(chargeParam));

        logger.debug("end to build chart for request: {} , context is {}", chargeParam, chargeContext);

        return chargeContext;
    }


    private UserInfo getUserInfo(ChargeParam chargeParam) {
        int uid = chargeParam.getUid();
        logger.info("query user level,uid is {}", uid);
        UserInfo userInfo = new UserInfo();
        //访客
        if (uid <= 0) {
            return userInfo;
        }

        userInfo.setUid(uid);

        VipInfo vipInfo = getUserLevel(uid);

        userInfo.setUserLevel(vipInfo.getCurVipInfo() == null ? 0 : Integer.parseInt(vipInfo.getCurVipInfo().getCurLevel()));
        //当月订单数量
        userInfo.setMonthOrderCount(externalDegradeService.queryCurrentMonthOrderCountFromWebClient(uid));

        if (chargeParam.isNeedQueryYohoCoin()) {
            //调用会员中心接口获取用户yoho币后，根据货币单位(元、角、分)，转换成货币进行算费
            userInfo.setOrderYohoCoin(externalDegradeService.queryUsableYohoCoin(uid));
        }

        if (chargeParam.isNeedQueryRedEnvelopes() && gateCacheManager.isOpenRedEnvelope()) {
            userInfo.setRedEnvelopes(externalDegradeService.queryUserRedEnvelopes(uid));
        }


        logger.debug("userinfo is {} of uid {}", userInfo, uid);

        return userInfo;

    }


    /**
     * 查询用户vip等级
     *
     * @param uid
     * @return
     */
    private VipInfo getUserLevel(int uid) {
        VipInfo vipInfo = null;
        try {
            vipInfo = externalDegradeService.queryVipDetailInfo(uid);
        } catch (ServiceException se) {
            logger.warn("query user {} vip info error,", uid, se);
            // vipInfo = null;
        }
        if (vipInfo == null) {
            //throw new Exception('没有用户信息.');
            throw new ServiceException(ServiceError.SHOPPING_PROFILE_IS_EMPTY);
        }
        return vipInfo;
    }

    /**
     * 根据用户id获取用户的yoho币数量，
     * 若单位为角、分，则需要将数量/10、数量/100
     *
     * @param uid
     * @return
     */


    /**
     * 根据shopping-cart-id 查询
     * 产品信息： 基本信息、价格、颜色、尺码、库存、活动、购买限制等信息
     */
    private List<ShoppingCartItems> getChargeShoppingList(int shoppingCartId, boolean selected, int uid) {

        //0. 从数据库表shopping_cart_items找出当前购物车商品
        List<ShoppingCartItems> goodsList = shoppingCartItemsDAO.selectShoppingCartGoods(shoppingCartId, (selected ? "Y" : null), uid);
        final List<ShoppingCartItems> toChargeList = setupChargeGoods(goodsList);
        return toChargeList;
    }

    public List<ShoppingCartItems> setupChargeGoods(List<ShoppingCartItems> goodsList) {

        //用于参数传递，holder了 skns， skus，ShoppingCartGoods
        ShoppingCartHolder holder = new ShoppingCartHolder(goodsList);

        //2.调用商品模块，获取购物车中商品的详细信息，用于计算价格
        final List<ShoppingCartItems> toChargeList = this.getProductInfo(holder);
        //

        //4. 设置购买SKN限制
        this.setupProductPromotionBuyLimit(holder.skns, toChargeList);

        //5.设置promotionType
        this.setupPromotionType(toChargeList);
        return toChargeList;
    }


    class ShoppingCartHolder {

        public List<Integer> skns = new LinkedList<>();
        public List<Integer> skus = new LinkedList<>();

        public List<ShoppingCartItems> goodsList = new LinkedList<>();

        public ShoppingCartHolder(List<ShoppingCartItems> _goodsList) {

            for (ShoppingCartItems goods : _goodsList) {

                skus.add(goods.getSkuId());

                skns.add(goods.getProductSkn());

                goodsList.add(goods);
            }

        }
    }

    private void setupPromotionType(List<ShoppingCartItems> toSetGoods) {
        for (ShoppingCartItems goods : toSetGoods) {
            if (goods.getPromotionId() < 1) {
                continue;
            }
            PromotionInfo promotionInfo = promotionInfoRepository.getPromotionById(goods.getPromotionId());
            if (promotionInfo == null) {
                logger.warn("promotion {} of shopping cart sku item {} not exist", goods.getPromotionId(), goods.getId());
                continue;
                //促销可能失效或被删除
                //throw new ServiceException(ServiceError.SHOPPING_PROMOTION_NOT_EXIST);
            }
            //促销不存在，应该不可能
            String promotionType = promotionInfo.getPromotionType();
            goods.getExtMap().put("promotion_type", promotionType);//促销类型
            if ("Gift".equals(promotionType)) {
                goods.getExtMap().put("last_price", new Double(0));//最终价格
                goods.getExtMap().put("real_price", new Double(0));
            } else if ("Needpaygift".equals(promotionType)) {
                String actionParam = promotionInfo.getActionParam();
                if (actionParam != null && StringUtils.isNotEmpty(actionParam)) {
                    Map<String, String> actionParamMap = JSONObject.parseObject(actionParam, Map.class);
                    if (actionParamMap != null && actionParamMap.containsKey("add_cost")) {
                        goods.getExtMap().put("last_price", new Double(actionParamMap.get("add_cost")));//最终价格
                        goods.getExtMap().put("real_price", new Double(actionParamMap.get("add_cost")));
                    }
                }
            }
        }
    }

    private void setupProductPromotionBuyLimit(List<Integer> sknIds, List<ShoppingCartItems> toSetLimitGoods) {

        if (CollectionUtils.isEmpty(sknIds)) {
            return;
        }

        Map<Integer, ProductBuyLimitBo> limits = this.getProductBuyLimitBySkns(sknIds);

        for (ShoppingCartItems goods : toSetLimitGoods) {
            ProductBuyLimitBo bo = limits.get(goods.getProductSkn());

            int limit = bo == null ? 0 : bo.getLevel0Limit();
            goods.getExtMap().put("buy_limit", limit);//商品限购数量
        }

    }

    /**
     * 根据SKN查出商品的购买限制。例如，一个商品最多卖出多少个
     * <p/>
     * 从yhb.product_buy_limit表中查找
     * 支持降级
     * @param skns
     * @return
     */
    private Map<Integer, ProductBuyLimitBo> getProductBuyLimitBySkns(List<Integer> skns) {
        logger.info("call service {} for skns {}", ShoppingConfig.PROMOTION_QUERY_PRODUCTBUYLIMIT_BYSKNIDS_REST_URL, skns);

        //promotion
        ProductBuyLimitBo[] productBuyLimitBos = externalDegradeService.queryProductBuyLimitBySknids(skns);

        //如果找不到，则设置一个默认
        Map<Integer, ProductBuyLimitBo> sknLimitMap = new HashMap<>();
        for (int i = 0; i < skns.size(); i++) {
            int skn = skns.get(i);

            ProductBuyLimitBo limitBO = this.findLimit(productBuyLimitBos, skn);
            if (limitBO != null) {
                sknLimitMap.put(skn, limitBO);
            }
        }

        return sknLimitMap;
    }

    private ProductBuyLimitBo findLimit(ProductBuyLimitBo[] founds, int skn) {

        if (ArrayUtils.isNotEmpty(founds)) {
            for (ProductBuyLimitBo bo : founds) {
                if (bo.getProductSkn().intValue() == skn) {
                    return bo;
                }
            }
        }
        return null;
    }


    /**
     * 通过sku查询产品、库存、商品、价格信息，并将这些信息赋值给ShoppingCartGoods
     */
    private List<ShoppingCartItems> getProductInfo(ShoppingCartHolder holder) {

        List<ShoppingCartItems> tochanger = new LinkedList<>();
        if (holder.goodsList.isEmpty()) {
            return tochanger;
        }

        ProductShopCartBo[] productShopCartBoArray = externalDegradeService.queryProductShoppingCartBySkuids(holder.skus);

        if (ArrayUtils.isEmpty(productShopCartBoArray)) {
            return tochanger;
        }

        List<ShoppingCartItems> delItems = new ArrayList<>();

        /**
         * 同一个购物车存在相同的sku，一个是普通的，另外一个是加价购的
         * 必須按照items来循环
         */
        for (ShoppingCartItems goods : holder.goodsList) {

            //
            if (goods.getNum() < 1) {
                delItems.add(goods);
                continue;
                //删除
            }

            ProductShopCartBo bo = getProductShopCartBoBySku(goods.getSkuId(), productShopCartBoArray);
            if (bo == null) {
                logger.warn("sku {} can't find the product info from {}", goods.getSkuId(), productShopCartBoArray);
                continue;
            }
            boolean mergedRet = doMerge(goods, bo);
            if (!mergedRet) {
                continue;
            }
            tochanger.add(goods);
        }

        publishCartItemDelEvent(delItems);


        return tochanger;
    }

    private ProductShopCartBo getProductShopCartBoBySku(int sku, ProductShopCartBo[] productShopCartBoArray) {
        if (ArrayUtils.isNotEmpty(productShopCartBoArray)) {
            for (ProductShopCartBo bo : productShopCartBoArray) {
                if (bo.getStorageBo().getErpSkuId() == sku) {
                    return bo;
                }
            }
        }

        return null;
    }


    private boolean doMerge(ShoppingCartItems goods, ProductShopCartBo bo) {
        if (bo == null || goods == null) {
            return false;
        }
        ProductBasicInfoBo productBasicInfoBo = bo.getProductBasicInfoBo();
        StorageBo storageBo = bo.getStorageBo();
        ProductPriceBo productPriceBo = bo.getProductPriceBo();
        GoodsBo goodsBo = bo.getGoodsBo();
        GoodsSizeBo goodsSizeBo = bo.getGoodsSizeBo();
        //xxxx???
        if (productBasicInfoBo == null || storageBo == null || productPriceBo == null || productPriceBo.getMarketPrice() < 1 || goodsBo == null || goodsSizeBo == null) {
            logger.warn("skip sku {} because productBasicInfoBo == null-> {},storageBo == null -> {},productPriceBo == null -> {},productPriceBo.getMarketPrice() < 1 -> {},goodsBo == null -> {},goodsSizeBo == null -> {}",
                    goods.getSkuId(), productBasicInfoBo == null, storageBo == null, productPriceBo == null, productPriceBo.getMarketPrice() < 1, goodsBo == null, goodsSizeBo == null);
            return false;
        }


        //设置产品信息
        goods.getExtMap().put("vip_price", productPriceBo.getVipPrice() == null ? "0" : productPriceBo.getVipPrice());
        goods.getExtMap().put("sales_price", productPriceBo.getSalesPrice() * 1.0);
        goods.getExtMap().put("real_price", productPriceBo.getSalesPrice() * 1.0);
        goods.getExtMap().put("market_price", productPriceBo.getMarketPrice() * 1.0);
        goods.getExtMap().put("last_vip_price", productPriceBo.getSalesPrice() * 1.0);
        goods.getExtMap().put("last_price", productPriceBo.getSalesPrice() * 1.0);

        goods.getExtMap().put("goods_images", StringUtils.isEmpty(goodsBo.getColorImage()) ? "" : goodsBo.getColorImage());
        goods.getExtMap().put("color_name", goodsBo.getColorName());
        goods.getExtMap().put("product_id", productBasicInfoBo.getId());
        goods.getExtMap().put("product_name", productBasicInfoBo.getProductName());
        goods.getExtMap().put("selected", goods.getSelected());
        goods.getExtMap().put("promotion_id", goods.getPromotionId());
        goods.getExtMap().put("color_id", goodsBo.getColorId());
        goods.getExtMap().put("goods_id", goodsBo.getId());
        goods.getExtMap().put("size_name", goodsSizeBo.getSizeName());
        goods.getExtMap().put("size_id", goodsSizeBo.getId());
        goods.getExtMap().put("storage_number", storageBo.getStorageNum());

        Integer gainYohoCoinNum = productPriceBo.getYohoCoinNum();
        if (gainYohoCoinNum == null) {
            gainYohoCoinNum = 0;
        }

        goods.getExtMap().put("yoho_coin_num", gainYohoCoinNum);
        goods.getExtMap().put("get_yoho_coin", gainYohoCoinNum * (null == goods.getNum() ? 0 : goods.getNum()));

        goods.getExtMap().put("real_vip_price", new Integer(0));//vip 价格
        goods.getExtMap().put("vip_discount_money", new Integer(0));//vip 金额
        goods.getExtMap().put("vip_discount_type", productPriceBo.getVipDiscountType() == null ? 0 : productPriceBo.getVipDiscountType());//VIP折扣类型
        goods.getExtMap().put("vip_discount", productPriceBo.getVipDiscount());//VIP折扣
        List<VipPriceBo> vipPriceBos = productPriceBo.getVipPrices();
        String vip1Prices = "0.00";
        String vip2Prices = "0.00";
        String vip3Prices = "0.00";
        if (CollectionUtils.isNotEmpty(vipPriceBos)) {
            for (VipPriceBo vipPriceBo : vipPriceBos) {
                if (vipPriceBo.getVipLevel() == 1 && org.apache.commons.lang.StringUtils.isNotEmpty(vipPriceBo.getVipPrice())) {
                    vip1Prices = vipPriceBo.getVipPrice();
                } else if (vipPriceBo.getVipLevel() == 2 && org.apache.commons.lang.StringUtils.isNotEmpty(vipPriceBo.getVipPrice())) {
                    vip2Prices = vipPriceBo.getVipPrice();
                } else if (vipPriceBo.getVipLevel() == 3 && org.apache.commons.lang.StringUtils.isNotEmpty(vipPriceBo.getVipPrice())) {
                    vip3Prices = vipPriceBo.getVipPrice();
                }
            }
        }

        goods.getExtMap().put("vip1_price", vip1Prices);//VIP1
        goods.getExtMap().put("vip2_price", vip2Prices);//VIP2
        goods.getExtMap().put("vip3_price", vip3Prices);//VIP3

        goods.getExtMap().put("brand_id", productBasicInfoBo.getBrandId());
        goods.getExtMap().put("is_limited", productBasicInfoBo.getIsLimited());
        goods.getExtMap().put("max_sort_id", productBasicInfoBo.getMaxSortId());
        goods.getExtMap().put("middle_sort_id", productBasicInfoBo.getMiddleSortId());
        goods.getExtMap().put("small_sort_id", productBasicInfoBo.getSmallSortId());
        goods.getExtMap().put("is_special", productBasicInfoBo.getIsSpecial());//是否特殊商品

        //货到付款限制 0、不支持货到付款  1、支持货到付款
        goods.getExtMap().put("can_cod_pay", "0".equals(productBasicInfoBo.getIsPayDelivery()) ? "N" : "Y");

        goods.getExtMap().put("promotion_flag", productBasicInfoBo.getIsPromotion());//促销ID
        goods.getExtMap().put("cn_alphabet", productBasicInfoBo.getCnAlphabet());
        goods.getExtMap().put("buy_number", goods.getNum());
        goods.getExtMap().put("is_advance", productBasicInfoBo.getIsAdvance());//是否预售
        goods.getExtMap().put("is_outlets", productBasicInfoBo.getIsOutlets());//outlets
        goods.getExtMap().put("attribute", productBasicInfoBo.getAttribute());//商品属性
        goods.getExtMap().put("uid", goods.getUid());
        goods.getExtMap().put("shopping_cart_id", goods.getShoppingCartId());
        goods.getExtMap().put("product_skn", goods.getProductSkn());
        goods.getExtMap().put("product_skc", goodsBo.getProductSkc());
        goods.getExtMap().put("product_sku", goods.getSkuId());

        //  $goodsVal['activities_id'] = $activitiesID;//活动ID

        //接口中已经做了 > 15的逻辑转换
        if (productBasicInfoBo.getExpectArrivalTime() != null && productBasicInfoBo.getExpectArrivalTime() > 0) {
            goods.getExtMap().put("expect_arrival_time", productBasicInfoBo.getExpectArrivalTime() + "月");
        } else {
            goods.getExtMap().put("expect_arrival_time", "");
        }


        //活动id
        goods.getExtMap().put("activities_id", bo.getProductActivityId());

        goods.getExtMap().put("is_jit", (StringUtils.isEmpty(productBasicInfoBo.getIsJit()) ? "N" : productBasicInfoBo.getIsJit()));//JIT
        goods.getExtMap().put("shop_id", productBasicInfoBo.getShopId());//店铺
        goods.getExtMap().put("supplier_id", productBasicInfoBo.getSupplierId());//供应商
        goods.getExtMap().put("shopping_cart_goods_id", goods.getId());

        if (productBasicInfoBo.getStatus() == 0) {
            //商品下架了
            goods.getExtMap().put("off_shelves", 1);
        }
        return true;
    }

    /**
     * 根据uid或者shopping key找到购物车对象
     *
     * @param uid
     * @param shoppingKey
     * @return
     */
    private ShoppingCart getShoppingCart(int uid, String shoppingKey) {
        ShoppingCart shoppingCart = null;
        if (uid > 0) {
            shoppingCart = shoppingCartMapper.selectShoppingCartByUid(uid);
        } else if (StringUtils.isNotEmpty(shoppingKey)) {
            shoppingCart = shoppingCartMapper.selectShoppingCartByShoppingKey(shoppingKey, 0);
        }
        return shoppingCart;
    }

    private void publishCartItemDelEvent(List<ShoppingCartItems> items) {
        if (CollectionUtils.isNotEmpty(items)) {
            this.publisher.publishEvent(new ShoppingCartItemDelEvent(items));
        }

    }


}