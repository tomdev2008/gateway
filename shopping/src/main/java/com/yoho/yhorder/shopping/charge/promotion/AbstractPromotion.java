package com.yoho.yhorder.shopping.charge.promotion;

import com.google.common.collect.Lists;
import com.yoho.service.model.order.model.promotion.PromotionCondition;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.service.model.order.model.promotion.SecondCondition;
import com.yoho.service.model.order.model.promotion.ThirdCondition;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 所有优惠计算的父类。
 * <p>
 * 1. 判断 chargecontext是否满足优惠条件
 * 2. 找出所有符合条件的商品，并且设置promotion信息到商品中
 *
 * @author chzhang@yoho.cn
 * @Time 2015/12/15
 */
public abstract class AbstractPromotion {


    private final static Logger logger = LoggerFactory.getLogger(AbstractPromotion.class);


    private final static List<String> notJoinPromotionGoodsType = Lists.newArrayList("price_gift", "gift");


    /**
     * 计算促销
     *
     * @param promotionInfo 促销信息
     * @param chargeContext 购物车对象
     * @return 是否满足计算条件
     */
    public boolean compute(PromotionInfo promotionInfo, ChargeContext chargeContext) {

        //param check
        if (StringUtils.isEmpty(promotionInfo.getActionParam())) {
            logger.warn("can not find action param for {}", promotionInfo);
            return false;
        }

        if (this.calcRule(promotionInfo, chargeContext)) {
            this.doCompute(chargeContext, promotionInfo);
            return true;
        } else {
            return false;
        }

    }


    protected abstract void doCompute(ChargeContext chargeContext, PromotionInfo promotionInfoBo);


    /**
     * 1. 判断 chargecontext是否满足优惠条件
     * 2. 找出所有符合条件的商品，并且设置promotion信息到商品中
     * <p>
     * 作用： 1.会修改mainGoods的属性： fit_promotions,  limit_param, reject_params, limit_coupon
     * 2. 会修改chargeTotal中大部分属性
     *
     * @param promotionInfo 数据库中一个优惠记录
     * @param chargeContext 购物车
     * @return 是否满足
     */
    private boolean calcRule(PromotionInfo promotionInfo, ChargeContext chargeContext) {

        logger.debug("start to caculate whether promotion fits. promotionInfo: {},chargeContext: {}", promotionInfo, chargeContext);

        if (promotionInfo.getCondition() == null || promotionInfo.getCondition().getConditions() == null || promotionInfo.getCondition().getConditions().size() == 0) {
            logger.info("no conditions found at: {}. do not need to caculate promotion.", promotionInfo);
            return false;
        }


        /**
         * 优惠条件的匹配结果，是否能够使用此优惠规则
         */
        boolean isMatch = false;

        Set<String> fitGoodsSKUs = new HashSet<>();

        for (SecondCondition secondCondition : promotionInfo.getCondition().getConditions()) {

            // is second condition match ?
            boolean isSecondMatch = false;

            switch (secondCondition.getType()) {
                case "condition_product":       // 产品条件
                    isSecondMatch = calcProductCondition(chargeContext, secondCondition, promotionInfo.getId(), fitGoodsSKUs);
                    break;
                case "condition_payment":   // 支付条件
                    isSecondMatch = calcPayment(promotionInfo.getCondition(), chargeContext);
                    break;
            }


            // second match result == first condition?
            boolean isMatchFirst = judgeOperator(String.valueOf(isSecondMatch), promotionInfo.getCondition().getValue(), promotionInfo.getCondition().getOperator());
            if (StringUtils.equals("all", promotionInfo.getCondition().getAggregator())) {
                if (isMatchFirst) {
                    isMatch = true;  //level—1 全部，当前levle2条件已满足，继续
                } else {
                    isMatch = false;  //level—1 全部，当前levle2条件已不满足，不满足当前优惠规则的使用条件
                    break;
                }
            } else {
                if (isMatchFirst) {  //level—1 any，当前levle2条件满足，即可使用此优惠规则
                    isMatch = true;
                    break;
                } else {   // level—1的任意，当前levle2条件不满足，仍然需要继续计算后续的level2是否满足
                    isMatch = false;
                }
            }
        }//end second iterator


        if (isMatch) {
            this.markMainGoods(promotionInfo, fitGoodsSKUs, chargeContext);
        }

        logger.debug("End to caculate whether promotion fits. promotionInfo: {},chargeContext: {}, result is:{} ", promotionInfo, chargeContext, isMatch);
        return isMatch;
    }


    /**
     * after caculate rule. 如果 promotion满足了，则需要修改 mainGoods的一些属性
     *
     * @param chargeContext
     */
    private void markMainGoods(final PromotionInfo promotionInfo, final Set<String> fitGoodsSKUs, final ChargeContext chargeContext) {

        for (ChargeGoods mainGoods : chargeContext.getMainGoods()) {

            final String sku = mainGoods.getShoppingGoods().getProduct_sku();
            if (!fitGoodsSKUs.contains(sku)) {   //not match main goods
                continue;
            }

            // 1. add promotion id to goods's fit_promotions
            List<String> fitPromotions = mainGoods.getFit_promotions();
            if (fitPromotions == null) {
                fitPromotions = new LinkedList<>();
                mainGoods.setFit_promotions(fitPromotions);
                mainGoods.getShoppingGoods().setFit_promotions(fitPromotions);
            }
            fitPromotions.add(promotionInfo.getId());

            //2. add limit_param  to goods's limit_param promotions
            String limit_param = promotionInfo.getLimitParam();
            if (StringUtils.isNotEmpty(limit_param) && StringUtils.isEmpty(limit_param)) {
                mainGoods.getShoppingGoods().setIs_limited(limit_param);
            }

            //3. add all reject params
            List<String> reject_params = promotionInfo.getReject_param();
            if (CollectionUtils.isNotEmpty(reject_params)) {
                List<String> rejectPromotions = mainGoods.getRejectPromotions();
                if (rejectPromotions == null) {
                    rejectPromotions = new LinkedList<>();
                    mainGoods.setRejectPromotions(rejectPromotions);
                }
                rejectPromotions.addAll(reject_params);
            }


            //4. add limit_param coupon  limit_param-coupon = limit_param-param = 1
            if (StringUtils.isNotEmpty(limit_param)) {
                mainGoods.getShoppingGoods().setLimit_coupon(limit_param);
            }

        }


    }

    /**
     * 计算二级条件是否满足
     * booolean : 是否满足
     * Set<String> 满足促销的sku列表
     */
    private boolean calcProductCondition(ChargeContext chargeContext, SecondCondition secondCondition, String promotionId, Set<String> fitGoodsSKUS) {
        if (CollectionUtils.isEmpty(chargeContext.getMainGoods())) {
            return false;
        }

        //本次2级商品条件满足的所有商品SKU
        final Set<String> _tmpFitGoods = new HashSet<>();

        for (ChargeGoods mainGoods : chargeContext.getMainGoods()) {

            String product_sku = mainGoods.getShoppingGoods().getProduct_sku();

            //直接过滤的场景
            if (!StringUtils.equals("Y", mainGoods.getShoppingGoods().getSelected())
                    || StringUtils.isEmpty(product_sku)  //sku 为空
                    || StringUtils.equals(mainGoods.getShoppingGoods().getIs_special(), "Y")//如果是特价
                    || notJoinPromotionGoodsType.contains(mainGoods.getShoppingGoods().getGoods_type())  //不能计算
                    || (CollectionUtils.isNotEmpty(mainGoods.getRejectPromotions()) && mainGoods.getRejectPromotions().contains(promotionId))) { //判断当前活动是否在订单的互斥活动名单中
                logger.debug("chargeGoods:{} not fit for promotion:{} . cause: product no selected, is special, is gift, is in reject.",promotionId, mainGoods);
                continue;
            }

            //条件为空，说明是全场商品都满足, 直接返回
            if (secondCondition.getConditions().size() == 0) {
                _tmpFitGoods.add(product_sku);
                logger.debug("all goods fit,secondCondition:{},chargeGoods:{}", secondCondition, mainGoods);
                continue;
            }


            /**
             *
             * 判断level-2 和 level-3 的满足情况，输出：适合此优惠的商品
             *
             * level-1 如果下面 aaa (全部、任意)  条件为  bbb （是、否）  :
             *   level-2 如果购物车中符合下面 ccc (任意、全部) 条件的商品的 ddd （个数、金额） eee （大于、大于等于、小于、小于等于） fff （数字）
             *     level-3  品牌匹配
             *     level-3  促销标记匹配
             *     level-3  产品分类匹配
             *     level-3  特定商品匹配
             *
             * 此处：只判断 level-3 的满足情况、 level-2的 xxx
             * level-1 的 aaa 、bbb 只用于快速确定后续的level-3有没有必要继续判断
             *
             */
            for (ThirdCondition thirdCondition : secondCondition.getConditions()) {
                boolean conditionsState = false;
                switch (thirdCondition.getType()) {
                    //分类条件
                    case "condition_product_sort": {

                        boolean maxSort = false;
                        if (mainGoods.getShoppingGoods().getMax_sort_id() != null) {
                            maxSort = judgeOperator(mainGoods.getShoppingGoods().getMax_sort_id(), thirdCondition.getValue(), thirdCondition.getOperator());
                        }

                        boolean middleSort = false;
                        if (mainGoods.getShoppingGoods().getMiddle_sort_id() != null) {
                            middleSort = judgeOperator(mainGoods.getShoppingGoods().getMiddle_sort_id(), thirdCondition.getValue(), thirdCondition.getOperator());
                        }

                        boolean smallSort = false;
                        if (mainGoods.getShoppingGoods().getSmall_sort_id() != null) {
                            smallSort = judgeOperator(mainGoods.getShoppingGoods().getSmall_sort_id(), thirdCondition.getValue(), thirdCondition.getOperator());
                        }

                        conditionsState = maxSort || middleSort || smallSort;
                        break;
                    }

                    //品牌条件
                    case "condition_product_brand":
                        conditionsState = judgeOperator(mainGoods.getShoppingGoods().getBrand_id(), thirdCondition.getValue(), thirdCondition.getOperator());
                        break;

                    //促销条件
                    case "condition_promotion_flag":
                        conditionsState = judgeOperator(mainGoods.getShoppingGoods().getPromotion_flag(), thirdCondition.getValue(), thirdCondition.getOperator());
                        break;

                    //特定商品条件
                    case "condition_special_product":
                        conditionsState = judgeOperator(mainGoods.getShoppingGoods().getProduct_skn(), thirdCondition.getValue(), thirdCondition.getOperator());
                        break;
                }

                //level3 condition
                if (conditionsState) {
                    if (StringUtils.equals("all", secondCondition.getAggregator())) {
                        _tmpFitGoods.add(product_sku);  //如果当前 level-3 匹配， 并且 level-2 为 all， 临时保存sku，如果后续规则不匹配，则移除
                    } else {
                        _tmpFitGoods.add(product_sku);  //*如果当前 level-3 匹配， 并且 level-2 为 any， 则此sku已满足优惠规则
                        break;
                    }
                } else {
                    if (StringUtils.equals("all", secondCondition.getAggregator())) {  // second is all but do not match, remove sku and break
                        _tmpFitGoods.remove(product_sku);
                        break;
                    }
                }
            }
        }

        //判断条件是否满足
        boolean isMatch = false;
        switch (secondCondition.getOperatobj()) {
            case "num": {   // 按数量
                int total = 0;
                for (ChargeGoods chargeGoods : chargeContext.getMainGoods()) {
                    if (_tmpFitGoods.contains(chargeGoods.getShoppingGoods().getProduct_sku())) {
                        total = total + Integer.parseInt(chargeGoods.getShoppingGoods().getBuy_number());
                    }
                }

                String secValue = StringUtils.isEmpty(secondCondition.getValue()) ? "0" : secondCondition.getValue();
                isMatch = judgeIntOperator(total, Integer.parseInt(secValue), secondCondition.getOperator());
                break;
            }

            case "amount": { //按金额
                BigDecimal orderAmount = new BigDecimal(0);
                for (ChargeGoods chargeGoods : chargeContext.getMainGoods()) {
                    if (_tmpFitGoods.contains(chargeGoods.getShoppingGoods().getProduct_sku())) {
                        orderAmount = orderAmount.add(chargeGoods.getRealPriceTotalBigDecimal());
                    }
                }

                String secValue = StringUtils.isEmpty(secondCondition.getValue()) ? "0.0" : secondCondition.getValue();
                isMatch = judgeDoubleOperator(orderAmount.doubleValue(), Double.parseDouble(secValue), secondCondition.getOperator());
                break;
            }

        }

        fitGoodsSKUS.addAll(_tmpFitGoods);
        return isMatch;
    }


    /**
     * 验证支付条件是否满足
     */
    private boolean calcPayment(PromotionCondition condition, ChargeContext chargeContext) {
        return judgeOperator(chargeContext.getChargeParam().getPaymentType() + "", condition.getValue(), condition.getOperator());
    }


    //------------------------------ operator caculate ----------------------------------------------

    /**
     * 判断运算符
     *
     * @param srcValue   要判断的值
     * @param judgeValue 要判断的条件的集合
     * @param operator   操作符合
     * @return
     */
    private boolean judgeOperator(String srcValue, String judgeValue, String operator) {
        switch (operator) {
            case "=="://等于
                return srcValue.equals(judgeValue);
            case "!="://不等于
                return !srcValue.equals(judgeValue);
            case ">"://大于
                return srcValue.compareTo(judgeValue) > 0;
            case ">="://大于等于
                return srcValue.compareTo(judgeValue) >= 0;
            case "<=":
                //小于等于
                return srcValue.compareTo(judgeValue) <= 0;
            case "()"://属于
                return belongs(srcValue, judgeValue);
            case "!()"://不属于
                return !belongs(srcValue, judgeValue);
        }

        return false;
    }

    /**
     * 判断运算符
     *
     * @param srcValue   要判断的值
     * @param judgeValue 要判断的条件的集合
     * @param operator   操作符合
     * @return
     */
    private boolean judgeDoubleOperator(double srcValue, double judgeValue, String operator) {
        switch (operator) {
            case "=="://等于
                return srcValue == judgeValue;
            case "!="://不等于
                return srcValue != judgeValue;
            case ">"://大于
                return srcValue > judgeValue;
            case ">="://大于等于
                return srcValue >= judgeValue;
            case "<=":
                //小于等于
                return srcValue <= judgeValue;
        }

        return false;
    }

    /**
     * 判断运算符
     *
     * @param srcValue   要判断的值
     * @param judgeValue 要判断的条件的集合
     * @param operator   操作符合
     * @return
     */
    private boolean judgeIntOperator(int srcValue, int judgeValue, String operator) {
        switch (operator) {
            case "=="://等于
                return srcValue == judgeValue;
            case "!="://不等于
                return srcValue != judgeValue;
            case ">"://大于
                return srcValue > judgeValue;
            case ">="://大于等于
                return srcValue >= judgeValue;
            case "<=":
                //小于等于
                return srcValue <= judgeValue;
        }

        return false;
    }

    private boolean belongs(String srcValue, String judgeValue) {
        String[] belongArray = null;
        if (judgeValue != null) {
            belongArray = judgeValue.split(",");
        }
        return ArrayUtils.contains(belongArray, srcValue);
    }


}
