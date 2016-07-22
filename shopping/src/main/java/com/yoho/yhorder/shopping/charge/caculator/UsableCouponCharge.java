package com.yoho.yhorder.shopping.charge.caculator;

import com.google.common.collect.Lists;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.core.rest.client.hystrix.AsyncFuture;
import com.yoho.service.model.order.model.coupon.ShoppingCouponBO;
import com.yoho.service.model.order.request.OrdersStatusStatisticsRequest;
import com.yoho.service.model.order.response.CountBO;
import com.yoho.service.model.promotion.CouponsBo;
import com.yoho.service.model.promotion.UserCouponsListBO;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.coupon.service.CouponService;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.charge.model.CouponWapper;
import com.yoho.yhorder.shopping.model.UserInfo;
import com.yoho.yhorder.shopping.service.ExternalDegradeService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Created by wujiexiang on 16/4/6.
 * 可用的优惠券计算
 */
@Component
public class UsableCouponCharge {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    @Autowired
    ExternalDegradeService externalDegradeService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private ServiceCaller serviceCaller;

    /**
     * 创建异步调用的future
     *
     * @param chargeContext
     * @return
     */
    public AsyncFuture<UserCouponsListBO> createAsyncFuture(ChargeContext chargeContext) {

        AsyncFuture<UserCouponsListBO> future = null;
        if (shouldCharge(chargeContext)) {
            future = externalDegradeService.createQueryUserNotUseCouponsAsyncFuture(chargeContext.getChargeParam().getUid());
        }

        return future;
    }


    public void charge(ChargeContext chargeContext, AsyncFuture<UserCouponsListBO> asyncFuture) {

        if (shouldCharge(chargeContext)) {
            doCharge(chargeContext, asyncFuture);
        }
    }

    private void doCharge(ChargeContext chargeContext, AsyncFuture<UserCouponsListBO> asyncFuture) {
        //获取用户未使用的优惠券
        List<CouponsBo> couponBoList = getUserNotUseCouponList(chargeContext, asyncFuture);

        if (CollectionUtils.isNotEmpty(couponBoList)) {
            //分成可用和不可用两组
            classifyToUsableAndUnUsable(couponBoList, chargeContext);
        }

        logger.info("STEP usableCoupon charge,usable coupons ares {},unusable coupons are {}",
                chargeContext.getChargeTotal().getUsableCouponList(),
                chargeContext.getChargeTotal().getUnusableCouponList());
    }

    private boolean shouldCharge(ChargeContext chargeContext) {
        return CollectionUtils.isNotEmpty(chargeContext.getMainGoods());
    }


    private List<CouponsBo> getUserNotUseCouponList(ChargeContext chargeContext, AsyncFuture<UserCouponsListBO> asyncFuture) {
        ChargeParam chargeParam = chargeContext.getChargeParam();
        if (asyncFuture == null) {
            logger.info("asyncFuture is null,charge param is {}", chargeParam);
            return Collections.emptyList();
        }

        UserCouponsListBO userCouponsListBO = asyncFuture.get();

        if (userCouponsListBO == null) {
            logger.info("asyncFuture response is null,charge param is {}", chargeParam);
            return Collections.emptyList();
        }

        return userCouponsListBO.getCouponList();
    }

    private void classifyToUsableAndUnUsable(List<CouponsBo> couponBoList, ChargeContext chargeContext) {
        ChargeTotal chargetTotal = chargeContext.getChargeTotal();

        UserInfo userInfo = chargeContext.getUserInfo();
        int uid = chargeContext.getChargeParam().getUid();
        int userLevel = userInfo.getUserLevel();
        // boolean isNewUser=isNewUser(uid,userLevel);

        for (CouponsBo couponBo : couponBoList) {
            boolean isUsable = isUsableCoupon(uid, userLevel, couponBo, chargeContext.getMainGoods());
            ShoppingCouponBO couponBO = transToShoppingCoupon(couponBo);
            if (isUsable) {
                chargetTotal.getUsableCouponList().add(couponBO);
            } else {
                chargetTotal.getUnusableCouponList().add(couponBO);
            }
        }
    }

    private boolean isNewUser(int uid, int userLevel) {
        if (userLevel > 0) return false;
        OrdersStatusStatisticsRequest request = new OrdersStatusStatisticsRequest();
        request.setStatus(Lists.newArrayList(6));
        request.setUid(uid);
        try {
            logger.info("begin serviceCaller.call(order.getOrdersCountByUidAndStatus),uid:{}", uid);
            CountBO countBO = serviceCaller.call("order.getOrdersCountByUidAndStatus", request, CountBO.class);
            logger.info("end serviceCaller.call(order.getOrdersCountByUidAndStatus),uid:{}", uid);
            if (countBO != null && countBO.getCount() < 1) {
                return true;
            }
        } catch (Exception e) {
            logger.warn("has exception:serviceCaller.call(order.getOrdersCountByUidAndStatus),uid:{}", uid);
            return false;
        }
        return false;
    }

    private boolean isUsableCoupon(int uid, int userLevel, CouponsBo couponsBo, List<ChargeGoods> mainGoodsList) {
        //优惠券的包装类
        CouponWapper couponWapper = new CouponWapper();
        couponWapper.setUid(uid);
        couponWapper.setCouponsBo(couponsBo);
        couponWapper.setUserLevel(userLevel);
        // couponWapper.setNewUser(isNewUser);
        couponService.caculateCoupon(couponWapper, mainGoodsList);
        return couponWapper.isUsable();
    }

    private ShoppingCouponBO transToShoppingCoupon(CouponsBo userCouponBO) {
        //TODO
        ShoppingCouponBO shoppingCouponBO = new ShoppingCouponBO();
        shoppingCouponBO.setCouponCode(userCouponBO.getCouponCode());
        shoppingCouponBO.setCouponName(userCouponBO.getCouponName());
        shoppingCouponBO.setCouponType(userCouponBO.getCouponType());
        shoppingCouponBO.setCouponValue(String.valueOf(userCouponBO.getCouponAmount()));
        shoppingCouponBO.setCouponValidity(userCouponBO.getCouponValidity());

        return shoppingCouponBO;
    }
}
