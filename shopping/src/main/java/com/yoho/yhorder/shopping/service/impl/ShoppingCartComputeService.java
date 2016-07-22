package com.yoho.yhorder.shopping.service.impl;

import com.yoho.service.model.order.request.ShoppingComputeRequest;
import com.yoho.service.model.order.response.shopping.ShoppingChargeResult;
import com.yoho.service.model.order.response.shopping.ShoppingComputeResponse;
import com.yoho.yhorder.common.cache.redis.ShoppingCartRedis;
import com.yoho.yhorder.common.utils.OrderPackageUtils;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.ChargeContextFactory;
import com.yoho.yhorder.shopping.charge.ChargerService;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.model.PaymentSetting;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by JXWU on 2015/11/25.
 */
@Service
public class ShoppingCartComputeService {

    private final Logger logger = LoggerFactory.getLogger("addPaymentComputeLog");

    @Autowired
    private ChargeContextFactory changeContextFactory;

    @Autowired
    private ChargerService chargerService;

    @Autowired
    private ShoppingCartPaymentService paymentService;
    
    @Autowired
    private ShoppingCartRedis shoppingCartRedis;

    public ShoppingComputeResponse compute(ShoppingComputeRequest request) {

        logger.info("enter shopping cart compute service,request is {}", request);
        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), request.getShopping_key());
        //1.算费
        //1.1新建算费参数
        ChargeParam chargeParam = newComputeChargeParam(request);
        //1.2新建普通商品算费context，context中包括需要算费的sku、用户信息等
        //查询数据库表shopping_cart_items的自选sku和未选sku
        ChargeContext chargeContext = changeContextFactory.build(true, chargeParam);
        //1.3算费，算费结果在context对象的ChargeTotal中。
        chargerService.charge(chargeContext);

        //2.1获取订单默认支付、快递方式、邮寄地址、发票设置，用户在订单确认可以修改这些设置。
        PaymentSetting defaultPaymentSetting = paymentService.getDefaultPaymentSetting(request.getUid());

        //2.2根据算费结果修改支付、快递设置
        paymentService.resetPaymentTypeSetting(chargeContext, defaultPaymentSetting);

        //2.构造返回结果
        ShoppingComputeResponse response = getShoppingComputeResponse(defaultPaymentSetting,chargeContext);

        logger.info("exit shopping cart compute service,\nrequest is: {}\n,promotion_formula_list is: {} \n," +
                "payment_way is {}\n,is_multi_package: {}\n",
                request, response.getPromotion_formula_list(),response.getPayment_way(),response.getIs_multi_package());

        return response;
    }


    private ShoppingComputeResponse getShoppingComputeResponse(PaymentSetting defaultPaymentSetting,ChargeContext chargeContext) {
        ShoppingComputeResponse computeResponse = new ShoppingComputeResponse();
        //获取算费结果
        ShoppingChargeResult chargeResult = chargeContext.getChargeResult();
        computeResponse.setOrder_amount(chargeResult.getShopping_cart_data().getOrder_amount());
        computeResponse.setLast_order_amount(chargeResult.getShopping_cart_data().getLast_order_amount());
        computeResponse.setDiscount_amount(chargeResult.getShopping_cart_data().getDiscount_amount());
        computeResponse.setGoods_count(chargeResult.getShopping_cart_data().getGoods_count());
        computeResponse.setSelected_goods_count(chargeResult.getShopping_cart_data().getSelected_goods_count());
        computeResponse.setGain_yoho_coin(chargeResult.getShopping_cart_data().getGain_yoho_coin());
        computeResponse.setPromotion_formula(chargeResult.getShopping_cart_data().getPromotion_formula());
        computeResponse.setStr_order_amount(chargeResult.getShopping_cart_data().getStr_order_amount());
        computeResponse.setStr_discount_amount(chargeResult.getShopping_cart_data().getStr_discount_amount());
        computeResponse.setShipping_cost(chargeResult.getShopping_cart_data().getShipping_cost());
        computeResponse.setFast_shopping_cost(chargeResult.getShopping_cart_data().getFast_shopping_cost());
        computeResponse.setPromotion_formula_list(chargeResult.getShopping_cart_data().getPromotion_formula_list());
        computeResponse.setUse_yoho_coin(chargeContext.getChargeTotal().getUseYohoCoin());
        computeResponse.setCoupon_amount(chargeContext.getChargeTotal().getCouponAmount());
        computeResponse.setShipping_cost(chargeContext.getChargeTotal().getShippingCost());
        computeResponse.setGoods_count(chargeContext.getChargeTotal().getGoodsCount());
        computeResponse.setUse_red_envelopes(chargeContext.getChargeTotal().getUseRedEnvelopes());

        //重新设置运费，折扣可能不满足免运费
        computeResponse.setDelivery_way(paymentService.getDeliveryWay(chargeResult.getShopping_cart_data(), defaultPaymentSetting));


        //设置订单支付方式
        computeResponse.setPayment_way(paymentService.getPaymentWay(defaultPaymentSetting));

        computeResponse.setPackage_list(chargeContext.getChargeTotal().getPackageList());

        computeResponse.setIs_multi_package(OrderPackageUtils.canSplitSubOrder(computeResponse.getPackage_list().size()) ? "Y" : "N");


        return computeResponse;
    }

    private ChargeParam newComputeChargeParam(ShoppingComputeRequest request) {
        ChargeParam chargeParam = new ChargeParam();
        chargeParam.setUseYohoCoin(request.getUse_yoho_coin());
        chargeParam.setCouponCode(request.getCoupon_code());
        chargeParam.setPaymentType(request.getPayment_type());
        chargeParam.setShippingManner(request.getDelivery_way());
        chargeParam.setUseRedEnvelopes(request.getUse_red_envelopes());
        chargeParam.setCartType(request.getCart_type());
        chargeParam.parseProductSkuListParameterAndSetupChargeType(request.getCart_type(),request.getProduct_sku_list());
        chargeParam.setUid(request.getUid());
        chargeParam.setNeedCalcShippingCost(true);//计算运费
        chargeParam.setUserAgent(request.getUser_agent());
        //优惠码
        chargeParam.setPromotionCode(request.getPromotion_code());



        return chargeParam;
    }

}
