package com.yoho.yhorder.shopping.charge.caculator;

import com.yoho.core.common.utils.YHMath;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MathUtils;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by JXWU on 2015/11/21.
 */
@Component
public class SumCharge {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    public void charge(ChargeContext chargeContext) {
        ChargeParam chargeParam = chargeContext.getChargeParam();
        ChargeTotal total = chargeContext.getChargeTotal();

        //GoodsCount购物车中所有商品数量，
        //清零，计算优惠时，可能会有赠品，但是不修改GoodsCount
        total.setGoodsCount(0);


        total.setLastOrderAmount(0);
        total.setSelectedGoodsCount(0);
        total.setSelectedMainGoodsCount(0);
        total.setOrderAmount(0);
        chargeMainGoods(chargeContext);

        if (chargeParam.getCartType().equals(Constants.ORDINARY_CART_TYPE)) {
            chargeOtherGoods(chargeContext.getMainGoodsPriceGift(), total);
            chargeOtherGoods(chargeContext.getMainGoodsGift(), total);
            chargeOtherGoods(chargeContext.getOutletGoods(), total);
        }

        total.setDiscountAmount(MathUtils.round(total.getDiscountAmount()));

    }

    private void chargeMainGoods(ChargeContext chargeContext) {
        ChargeTotal total = chargeContext.getChargeTotal();
        List<ChargeGoods> mainGoodsList = chargeContext.getMainGoods();
        double gainYohoCoin = total.getGainYohoCoin();
        int goodsCount = total.getGoodsCount();
        double lastOrderAmount = total.getLastOrderAmount();
        int selectedGoodsCount = total.getSelectedGoodsCount();
        int selectedMainGoodsCount = 0;
        double lastMainGoodsOrderAmount = 0;
        //orderAmount用于计算总价
        double orderAmount = total.getOrderAmount();
        //subtotal 用于计算会员总价
        double subtotal = 0;

        if (CollectionUtils.isNotEmpty(mainGoodsList)) {
            for (ChargeGoods goods : mainGoodsList) {


                double realPrice = MathUtils.round(goods.getShoppingGoods().getReal_price());
                if (StringUtils.equals("Y", goods.getShoppingGoods().getSelected())) {

                    lastMainGoodsOrderAmount = YHMath.add(lastMainGoodsOrderAmount, YHMath.mul(realPrice, Double.valueOf(goods.getShoppingGoods().getBuy_number())));
                    //add last order amount
                    lastOrderAmount  =  YHMath.add(lastOrderAmount , YHMath.mul(realPrice, Double.valueOf(goods.getShoppingGoods().getBuy_number())));
                    logger.debug("summer charge, add:{}", lastOrderAmount);
                    selectedGoodsCount += goods.getBuyNumber();
                    selectedMainGoodsCount += goods.getBuyNumber();
                    orderAmount += YHMath.mul(goods.getShoppingGoods().getSales_price(), goods.getBuyNumber());

                    //预售商品不返yoho币 后期放在单独的charge计算
                    if (goods.isShoppingGoodsAdvanced()) {
                        unableGetYohoCoinFrom(goods);
                    } else {
                        gainYohoCoin += Integer.parseInt(goods.getShoppingGoods().getGet_yoho_coin());
                    }
                }
                goodsCount += goods.getBuyNumber();
                goods.getShoppingGoods().setReal_price(realPrice);
                goods.getShoppingGoods().setLast_price(String.valueOf(realPrice));

                if (Constants.ORDER_GOODS_TYPE_PRICE_GIFT_STR.equals(goods.getShoppingGoods().getGoods_type())) {
                    subtotal = YHMath.mul(Double.parseDouble(goods.getShoppingGoods().getLast_price()), goods.getBuyNumber());

                } else if (Constants.ORDER_GOODS_TYPE_GIFT_STR.equals(goods.getShoppingGoods().getGoods_type())) {
                    subtotal = YHMath.mul(Double.parseDouble(goods.getShoppingGoods().getLast_price()), goods.getBuyNumber());
                } else {
                    subtotal = YHMath.mul(goods.getShoppingGoods().getLast_vip_price() , goods.getBuyNumber());
                }
                goods.getShoppingGoods().setSubtotal(subtotal);
                goods.getShoppingGoods().setStr_subtotal(MathUtils.formatCurrencyStr(subtotal));
            }
        }


        total.setSelectedGoodsCount(selectedGoodsCount);
        total.setSelectedMainGoodsCount(selectedMainGoodsCount);
        total.setGainYohoCoin(gainYohoCoin);
        total.setOrderAmount(MathUtils.round(orderAmount));
        total.setGoodsCount(goodsCount);
        total.setLastOrderAmount(lastOrderAmount);
        total.setLastMainGoodsOrderAmount(lastMainGoodsOrderAmount);

        logger.info("STEP Charge MainGoods [{}]  sum charge , SelectedGoodsCount {},SelectedMainGoodsCount {}, GainYohoCoin {}, OrderAmount {}, GoodsCount {}, FinalOrderAmount {},mainGoodsLastOrderAmount {} ",
                chargeContext.getChargeParam().getUid(),
                total.getSelectedGoodsCount(),
                total.getSelectedMainGoodsCount(),
                total.getGainYohoCoin(),
                total.getOrderAmount(),
                total.getGoodsCount(),
                total.getLastOrderAmount(),
                lastMainGoodsOrderAmount);

    }


    private void chargeOtherGoods(List<ChargeGoods> goodsList, ChargeTotal total) {
        double gainYohoCoin = total.getGainYohoCoin();
        int goodsCount = total.getGoodsCount();
        double lastOrderAmount = total.getLastOrderAmount();
        int selectedGoodsCount = total.getSelectedGoodsCount();
        double orderAmount = total.getOrderAmount();

        if (CollectionUtils.isNotEmpty(goodsList)) {
            for (ChargeGoods goods : goodsList) {
                if ("Y".equals(goods.getShoppingGoods().getSelected())) {
                    orderAmount += YHMath.mul(goods.getShoppingGoods().getSales_price() , goods.getBuyNumber());

                    //不返还yoho币
                    unableGetYohoCoinFrom(goods);

                    //重新设置real price
                    double realPrice = MathUtils.round(goods.getShoppingGoods().getReal_price());
                    goods.getShoppingGoods().setReal_price(realPrice);
                    double _new_amount = YHMath.mul(realPrice , Double.valueOf(goods.getShoppingGoods().getBuy_number()));
                    lastOrderAmount =  YHMath.add(lastOrderAmount, _new_amount);

                    logger.debug("summer otherGoods charge, add:{}", _new_amount);

                    selectedGoodsCount += goods.getBuyNumber();
                }
                goodsCount += goods.getBuyNumber();
            }
        }

        total.setSelectedGoodsCount(selectedGoodsCount);
        total.setGainYohoCoin(gainYohoCoin);
        total.setOrderAmount(MathUtils.round(orderAmount));
        total.setGoodsCount(goodsCount);
        total.setLastOrderAmount(lastOrderAmount);

        logger.info("STEP Charge OtherGoods , SelectedGoodsCount {} GainYohoCoin {}, OrderAmount {}, GoodsCount {}, FinalOrderAmount {} ",
                total.getSelectedGoodsCount(),
                total.getGainYohoCoin(),
                total.getOrderAmount(),
                total.getGoodsCount(),
                total.getLastOrderAmount());
    }


    private void unableGetYohoCoinFrom(ChargeGoods goods) {
        goods.getShoppingGoods().setGet_yoho_coin("0");
        goods.getShoppingGoods().setYoho_coin_num("0");
    }
}
