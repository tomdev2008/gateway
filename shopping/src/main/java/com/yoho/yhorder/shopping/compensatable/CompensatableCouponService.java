package com.yoho.yhorder.shopping.compensatable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.core.transaction.annoation.TxCompensatable;
import com.yoho.core.transaction.annoation.TxCompensateArgs;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.promotion.request.CouponUseReq;
import com.yoho.service.model.promotion.request.CouponsLogReq;
import com.yoho.yhorder.common.model.Coupon;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by wujiexiang on 16/5/18.
 * 优惠码
 */
@Component
public class CompensatableCouponService {

    private final Logger orderSubmitLog = LoggerFactory.getLogger("orderSubmitLog");

    private final Logger orderCompensateLog = LoggerFactory.getLogger("orderCompensateLog");

    @Autowired
    protected ServiceCaller serviceCaller;

    @TxCompensatable(value = CompensatableCouponService.class)
    public void usePromotionCoupon(@TxCompensateArgs("uid") int uid, @TxCompensateArgs("orderCode") long orderCode,
                                   @TxCompensateArgs("coupon") Coupon coupon) {
        orderSubmitLog.info("order {} request to use coupon,uid is {}, coupon is {}", orderCode, uid, coupon);
        if (coupon != null && StringUtils.isNotEmpty(coupon.getCoupon_code())) {
            //添加优惠券的关联,并标记原来的优惠券为已经使用状态coupon_info_id  coupon_code
            CouponUseReq req = new CouponUseReq();
            req.setUid(uid);
            req.setCouponId(coupon.getCoupon_id());
            req.setCouponCode(coupon.getCoupon_code());
            req.setOrderCode(orderCode);
            Boolean success = serviceCaller.asyncCall(ShoppingConfig.PROMOTION_USECOUPON_REST_URL, req, Boolean.class).get(12);
            orderSubmitLog.info("call service {} ,request is {},response is {}", ShoppingConfig.PROMOTION_USECOUPON_REST_URL, req, success);
            if (success != null && success.booleanValue() == false) {
                orderSubmitLog.warn("coupon:{} can not be used.", req);
                throw new ServiceException(ServiceError.PROMOTION_COUPON_IS_NOT_VAILD);
            }
        } else {
            orderSubmitLog.info("order can not use coupon,uid is {},order code is {}", uid, orderCode);
        }
    }

    public void compensate(String message) {
        orderCompensateLog.info("CompensatableCouponService begin to compensate : {}", message);
        int uid = 0;
        long orderCode = 0;
        Coupon coupon = null;
        try {
            JSONObject json = JSON.parseObject(message);
            uid = json.getIntValue("uid");
            orderCode = json.getLongValue("orderCode");
            coupon = json.getObject("coupon", Coupon.class);
        } catch (Exception ex) {
            orderCompensateLog.warn("parse message to json error,message is {}", message, ex);
        }

        compensateCoupon(uid, orderCode, coupon);

        orderCompensateLog.info("CompensatableCouponService compensate end,uid is {},orderCode is {}", uid, orderCode);
    }

    private void compensateCoupon(int uid, long orderCode, Coupon coupon) {
        orderCompensateLog.info("compensate Coupon,uid is {},orderCode is {},coupon is {}", uid, orderCode, coupon);
        if (uid > 0 && orderCode > 0 && coupon != null && StringUtils.isNotEmpty(coupon.getCoupon_code())) {
            CouponsLogReq couponsLogReq = new CouponsLogReq();
            couponsLogReq.setUid(uid);
            couponsLogReq.setOrderCode(orderCode);
            boolean result = serviceCaller.call("promotion.cancelOrderCouponUse", couponsLogReq, Boolean.class);
            if (result) {
                orderCompensateLog.info("cancelOrderCouponUse success by order code {}, uid {}.", orderCode, uid);
            } else {
                orderCompensateLog.info("cancelOrderCouponUse fail by order code {}, uid {}.", orderCode, uid);
            }
        }
    }
}
