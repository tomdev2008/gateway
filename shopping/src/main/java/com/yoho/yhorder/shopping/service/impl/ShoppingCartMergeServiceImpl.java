package com.yoho.yhorder.shopping.service.impl;

import java.util.*;

import javax.annotation.Resource;

import com.google.common.collect.Multisets;
import com.yoho.product.model.StorageBo;
import com.yoho.service.model.order.request.ShoppingCartLocalMergeRequestBO;
import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.service.model.order.response.OrdersGoods;
import com.yoho.service.model.order.utils.CalendarUtils;
import com.yoho.yhorder.common.bean.ShoppingItemReq;
import com.yoho.yhorder.common.cache.redis.ShoppingCartRedis;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.service.ExternalService;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MyStringUtils;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.response.shopping.ShoppingCartMergeRequestBO;
import com.yoho.service.model.order.response.shopping.ShoppingCartMergeResponseBO;
import com.yoho.yhorder.dal.IShoppingCartItemsDAO;
import com.yoho.yhorder.dal.IShoppingCartDAO;
import com.yoho.yhorder.dal.model.ShoppingCart;
import com.yoho.yhorder.dal.model.ShoppingCartItems;
import com.yoho.yhorder.shopping.service.IShoppingCartMergeService;

import org.springframework.util.Assert;

@Service
public class ShoppingCartMergeServiceImpl implements IShoppingCartMergeService {

    static Logger log = LoggerFactory.getLogger(ShoppingCartMergeServiceImpl.class);

    @Resource
    IShoppingCartDAO shoppingCartMapper;
    @Resource
    IShoppingCartItemsDAO shoppingCartItemsDAO;

    @Autowired
    private ExternalService externalService;

    @Autowired
    private ShoppingCartRedis shoppingCartRedis;

    /**
     * 设置默认值
     *
     * @param request
     */
    private void setDefault(ShoppingCartMergeRequestBO request) {
        request.setShopping_key(StringUtils.defaultString(request.getShopping_key()));
    }

    public ShoppingCartMergeResponseBO mergeCart(ShoppingCartMergeRequestBO request) throws ServiceException {
        //设置默认值
        setDefault(request);

        //如果为空，则直接返回
        if (StringUtils.isEmpty(request.getShopping_key()) || request.getUid() <= 0) {
            log.warn("checkParam is null with param is {}", request);
            return new ShoppingCartMergeResponseBO();
        }

        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), request.getShopping_key());

        //根据shopping_key查询临客购物车信息
        ShoppingCart srcCartInfo = shoppingCartMapper.selectShoppingCartByShoppingKey(request.getShopping_key(), 0);
        //用户购物车信息
        ShoppingCart userCartInfo = shoppingCartMapper.selectShoppingCartByUid(request.getUid());

        //处理合并逻辑
        dealCart(request, srcCartInfo, userCartInfo);

        //组装返回对象
        ShoppingCartMergeResponseBO response = new ShoppingCartMergeResponseBO();
        response.setShopping_key(request.getShopping_key());
        response.setUid(request.getUid());
        return response;
    }

    @Override
    public ShoppingCartMergeResponseBO mergeCartByLocal(ShoppingCartLocalMergeRequestBO request) throws ServiceException {

        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), null);

        //如果为空，则直接返回
        if (StringUtils.isEmpty(request.getProduct_sku_list()) || request.getUid() <= 0) {
            log.warn("mergeCartByLocal checkParam is null with param is {}", request);
            return new ShoppingCartMergeResponseBO();
        }

        List<ShoppingItemReq> reqShopCartItems = ChargeParam.parseReqShopCartItems(request.getProduct_sku_list());
        //ShoppingCartRequest srcCartInfo = shoppingCartMapper.selectShoppingCartByShoppingKey(request.getShopping_key());

        ShoppingCart userCartInfo = shoppingCartMapper.selectShoppingCartByUid(request.getUid());


        //处理合并逻辑
        dealCart(request, reqShopCartItems, userCartInfo);

        //组装返回对象
        ShoppingCartMergeResponseBO response = new ShoppingCartMergeResponseBO();
//		response.setShopping_key(request.getShopping_key());
        response.setUid(request.getUid());
        return response;
    }

    /**
     * 处理合并逻辑
     *
     * @param request
     * @param srcCartInfo
     * @param userCartInfo
     */
    private void dealCart(ShoppingCartMergeRequestBO request, ShoppingCart srcCartInfo, ShoppingCart userCartInfo) {
        if (srcCartInfo == null) {
            log.debug("srcCartInfo is null");
            return;
        }
        int toCartId = 0;
        ShoppingCart shoppingCart = null;
        ShoppingCartItems shoppingCartItems = null;
        int uid = request.getUid();
        //用户购物车不存在，直接把临客购物车放到用户购物车内，用户购物车使用临客的shoppingkey
        if (userCartInfo == null) {
            log.debug("userCartInfo is null");
            shoppingCart = new ShoppingCart();
            shoppingCart.setUid(uid);
            shoppingCart.setShoppingKey(request.getShopping_key());

            //记录访客购物车内信息
            List<ShoppingCartItems> list = shoppingCartItemsDAO.selectByCartId(srcCartInfo.getId(), 0);
            //删除访客购物车记录
            int delOldCartRel = shoppingCartMapper.delShoppingCartById(srcCartInfo.getId(), 0);
            //添加新购物车记录
            shoppingCartMapper.insertShoppingCart(shoppingCart);

            //添加新购物车列表 insert
            for (ShoppingCartItems items : list) {
                items.setId(null);
                items.setShoppingCartId(shoppingCart.getId());
                items.setUid(uid);  //设置新添加的item的uid
                items.setShoppingKey(shoppingCart.getShoppingKey());  //设置新添加的item的shoppingkey
            }
            int addItemsRel = 0;
            if (list.size() > 0) {
                log.info("[{}] change cart batch insert items size {}", uid, list.size());
                addItemsRel = shoppingCartItemsDAO.batchInsertShoppingCartItems(list);
            }

            //删除访客购物车列表
            int delOldItemsRel = shoppingCartItemsDAO.deleteShoppingCartItemsByCartId(srcCartInfo.getId(), 0);

            log.info("[{}] change cart  result is new cart: {}  updateCartItemsRel :{} delOldCartRel:{} ,delOldItemsRel:{}", uid, shoppingCart, addItemsRel, delOldCartRel, delOldItemsRel);

//            int setUidByShoppingKeyResult = shoppingCartMapper.setUidByShoppingKey(shoppingCart);
//            //此时需要同时设置默认购物车列表的uid
//            int shopcartId = srcCartInfo.getId();
//            int updateCartItemsRel = shoppingCartItemsDAO.updateCartGoodsUserIdById(shopcartId, request.getUid());
//            log.info("setUidByShoppingKey success result is {}  updateCartItemsRel :{}", setUidByShoppingKeyResult, updateCartItemsRel);
            return;
        }
        //临客购物车->用户购物车 1.商品迁移 2.临客购物车删除
        toCartId = userCartInfo.getId();
        if (srcCartInfo.getUid() == 0 && toCartId != srcCartInfo.getId()) {

            //获取临客购物车商品列表
            List<ShoppingCartItems> customlist = shoppingCartItemsDAO.selectByCartId(srcCartInfo.getId(), 0);
            //取用户购物车商品列表
            List<ShoppingCartItems> userlist = shoppingCartItemsDAO.selectByCartId(toCartId, uid);
            //记录用户购物车商品信息->map key:sku,优惠id
            MultiKeyMap existsSku = new MultiKeyMap();
            for (ShoppingCartItems item : userlist) {
                if (!existsSku.containsKey(item.getSkuId(), item.getPromotionId()))
                    existsSku.put(item.getSkuId(), item.getPromotionId(), true);
            }
            //去除临客购物车商品中重复的sku
            Iterator<ShoppingCartItems> customCartIter = customlist.iterator();
            while (customCartIter.hasNext()) {
                ShoppingCartItems current = customCartIter.next();
                if (existsSku.containsKey(current.getSkuId(), current.getPromotionId())) {
                    log.info("user cart already has good:{}", current);
                    customCartIter.remove();
                } else {
                    //设置插入商品的uid 和 cartId
                    current.setId(null);
                    current.setUid(uid);
                    current.setShoppingCartId(toCartId);
                    current.setShoppingKey(userCartInfo.getShoppingKey());
                }
            }

            if (customlist.size() > 0) {
                int batchAddItemsRel = shoppingCartItemsDAO.batchInsertShoppingCartItems(customlist);
                log.debug("batch add items success result is {},addList size:{}", batchAddItemsRel, customlist.size());
            }

//            //批量置状态为0
//            if (delList.size() > 0) {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put("query", StringUtils.join(delList, ","));
//                //原重复sku逻辑删除改为物理删除
//                //int batchUpdateStatusResult = shoppingCartItemsDAO.batchUpdateStatusTo_0(map);
//                int batchDelByIdsResult = shoppingCartItemsDAO.batchDeleteByIds(map);
//                log.debug("batchDeleteByIds success result is {}", batchDelByIdsResult);
//            }

            if (StringUtils.isEmpty(userCartInfo.getShoppingKey())) {
                shoppingCart = new ShoppingCart();
                shoppingCart.setId(toCartId);
                shoppingCart.setUid(uid);
                shoppingCart.setShoppingKey(request.getShopping_key());
                int setShoppingKeyByIdResult = shoppingCartMapper.setShoppingKeyById(shoppingCart);
                log.debug("setShoppingKeyById success result is {}", setShoppingKeyByIdResult);
            } else {
                request.setShopping_key(userCartInfo.getShoppingKey());
            }

//            //删除购物车
//            if (changeCartResult > 0) {
            //删除原购物车
            shoppingCartMapper.delShoppingCartById(srcCartInfo.getId(), 0);
            //删除访客购物车列表
            shoppingCartItemsDAO.deleteShoppingCartItemsByCartId(srcCartInfo.getId(), 0);

//            }
        }
    }


    /**
     * 处理合并逻辑 by local
     *
     * @param request
     * @param reqShopCartItems
     * @param userCartInfo
     */
    private void dealCart(ShoppingCartLocalMergeRequestBO request, List<ShoppingItemReq> reqShopCartItems, ShoppingCart userCartInfo) {
        if (reqShopCartItems == null) {
            log.debug("reqShopCartItems is null");
            return;
        }
        int toCartId = 0;
        ShoppingCart shoppingCart = null;
        ShoppingCartItems shoppingCartItems = null;
        int shoppingCartId;
        if (userCartInfo == null) {
            log.debug("userCartInfo is null");


            //用户在第一次添加购物车的时候，系统生成一个唯一的key
            String shoppingKey = MyStringUtils.getShoppingKey();
            log.info("create a new shopping key {}", shoppingKey);
            shoppingCart = new ShoppingCart();
            shoppingCart.setUid(request.getUid());
            shoppingCart.setShoppingKey(shoppingKey);

            shoppingCartMapper.insertShoppingCart(shoppingCart);

            //批量添加reqShopCartItems到shopping_cart_items表
            shoppingCartId = shoppingCart.getId();
            List<ShoppingCartItems> list = new ArrayList<ShoppingCartItems>(reqShopCartItems.size());
            for (ShoppingItemReq reqItem : reqShopCartItems) {
                ShoppingCartItems tmpItem = new ShoppingCartItems();
                tmpItem.setShoppingCartId(shoppingCartId);
                tmpItem.setNum(reqItem.getNum());
                tmpItem.setUid(request.getUid());
                tmpItem.setSkuId(reqItem.getSkuId());
                tmpItem.setPromotionId(reqItem.getPromotionId());
                tmpItem.setSelected(reqItem.getSelected());
                tmpItem.setProductSkn(reqItem.getProductSkn());
                //TODO frwnote new or get
                tmpItem.setCreateTime(reqItem.getCreateTime());
                tmpItem.setStatus((byte) 1);
                tmpItem.setShoppingKey(shoppingKey);
                list.add(tmpItem);
            }
            //buildItemsByStorage(reqShopCartItemst);
            //5.添加到购物车中
            int batchInsertRel = shoppingCartItemsDAO.batchInsertShoppingCartItems(list);
            log.debug("insert shopping cart and items  success result is   batchInsertRel :{}", batchInsertRel);
            return;
        }
        toCartId = userCartInfo.getId();
        shoppingCartId = userCartInfo.getId();
        // update
//		if (srcCartInfo.getUid() == 0 && toCartId != srcCartInfo.getId()) {
        //		shoppingCartItems = new ShoppingCartItems();
//			shoppingCartItems.setSrcCartId(srcCartInfo.getId());
        //		shoppingCartItems.setToCartId(toCartId);
        //		shoppingCartItems.setUid(request.getUid());
        //		int changeCartResult = shoppingCartItemsDAO.changeCart(shoppingCartItems);
        //		log.debug("changeCart success result is {}", changeCartResult);
        //去除重复的sku
        List<ShoppingCartItems> list = shoppingCartItemsDAO.selectByCartId(toCartId, userCartInfo.getUid());
        Set<Integer> skuSet = new HashSet<Integer>();
        for (ShoppingCartItems item : list) {
            skuSet.add(item.getSkuId());
        }
        Boolean b = null;
        List<ShoppingCartItems> insertList = new ArrayList<ShoppingCartItems>();
        for (ShoppingItemReq reqItem : reqShopCartItems) {
            if (skuSet.contains(reqItem.getSkuId())) {
                continue;
            }
            ShoppingCartItems tmpItem = new ShoppingCartItems();
            tmpItem.setShoppingCartId(shoppingCartId);
            tmpItem.setNum(reqItem.getNum());
            tmpItem.setUid(request.getUid());
            tmpItem.setSkuId(reqItem.getSkuId());
            tmpItem.setPromotionId(reqItem.getPromotionId());
            tmpItem.setSelected(reqItem.getSelected());
            tmpItem.setProductSkn(reqItem.getProductSkn());
            //TODO frwnote new or get
            tmpItem.setCreateTime(reqItem.getCreateTime());
            tmpItem.setStatus((byte) 1);
            tmpItem.setShoppingKey(userCartInfo.getShoppingKey());
            insertList.add(tmpItem);
        }
        int batchInsertRel = shoppingCartItemsDAO.batchInsertShoppingCartItems(insertList);

        //批量置状态为0
//			if (delList.size() > 0) {
//				Map<String, String> map = new HashMap<String, String>();
//				map.put("query", StringUtils.join(delList, ","));
//				int batchUpdateStatusResult = shoppingCartItemsDAO.batchUpdateStatusTo_0(map);
//				log.debug("batchUpdateStatus success result is {}", batchUpdateStatusResult);
//			}

//			if (StringUtils.isEmpty(userCartInfo.getShoppingKey())) {
//				shoppingCart = new ShoppingCart();
//				shoppingCart.setId(toCartId);
//				shoppingCart.setShoppingKey(request.getShopping_key());
//				int setShoppingKeyByIdResult = shoppingCartMapper.setShoppingKeyById(shoppingCart);
//				log.debug("setShoppingKeyById success result is {}", setShoppingKeyByIdResult);
//			} else {
//				request.setShopping_key(userCartInfo.getShoppingKey());
//			}

//			//删除购物车
//			if (changeCartResult > 0) {
//				shoppingCartMapper.delShoppingCartById(srcCartInfo.getId());
//			}
//		}
    }

    private List<ShoppingCartItems> buildItemsByStorage(ShoppingCart shoppingCart, List<OrdersGoods> ordersGoodsList) {
        Assert.notEmpty(ordersGoodsList);
        List<ShoppingCartItems> cartItems = buildItems(shoppingCart, ordersGoodsList);
        //重新设置购物数量
        return resetItemsBuyNumberByStorage(cartItems);
    }

    private List<ShoppingCartItems> buildItems(ShoppingCart shoppingCart, List<OrdersGoods> ordersGoodsList) {
        List<ShoppingCartItems> cartItems = new ArrayList<>();
        for (OrdersGoods goods : ordersGoodsList) {
            ShoppingCartItems item = new ShoppingCartItems();
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
            cartItems.add(item);
        }
        return cartItems;
    }

    /**
     * 根据库存重新设置购物数量
     *
     * @param itemsList
     * @return
     */
    private List<ShoppingCartItems> resetItemsBuyNumberByStorage(List<ShoppingCartItems> itemsList) {
        Assert.notEmpty(itemsList);
        List<Integer> skuList = new ArrayList<>();
        for (ShoppingCartItems items : itemsList) {
            skuList.add(items.getSkuId());
        }

        //库存
        StorageBo[] storageBoArray = externalService.queryStorageBySkus(skuList);
        if (ArrayUtils.isNotEmpty(storageBoArray)) {
            for (ShoppingCartItems items : itemsList) {
                StorageBo _storageBo = null;
                for (StorageBo storageBo : storageBoArray) {
                    if (storageBo.getErpSkuId().intValue() == items.getSkuId().intValue()) {
                        _storageBo = storageBo;
                        break;
                    }
                }

                if (_storageBo != null) {
                    //有库存，则以库存数量为主
                    if (_storageBo.getStorageNum() > 0 && items.getNum() > _storageBo.getStorageNum()) {
                        items.setNum(_storageBo.getStorageNum());
                    }
                }
            }
        }

        return itemsList;
    }

}
