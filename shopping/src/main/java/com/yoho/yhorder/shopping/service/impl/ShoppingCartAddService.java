package com.yoho.yhorder.shopping.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.product.model.ProductBo;
import com.yoho.product.model.StorageBo;
import com.yoho.product.request.BaseRequest;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.service.model.order.request.ShoppingReAddRequest;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.OrdersGoods;
import com.yoho.service.model.order.response.shopping.ShoppingAddResponse;
import com.yoho.yhorder.common.bean.ShoppingItem;
import com.yoho.yhorder.common.cache.redis.ShoppingCartRedis;
import com.yoho.yhorder.dal.*;
import com.yoho.yhorder.dal.model.ShoppingCart;
import com.yoho.yhorder.dal.model.ShoppingCartItems;
import com.yoho.yhorder.shopping.charge.promotion.service.PromotionInfoRepository;
import com.yoho.yhorder.shopping.service.ExternalService;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MyStringUtils;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Created by JXWU on 2015/11/17.
 * 添加购物车
 */
@Service
public class ShoppingCartAddService {

    private final Logger logger = LoggerFactory.getLogger("addPaymentComputeLog");
    
    @Autowired
    private ShoppingCartRedis shoppingCartRedis;

    @Autowired
    private IShoppingCartDAO shoppingCartMapper;

    @Autowired
    private IShoppingCartItemsDAO shoppingCartItemsDAO;

    @Autowired
    private IOrderExtAttributeDAO orderExtAttributeDAO;

    @Autowired
    private ServiceCaller serviceCaller;

    @Autowired
    private PromotionInfoRepository promotionInfoRepository;

    @Autowired
    private ExternalService externalService;

    @Autowired
    IOrdersMapper ordersMapper;

    @Autowired
    private IOrdersGoodsMapper ordersGoodsMapper;

    public ShoppingAddResponse add(ShoppingCartRequest request) {
        logger.info("enter shopping cart add service,request is {}", request);
        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), request.getShopping_key());

        //1.校验sku库存
        StorageBo storageBo = checkAndGetStorage(request.getProduct_sku(), request.getBuy_number());
        //2.校验产品是否存在及属性
        ProductBo productBo = checkProductStatusAndGetProduct(storageBo.getProductId());

        //3.yohood电子票，暂不实现
        //updateYohoodSkuBuyNumber();

        //4.根据uid或shoppingkey获取购物车，若没有查询到购物车，则新建shoppingkey,然后购物车
        //4.1访客，购物车中uid为0
        ShoppingCart shoppingCart = createShoppingCartIfNecessary(request.getUid(), request.getShopping_key());

        //5.判断sku购买的总款数以及每个sku的购买数量是否超过限制
        checkSkuTotalNumAndBuyNumber(shoppingCart.getId(), request.getProduct_sku(), request.getBuy_number(),request.getUid());

        //6.核实skn是否属于本次促销活动
        verifySknJoinPromotion(request.getPromotion_id(), productBo);

        //7.将sku更新到数据库表shopping_cart_items中
        int buyNumber = doAdd(request, productBo, shoppingCart);

        //8.返回结果
        ShoppingAddResponse addResponse = new ShoppingAddResponse();
        addResponse.setShopping_key(shoppingCart.getShoppingKey());
        addResponse.setGoods_count(String.valueOf(buyNumber));

        logger.info("exit shopping cart add service,request is: {}\n,response is: {}\n", request, addResponse);

        return addResponse;
    }

    /**
     * 将商品信息更新到数据库中
     *
     * @param request
     * @param productBo
     * @param shoppingCart
     * @return 返回更新后的sku购买数量
     */
    private int doAdd(ShoppingCartRequest request, ProductBo productBo, ShoppingCart shoppingCart) {
        //7.1获取购物车items中sku的已购买的数量
        int alreadyBuyGoodsNum = shoppingCartItemsDAO.sumShoppingCartGoodsByProductSku(shoppingCart.getId(), request.getProduct_sku(), request.getPromotion_id(),shoppingCart.getUid());
        //7.2若items表购买数量为0，插入，否则累加购买数量
        if (alreadyBuyGoodsNum == 0) {
            int erp_product_id = productBo.getErpProductId();
            int status = 1;
            shoppingCartItemsDAO.insertShoppingCartGoods(shoppingCart.getUid(), shoppingCart.getId(), erp_product_id, request.getProduct_sku(), request.getBuy_number(), request.getPromotion_id(), request.getSelected(), status,shoppingCart.getShoppingKey());
        } else {
            shoppingCartItemsDAO.updateShoppingCartGoodsNumber(shoppingCart.getId(), request.getProduct_sku(), request.getBuy_number(),request.getPromotion_id(),shoppingCart.getUid());
        }
        return shoppingCartItemsDAO.sumShoppingCartGoods(shoppingCart.getId(),shoppingCart.getUid());
    }

    /**
     * 校验sku库存数量跟用户购物的数量
     *
     * @return
     * @throws ServiceException
     */
    private StorageBo checkAndGetStorage(Integer skuId, int buyNum) throws ServiceException {
        //根据SKU获取库存信息
        logger.info("call service {} for sku {}", ShoppingConfig.PRODUCT_QUERY_PRODUCTDETAIL_BYSKU_URL, skuId);
        BaseRequest<Integer> baseRequest = new BaseRequest<Integer>();
        baseRequest.setParam(skuId);
        StorageBo storageBo = serviceCaller.call(ShoppingConfig.PRODUCT_QUERY_PRODUCTDETAIL_BYSKU_URL, baseRequest, StorageBo.class);
        int storageNum = storageBo != null ? storageBo.getStorageNum() : 0;
        //库存数量小于用户期望购物的数量，抛出异常
        if (storageNum < buyNum) {
            logger.info("product_sku {} ,storage number {} < buy number {}", skuId, storageNum, buyNum);
            //throw new Exception('你选择的商品 ' . $package->product_name . ' 库存不足.');
            throw new ServiceException(ServiceError.SHOPPING_BUSCHECK_STORAGE_NUM_NOT_SUPPORT);
        }
        return storageBo;
    }

    private ProductBo checkProductStatusAndGetProduct(Integer productId) throws ServiceException {
        logger.info("call service {} to query product info,product id {}", ShoppingConfig.PRODUCT_QUERY_PRODUCTDETAILINFO_URL, productId);
        BaseRequest<Integer> baseRequest;
        baseRequest = new BaseRequest<>();
        baseRequest.setParam(productId);
        ProductBo productBo = serviceCaller.call(ShoppingConfig.PRODUCT_QUERY_PRODUCTDETAILINFO_URL, baseRequest, ProductBo.class);

        if (productBo == null) {
            //  throw new Exception('没有这个产品.');
            logger.warn("product id {} not find", productId);
            throw new ServiceException(ServiceError.SHOPPING_PRODUCT_NOT_EXIST);
        }
        logger.debug("product is {} of productid {}", productBo, productId);

        /**
         *  /**
         * 订单属性
         * 1、正常订单
         * 2、
         * 3、虚拟订单
         * 4、
         * 5、预售订单
         * 6、
         * 7、特殊订单
         *
         * @var string
         */
        if (productBo.getAttribute() == Constants.ATTRIBUTE_VIRTUAL) {
            logger.warn("product attribute value is 3,not support,product info is {}", productBo);
            //throw new Exception('你当前的版本不支持电子票的购买，请更新版本后再试.');
            throw new ServiceException(ServiceError.SHOPPING_PRODUCT_ATTRIBUTE_NOT_SUPPORT);
        }

        if ("Y".equals(productBo.getIsLimitBuy())) {
            logger.warn("skn {} is limitcode product,can't add to shopping cart", productBo.getErpProductId());
            //限购商品不能加入购物车
            throw new ServiceException(ServiceError.SHOPPING_CART_VERSION_NOT_SUPPORT);
        }

        //没有版本
//        if (request.getApiVersion() < 5 && Constants.IS_ADVANCE_STR.equals(productBo.getIsAdvance())) {
//            //throw new Exception('当前版本不支持预售商品购买,请更新最新版本.');
//            throw new ServiceException(ServiceError.SHOPPING_VRESION_NOT_SUPPORT_ADVANCE);
//        }
        return productBo;
    }

    /**
     * 核实skn是否属于本次活动
     *
     * @param productBo
     * @return
     */
    private void verifySknJoinPromotion(int promotionId, ProductBo productBo) throws ServiceException {
        //TODO：曹留确认，为什么传入Promotion_id
        if (promotionId > 0 && !Constants.IS_ADVANCE_STR.equals(productBo.getIsAdvance())) {
            PromotionInfo promotionInfo = promotionInfoRepository.getPromotionById(promotionId);
            if (promotionInfo == null) {
                logger.warn("get promotion is null by promotionid {}", promotionId);
                throw new ServiceException(ServiceError.SHOPPING_PROMOTION_NOT_EXIST);
            }
            logger.debug("promotion info is {} of promotionid {}", promotionInfo, promotionId);
            String actionParamStr = promotionInfo.getActionParam();
            if (org.apache.commons.lang3.StringUtils.isEmpty(actionParamStr)) {
                return;
            }

            Map<String, String> actionParam = JSONObject.parseObject(actionParamStr, Map.class);
            if (actionParam == null) {
                return;
            }
            if (actionParam.containsKey("gift_list") && actionParam.get("gift_list") != null) {
                //只保留数字和,
                String replaceAllGiftListStr = replaceHoldNaNAndSplit(actionParam.get("gift_list"));
                if (replaceAllGiftListStr != null) {
                    List<String> giftGoodsList = Arrays.asList(replaceAllGiftListStr.split(","));
                    if (!giftGoodsList.contains(String.valueOf(productBo.getErpProductId()))) {
                        //商品不属于本次活动
                        throw new ServiceException(ServiceError.SHOPPING_PRODUCT_NOT_BELONG_PROMOTION);
                    }
                }

            } else if (actionParam.containsKey("goods_list") && actionParam.get("goods_list") != null) {
                String replaceAllGoodsListStr = replaceHoldNaNAndSplit(actionParam.get("goods_list"));
                if (replaceAllGoodsListStr != null) {
                    List<String> goodsList = Arrays.asList(replaceAllGoodsListStr.split(","));
                    if (!goodsList.contains(String.valueOf(productBo.getErpProductId()))) {
                        //商品不属于本次活动
                        throw new ServiceException(ServiceError.SHOPPING_PRODUCT_NOT_BELONG_PROMOTION);
                    }
                }
            }
        }
    }

    private void updateYohoodSkuBuyNumber() {
        //TODO 更新已购买数量 update yohood.yohood_product set buy_number=buy_number+:buy_number where product_sku=:product_sku ?
    }

    /**
     * 获取购物车
     *
     * @return
     */
    private ShoppingCart createShoppingCartIfNecessary(int uid, String shoppingKey) {
        ShoppingCart shoppingCart = null;
        if (uid > 0) {
            shoppingCart = shoppingCartMapper.selectShoppingCartByUid(uid);
        } else if (org.apache.commons.lang.StringUtils.isNotEmpty(shoppingKey)) {
            shoppingCart = shoppingCartMapper.selectShoppingCartByShoppingKey(shoppingKey,0);
        }
        if (shoppingCart == null) {
            //shoppingKey为空的场景：用户在第一次添加购物车的时候，系统生成一个唯一的key
            if (StringUtils.isEmpty(shoppingKey)) {
                shoppingKey = MyStringUtils.getShoppingKey();
                logger.info("create a new shopping key {}", shoppingKey);
            }

            //TODO frw add or update
            shoppingCart = insertShoppingCart(uid, shoppingKey);
        }
        return shoppingCart;
    }

    /**
     * 校验购物车可以购买sku的总款数以及单个sku可以购买的总数量
     *
     * @param shoppingCartId
     * @param skuId
     * @param buyNumber
     */
    private void checkSkuTotalNumAndBuyNumber(int shoppingCartId, int skuId, int buyNumber,int uid) throws ServiceException {
        //数量等校验
        int goodsTotal = shoppingCartItemsDAO.countShoppingCartGoods(shoppingCartId,uid);
        if (goodsTotal >= Constants.MAX_BUY_SKU_NUMBER) {
            //throw new Exception('购物车中最多加入' . YOHOCart_Config::$buyProductSKNMaxNumber . '款商品.');
            throw new ServiceException(ServiceError.SHOPPING_CART_NUMBER_OVER_MAX);
        }
        int alreadyBuyGoodsNum = shoppingCartItemsDAO.sumShoppingCartGoodsByProductSku(shoppingCartId, skuId, 0,uid);
        if (alreadyBuyGoodsNum >= Constants.MAX_BUY_ONE_SKU_NUMBER || buyNumber > Constants.MAX_BUY_ONE_SKU_NUMBER) {
            // throw new Exception('此商品不能购买更多!!!');
            throw new ServiceException(ServiceError.SHOPPING_PRODUCTSKN_BUYNUMBER_OVER_MAX);
        }
    }

    /**
     * 新建购物车
     *
     * @param uid
     * @param shoppingKeyStr
     * @return 购物车id
     */
    private ShoppingCart insertShoppingCart(int uid, String shoppingKeyStr) {

        //新建一条
        ShoppingCart temp = new ShoppingCart();
        temp.setUid(uid);
        temp.setShoppingKey(shoppingKeyStr);

        shoppingCartMapper.insertShoppingCart(temp);
        return temp;
    }

    /**
     * 只保留数字和","
     * @param replaceStr
     * @return
     */
    private String replaceHoldNaNAndSplit(String replaceStr) {
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(replaceStr)) {
            return replaceStr.replaceAll("[^,\\d]", "");
        }
        return null;
    }

    /**
     * 再次购买
     * @param request
     * @return
     */
    public ShoppingAddResponse readd(ShoppingReAddRequest request) {
        logger.info("enter shopping cart readd service,request is {}", request);
        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), null);
        
        int uid = request.getUid();
        String orderCode = request.getOrder_code();
        //1.根据uid和order_code查询用户订单。
        Orders order = getOrderAndCheckIfNull(orderCode, String.valueOf(uid));
        ShoppingCart shoppingCart = createShoppingCartIfNecessary(uid, null);
        //2.判断订单状态是否已取消
        if (canReAdd(order)) {
            doReadd(shoppingCart, order);
        } else {
            logger.info("[{}]order code {} is not cancel or use limit code or ,order info is {}",uid, orderCode, order);
        }

        //返回结果
        int buyNumber = shoppingCartItemsDAO.sumShoppingCartGoods(shoppingCart.getId(),uid);
        ShoppingAddResponse addResponse = new ShoppingAddResponse();
        addResponse.setShopping_key(shoppingCart.getShoppingKey());
        addResponse.setGoods_count(String.valueOf(buyNumber));
        logger.info("[{}]exit shopping cart readd service,response is {}",uid, addResponse);
        return addResponse;
    }


    private Orders getOrderAndCheckIfNull(String orderCode,String uid) throws ServiceException {
        Orders order = ordersMapper.selectByCodeAndUid(orderCode, uid);
        if (order == null) {
            logger.info("order is null,select by order code {}", orderCode);
            throw new ServiceException(ServiceError.ORDER_DOES_NOT_EXIST);
        }
        return order;
    }

    /**
     *  是否可以再次购物,
     *  必须是已取消的订单,并且不是限购,虚拟订单
     * @param order
     * @return
     */
    private boolean canReAdd(Orders order) {
        logger.info("order info,order code:{},is cancel: {},attribute :{}", order.getOrderCode(), order.getIsCancel(), order.getAttribute());

        return isCanceledOrder(order) && isNotLimitCodeChargeType(order) && isNotVirtualOrder(order);
    }

    private boolean isCanceledOrder(Orders order)
    {
        return Constants.IS_CANCELED_ORDER.equals(order.getIsCancel());
    }

    private boolean isNotLimitCodeChargeType(Orders order) {
        String extAttributeStr = orderExtAttributeDAO.selectExtAttributeByOrderCodeAndUid(order.getOrderCode(), order.getUid());
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(extAttributeStr)) {
            logger.info("order code {} of uid {} has ext attribute {}", order.getUid(), order.getOrderCode(), extAttributeStr);
            try {
                JSONObject extAttributeJson = JSON.parseObject(extAttributeStr);
                if (extAttributeJson.containsKey(Constants.ORDER_EXTATTR_KEY_PRODUCT_SKU_LIST)) {
                    List<ShoppingItem> items = JSON.parseArray(extAttributeJson.getString(Constants.ORDER_EXTATTR_KEY_PRODUCT_SKU_LIST), ShoppingItem.class);
                    if (CollectionUtils.isNotEmpty(items)) {
                        ShoppingItem item = items.get(0);
                        return !Constants.LIMITCODE_CHARGE_TYPE.equals(item.getType());
                    }
                }
            } catch (Exception e) {
                logger.error("parse json {} error", extAttributeStr, e);
            }
        }
        return true;
    }

    private boolean isNotVirtualOrder(Orders order) {
        return order.getAttribute() != com.yoho.yhorder.common.utils.Constants.ATTRIBUTE_VIRTUAL;
    }

    private void doReadd(ShoppingCart shoppingCart,Orders order)
    {
        //3.获取购物车中没有的sku。
        List<OrdersGoods> ordersGoodsList =  getOrderGoodsNotInShoppingCart(shoppingCart,order);
        if(CollectionUtils.isNotEmpty(ordersGoodsList))
        {
           //4.查询sku对应的库存
           List<ShoppingCartItems> list = buildItemsByStorage(shoppingCart,ordersGoodsList);
            //5.添加到购物车中
            shoppingCartItemsDAO.batchInsertShoppingCartItems(list);
        }

    }

    private List<OrdersGoods> getOrderGoodsNotInShoppingCart(ShoppingCart shoppingCart, Orders order) {
        List<OrdersGoods> retOrderGoods = new ArrayList<>();
        //1.查询订单对应的sku
        List<OrdersGoods> ordersGoodsList = ordersGoodsMapper.selectOrderGoodsByOrder(order);
        //2.获取购物车中没有的sku。
        List<ShoppingCartItems> items = shoppingCartItemsDAO.selectByCartId(shoppingCart.getId(),shoppingCart.getUid());

        if (CollectionUtils.isNotEmpty(ordersGoodsList)) {
            for (OrdersGoods goods : ordersGoodsList) {
                //过滤 赠品、加价购
                if (goods.getGoodsType() == Constants.ORDER_GOODS_TYPE_GIFT || goods.getGoodsType() == Constants.ORDER_GOODS_TYPE_PRICE_GIFT) {
                    continue;
                }
                if (notExistShoppingCart(goods, items)) {
                    retOrderGoods.add(goods);
                }
            }
        }

        return retOrderGoods;
    }

    private boolean notExistShoppingCart(OrdersGoods goods, List<ShoppingCartItems> items) {
        if (CollectionUtils.isNotEmpty(items)) {
            for (ShoppingCartItems item : items) {
                if (goods.getErpSkuId().intValue() == item.getSkuId().intValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<ShoppingCartItems> buildItemsByStorage(ShoppingCart shoppingCart, List<OrdersGoods> ordersGoodsList) {
        Assert.notEmpty(ordersGoodsList);
        List<ShoppingCartItems> cartItems = buildItems(shoppingCart,ordersGoodsList);
        //重新设置购物数量
        return resetItemsBuyNumberByStorage(cartItems);
    }

    private List<ShoppingCartItems> buildItems(ShoppingCart shoppingCart, List<OrdersGoods> ordersGoodsList) {
        List<ShoppingCartItems> cartItems = new ArrayList<>();
        Map<Integer,ShoppingCartItems> skuMap = new HashMap<>();
        ShoppingCartItems item = null;
        for (OrdersGoods goods : ordersGoodsList) {
            if (skuMap.containsKey(goods.getErpSkuId())) {
                //有重复的sku,添加数量,不包括赠品和加价购物
                item = skuMap.get(goods.getErpSkuId());
                item.setNum(item.getNum() + goods.getNum());
            } else {
                item = new ShoppingCartItems();
                //非促销
                item.setPromotionId(0);
                //选中
                item.setSelected(Constants.IS_SELECTED_Y);
                //有效状态
                item.setStatus((byte) 1);
                item.setNum(goods.getNum());
                item.setSkuId(goods.getErpSkuId());
                item.setProductSkn(goods.getProductSkn());
                item.setUid(shoppingCart.getUid());
                item.setShoppingCartId(shoppingCart.getId());
                //add ShoppingKey
                item.setShoppingKey(shoppingCart.getShoppingKey());

                skuMap.put(goods.getErpSkuId(),item);

                cartItems.add(item);
            }
        }
        return cartItems;
    }

    /**
     * 根据库存重新设置购物数量
     * @param itemsList
     * @return
     */
    private List<ShoppingCartItems> resetItemsBuyNumberByStorage(List<ShoppingCartItems> itemsList)
    {
        Assert.notEmpty(itemsList);
        List<Integer> skuList = new ArrayList<>();
        for(ShoppingCartItems items :itemsList)
        {
            skuList.add(items.getSkuId());
        }

        //库存
        StorageBo[] storageBoArray =  externalService.queryStorageBySkus(skuList);
        if(ArrayUtils.isNotEmpty(storageBoArray))
        {
            for(ShoppingCartItems items :itemsList)
            {
                StorageBo _storageBo = null;
                for(StorageBo storageBo: storageBoArray)
                {
                    if(storageBo.getErpSkuId().intValue() == items.getSkuId().intValue())
                    {
                        _storageBo = storageBo;
                        break;
                    }
                }

                if(_storageBo != null )
                {
                    //有库存，则以库存数量为主
                    if( _storageBo.getStorageNum() > 0 && items.getNum() > _storageBo.getStorageNum())
                    {
                        items.setNum(_storageBo.getStorageNum());
                    }
                }
            }
        }

        return itemsList;
    }
}
