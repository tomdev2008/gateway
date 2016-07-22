package com.yoho.yhorder.shopping.charge.coupon.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yoho.service.model.promotion.CouponsBo;
import com.yoho.service.model.promotion.constant.coupons.CouponsValidityDayEnum;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.CouponWapper;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MathUtils;

/**
 * Created by wujiexiang on 16/4/7.
 * 优惠券匹配
 */
@Component
public class CouponService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void caculateCoupon(CouponWapper couponWapper, List<ChargeGoods> goodsList) {

        if (CollectionUtils.isEmpty(goodsList)) {
            return;
        }

        CouponsBo couponsBo = couponWapper.getCouponsBo();
        if (couponsBo == null) {
            return;
        }
        //不在使用期限的优惠券，不可用
        if (isInValidCoupon(couponsBo)) {
            logger.info("coupon is invalid,coupon is {},uid is {}", couponsBo, couponWapper.getUid());
        } else {
            processMatch(couponWapper, goodsList);
        }

    }

    private boolean isInValidCoupon(CouponsBo couponsBo) {
        return couponsBo.getValidityDay() != CouponsValidityDayEnum.WITHIN_VALIDITY.getCode();
    }

    /**
     * 校验用户可否使用该优惠券
     *
     * @param uid
     * @param customType
     * @return
     */
    private boolean validateCouponsByUser(CouponWapper couponWapper) {
    	CouponsBo couponBo=couponWapper.getCouponsBo();
    	int uid=couponWapper.getUid();
    	int userLevel=couponWapper.getUserLevel();
    	String customType=couponBo.getCustomType();
    	
        if (StringUtils.isBlank(customType)) {
            logger.info("uid:{} couponCustomeTypes is blank return true",uid);
            return true;
        }
        logger.info("begin validate：uid:{} ,userLevel:{} ,customType:{}", uid, customType);
        //此处存放是带逗号的多类型字符串
        String[] types = customType.split(",");
        boolean status = false;
        for (String type:types) {
            Integer typeOne = NumberUtils.toInt(type, 0);  
            switch (typeOne) {
                case 0:
                    return true;
                case 1:
                	if(couponWapper.isNewUser()) return true;
                    break;
                case 2:
                case 3:
                case 4:
                    if (typeOne - 1 == userLevel) {
                        return true;
                    }
                    break;
                case 5:
                    if (userLevel==0 &&!couponWapper.isNewUser()) {
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        logger.info("uid:{} ,userLevel:{} ,customType:{},the user can use the Coupons:{}", uid, customType,status);
        return status;
    }

    private void processMatch(CouponWapper couponWapper, List<ChargeGoods> goodsList) {

        //判断优惠券的用户使用等级是否满足,不滿足返回
		// if(!validateCouponsByUser(couponWapper)) return;
        //找到可以使用优惠券的商品
        List<ChargeGoods> matchedCouponsGoodsList = findGoodsListThatMatchCoupon(couponWapper.getCouponsBo(), goodsList);

        /**
         * 免邮券比较特殊,业务上只设置"使用条件"(金额和数量)
         * 使用非免邮券的购物车,必须有符合条件的商品.
         */

        if (CollectionUtils.isEmpty(matchedCouponsGoodsList)) {
            //非免邮券,必须有匹配的商品
            if (!isFreeShippingCoupon(couponWapper.getCouponsBo())) {
                return;
            }
        }
        CouponsBo couponsBo = couponWapper.getCouponsBo();
        boolean isMatch = isMatchBuyNumberAndAmount(couponsBo, matchedCouponsGoodsList);
        if (isMatch) {
            couponWapper.setGoodsList(matchedCouponsGoodsList);
            couponWapper.setUsable(true);
        } else {
            logger.info("coupon is not match,uid is {},couponCode is {},couponType is {},useLimitType is {}," +
                    "useLimit is {},brandLimit is {},sortLimit is {},productLimit is {},isProductLimit is {}",
                    couponWapper.getUid(),
                    couponsBo.getCouponCode(), couponsBo.getCouponType(), couponsBo.getUseLimitType(),
                    couponsBo.getUseLimit(), couponsBo.getBrandLimit(), couponsBo.getSortLimit(),
                    couponsBo.getProductLimit(),
                    couponsBo.getIsProductLimit());
        }
    }

    private boolean isMatchBuyNumberAndAmount(CouponsBo couponsBo, List<ChargeGoods> matchedCouponsGoodsList) {
        /// ------------- 判断SKU的数量或价格，是否达到使用条件----------------
        Pair<Integer, Double> buyNumberAndAmountPair = calcMatchedCouponGoodsBuyNumberAndAmount(matchedCouponsGoodsList);

        switch (couponsBo.getUseLimitType()) {
            case "1":
                return buyNumberAndAmountPair.getKey() >= Integer.parseInt(couponsBo.getUseLimit());
            case "2":
                return buyNumberAndAmountPair.getValue() >= Double.parseDouble(couponsBo.getUseLimit());
            default:
                //没有限制
                return true;
        }
    }

    /**
     * 对匹配优惠券商品进行计算,key=购物数量,value=商品金额
     *
     * @param matchedCouponsGoodsList
     * @return
     */
    private Pair<Integer, Double> calcMatchedCouponGoodsBuyNumberAndAmount(List<ChargeGoods> matchedCouponsGoodsList) {
        double fitAmount = 0;
        int buyNumber = 0;
        if (CollectionUtils.isNotEmpty(matchedCouponsGoodsList)) {
            for (ChargeGoods couponsGoods : matchedCouponsGoodsList) {
                fitAmount += MathUtils.round(couponsGoods.getShoppingGoods().getReal_price() * couponsGoods.getBuyNumber());
                buyNumber += couponsGoods.getBuyNumber();
            }
        }
        return Pair.of(buyNumber, fitAmount);
    }


    private List<ChargeGoods> findGoodsListThatMatchCoupon(CouponsBo couponsBo, List<ChargeGoods> mainGoodsList) {
        /// ------------- 查询出所有能使用优惠券的SKU----------------
        List<ChargeGoods> couponsGoodsList = new ArrayList<ChargeGoods>();

        if (CollectionUtils.isEmpty(mainGoodsList)) {
            return couponsGoodsList;
        }

        //优惠券只能用于那些品类
        List<String> onlyUsedSortIdList = null;

        //优惠券只能用于某些品牌
        List<String> onlyUsedbrandIdList = null;

        //优惠券不能用于哪些skn
        List<String> onlyUsedproductSknList = null;

        if (StringUtils.isNotEmpty(couponsBo.getSortLimit())) {
            onlyUsedSortIdList = Arrays.asList(couponsBo.getSortLimit().split(","));
        }

        if (StringUtils.isNotEmpty(couponsBo.getBrandLimit())) {
            onlyUsedbrandIdList = Arrays.asList(couponsBo.getBrandLimit().split(","));
        }

        if (StringUtils.isNotEmpty(couponsBo.getProductLimit())) {
            onlyUsedproductSknList = Arrays.asList(couponsBo.getProductLimit().split(","));
        }

        for (ChargeGoods chargeGoods : mainGoodsList) {
            if ("N".equals(chargeGoods.getShoppingGoods().getSelected())) {
                continue;
            }

            //禁止使用优惠券
            if ("1".equals(chargeGoods.getShoppingGoods().getLimit_coupon())) {
                continue;
            }

            //优惠券只能用于那些品牌
            if (onlyUsedbrandIdList != null && !onlyUsedbrandIdList.contains(chargeGoods.getShoppingGoods().getBrand_id())) {
                continue;
            }
            //优惠券只能用于那些品类
            if (onlyUsedSortIdList != null
                    && !onlyUsedSortIdList.contains(chargeGoods.getShoppingGoods().getMax_sort_id())
                    && !onlyUsedSortIdList.contains(chargeGoods.getShoppingGoods().getMiddle_sort_id())
                    && !onlyUsedSortIdList.contains(chargeGoods.getShoppingGoods().getSmall_sort_id())) {
                continue;
            }
            //优惠券只能用于那些skn
            if (onlyUsedproductSknList != null
                    && Constants.IS_PRODUCT_LIMIT_STR.equals(couponsBo.getIsProductLimit())
                    && !onlyUsedproductSknList.contains(chargeGoods.getShoppingGoods().getProduct_skn())) {
                continue;
            }
            couponsGoodsList.add(chargeGoods);
        }

        return couponsGoodsList;
    }


    /**
     * 免邮券
     *
     * @param couponsBo
     * @return
     */
    public static boolean isFreeShippingCoupon(CouponsBo couponsBo) {
        if (couponsBo != null) {
            //CouponTypeBo couponTypeBo = couponsBo.getCouponTypeBo();
            //return couponTypeBo != null && "m".equals(couponTypeBo.getAlphabet());
        	/**5代表免邮券*/
        	return "5".equals(couponsBo.getCouponType());
        }

        return false;
    }
}
