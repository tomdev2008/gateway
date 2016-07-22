package com.yoho.yhorder.shopping.charge.caculator;

import com.yoho.core.common.utils.YHMath;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.model.UserInfo;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MathUtils;
import com.yoho.yhorder.shopping.utils.VIPEnum;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * VIP 折扣：只处理 mainGoods
 * <p>
 * Created by JXWU on 2015/11/22.
 */
@Component
public class VipCharge {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    private boolean shouldCharge(ChargeContext chargeContext) {

        //预售商品和普通商品都需要进行vip计算
        List<ChargeGoods> mainGoodsList = chargeContext.getMainGoods();
        if (CollectionUtils.isNotEmpty(mainGoodsList)) {
            return true;
        }

        return false;
    }

    public void charge(ChargeContext chargeContext) {

        if (!shouldCharge(chargeContext)) {
            return;
        }

        //List<ChargeGoods> mainGoodsList = chargeContext.getMainGoods();
        //List<ChargeGoods> advanceGoodsList =  chargeContext.getAdvanceGoods();
        //校验每个月计算VIP的订单数量限制，如果超过这个数量，则不享受VIP的优惠
        if (chargeContext.getUserInfo().getMonthOrderCount() > Constants.VIP_CHARGE__ORDER_COUNT_LIMIT) {
            return;
        }

        ChargeTotal chargeTotal = chargeContext.getChargeTotal();

        if (chargeContext.getUserInfo().getUserLevel() > 0) {
            //VIP计算
            vipGoodsCalculate(chargeContext);

            /**
            if (chargeTotal.getVipCutdownAmount() > 0) {
                chargeTotal.setOrderAmount(MathUtils.round(MathUtils.minus(chargeTotal.getOrderAmount(), chargeTotal.getVipCutdownAmount())));
            }
             **/
        }

    }

    private void vipGoodsCalculate(ChargeContext chargeContext) {
        UserInfo userInfo = chargeContext.getUserInfo();
        List<ChargeGoods> mainGoodsList = chargeContext.getMainGoods();
        ChargeTotal total = chargeContext.getChargeTotal();

        int userLevel = userInfo.getUserLevel();
        for (ChargeGoods goods : mainGoodsList) {
            //特价不享受VIP
            if (goods.isSpecial()) {
                continue;
            }

            logger.debug("before vip charge,skn {}, real price {}, ", goods.getShoppingGoods().getProduct_skn() ,goods.getShoppingGoods().getReal_price());

            double discountPrice = 0;
            double tempDiscount = 1;
            switch (Integer.parseInt(goods.getShoppingGoods().getVip_discount_type())) {
                case 1:
                    //正常折扣
                    discountPrice = YHMath.mul(goods.getShoppingGoods().getReal_price(), VIPEnum.valueOf(userLevel).commonDiscount);
                    tempDiscount = VIPEnum.valueOf(userLevel).commonDiscount;
                    break;
                case 2:
                    //统一9.5折
                    discountPrice = YHMath.mul(goods.getShoppingGoods().getReal_price() , 0.95);
                    tempDiscount = 0.95;
                    break;
                case 3:
                    //无折扣
                    discountPrice = MathUtils.round(goods.getShoppingGoods().getReal_price().doubleValue());
                    break;
                case 4:
                    //VIP价
                    discountPrice = MathUtils.round(goods.getShoppingGoods().getVip_price());
                    // tempDiscount = sprintf("%.4f", $g['vip_price'] / $g['real_price']);
                    tempDiscount = MathUtils.numberFormat(goods.getShoppingGoods().getVip_price() / goods.getShoppingGoods().getReal_price(), 4);
                    break;
                case 5: //VIP 自定义价格
                    switch (userLevel) {
                        case 1:
                            discountPrice = MathUtils.round(Double.parseDouble(goods.getShoppingGoods().getVip1_price()));
                            tempDiscount = discountPrice / goods.getShoppingGoods().getReal_price();
                            break;
                        case 2:
                            discountPrice = MathUtils.round(Double.parseDouble(goods.getShoppingGoods().getVip2_price()));
                            tempDiscount = discountPrice / goods.getShoppingGoods().getReal_price();
                            break;
                        case 3:
                            discountPrice = MathUtils.round(Double.parseDouble(goods.getShoppingGoods().getVip3_price()));
                            tempDiscount = discountPrice/ goods.getShoppingGoods().getReal_price();
                            break;
                        default:
                            discountPrice = MathUtils.round(goods.getShoppingGoods().getReal_price());
                            break;
                    }
                    break;
                default:
                    discountPrice = MathUtils.round(goods.getShoppingGoods().getReal_price());
                    break;

            }


            logger.debug("after vip charge, skn {}, discount price {}, ", goods.getShoppingGoods().getProduct_skn() ,discountPrice);
            /**
             *  基于marketprice（市场价）计算的vip折扣价格小于 实际销售价。说明发生了折扣
             *  saleprice 实际销售价格
             *  marketprice 市场价，一般会被杠掉
             *  realprice lastprice 初始化为saleprice 销售价
             *
             *
             *   TODO lastVipPrice 一定要有值
             *  discountPrice:9.875
             *  lastVipPrice: 9.87
             */
            if (discountPrice < goods.getShoppingGoods().getReal_price()) {
                double lastVipPrice = 0;


                double oldRealPriceForLog = goods.getShoppingGoods().getReal_price() ;

                if (userLevel > 0 || discountPrice < goods.getShoppingGoods().getMarket_price()) {
                    lastVipPrice = MathUtils.round(discountPrice);
                }

                discountPrice = MathUtils.round(discountPrice);


                //减了多少钱
                goods.getShoppingGoods().setVip_discount_money(MathUtils.round(MathUtils.minus(goods.getShoppingGoods().getReal_price(), discountPrice)));

                goods.getShoppingGoods().setReal_price(discountPrice);
                goods.getShoppingGoods().setVip_discount(tempDiscount);
                goods.getShoppingGoods().setReal_vip_price(discountPrice);
                goods.getShoppingGoods().setLast_vip_price(lastVipPrice);


                logger.info("[{}] vip charge and reset shopping goods, skn {} sku {}, buy num {}, before realprice {},after realprice {}, Vip_discount {}, Last_vip_price {}, Vip_discount_money {} ",
                        chargeContext.getChargeParam().getUid(),
                        goods.getShoppingGoods().getProduct_skn(),
                        goods.getShoppingGoods().getProduct_sku(),
                        goods.getShoppingGoods().getBuy_number(),
                        oldRealPriceForLog,
                        goods.getShoppingGoods().getReal_price(),
                        goods.getShoppingGoods().getVip_discount(),
                        goods.getShoppingGoods().getLast_vip_price(),
                        goods.getShoppingGoods().getVip_discount_money()
                );


                //提交vip减了多少钱，传给后台
                if ("Y".equals(goods.getShoppingGoods().getSelected())) {
                    // double tempAmount = MathUtils.round((goods.getShoppingGoods().getSales_price() - goods.getShoppingGoods().getReal_price()) * goods.getBuyNumber());

                     double _discount = YHMath.sub(goods.getShoppingGoods().getSales_price(),  goods.getShoppingGoods().getReal_price());

                     double discount_amount = YHMath.mul(_discount, goods.getBuyNumber());

                     total.setVipCutdownAmount( YHMath.add(total.getVipCutdownAmount(), discount_amount) );

                     logger.info("[{}] vip charge, skn {} sku {}, buy num {}, discount amount: _discount:{}, discount_amount:{}, total discount:{}",
                             chargeContext.getChargeParam().getUid(),
                             goods.getShoppingGoods().getProduct_skn(),
                             goods.getShoppingGoods().getProduct_sku(),
                             goods.getShoppingGoods().getBuy_number(),
                             _discount, discount_amount, total.getVipCutdownAmount());

                }
            }

        }
    }
}
