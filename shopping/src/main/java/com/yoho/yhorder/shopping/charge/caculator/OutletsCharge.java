package com.yoho.yhorder.shopping.charge.caculator;

import com.yoho.core.common.utils.YHMath;
import com.yoho.service.model.order.response.shopping.PromotionFormula;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.charge.model.DiscountType;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MathUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * isRunPromotion: 预售：true。否则 false
 * <p/>
 * 计算所有outles商品的总价，如果大于1999，则每件outlets商品的价格打9折
 * <p/>
 * Created by JXWU on 2015/11/23.
 */
@Component
public class OutletsCharge {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    public void charge(ChargeContext chargeContext) {
        if (CollectionUtils.isEmpty(chargeContext.getOutletGoods())) {
            //预售购物车或者没有outlet sku
            return;
        }
        double outletAmount = 0;
        for (ChargeGoods chargeGoods : chargeContext.getOutletGoods()) {

            if(!chargeGoods.isSelected()){
                continue;
            }

            double roundRealPrice = MathUtils.round(chargeGoods.getShoppingGoods().getReal_price());
            chargeGoods.getShoppingGoods().setReal_price(roundRealPrice);
            chargeGoods.getShoppingGoods().setLast_price(String.valueOf(roundRealPrice));

            outletAmount += YHMath.mul(chargeGoods.getShoppingGoods().getReal_price() , chargeGoods.getBuyNumber());
        }

        logger.info("[{}] outlet charge, outletAmount:{}, Constants.OUTLET_AMOUNT:{}",
                chargeContext.getChargeParam().getUid(),outletAmount, Constants.OUTLET_AMOUNT);

        if (outletAmount > Constants.OUTLET_AMOUNT) {
            ChargeTotal chargeTotal = chargeContext.getChargeTotal();

            for (ChargeGoods chargeGoods : chargeContext.getOutletGoods()) {
                double oldRealPriceForLog = chargeGoods.getShoppingGoods().getReal_price() ;
                double tempPrice = YHMath.mul(chargeGoods.getShoppingGoods().getReal_price() , 0.9);
                double outletCutdownAmount = YHMath.mul(MathUtils.minus(chargeGoods.getShoppingGoods().getReal_price(), tempPrice) , chargeGoods.getBuyNumber());
                chargeTotal.setOutletCutdownAmount(YHMath.add(chargeTotal.getOutletCutdownAmount() , outletCutdownAmount));

                //设置到促销,orders_goods中没有outlet的减免字段,在拆单中会使用
                chargeGoods.getDiscountPerSku().setDiscountAmount(YHMath.sub(chargeGoods.getShoppingGoods().getReal_price(),tempPrice), DiscountType.PROMOTION);

                chargeGoods.getShoppingGoods().setReal_price(tempPrice);
                chargeGoods.getShoppingGoods().setLast_price(String.valueOf(tempPrice));

                logger.info("[{}] outlet charge, RealPrice before:{}, RealPrice after:{}, outletCutdownAmount{}, outletCutdownAmount total {}",
                        chargeContext.getChargeParam().getUid(),
                        oldRealPriceForLog,
                        tempPrice,
                        outletCutdownAmount,
                        chargeTotal.getOutletCutdownAmount()
                        );
            }

            //折扣信息
            if (chargeTotal.getOutletCutdownAmount() > 0) {
                chargeTotal.getPromotionFormulaList().add(new PromotionFormula("-", "outlet", MathUtils.formatCurrencyStr(chargeTotal.getOutletCutdownAmount())));
            }
        }
    }
}
