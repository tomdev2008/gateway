package com.yoho.yhorder.shopping.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.service.model.order.response.shopping.ShoppingAddResponse;
import com.yoho.service.model.order.response.shopping.ShoppingCountResponse;
import com.yoho.yhorder.common.bean.ShoppingItem;
import com.yoho.yhorder.common.cache.redis.ShoppingCartRedis;
import com.yoho.yhorder.dal.IShoppingCartDAO;
import com.yoho.yhorder.dal.IShoppingCartItemsDAO;
import com.yoho.yhorder.dal.model.ShoppingCart;
import com.yoho.yhorder.dal.model.ShoppingCartItems;
import com.yoho.yhorder.shopping.service.ExternalService;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.dal.model.ShoppingCart;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by JXWU on 2015/11/21.
 */
@Service
public class ShoppingCartSimpleActionService {

    private final Logger logger = LoggerFactory.getLogger("addPaymentComputeLog");

    @Autowired
    protected IShoppingCartDAO shoppingCartMapper;

    @Autowired
    protected IShoppingCartItemsDAO shoppingCartItemsDAO;

    @Autowired
    private ExternalService externalService;

    @Autowired
    private ShoppingCartRedis shoppingCartRedis;

    /**
     * 增加数量
     *
     * @param request
     * @return
     */
    public ShoppingAddResponse increase(ShoppingCartRequest request) {
        logger.info("enter shopping cart increase service,request is {}", request);
        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), request.getShopping_key());

        //增加的sku数量
        int increaseNumber = request.getIncrease_number();
        //获取购物车,没有查询到则抛出异常
        ShoppingCart shoppingCart = getShoppingCart(request);
        int productSku = request.getProduct_sku();
        Integer promotionId = request.getPromotion_id();
        if (promotionId == null) {
            promotionId = 0;
        }

        //优惠活动的商品无法手动++！
        if (promotionId > 0) {
            logger.warn("promotionId parameter is illegal in shopping cart increase service,promotionId is {},request is {}", promotionId, request);
            throw new ServiceException(ServiceError.SHOPPING_CART_ILLEGAL_PARAMETER);
        }

        int shoppingCartId = shoppingCart.getId();
        //从数据库表shopping_cart_items中查询sku已经购买的数量
        int alreadyBuyGoodsNum = shoppingCartItemsDAO.sumShoppingCartGoodsByProductSku(shoppingCartId, productSku, promotionId, shoppingCart.getUid());
        //已经购买的数量或者将要购买的数量超过限制
        if (alreadyBuyGoodsNum >= Constants.MAX_BUY_ONE_SKU_NUMBER || increaseNumber > Constants.MAX_BUY_ONE_SKU_NUMBER) {
            logger.info("already buy number {} or increase number {} over limit {} for request {}", alreadyBuyGoodsNum, increaseNumber, Constants.MAX_BUY_ONE_SKU_NUMBER, request);
            throw new ServiceException(ServiceError.SHOPPING_PRODUCTSKN_BUYNUMBER_OVER_MAX);
        }

        //累加sku的数量
        shoppingCartItemsDAO.updateShoppingCartGoodsNumber(shoppingCartId, productSku, increaseNumber, promotionId, shoppingCart.getUid());
        //查询购物车所有sku的购买数量
        int goodsCount = shoppingCartItemsDAO.sumShoppingCartGoods(shoppingCartId, shoppingCart.getUid());

        //返回结果
        ShoppingAddResponse response = new ShoppingAddResponse();
        response.setShopping_key(shoppingCart.getShoppingKey());
        response.setGoods_count(String.valueOf(goodsCount));

        logger.info("exit shopping increase service,request is: {}\n,response is: {}\n", request, response);

        return response;
    }


    public ShoppingAddResponse decrease(ShoppingCartRequest request) {
        logger.info("enter shopping cart decrease service,request is {}", request);
        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), request.getShopping_key());

        int decreaseNumber = request.getDecrease_number();
        ShoppingCart shoppingCart = getShoppingCart(request);
        int productSku = request.getProduct_sku();
        Integer promotionId = request.getPromotion_id();
        if (promotionId == null) {
            promotionId = 0;
        }

        if (promotionId > 0) {
            logger.warn("promotionId parameter is illegal in shopping cart decrease service,promotionId is {},request is {}", promotionId, request);
            throw new ServiceException(ServiceError.SHOPPING_CART_ILLEGAL_PARAMETER);
        }

        int shoppingCartId = shoppingCart.getId();
        // //从数据库表shopping_cart_items中查询sku还剩下多少
        int alreadyBuyGoodsNum = shoppingCartItemsDAO.sumShoppingCartGoodsByProductSku(shoppingCartId, productSku, promotionId, shoppingCart.getUid());
        //
        if (alreadyBuyGoodsNum < 2) {
            logger.info("shopping cart sku number  < 2,so can't decrese it,request is {}", request);
            throw new ServiceException(ServiceError.SHOPPING_CART_ONLY_ONE_PRODUCT);
        }

        //减少数量
        shoppingCartItemsDAO.decreaseShoppingCartGoodsNumber(shoppingCartId, productSku, decreaseNumber, promotionId, shoppingCart.getUid());

        //查询减少后的数量
        int goodsCount = shoppingCartItemsDAO.sumShoppingCartGoods(shoppingCartId, shoppingCart.getUid());

        //返回结果
        ShoppingAddResponse response = new ShoppingAddResponse();
        response.setShopping_key(shoppingCart.getShoppingKey());
        response.setGoods_count(String.valueOf(goodsCount));

        logger.info("exit shopping decrease service,request is: {}\n,response is: {}\n", request, response);
        return response;
    }

    /**
     * TODO 有一个问题，删除相同的sku，其中一个是普通商品，其他是促销商品
     *
     * @param request
     * @return
     */
    public ShoppingAddResponse remove(ShoppingCartRequest request) {
        logger.info("enter shopping cart remove service,request is {}", request);
        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), request.getShopping_key());

        //参数product_sku_list格式为：{"5001":1,"5002":2,"5003":3}，5001为sku，1为数量
        Map<String, Integer> removeProductSkuMap = parseProductSkuListParam(request.getProduct_sku_list());
        //查购物车，没有则抛出异常
        ShoppingCart shoppingCart = getShoppingCart(request);
        int shoppingCartId = shoppingCart.getId();
        Set<String> skus = removeProductSkuMap.keySet();
        //循环处理
        List<ShoppingCartItems> items = new ArrayList<>(skus.size());
        for (String sku : skus) {
            if (StringUtils.isEmpty(sku)) {
                continue;
            }
            int buyNumber = removeProductSkuMap.get(sku);
            int productSku = Integer.parseInt(sku);
            ShoppingCartItems item = new ShoppingCartItems();
            item.setShoppingCartId(shoppingCartId);
            item.setNum(buyNumber);
            item.setSkuId(productSku);
            items.add(item);
        }
        doRemoveItems(items, shoppingCart.getUid());

        int goodsCount = shoppingCartItemsDAO.sumShoppingCartGoods(shoppingCartId, shoppingCart.getUid());
        //返回结果
        ShoppingAddResponse response = new ShoppingAddResponse();
        response.setShopping_key(shoppingCart.getShoppingKey());
        response.setGoods_count(String.valueOf(goodsCount));

        logger.info("exit shopping remove service,request is: {}\n,response is: {}\n", request, response);
        return response;
    }


    public ShoppingAddResponse swap(ShoppingCartRequest request) {

        logger.info("enter shopping cart swap service,request is {}", request);

        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), request.getShopping_key());

        String swap_data = request.getSwap_data();
        //解析json串
        //格式[{"buy_number":"1","selected":"Y","new_product_sku":"735172","old_product_sku":"735171"}]
        JSONArray swapProductSkuArray = null;
        try {
            swapProductSkuArray = JSON.parseArray(swap_data);
        } catch (Exception e) {
            logger.info("swap data {} format is error,request is {}", swap_data, request);
            throw new ServiceException(ServiceError.SHOPPING_SWAP_DATA_IS_NULL);
        }

        ShoppingCart shoppingCart = getShoppingCart(request);
        int shoppingCartId = shoppingCart.getId();
        int uid = shoppingCart.getUid();
        if (swapProductSkuArray != null && swapProductSkuArray.size() > 0) {
            for (int i = 0; i < swapProductSkuArray.size(); i++) {
                JSONObject map = swapProductSkuArray.getJSONObject(i);
                Integer newProductSku = map.getInteger("new_product_sku");
                Integer oldProductSku = map.getInteger("old_product_sku");
                Integer buyNumber = map.getInteger("buy_number");
                //TODO 更换商品为什么不设置选中状态，默认选中的原因?更换情况如果是直接update，创建时间需不需要变化?
                //sku未发生变化
                if (newProductSku.intValue() == oldProductSku.intValue()) {
                    shoppingCartItemsDAO.updateShoppingCartGoodsWithOld(shoppingCartId, newProductSku, oldProductSku, buyNumber, uid);
                } else {
                    int goodsCount = shoppingCartItemsDAO.countShoppingCartGoodsByProductSku(shoppingCartId, newProductSku, 0, uid);
                    if (goodsCount > 0) {
                        //新商品存在，需要删除老的，如果直接替换的，会导致同一个商品存在多条记录
                        //赠品,加价购不会调用这个接口
                        //shoppingCartItemsDAO.updateShoppingCartGoodsStatusByProductSku(shoppingCartId, oldProductSku, 0);
                        //上一行逻辑删除老sku的item更改为物理删除
                        logger.info("swap deleteShoppingCartGoodsByProductSku  shopping cart id {} , old sku {}", shoppingCartId, oldProductSku);
                        shoppingCartItemsDAO.deleteShoppingCartGoodsByProductSku(shoppingCartId, oldProductSku, 0, uid);
                        //更新商品购买数量 = + buyNumber
                        shoppingCartItemsDAO.updateShoppingCartGoodsNumber(shoppingCartId, newProductSku, buyNumber, 0, uid);
                    } else {
                        shoppingCartItemsDAO.updateShoppingCartGoodsWithOld(shoppingCartId, newProductSku, oldProductSku, buyNumber, uid);
                    }
                }
            }
        }
        int goodsCount = shoppingCartItemsDAO.sumShoppingCartGoods(shoppingCartId, uid);
        //返回结果
        ShoppingAddResponse response = new ShoppingAddResponse();
        response.setShopping_key(shoppingCart.getShoppingKey());
        response.setGoods_count(String.valueOf(goodsCount));

        logger.info("exit shopping swap service,request is: {}\n,response is: {}\n", request, response);

        return response;
    }


    public ShoppingCountResponse count(ShoppingCartRequest request) {
        ShoppingCountResponse response = new ShoppingCountResponse();
        //不需要抛出异常，count接口uid或者shoppingkey大部分为空
        ShoppingCart shoppingCart = getShoppingCartWithoutException(request);
        if (shoppingCart != null) {
            int goodsCount = shoppingCartItemsDAO.sumShoppingCartGoods(shoppingCart.getId(), shoppingCart.getUid());
            //如果有携带uid，则需要根据UID过滤下
//            if( request.getUid() > 0 ) {
//                goodsCount = shoppingCartItemsDAO.sumShoppingCartGoodsByUid(shoppingCart.getId(), shoppingCart.getUid());
//            }else {
//                goodsCount = shoppingCartItemsDAO.sumShoppingCartGoods(shoppingCart.getId());
//            }
            response.setCart_goods_count(goodsCount);
        }
        return response;
    }


    public void selected(ShoppingCartRequest request) {
        logger.info("enter shopping cart selected service,request is {}", request);

        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), request.getShopping_key());

        String productSkuListStr = request.getProduct_sku_list();
        //解析json串
        JSONArray productSkuArray = null;
        try {
            productSkuArray = JSON.parseArray(productSkuListStr);
        } catch (Exception e) {
            logger.info("package {} format is error，request is {}", productSkuListStr, request);
            throw new ServiceException(ServiceError.SHOPPING_PACKAGE_FORMAT_IS_NULL);
        }

        ShoppingCart shoppingCart = getShoppingCart(request);

        if (productSkuArray != null && productSkuArray.size() > 0) {
            int shoppingCartId = shoppingCart.getId();
            List<ShoppingCartItems> list = new ArrayList<>();
            for (int i = 0; i < productSkuArray.size(); i++) {
                JSONObject productSkuObj = productSkuArray.getJSONObject(i);
                if (productSkuObj.containsKey("product_sku") && productSkuObj.containsKey("selected") && productSkuObj.containsKey("buy_number")) {
                    String selected = productSkuObj.getString("selected");
                    //是否选中只有Y和N
                    if ("Y".equals(selected) || "N".equals(selected)) {
                        Integer productSku = productSkuObj.getInteger("product_sku");
                        if (productSku == null || productSku < 1) {
                            continue;
                        }
                        ShoppingCartItems item = new ShoppingCartItems();
                        item.setShoppingCartId(shoppingCartId);
                        item.setSkuId(productSku);
                        item.setSelected(selected);
                        //4.1版本才有promotion_id参数
                        if (productSkuObj.containsKey("promotion_id")) {
                            item.setPromotionId(productSkuObj.getInteger("promotion_id"));
                        }
                        list.add(item);
                    }
                }
            }
            //批量更新
            if (CollectionUtils.isNotEmpty(list)) {
                shoppingCartItemsDAO.batchUpdateShoppingCartGoodsSelectedStatus(list, shoppingCart.getUid());
            }
        }

        logger.info("exit shopping selected service,request is: {}\n,response is void\n", request);
    }

    private Map<String, Integer> parseProductSkuListParam(String product_sku_list_str) {
        if (StringUtils.isEmpty(product_sku_list_str)) {
            //return self::result(400, 'product_sku_list is null.');
            throw new ServiceException(ServiceError.SHOPPING_PRODUCT_SKU_LIST_IS_NULL);
        }
        //解析json串
        Exception ex = null;
        Map<String, Integer> removeProductSkuMap = null;
        try {
            removeProductSkuMap = JSON.parseObject(product_sku_list_str, Map.class);
        } catch (Exception e) {
            ex = e;
        }
        if (removeProductSkuMap == null || removeProductSkuMap.isEmpty() || ex != null) {
            logger.info("package {} format is error", product_sku_list_str);
            throw new ServiceException(ServiceError.SHOPPING_PACKAGE_FORMAT_IS_NULL);
        }

        return removeProductSkuMap;
    }


    /**
     * product_sku_list {"1049084":1,"1055439":1}
     *
     * @param request
     * @return
     */
    public ShoppingAddResponse addfavorite(ShoppingCartRequest request) {
        logger.info("enter shopping cart addfavorite service,request is {}", request);

        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), request.getShopping_key());

        String productSkuStr = request.getProduct_sku_list();

        Set<String> skus = null;
        //解析json串
        try {
            JSONObject skusJson = JSON.parseObject(productSkuStr);
            skus = skusJson.keySet();

        } catch (Exception e) {
            logger.warn("package {} format is error，request is {}", productSkuStr, request, e);
            throw new ServiceException(ServiceError.SHOPPING_PACKAGE_FORMAT_IS_NULL);
        }

        if (CollectionUtils.isNotEmpty(skus)) {
            List<Integer> skuIds = new ArrayList<>();
            for (String skuStr : skus) {
                skuIds.add(Integer.parseInt(skuStr));
            }
            //收藏商品
            externalService.batchAddFavorite(request.getUid(), skuIds);
        }

        //删除商品
        ShoppingAddResponse response = remove(request);

        logger.info("exit shopping addfavorite service,request is: {}\n,response is: {}\n", request, response);

        return response;
    }

    public ShoppingCart getShoppingCart(ShoppingCartRequest request) throws ServiceException {
        ShoppingCart shoppingCart = getShoppingCartWithoutException(request);
        if (shoppingCart == null) {
            //购物车为空
            logger.info("can't query shopping cart by uid or shopping_key,uid is {},shopping_key is {}", request.getUid(), request.getShopping_key());
            throw new ServiceException(ServiceError.SHOPPING_CART_IS_EMPTY);
        }
        return shoppingCart;
    }


    /**
     * 获取购物车信息
     *
     * @param request
     * @return
     */
    protected ShoppingCart getShoppingCartWithoutException(ShoppingCartRequest request) {
        ShoppingCart shoppingCart = null;
        int uid = request.getUid();
        if (uid > 0) {
            shoppingCart = shoppingCartMapper.selectShoppingCartByUid(uid);
        } else if (StringUtils.isNotEmpty(request.getShopping_key())) {
            //根据shoppingkey查询临客购物车
            shoppingCart = shoppingCartMapper.selectShoppingCartByShoppingKey(request.getShopping_key(), 0);
        }
        return shoppingCart;
    }

    private void doRemoveItems(List<ShoppingCartItems> itemsList, int uid) {
        if (CollectionUtils.isEmpty(itemsList)) {
            return;
        }
        //先合并
        itemsList = mergeShoppingCartItems(itemsList);

        List<ShoppingCartItems> decreaseList = new ArrayList<>();
        List<ShoppingCartItems> removeList = new ArrayList<>();

        List<ShoppingCartItems> cartItemsList = shoppingCartItemsDAO.selectItemsBySkuAndPromotionId(itemsList, uid);
        for (ShoppingCartItems cartItems : cartItemsList) {
            //add NullPointer check
            if (cartItems.getSkuId() == null || cartItems.getPromotionId() == null) {
                continue;
            }
            ShoppingCartItems removeItem = findItemBySkuAndPromotionId(cartItems.getSkuId(), cartItems.getPromotionId(), itemsList);
            if (removeItem == null) {
                continue;
            }

            //TODO frwnote getNum may be null
            if (cartItems.getNum() > removeItem.getNum()) {
                //购物车中数量大于要删除的数量
                decreaseList.add(removeItem);
            } else {
                removeList.add(cartItems);
            }
        }


        if (CollectionUtils.isNotEmpty(decreaseList)) {
            shoppingCartItemsDAO.updateNumInBatch(decreaseList, uid);
        }

        //TODO frw delete items  instande of update
        //如果有删除items，修改原逻辑删除为物理删除
        if (CollectionUtils.isNotEmpty(removeList)) {
//            shoppingCartItemsDAO.disableStatusBySkuAndPromotionId(removeList);
            logger.info("doRemoveItems deleteItemsBySkuAndPromotionId {} ", removeList);
            shoppingCartItemsDAO.deleteItemsBySkuAndPromotionId(removeList, uid);
        }
    }

    private ShoppingCartItems findItemBySkuAndPromotionId(int sku, int promotionId, List<ShoppingCartItems> itemsList) {
        for (ShoppingCartItems item : itemsList) {
            if (item.getSkuId().intValue() == sku) {
                //remove接口中promotionId为null,removeAndCart接口中promotionId不为null
                if (item.getPromotionId() == null) {
                    return item;
                }
                if (item.getPromotionId().intValue() == promotionId) {

                    return item;
                }
            }
        }
        return null;
    }

    public void removeItemsByShoppingCartId(ShoppingCartRequest request, List<ShoppingCartItems> removeItems) {
        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), request.getShopping_key());

        if (CollectionUtils.isNotEmpty(removeItems)) {
            ShoppingCart shoppingCart = getShoppingCart(request);

            for (ShoppingCartItems item : removeItems) {
                item.setShoppingCartId(shoppingCart.getId());
            }

            doRemoveItems(removeItems, request.getUid());
        }
    }

    public void addItemsToFavorite(int uid, List<ShoppingCartItems> items) {
        if (CollectionUtils.isNotEmpty(items)) {
            List<Integer> skuIds = new ArrayList<>();
            for (ShoppingCartItems item : items) {
                skuIds.add(item.getSkuId());
            }
            //收藏商品
            externalService.batchAddFavorite(uid, skuIds);
        }
    }

    /**
     * 合并
     *
     * @param itemsList
     * @return
     */
    private List<ShoppingCartItems> mergeShoppingCartItems(List<ShoppingCartItems> itemsList) {
        Map<String, ShoppingCartItems> mergeMap = new HashMap<>();
        for (ShoppingCartItems items : itemsList) {
            String key = "" + items.getShoppingCartId() + "-" + items.getSkuId() + "-" + items.getPromotionId();
            if (!mergeMap.containsKey(key)) {
                mergeMap.put(key, items);
            } else {
                ShoppingCartItems tmp = mergeMap.get(key);
                tmp.setNum(tmp.getNum() + items.getNum());
            }
        }
        return new ArrayList<>(mergeMap.values());
    }
}
