package com.yoho.yhorder.shopping.service.impl;

import com.yoho.error.ServiceError;
import com.yoho.service.model.order.request.ShoppingCouponRequest;
import com.yoho.service.model.order.response.CountBO;
import com.yoho.service.model.order.response.shopping.ShoppingCouponListResponse;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.ChargeContextFactory;
import com.yoho.yhorder.shopping.charge.ChargerService;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.service.IShoppingCouponService;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MyAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wujiexiang on 16/4/6.
 * 计算用户有多少张优惠券
 */
@Service
public class ShoppingCouponService implements IShoppingCouponService {

    private final Logger logger = LoggerFactory.getLogger("addPaymentComputeLog");

    @Autowired
    private ChargeContextFactory chargeContextFactory;

    @Autowired
    private ChargerService chargerService;

    @Override
    public CountBO countUsableCoupon(ShoppingCouponRequest request) {
        logger.info("enter countUsableCoupon service,request is {}", request);

        ChargeContext chargeContext = chargeUsableCoupon(request);

        int usalbeCouponNum = chargeContext.getChargeTotal().getUsableCouponList().size();

        CountBO countBO = CountBO.valueOf(usalbeCouponNum);

        logger.info("exit countUsableCoupon service,request is {}\n,response is {}\n", request, countBO);

        return countBO;
    }

    @Override
    public ShoppingCouponListResponse listCoupon(ShoppingCouponRequest request) {

        logger.info("enter listCoupon service,request is {}", request);

        ChargeContext chargeContext = chargeUsableCoupon(request);

        ShoppingCouponListResponse response = new ShoppingCouponListResponse();
        response.setUsableCoupons(chargeContext.getChargeTotal().getUsableCouponList());
        response.setUnusableCoupons(chargeContext.getChargeTotal().getUnusableCouponList());

        logger.info("exit listCoupon service,request is {}\n,response is {}\n", request, response);
        return response;
    }


    /**
     * 算费参数
     *
     * @param request
     * @return
     */
    private ChargeParam newChargeParam(ShoppingCouponRequest request) {
        ChargeParam chargeParam = new ChargeParam();
        chargeParam.setUid(request.getUid());
        chargeParam.setCartType(Constants.ORDINARY_CART_TYPE);
        chargeParam.setChargeType(Constants.LISTCOUPON_CHARGE_TYPE);
        chargeParam.setNeedCalcShippingCost(false);
        chargeParam.setNeedQueryYohoCoin(false);
        chargeParam.setNeedQueryRedEnvelopes(false);
        chargeParam.setNeedAuditCodPay(false);
        return chargeParam;
    }

    private ChargeContext chargeUsableCoupon(ShoppingCouponRequest request) {
        int uid = request.getUid();
        MyAssert.isTrue(uid < 1, ServiceError.SHOPPING_UID_IS_NULL);

        ChargeParam chargeParam = newChargeParam(request);

        ChargeContext chargeContext = chargeContextFactory.build(true, chargeParam);

        chargerService.charge(chargeContext);

        return chargeContext;
    }
}