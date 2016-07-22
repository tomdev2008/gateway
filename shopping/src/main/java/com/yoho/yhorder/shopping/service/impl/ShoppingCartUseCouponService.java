package com.yoho.yhorder.shopping.service.impl;

import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.request.ShoppingComputeRequest;
import com.yoho.service.model.order.response.shopping.ShoppingUseCouponResponse;
import com.yoho.yhorder.shopping.charge.ChargeContextFactory;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.ChargerService;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by JXWU on 2015/11/28.
 */
@Service
public class ShoppingCartUseCouponService {

    @Autowired
    private ChargeContextFactory changeContextFactory;

    @Autowired
    private ChargerService chargerService;


    public ShoppingUseCouponResponse useCoupon(ShoppingComputeRequest request) {

        //1.算费
        //1.1新建算费参数
        ChargeParam chargeParam = newUseCouponChargeParam(request);
        //1.2新建普通商品算费context，context中包括需要算费的sku、用户信息等
        ChargeContext chargeContext = changeContextFactory.build(false, chargeParam);
        //1.3算费，算费结果在context对象的ChargeTotal中。
        chargerService.charge(chargeContext);

        //2.判断运费是否小于6元，小于则不能使用优惠券
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        if (!chargeTotal.isUseCoupon() && chargeTotal.getShippingCost() < 6 && "m".equals(chargeTotal.getCouponAlphabet())) {
            throw new ServiceException(ServiceError.SHOPPING_SHIPPINGCOST_IS_FREE_NEXTTEIME_USE_COUPONCODE);
            //500, '订单已免邮,免邮券下次再用吧.'
        }

        //3.返回结果
        ShoppingUseCouponResponse response = new ShoppingUseCouponResponse();
        response.setCoupon_code(chargeTotal.getCouponCode());
        response.setCoupon_title(chargeTotal.getCouponTitle());
        response.setCoupon_amount(chargeTotal.getCouponAmount());
        response.setUid(chargeContext.getChargeParam().getUid());
        return response;

    }


    /**
     * 新建一个购物车payment订单
     *
     * @return
     */
    private ChargeParam newUseCouponChargeParam(final ShoppingComputeRequest request) {
        ChargeParam chargeParam = new ChargeParam();
        chargeParam.setChargeType(Constants.USECOUPON_CHARGE_TYPE);
        chargeParam.setCouponCode(request.getCoupon_code());
        chargeParam.setUid(request.getUid());
        chargeParam.setCartType(request.getCart_type());
        chargeParam.setNeedCalcShippingCost(true);//不需要计算运费
        chargeParam.setNeedQueryYohoCoin(false);//不需要查询yoho币
        chargeParam.setNeedQueryRedEnvelopes(false);//不需要查询红包
        chargeParam.setNeedAuditCodPay(false);//不需要计算货到付款
        chargeParam.setUserAgent(request.getUser_agent());
        return chargeParam;
    }
}
