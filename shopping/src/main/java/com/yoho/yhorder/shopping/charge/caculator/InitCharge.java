package com.yoho.yhorder.shopping.charge.caculator;

import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MathUtils;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 计算普通商品的总价
 * <p/>
 * Created by JXWU on 2015/11/20.
 */
@Component
public class InitCharge {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    public void charge(ChargeContext chargeContext) {
        int goodsCount = 0;
        int selectedGoodsCount = 0;
        double orderAmount = 0;
        List<ChargeGoods> mainGoods = chargeContext.getMainGoods();
        for (ChargeGoods goods : mainGoods) {
            if ("Y".equals(goods.getShoppingGoods().getSelected())) {
                //对赠品，加价购的商品部计算总价
                if (Constants.ORDER_GOODS_TYPE_GIFT_STR.equals(goods.getShoppingGoods().getGoods_type())) {

                } else if (Constants.ORDER_GOODS_TYPE_PRICE_GIFT_STR.equals(goods.getShoppingGoods().getGoods_type())) {

                } else {
                    orderAmount += MathUtils.round(goods.getShoppingGoods().getSales_price() * goods.getBuyNumber());
                }

                selectedGoodsCount += goods.getBuyNumber();
            }
            goodsCount += goods.getBuyNumber();
        }

        ChargeTotal total = new ChargeTotal();
        total.setOrderAmount(MathUtils.round(orderAmount));
        total.setGoodsCount(goodsCount);
        total.setSelectedGoodsCount(selectedGoodsCount);
        //设置运费
        ChargeParam chargeParam = chargeContext.getChargeParam();
        if (chargeParam.isNeedCalcShippingCost() && chargeParam.getShippingManner() > 0) {
            total.setShippingCost(Constants.SHIPPING_COST);
            if (chargeParam.getShippingManner() == 2) {
                //加急
                total.setFastShoppingCost(MathUtils.round(Constants.SHIPPING_COST + Constants.FAST_SHOPPING_COST));
            }
        }

        logger.info("[{}] init charge, user info {}, total {} , chargeParam {}. ",
                chargeContext.getChargeParam().getUid(),
                chargeContext.getUserInfo(),
                total,chargeParam);

        chargeContext.setChargeTotal(total);
    }
}
