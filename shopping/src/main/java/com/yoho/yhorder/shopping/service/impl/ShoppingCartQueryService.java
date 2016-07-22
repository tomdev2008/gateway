package com.yoho.yhorder.shopping.service.impl;

import com.alibaba.fastjson.JSON;
import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.service.model.order.response.shopping.*;
import com.yoho.yhorder.common.cache.redis.ShoppingCartRedis;
import com.yoho.yhorder.shopping.charge.ChargeContextFactory;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.ChargerService;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.utils.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by JXWU on 2015/11/20.
 */
@Service
public class ShoppingCartQueryService {

    private final Logger logger = LoggerFactory.getLogger("cartQueryLog");

    @Autowired
    private ChargeContextFactory changeContextFactory;

    @Autowired
    private ChargerService chargerService;
    
    @Autowired
    private ShoppingCartRedis shoppingCartRedis;


    public ShoppingQueryResponse query(ShoppingCartRequest request) {
        ShoppingQueryResponse sqr=buildShoppingQueryResponseFromRedis(request);
        //计算结果缓存命中
        if(sqr!=null){
            logger.info("query shopping cart from redis success, ## request {}, ## Promotion_info {},## Shopping_cart_data {}",
                    request,sqr.getOrdinary_cart_data().getPromotion_info(),
                    sqr.getOrdinary_cart_data().getShopping_cart_data());
            return sqr;
        }
        //1.普通商品算费
        //1.1新建普通商品算费参数
        ChargeParam ordinaryChargeParam = newOrdinaryChargeParam(request);
        //1.2新建普通商品算费context，context中包括需要算费的sku、用户信息等
        //查询数据库表shopping_cart_items的自选sku和未选sku
        ChargeContext ordinaryChargeContext = changeContextFactory.build(false, ordinaryChargeParam);
        //1.3算费，算费结果在context对象的ChargeTotal中。
        chargerService.charge(ordinaryChargeContext);
        //1.4获取算费结果
        ShoppingChargeResult ordinaryChargeResult = ordinaryChargeContext.getChargeResult();

        //2预售商品算费
        //之前普通商品算费已经将取出预售商品列表，算费过程必须使用预约商品的ChargeParam对象
        ChargeContext preSaleChargeContext = new ChargeContext();
        preSaleChargeContext.setChargeGoodsList(ordinaryChargeContext.getChargeGoodsList());
        preSaleChargeContext.setUserInfo(ordinaryChargeContext.getUserInfo());
        preSaleChargeContext.setChargeParam(newPreSaleChargeParam(request));
        //2.3算费，算费结果在context对象的ChargeTotal中。
        chargerService.charge(preSaleChargeContext);
        //2.4获取算费结果
        ShoppingChargeResult preSaleChargeResult = preSaleChargeContext.getChargeResult();

        //3.构造返回结果
        ShoppingQueryResponse response = new ShoppingQueryResponse();
        response.setOrdinary_cart_data(getShoppingQueryResult(ordinaryChargeResult));
        response.setAdvance_cart_data(getShoppingQueryResult(preSaleChargeResult));
        
        //写缓存
        writeShoppingQueryResponseToRedis(request,JSON.toJSONString(response));
        
        logger.info("query shopping cart success, ##request {}, ##Promotion_info {}, ##Shopping_cart_data {} \n",
                request,response.getOrdinary_cart_data().getPromotion_info(),
                response.getOrdinary_cart_data().getShopping_cart_data());
        return response;
    }
    
    private void writeShoppingQueryResponseToRedis(ShoppingCartRequest request,String shoppingQueryResult){
        if(request.getUid()>0){
            shoppingCartRedis.setShoppingQueryResponse(String.valueOf(request.getUid()), 1,shoppingQueryResult); 
        }else{
            shoppingCartRedis.setShoppingQueryResponse(request.getShopping_key(), 0,shoppingQueryResult); 
        }
    }
    /**
     * 根据Uid或者Shopping_key从缓存中获取购物车列表缓存结果
     * @param request
     */
    private ShoppingQueryResponse buildShoppingQueryResponseFromRedis(ShoppingCartRequest request){
        String redisString;
        if(request.getUid()>0){
            redisString = shoppingCartRedis.getShoppingQueryResponse(String.valueOf(request.getUid()), 1); 
        }else{
            redisString = shoppingCartRedis.getShoppingQueryResponse(request.getShopping_key(), 0); 
        }
        if(redisString==null){
            return null;
        }
        ShoppingQueryResponse res =JSON.parseObject(redisString, ShoppingQueryResponse.class); 
        return res;
    }

    private ShoppingQueryResult getShoppingQueryResult(ShoppingChargeResult chargeResult) {
        ShoppingQueryResult shoppingQueryResult = new ShoppingQueryResult();
        shoppingQueryResult.setShopping_cart_data(chargeResult.getShopping_cart_data());
        shoppingQueryResult.setGoods_list(chargeResult.getGoods_list());
        shoppingQueryResult.setSold_out_goods_list(chargeResult.getSold_out_goods_list());
        shoppingQueryResult.setOff_shelves_goods_list(chargeResult.getOff_shelves_goods_list());
        shoppingQueryResult.setPromotion_info(chargeResult.getPromotion_info());
        shoppingQueryResult.setGift_list(chargeResult.getGift_list());
        shoppingQueryResult.setPrice_gift(chargeResult.getPrice_gift());

        return shoppingQueryResult;
    }

    /**
     * 普通商品算费参数对象
     *
     * @param request
     * @return
     */
    private ChargeParam newOrdinaryChargeParam(final ShoppingCartRequest request) {
        ChargeParam chargeParam = new ChargeParam();
        chargeParam.setShoppingKey(request.getShopping_key());
        chargeParam.setUid(request.getUid());
        chargeParam.setCartType(Constants.ORDINARY_CART_TYPE);
        chargeParam.setChargeType(Constants.ORDINARY_CHARGE_TYPE);
        chargeParam.setSaleChannel(request.getSale_channel());
        chargeParam.setNeedCalcShippingCost(false);
        chargeParam.setNeedQueryYohoCoin(false);
        chargeParam.setNeedQueryRedEnvelopes(false);
        chargeParam.setNeedAuditCodPay(false);
        chargeParam.setUserAgent(request.getUser_agent());
     //TODO 5-30 version
	 /*  5-30版本
	    //设置客户端发送过来的购物车信息
        if(request.getProduct_sku_list()!=null){
            chargeParam.setReqShopCartItems(request.getProduct_sku_list());
        }  */
        return chargeParam;
    }

    /**
     * 预售商品算费参数对象
     *
     * @param request
     * @return
     */
    private ChargeParam newPreSaleChargeParam(final ShoppingCartRequest request) {
        ChargeParam preSaleChargeParam = new ChargeParam();
        preSaleChargeParam.setShoppingKey(request.getShopping_key());
        preSaleChargeParam.setUid(request.getUid());
        preSaleChargeParam.setCartType(Constants.PRESALE_CART_TYPE);
        preSaleChargeParam.setChargeType(Constants.ADVANCE_CHARGE_TYPE);
        preSaleChargeParam.setSaleChannel(request.getSale_channel());
        preSaleChargeParam.setNeedCalcShippingCost(false);
        preSaleChargeParam.setNeedQueryYohoCoin(false);
        preSaleChargeParam.setNeedQueryRedEnvelopes(false);
        preSaleChargeParam.setNeedAuditCodPay(false);
        preSaleChargeParam.setUserAgent(request.getUser_agent());
        return preSaleChargeParam;
    }
}
