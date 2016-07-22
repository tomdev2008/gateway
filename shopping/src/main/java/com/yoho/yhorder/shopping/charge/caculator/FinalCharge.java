package com.yoho.yhorder.shopping.charge.caculator;

import com.yoho.core.common.utils.YHMath;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.model.PromotionBO;
import com.yoho.service.model.order.response.shopping.PromotionFormula;
import com.yoho.service.model.promotion.PromotionCodeBo;
import com.yoho.service.model.promotion.request.PromotionCodeReq;
import com.yoho.yhorder.dal.IOrdersMapper;
import com.yoho.yhorder.dal.model.Gate;
import com.yoho.yhorder.shopping.cache.GateCacheService;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.*;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MathUtils;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by JXWU on 2015/11/27.
 */

/**
 * 最终计算订单金额
 * <p/>
 * yoho币  免邮费
 */
@Component
public class FinalCharge {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    @Autowired
    protected ServiceCaller serviceCaller;

    @Autowired
    private GateCacheService gateCacheManager;

    @Autowired
    private IOrdersMapper ordersMapper;

    public void charge(ChargeContext chargeContext) {
        /**
         * 普通商品，计算运费
         */
        ChargeType chargeType = chargeContext.getChargeParam().getChargeType();

        if (chargeType.freeShippingLimit()) {
            logChargeTotal("freeShippingLimit", chargeContext);
            freeShippingLimit(chargeContext);
        }

        if (chargeType.caculateShippingFee()) {
            logChargeTotal("caculateShippingFee", chargeContext);
            caculateShippingFee(chargeContext);
        }

        logChargeTotal("usingCoupons", chargeContext);
        usingCoupons(chargeContext);

        if (chargeType.usingPromotionCode()) {
            logChargeTotal("usingPromotionCode", chargeContext);
            usingPromotionCode(chargeContext);
        }


        if (chargeType.usingRedEnvelopes()) {
            //预售购物车不能使用红包
            logChargeTotal("freeRedEnvelopes", chargeContext);
            usingRedEnvelopes(chargeContext);
        }

        logChargeTotal("newCustomerFreeShippingLimit", chargeContext);
        newCustomerFreeShippingLimit(chargeContext);

        if (chargeType.usingYohoCoin()) {
            logChargeTotal("freeShippingCoin", chargeContext);
            this.usingYohoCoin(chargeContext);
        }



    }



    private void logChargeTotal(String freeAction, ChargeContext chargeContext) {
        logger.debug(chargeContext.getChargeTotal().toString());
    }

    /**
     * 先把运费计算出来，设置到chagetotal里面
     * <p/>
     * 499免运费
     *
     * @param chargeContext
     */
    private void freeShippingLimit(ChargeContext chargeContext) {
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        double lastOrderAmount = chargeTotal.getLastOrderAmount();
        if (lastOrderAmount < 0) {
            return;
        }

        chargeTotal.setShippingCost(Constants.SHIPPING_COST);
        chargeTotal.setFastShoppingCost(Constants.FAST_SHOPPING_COST);


        //TODO 499 免邮的限制，需要从数据库中查找
        //配置满免运费的金额
        //$cartConfig = QINOperations_Models_Gate_Client::getInfoByMetakey('freeShippingLimit');
        //$freeShippingLimit = empty($cartConfig) || $cartConfig['status'] == 0 ? QINConfig_Orders::$freeShippingLimit : $cartConfig['price'];
        int freeShippingLimit = Constants.FREE_SHIPPING_LIMIT;
        //免邮的限制，需要从数据库中查找
        Gate gate = gateCacheManager.getGateFor(GateCacheService.FREE_SHIPPING_LIMIT);
        //免邮限制开关
        if (gate != null && gate.getStatus() && gate.getPrice() != null) {
            freeShippingLimit = gate.getPrice();
        }

        //$cartConfig查询的gate没有price属性


        //促销中免运费
        if (chargeTotal.getShippingCostPromotion() != null && chargeTotal.getShippingCostPromotion().containsKey("promotion_shipping_cost")) {

            chargeTotal.setShippingCost((double) chargeTotal.getShippingCostPromotion().get("promotion_shipping_cost"));
        } else {
            // ChargeParam cartOrder = chargeContext.getCartOrder();
            PromotionBO promotionInfo = null;
            // 购物满499免运费
            if (lastOrderAmount >= freeShippingLimit) {
                chargeTotal.setShippingCost(0);
                chargeTotal.setFastShoppingCost(YHMath.add(chargeTotal.getShippingCost(), Constants.FAST_SHOPPING_COST));
                promotionInfo = new PromotionBO(0, "满" + freeShippingLimit + "免运费", 10, "FreeShippingCost");
            } else if (chargeContext.getUserInfo().getUserLevel() > 1) {
                // 金卡免运费
                chargeTotal.setShippingCost(0);
                chargeTotal.setFastShoppingCost(YHMath.add(chargeTotal.getShippingCost(), Constants.FAST_SHOPPING_COST));
                promotionInfo = new PromotionBO(0, "金卡免运费", 10, "VipFreeShippingCost");
            }


            if (promotionInfo != null) {
                chargeContext.setupPromotionInfo(promotionInfo);
            }
        }
    }

    /**
     * VIP
     * 1. 白金用户免邮费，加急送免费
     * 2. 投递方式：
     * - 如果是免邮券，邮费=0
     * - 如果是加急送，需要在原来的原来邮费上加上加急送费用
     * - 如果是3，免费自提
     *
     * @param chargeContext
     */
    private void caculateShippingFee(ChargeContext chargeContext) {
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        double lastOrderAmount = chargeTotal.getLastOrderAmount();
        if (lastOrderAmount < 0) {
            return;
        }
        // 白金用户可免费享受加急
        if (chargeContext.getUserInfo().getUserLevel() > 2) {
            chargeTotal.setShippingCost(0);
            chargeTotal.setFastShoppingCost(0);
        }

        ChargeParam cartOrder = chargeContext.getChargeParam();

        // 查询购物车场景下 不需要计算运费
        if (cartOrder.isNeedCalcShippingCost()) {
            // 免邮券
            if ("m".equals(chargeTotal.getCouponAlphabet()) && StringUtils.isNotEmpty(cartOrder.getCouponCode())) {
                chargeTotal.setShippingCost(0);
            }

            // 默认邮费10
            double shippingCost = chargeTotal.getShippingCost();
            if (cartOrder.getShippingManner() == 2) {
                shippingCost = YHMath.add(chargeTotal.getShippingCost(), chargeTotal.getFastShoppingCost());
            } else if (cartOrder.getShippingManner() == 3) {
                // YOHOOD现场自提免运费
                shippingCost = 0;
                chargeTotal.setShippingCost(0);
                chargeTotal.setFastShoppingCost(0);
            }

            /**
           if (Constants.PRESALE_CART_TYPE.equals(cartOrder.getCartType())) {
                // 预售免运费
               shippingCost = 0;
                chargeTotal.setShippingCost(0);
                chargeTotal.setFastShoppingCost(0);
            }**/

            //移到最后
//            PromotionFormula formula = new PromotionFormula("+运费", MathUtils.formatCurrencyStr(shippingCost));
//            chargeTotal.getPromotionFormulaList().add(formula);

            /** 最后添加运费 */
            double rounded_shipping_fee = MathUtils.round(shippingCost);
            chargeTotal.setLastShippingCost(rounded_shipping_fee);
            chargeTotal.setLastOrderAmount(YHMath.add(chargeTotal.getLastOrderAmount(), rounded_shipping_fee));

            logger.info("STEP Charge ship fee. shipping fee: {} last order amount:{}", rounded_shipping_fee, chargeTotal.getLastOrderAmount());


        }
    }

    /**
     * 优惠券
     *
     * @param chargeContext
     */
    private void usingCoupons(ChargeContext chargeContext) {
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        double lastOrderAmount = chargeTotal.getLastOrderAmount();
        if (lastOrderAmount < 1) {
            return;
        }

        // 优惠券

        double coupons = MathUtils.round(chargeTotal.getCouponAmount());

        if (!"m".equals(chargeTotal.getCouponAlphabet()) && coupons > 0) {
            if ((chargeTotal.getLastOrderAmount() - coupons) >= 0) {

                chargeTotal.setCouponAmount(coupons);
                chargeTotal.setLastOrderAmount(YHMath.sub(chargeTotal.getLastOrderAmount(), coupons));

                PromotionFormula formula = new PromotionFormula("-", "优惠券", MathUtils.formatCurrencyStr(chargeTotal.getCouponAmount()));
                chargeTotal.getPromotionFormulaList().add(formula);
            } else {
                //不可能走到这里啊?
                chargeTotal.setCouponAmount(chargeTotal.getLastOrderAmount());
                chargeTotal.setLastOrderAmount(0);
            }
        }

        logger.info("STEP charge coupons. [{}] using coupons, coupon code {} , total before {}, coupon amount {}, total after {}. ",
                chargeContext.getChargeParam().getUid(),
                chargeContext.getChargeParam().getCouponCode(),
                lastOrderAmount, chargeTotal.getCouponAmount(), chargeTotal.getLastOrderAmount());
    }

    /**
     * 优惠码
     *
     * @param chargeContext
     */
    private void usingPromotionCode(ChargeContext chargeContext) {
        ChargeParam chargeParam = chargeContext.getChargeParam();
        //预售购物车
        if (chargeParam.isPreSaleCart()) {
            return;
        }
        String promotionCodeStr = chargeParam.getPromotionCode();
        //登陆用户（是游客） 或者 没有使用优惠码
        if (chargeParam.getUid() < 1 || StringUtils.isEmpty(promotionCodeStr)) {
            return;
        }

        //优惠券和优惠码不能同时使用
        if (StringUtils.isNotEmpty(chargeParam.getCouponCode()) && StringUtils.isNotEmpty(promotionCodeStr)) {
            logger.info("coupon code and promotion code can't be used at the same time, coupon code {},promotion code {}",
                    chargeParam.getCouponCode(),
                    promotionCodeStr);
            throw new ServiceException(ServiceError.SHOPPING_COUPONS_PROMOTIONCODE_BOTH_NOT_NULL);
        }
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        double lastMainGoodsOrderAmount = chargeTotal.getLastMainGoodsOrderAmount();
        PromotionCodeChargeResult promotionCodeChargeResultForLog = caculatePromotionCodeDiscountAmount(chargeContext);
        logger.info("[{}] use promotionCode,promotion code {},mainGoodsLastOrderAmount {},shippingCost {} , lastOrderAmount {},promotion code charge result {}",
                chargeParam.getUid(),
                promotionCodeStr,
                lastMainGoodsOrderAmount,
                chargeTotal.getLastShippingCost(),
                chargeTotal.getLastOrderAmount(),
                promotionCodeChargeResultForLog);
    }

    private PromotionCodeChargeResult caculatePromotionCodeDiscountAmount(ChargeContext chargeContext) {
        ChargeParam chargeParam = chargeContext.getChargeParam();
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();

        double lastMainGoodsOrderAmount = chargeTotal.getLastMainGoodsOrderAmount();
        if (lastMainGoodsOrderAmount < 1) {
            return null;
        }
        int selectedMainGoodsCount = chargeTotal.getSelectedMainGoodsCount();
        //校验优惠码并返回优惠码信息
        PromotionCodeChargeResult promotionCodeChargeResult = validatePromotionCodeAndRetrunPromotionCodeChargeResult(chargeParam.getUid(), chargeParam.getPromotionCode());

        //根据优惠码的优惠条件，计算优惠码折扣金额
        promotionCodeChargeResult.caculateDiscountAmount(lastMainGoodsOrderAmount, selectedMainGoodsCount);

        if (!promotionCodeChargeResult.isValid()) {
            //不满足优惠条件
            logger.info("[{}] promotionCode {} not meeting condition", chargeParam.getUid(), promotionCodeChargeResult);
            ServiceException exception = new ServiceException(ServiceError.SHOPPING_PROMOTIONCODE_NOTMEETING_CONDITION);
            exception.setParams(new String[]{String.valueOf(promotionCodeChargeResult.getAmountAtLeast()), String.valueOf(promotionCodeChargeResult.getCountAtLeast())});
            throw exception;
        }
        double discountAmount = promotionCodeChargeResult.getDiscountAmount();
        chargeTotal.setPromotionCodeChargeResult(promotionCodeChargeResult);
        if (discountAmount > 0) {
            if (lastMainGoodsOrderAmount - discountAmount >= 0) {
                chargeTotal.setLastOrderAmount(YHMath.sub(chargeTotal.getLastOrderAmount(), discountAmount));
                chargeTotal.getPromotionFormulaList().add(new PromotionFormula("-", "优惠码", MathUtils.formatCurrencyStr(discountAmount)));
            } else {
                //TODO 金额全免 不需要添加??
                promotionCodeChargeResult.setDiscountAmount(lastMainGoodsOrderAmount);
                chargeTotal.setLastOrderAmount(YHMath.sub(chargeTotal.getLastOrderAmount(), lastMainGoodsOrderAmount));
            }

            //分摊
            chargeContext.caculatePerSkuDiscount(promotionCodeChargeResult.getDiscountAmount(), DiscountType.PROMOTIONCODE);
        }
        return promotionCodeChargeResult;
    }

    private PromotionCodeChargeResult validatePromotionCodeAndRetrunPromotionCodeChargeResult(int uid, String promotionCode) {
        //调用promotion服务
        PromotionCodeReq req = new PromotionCodeReq();
        req.setUid(uid);
        req.setPromotionCode(promotionCode);
        PromotionCodeBo promotionCodeBo = serviceCaller.call(ShoppingConfig.PROMOTION_GET_PROMOITONCODE_REST_URL, req, PromotionCodeBo.class);
        if (promotionCodeBo == null) {
            logger.warn("promotionCodeBo is null,uid {},promotionCode{}", uid, promotionCode);
            //
            throw new ServiceException(ServiceError.PROMOTION_CODE_NOT_EXIST);
        }

        PromotionCodeChargeResult promotionCodeChargeResult = new PromotionCodeChargeResult();
        promotionCodeChargeResult.setPromotionCodeBo(promotionCodeBo);
        promotionCodeChargeResult.setPromotionId(promotionCodeBo.getId());
        promotionCodeChargeResult.setDiscountType(promotionCodeBo.getDiscountType());
        //float -> double 有精度丢失
        promotionCodeChargeResult.setAmountAtLeast(YHMath.mul(1, promotionCodeBo.getAmountAtLeast()));
        promotionCodeChargeResult.setCountAtLeast(promotionCodeBo.getCountAtLeast());
        promotionCodeChargeResult.setDiscount(YHMath.mul(1, promotionCodeBo.getDiscount()));
        promotionCodeChargeResult.setDiscountAtMost(YHMath.mul(1, promotionCodeBo.getDiscountAtMost()));
        promotionCodeChargeResult.setPromotionCode(promotionCode);
        return promotionCodeChargeResult;
    }

    /**
     * YOHO COIN
     *
     * @param chargeContext
     */
    private void usingYohoCoin(ChargeContext chargeContext) {
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        if (chargeTotal.getLastOrderAmount() < 1) {
            return;
        }

        ChargeParam chargeParam = chargeContext.getChargeParam();
        //请求使用的yohocoin
        double reqYohoCoin = chargeParam.getUseYohoCoin();

        if (reqYohoCoin <= 0) {
            return;
        }

        //用户实际的yohocoin
        double userRealYohoCoin = MathUtils.round(chargeContext.getUserInfo().getOrderYohoCoin().ratedYohoCoin());
        logger.debug("user (uid={}) current real yoho coin is {},", chargeParam.getUid(), userRealYohoCoin);

        if (reqYohoCoin > userRealYohoCoin) {
            //用户实际的yoho coin小于请求使用,则设置实际能使用的yoho coin为值
            chargeTotal.setUseYohoCoin(userRealYohoCoin);
        } else {
            chargeTotal.setUseYohoCoin(reqYohoCoin);
        }

        // yoho币金额大于实际订单总额
        if (chargeTotal.getUseYohoCoin() > chargeTotal.getLastOrderAmount()) {
            chargeTotal.setUseYohoCoin(chargeTotal.getLastOrderAmount());
        }

        if (chargeTotal.getUseYohoCoin() > 0) {

           //yoho币抵扣运费的金额
            double yohoCoin4Shippingcost = 0;
            //运费
            double shippingCost = chargeTotal.getLastShippingCost();
            //不包括运费的金额
            double orderAmountExcludeShippingCost = YHMath.sub(chargeTotal.getLastOrderAmount(), shippingCost);
            if (chargeTotal.getUseYohoCoin() > orderAmountExcludeShippingCost) {
                yohoCoin4Shippingcost = YHMath.sub(chargeTotal.getUseYohoCoin(), orderAmountExcludeShippingCost);
            }
            chargeTotal.setYohoCoinShippingCost(yohoCoin4Shippingcost);

            double discountYohoCoin4GoodsAmount = YHMath.sub(chargeTotal.getUseYohoCoin(),yohoCoin4Shippingcost);
            //使用的yoho币个数
            double discountYohoCoin4GoodsNum =  YHMath.mul(discountYohoCoin4GoodsAmount, chargeContext.getUserInfo().getOrderYohoCoin().ratio());

            logger.info("totalYohoCoin:{} , YohoCoin4Shipping:{} , yohoCoin4Goods:{} , shippingCost: {} , amount:{} ",
                    chargeTotal.getUseYohoCoin(),yohoCoin4Shippingcost,discountYohoCoin4GoodsNum,shippingCost,orderAmountExcludeShippingCost);
            //分摊
            chargeContext.caculatePerSkuDiscount(chargeContext.getSelectedChargeGoodsByCartType(),discountYohoCoin4GoodsNum, DiscountType.YOHOCOIIN);

            chargeTotal.getPromotionFormulaList().add(new PromotionFormula("-", "YOHO币", MathUtils.formatCurrencyStr(chargeTotal.getUseYohoCoin())));
        }

        chargeTotal.setLastOrderAmount(YHMath.sub(chargeTotal.getLastOrderAmount(), chargeTotal.getUseYohoCoin()));

        //     logger.info("############################################################\n {}", JSON.toJSONString(chargeContext.getSelectedChargeGoodsByCartType(),true));
        logger.info("STEP YOHO Coins. last order amount: {} shippingCost: {}  Coin : {}",
                chargeTotal.getLastOrderAmount(),chargeTotal.getLastShippingCost(), chargeTotal.getUseYohoCoin());

    }

    /**
     * 使用红包: 红包不能抵运费
     *
     * @param chargeContext
     */
    private void usingRedEnvelopes(ChargeContext chargeContext) {
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        if (chargeTotal.getLastOrderAmount() < 1) {
            return;
        }
        ChargeParam cartOrder = chargeContext.getChargeParam();
        //用户请求使用的红包
        double planUseRedEnvelopes = cartOrder.getUseRedEnvelopes();
        if (planUseRedEnvelopes <= 0) {
            return;
        }

        //先设置
        chargeTotal.setUseRedEnvelopes(MathUtils.round(planUseRedEnvelopes));

        //红包不能抵扣运费，先减去运费
        chargeTotal.setLastOrderAmount(YHMath.sub(chargeTotal.getLastOrderAmount(), chargeTotal.getLastShippingCost()));


        //比较用户实际拥有的红包和请求的红包金额
        double userRealRedEnvelopes = chargeContext.getUserInfo().getRedEnvelopes();
        if (planUseRedEnvelopes > userRealRedEnvelopes) {
            logger.debug("user (uid={}) current real RedEnvelopes is {},", cartOrder.getUid(), userRealRedEnvelopes);
            chargeTotal.setUseRedEnvelopes(MathUtils.round(userRealRedEnvelopes));
        }

        if (chargeTotal.getUseRedEnvelopes() > chargeTotal.getLastOrderAmount()) {
            chargeTotal.setUseRedEnvelopes(chargeTotal.getLastOrderAmount());
        }

        //应付金额先减去使用红包的金额再加上运费,== 0，就没必要提供扣费公式
        if (chargeTotal.getUseRedEnvelopes() > 0) {
            chargeTotal.getPromotionFormulaList().add(new PromotionFormula("-", "红包", MathUtils.formatCurrencyStr(chargeTotal.getUseRedEnvelopes())));
        }
        chargeTotal.setLastOrderAmount(YHMath.sub(chargeTotal.getLastOrderAmount(), chargeTotal.getUseRedEnvelopes()));
        chargeTotal.setLastOrderAmount(YHMath.add(chargeTotal.getLastOrderAmount(), chargeTotal.getLastShippingCost()));

        //分摊
        chargeContext.caculatePerSkuDiscount(chargeContext.getSelectedChargeGoodsByCartType(),chargeTotal.getUseRedEnvelopes(), DiscountType.REDENVELOPE);

        //??
        if (chargeTotal.getLastOrderAmount() < 1 && chargeTotal.getUseYohoCoin() > 0) {
            chargeTotal.setUseYohoCoin(0);
        }

        logger.info("STEP Red Enp. last order amount: {}  red amount : {}", chargeTotal.getLastOrderAmount(), chargeTotal.getUseRedEnvelopes());
    }

    /**
     * 新客满减运费
     */
    private void newCustomerFreeShippingLimit(ChargeContext chargeContext) {
        //#不计算运费
        ChargeParam chargeParam = chargeContext.getChargeParam();
        if (!chargeParam.isNeedCalcShippingCost()) {
            return;
        }
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();

        double lastShippingCosthippingCost = chargeTotal.getLastShippingCost();
        //#运费小于1
        if (lastShippingCosthippingCost < 1) {
            return;
        }

        if ((chargeTotal.getLastOrderAmount() - lastShippingCosthippingCost) >= Constants.NEW_CUSTOMER_FREE_SHIPPING_LIMIT) {
            //TODO 补充订单状态
            /**
             *0 => '未付款' and  is_cancel = 'N'
             4 => '已发货',
             6 => '已完成'
             */
            // select count(1) from orders where uid = #{uid} and ((status = 0 and is_cancel = 'N') or status = 4 or status = 6)
            int orderNum = ordersMapper.selectOrdersNumByStatus(chargeParam.getUid());
            if (orderNum > 0) {
                logger.info("order status(0,4,6) num is {} of userid {}", orderNum, chargeParam.getUid());
                return;
            }
            chargeTotal.setShippingCost(0);

            //FastShoppingCost为快递加急费用.
            if (chargeParam.getShippingManner() == 2 && chargeTotal.getFastShoppingCost() > 0) {
                chargeTotal.setLastOrderAmount(YHMath.sub(chargeTotal.getLastOrderAmount(), chargeTotal.getLastShippingCost()));
                chargeTotal.setLastShippingCost(chargeTotal.getFastShoppingCost());
                chargeTotal.setLastOrderAmount(YHMath.add(chargeTotal.getLastOrderAmount(), chargeTotal.getLastShippingCost()));

                logger.info("STEP Charge new guy ship fee. shipping fee: {} last order amount:{}", chargeTotal.getLastOrderAmount());

            } else {
                chargeTotal.setLastOrderAmount(YHMath.sub(chargeTotal.getLastOrderAmount(), chargeTotal.getLastShippingCost()));
                chargeTotal.setLastShippingCost(0);

                logger.info("STEP Charge new guy ship fee. shipping fee: {} last order amount:{}", 0, chargeTotal.getLastOrderAmount());

            }
        }
    }
}