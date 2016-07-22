package com.yoho.yhorder.shopping.charge.caculator;

import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by JXWU on 2015/11/28.
 */
@Component
public class RuleCheck {

    public void charge(ChargeContext chargeContext) {

        checkMainGoods(chargeContext.getMainGoods(), chargeContext.getChargeTotal());

        checkOutletGoods(chargeContext.getOutletGoods(), chargeContext.getChargeTotal());
    }

    private void checkMainGoods(List<ChargeGoods> goodsList, ChargeTotal chargeTotal) {
        if (CollectionUtils.isNotEmpty(goodsList)) {
            for (ChargeGoods goods : goodsList) {
                //化妆品不能货到付款
                if (ShoppingConfig.MUST_ONLINE_PAYMENT_MISORT_LIST.contains(goods.getShoppingGoods().getMiddle_sort_id())) {
                    chargeTotal.resetMustOnlinePaymentInfo(true);
                }
                if (Constants.IS_LIMIT_STR.equals(goods.getShoppingGoods().getIs_limited())) {
                    chargeTotal.resetMustOnlinePaymentInfo(true);
                }
                //如果是限量
                if (goods.getShoppingGoods().getBuy_limit() > 0) {
                    chargeTotal.resetMustOnlinePaymentInfo(true);
                }
            }
        }
    }

    private void checkOutletGoods(List<ChargeGoods> goodsList, ChargeTotal chargeTotal) {
        if (CollectionUtils.isNotEmpty(goodsList)) {
            for (ChargeGoods goods : goodsList) {
                //化妆品不能货到付款
                if (ShoppingConfig.MUST_ONLINE_PAYMENT_MISORT_LIST.contains(goods.getShoppingGoods().getMiddle_sort_id())) {
                    chargeTotal.resetMustOnlinePaymentInfo(true);
                }
                //如果是限量
                if (goods.getShoppingGoods().getBuy_limit() > 0) {
                    chargeTotal.resetMustOnlinePaymentInfo(true);
                }
            }
        }
    }

}
