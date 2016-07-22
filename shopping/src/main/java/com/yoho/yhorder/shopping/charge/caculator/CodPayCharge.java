package com.yoho.yhorder.shopping.charge.caculator;

import com.yoho.core.rest.client.hystrix.AsyncFuture;
import com.yoho.service.model.order.model.audit.AuditGoods;
import com.yoho.service.model.order.response.audit.AuditCodPayResponse;
import com.yoho.yhorder.common.utils.Constants;
import com.yoho.yhorder.common.utils.OrderYmlUtils;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.service.ExternalService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wujiexiang on 16/4/6.
 * 货到付款
 */
@Component
public class CodPayCharge {

    @Autowired
    ExternalService externalService;

    private final Logger logger = LoggerFactory.getLogger("calculateLog");


    public AsyncFuture<AuditCodPayResponse> createAsyncFuture(ChargeContext chargeContext) {

        AsyncFuture<AuditCodPayResponse> future = null;
        if (shouldCharge(chargeContext)) {
            future =  doCreateAsyncFuture(chargeContext);
        }
        return future;
    }

    private AsyncFuture<AuditCodPayResponse> doCreateAsyncFuture(ChargeContext chargeContext) {
        final List<AuditGoods> goodsList = mergeAuditGoodsExcludePromotionalGoods(chargeContext);

        return externalService.createAuditCodPayAsyncFuture(chargeContext.getChargeParam().getUid(), goodsList, 0);
    }


    public void charge(ChargeContext chargeContext, AsyncFuture<AuditCodPayResponse> asyncFuture) {
        if (shouldCharge(chargeContext)) {
            doCharge(chargeContext, asyncFuture);
        }
    }


    private void doCharge(ChargeContext chargeContext, AsyncFuture<AuditCodPayResponse> asyncFuture) {
        ChargeParam chargeParam = chargeContext.getChargeParam();
        if (asyncFuture == null) {
            logger.warn("codPayFuture is null,charge param is {}", chargeParam);
            return;
        }

        //用户及goodList的校验
        AuditCodPayResponse defaultResponse = new AuditCodPayResponse();
        //默认不支持货到付款,防止服务访问超时,导致不必要的事情发生,如jit拆单,必须是在线支付
        defaultResponse.setIsSupport("N");
        AuditCodPayResponse response = externalService.getAuditCodPayResponseFrom(asyncFuture, 1, defaultResponse);
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        chargeTotal.resetMustOnlinePaymentInfo(response);

        if (!chargeTotal.isMustOnlinePayment()) {
            //单笔订单金额限制
            double orderAmount = chargeTotal.getLastOrderAmount();
            if (orderAmount > Constants.CODPAY_ORDER_AMOUNT_THRESHOLD) {
                logger.info("uid {},current amount is {}", chargeParam.getUid(), orderAmount);
                chargeTotal.setMustOnlinePayment(true);
                chargeTotal.setMustOnlinePaymentReason(OrderYmlUtils.getCashOnDeliveryMessage("R5"));
            }
        }

        logger.info("Step codPay charge,uid {},last order amount {},must online pay result is: {},must online pay message: {}",
                chargeParam.getUid(),
                chargeTotal.getLastOrderAmount(),
                chargeTotal.isMustOnlinePayment(),
                chargeTotal.getMustOnlinePaymentReason());
    }


    /**
     * 获取需要审核的商品,不包括赠品和加价购
     *
     * @param chargeContext
     * @return
     */
    private List<AuditGoods> mergeAuditGoodsExcludePromotionalGoods(ChargeContext chargeContext) {
        List<AuditGoods> goodsList = new ArrayList<>();
        List<ChargeGoods> goodsListToUse = new ArrayList<>();
        ChargeParam chargeParam = chargeContext.getChargeParam();
        goodsListToUse.addAll(chargeContext.getMainGoods());
        if (!chargeParam.isPreSaleCart()) {
            goodsListToUse.addAll(chargeContext.getOutletGoods());
        }

        if (CollectionUtils.isNotEmpty(goodsListToUse)) {
            for (ChargeGoods chargeGoods : goodsListToUse) {
                if (!chargeGoods.isSelected()) {
                    continue;
                }
                AuditGoods goods = new AuditGoods();
                goods.setBuy_limit(chargeGoods.getShoppingGoods().getBuy_limit());
                goods.setIs_advance(chargeGoods.getShoppingGoods().getIs_advance());
                goods.setIs_limited(chargeGoods.getShoppingGoods().getIs_limited());
                goods.setMiddle_sort_id(chargeGoods.getShoppingGoods().getMiddle_sort_id());
                goods.setProduct_sku(chargeGoods.getShoppingGoods().getProduct_sku());
                goods.setProduct_skn(chargeGoods.getShoppingGoods().getProduct_skn());

                goods.setCan_cod_pay(chargeGoods.getShoppingGoods().getCan_cod_pay());

                goods.setIs_jit(chargeGoods.getShoppingGoods().getIs_jit());

                goodsList.add(goods);
            }
        }
        return goodsList;
    }

    private boolean shouldCharge(ChargeContext chargeContext) {
        boolean needChargeFlag = chargeContext.getChargeParam().isNeedAuditCodPay();
        List<AuditGoods> goodsList = mergeAuditGoodsExcludePromotionalGoods(chargeContext);
        return needChargeFlag && CollectionUtils.isNotEmpty(goodsList);
    }
}
