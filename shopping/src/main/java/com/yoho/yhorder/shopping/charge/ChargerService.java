package com.yoho.yhorder.shopping.charge;

import com.yoho.core.rest.client.hystrix.AsyncFuture;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.response.audit.AuditCodPayResponse;
import com.yoho.service.model.promotion.UserCouponsListBO;
import com.yoho.yhorder.shopping.charge.caculator.*;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.model.ChargeType;
import com.yoho.yhorder.shopping.event.UnionPushCartEvent;
import com.yoho.yhorder.shopping.event.UserVipCacheEvent;
import com.yoho.yhorder.shopping.union.UnionContext;
import com.yoho.yhorder.shopping.utils.VIPEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Created by JXWU on 2015/12/10.
 */
@Service
public class ChargerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    //
    @Autowired
    InitCharge initCharge;

    @Autowired
    VipCharge vipCharge;

    @Autowired
    OutletsCharge outletsCharge;

    // frwnote 会调用购物车删除
    @Autowired
    PromotionCharge promotionCharge;

    @Autowired
    CouponsPreCharge couponsCharge;

    @Autowired
    LimitCodeCharge limitCodeCharge;

    @Autowired
    SumCharge sumCharge;

    @Autowired
    FinalCharge finalCharge;

//    @Autowired
//    RuleCheck ruleCheck;

    @Autowired
    UsableCouponCharge usableCouponCharge;

    @Autowired
    CodPayCharge codPayCharge;

    @Autowired
    PackageSplitCharge packagePreCharge;

    @Autowired
    ShippingCostFormulaCharge shippingCostFormulaCharge;

    @Autowired
    private ApplicationEventPublisher publisher;


    /**
     * 计算购物车中商品的价格
     *
     * @param chargeContext 构造？？
     * @throws ServiceException
     */
    public void charge(ChargeContext chargeContext) throws ServiceException {
        this.doChange(chargeContext);

        //TODO 确认什么场景下需要做这个
        this.doPushing(chargeContext);
    }


    private void doChange(ChargeContext chargeContext) {
        //将商品进行分类，分类成普通商品、gift商品、price gift商品、outlets、advance商品
        chargeContext.classfic();
        //----  1 => 'YOHOCart_Hook_Calculate_Base',

        ChargeParam chargeParam = chargeContext.getChargeParam();
        ChargeType chargeType = chargeParam.getChargeType();

        //初始化
        initCharge.charge(chargeContext);

        switch (chargeType) {
            //普通商品
            case ORDINARY:
                doOrdinaryCharge(chargeContext);
                break;
            //预售产品
            case ADVANCE:
                doAdvanceCharge(chargeContext);
                break;
            //限购商品
            case LIMITCODE:
                doLimitCodeCharge(chargeContext);
                break;
            //计算可用的优惠券
            case LISTCOUPON:
                doListCouponCharge(chargeContext);
                break;
            case USECOUPON:
                doUseCouponCharge(chargeContext);
                break;
            case TICKET:
                doTicketCharge(chargeContext);
                break;
            default:
                //不应该出现的部分,直接异常
                throw new ServiceException(ServiceError.SHOPPING_SYS_ERROR);
        }

//        //计算VIP
//        if (chargeType.shouldVipCharge()) {
//            vipCharge.charge(chargeContext);
//        }
//
//        if (chargeType.shouldOutletsCharge()) {
//            //计算outlets
//            outletsCharge.charge(chargeContext);
//        }
//        if (chargeType.shouldPromotionCharge()) {
//            //计算促销
//            promotionCharge.charge(chargeContext);
//        }
//        if (chargeType.shouldCouponsCharge()) {
//            //验证设置优惠券
//            couponsCharge.charge(chargeContext);
//        }
//
//        if (chargeType.shouldLimitCodeCharge()) {
//            limitCodeCharge.charge(chargeContext);
//        }

        //
        //usableCouponCharge.charge(chargeContext, usalbeCouponAsyncFuture);

        //计算
       // sumCharge.charge(chargeContext);


        //基础优惠判断
       // finalCharge.charge(chargeContext);

        //货到付款
       // codPayCharge.charge(chargeContext,codPayAsyncFuture);

        // 2 => 'YOHOCart_Hook_Rules_Base'
        //ruleCheck.charge(chargeContext);


    }

    //普通购物车中的商品算费
    private void doOrdinaryCharge(ChargeContext chargeContext) {

        //异步调用货到付款
        AsyncFuture<AuditCodPayResponse> codPayAsyncFuture = codPayCharge.createAsyncFuture(chargeContext);
        //计算VIP
        vipCharge.charge(chargeContext);
        //计算outlets
        outletsCharge.charge(chargeContext);
        //计算促销
        promotionCharge.charge(chargeContext);
        //验证设置优惠券
        couponsCharge.charge(chargeContext);
        //计算
        sumCharge.charge(chargeContext);
        //基础优惠判断
        finalCharge.charge(chargeContext);
        //多jit
        packagePreCharge.charge(chargeContext);

        //运费,多包裹会拆分运费
        shippingCostFormulaCharge.charge(chargeContext);

        //货到付款
        codPayCharge.charge(chargeContext, codPayAsyncFuture);
    }

    //预售购物车中的商品算费
    private void doAdvanceCharge(ChargeContext chargeContext) {
        //异步调用货到付款
        AsyncFuture<AuditCodPayResponse> codPayAsyncFuture = codPayCharge.createAsyncFuture(chargeContext);
        //计算VIP
        vipCharge.charge(chargeContext);
        //计算
        sumCharge.charge(chargeContext);
        //基础优惠判断
        finalCharge.charge(chargeContext);
        //运费
        shippingCostFormulaCharge.charge(chargeContext);
        //货到付款
        codPayCharge.charge(chargeContext, codPayAsyncFuture);
    }

    //限购商品算费
    private void doLimitCodeCharge(ChargeContext chargeContext) {
        //异步调用货到付款
        AsyncFuture<AuditCodPayResponse> codPayAsyncFuture = codPayCharge.createAsyncFuture(chargeContext);
        //限购码校验
        limitCodeCharge.charge(chargeContext);
        //计算
        sumCharge.charge(chargeContext);
        //基础优惠判断
        finalCharge.charge(chargeContext);

        //运费
        shippingCostFormulaCharge.charge(chargeContext);

        //货到付款
        codPayCharge.charge(chargeContext, codPayAsyncFuture);
    }

    //可用优惠券的计算
    private void doListCouponCharge(ChargeContext chargeContext) {
        //可用优惠券
        AsyncFuture<UserCouponsListBO> usalbeCouponAsyncFuture = usableCouponCharge.createAsyncFuture(chargeContext);
        //计算VIP
        vipCharge.charge(chargeContext);
        //计算outlets
        outletsCharge.charge(chargeContext);
        //计算促销
        promotionCharge.charge(chargeContext);
        //可用的优惠券计算
        usableCouponCharge.charge(chargeContext, usalbeCouponAsyncFuture);
    }

    /**
     * useCoupon接口
     * @param chargeContext
     */
    private void doUseCouponCharge(ChargeContext chargeContext) {
        //计算VIP
        vipCharge.charge(chargeContext);
        //计算outlets
        outletsCharge.charge(chargeContext);
        //计算促销
        promotionCharge.charge(chargeContext);
        //验证设置优惠券
        couponsCharge.charge(chargeContext);
    }

    // 虚拟购物车中的商品算费
    private void doTicketCharge(ChargeContext chargeContext) {
        // 计算
        sumCharge.charge(chargeContext);
        // 基础优惠判断
        finalCharge.charge(chargeContext);
    }

    private void doPushing(ChargeContext chargeContext) {

        publishUnionPushEvent(chargeContext);

        publishUserVipCacheEventIfNecessary(chargeContext);
    }

    /**
     *  缓存用户等级信息
     * @param chargeContext
     */
    private void publishUserVipCacheEventIfNecessary(ChargeContext chargeContext) {
        int userLevel = chargeContext.getUserInfo().getUserLevel();
        if (userLevel > VIPEnum.VIP_0.curLevel) {
            UserVipCacheEvent event = new UserVipCacheEvent();
            event.setUserInfo(chargeContext.getUserInfo());
            publisher.publishEvent(event);
        }
    }

    private void publishUnionPushEvent(ChargeContext chargeContext) {
        //购物车推送
        UnionContext unionContext = new UnionContext();
        unionContext.setChargeContext(chargeContext);
        UnionPushCartEvent event = new UnionPushCartEvent();
        event.setUserAgent(chargeContext.getChargeParam().getUserAgent());
        event.setUnionContext(unionContext);
        publisher.publishEvent(event);
    }
}