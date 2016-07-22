package com.yoho.yhorder.shopping.charge.caculator;

import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.promotion.CouponForm;
import com.yoho.service.model.promotion.CouponTypeBo;
import com.yoho.service.model.promotion.CouponsBo;
import com.yoho.service.model.promotion.ProductLimitBo;
import com.yoho.service.model.promotion.request.ProductLimitReq;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.charge.model.DiscountType;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MathUtils;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 优惠券校验。 将可用的优惠券加入到context。不做价格计算减免
 * <p/>
 * Created by JXWU on 2015/11/26.
 */
@Component
public class CouponsPreCharge {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");
    @Autowired
    protected ServiceCaller serviceCaller;

    public void charge(ChargeContext chargeContext) {

        ChargeParam chargeParam = chargeContext.getChargeParam();
//        //预售购物车
//        if (chargeParam.isPreSaleCart()) {
//            return;
//        }

        //登陆用户（是游客） 或者 没有使用优惠码
        if (chargeParam.getUid() < 1 || StringUtils.isEmpty(chargeParam.getCouponCode()))
        {
            return;
        }

        //调用远程服务获取、校验优惠券
        CouponsBo couponsBo = getCoupons(chargeParam.getUid(), chargeParam.getCouponCode());

        logger.info("[{}] Coupons pre charge,coupon is valid,coupon info:{}", chargeParam.getUid(), couponsBo);

        CouponTypeBo couponTypeBo = couponsBo.getCouponTypeBo();

        // 免邮券
        boolean isShippingFree = "m".equals(couponTypeBo.getAlphabet());
        if (isShippingFree && chargeContext.getChargeTotal().getShippingCost() < 6) {
            //throw new Exception('订单已免邮，免邮券下次再用吧');
            throw new ServiceException(ServiceError.SHOPPING_SHIPPINGCOST_IS_FREE_NEXTTEIME_USE_COUPONCODE);
        }

        //优惠券只能用于那些品类
        List<String> onlyUsedSortIdList = null;

        //优惠券只能用于某些品牌
        List<String> onlyUsedbrandIdList = null;

        //优惠券不能用于哪些skn
        List<String> nonUsedproductSknList = null;

        if (StringUtils.isNotEmpty(couponsBo.getSortLimit())) {
            onlyUsedSortIdList = Arrays.asList(couponsBo.getSortLimit().split(","));
        }

        if (StringUtils.isNotEmpty(couponsBo.getBrandLimit())) {
            onlyUsedbrandIdList = Arrays.asList(couponsBo.getBrandLimit().split(","));
        }

        if (StringUtils.isNotEmpty(couponsBo.getProductLimit())) {
            nonUsedproductSknList = Arrays.asList(couponsBo.getProductLimit().split(","));
        }


        /// ------------- 查询出所有能使用优惠券的SKU----------------
        List<ChargeGoods> couponsGoodsList = new ArrayList<ChargeGoods>();

        List<ChargeGoods> mainGoodsList = chargeContext.getMainGoods();
        if (CollectionUtils.isNotEmpty(mainGoodsList)) {
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
                //若优惠和商品没有绑定关系，则该优惠券不能使用
                if (Constants.IS_PRODUCT_LIMIT_STR.equals(couponsBo.getIsProductLimit())) {
                	//增加返回对象内容的判断，因为没有绑定关系接口也会返回一个空对象。故要进一步判断。
                	ProductLimitBo productLimitBo=getBindingRelationshipFor(couponsBo.getId(), chargeGoods.getShoppingGoods().getProduct_skn());
                    if ( productLimitBo== null || productLimitBo.getProductSkn()==null) {
                        continue;
                    }
                }
                //判断商品是否限制使用优惠券
                if (nonUsedproductSknList != null && Constants.IS_PRODUCT_LIMIT_STR.equals(couponsBo.getIsProductLimit()) && nonUsedproductSknList.contains(chargeGoods.getShoppingGoods().getProduct_skn())) {
                    continue;
                }
                couponsGoodsList.add(chargeGoods);
            }
        }

        /// ------------- 判断SKU的数量或价格，是否达到使用条件----------------

        double fitAmount = 0;
        int buyNumber = 0;
        if (couponsGoodsList.size() > 0) {
            for (ChargeGoods couponsGoods : couponsGoodsList) {
                fitAmount += MathUtils.round(couponsGoods.getShoppingGoods().getReal_price() * couponsGoods.getBuyNumber());
                buyNumber += couponsGoods.getBuyNumber();
            }
        } else {
            if (!isShippingFree) {
                //throw new Exception('没有可以使用优惠券的商品');
                throw new ServiceException(ServiceError.SHOPPING_NON_USE_COUPONS_GOODS);
            }
        }

        if ("1".equals(couponsBo.getUseLimitType()))//数量判断
        {
            if (buyNumber < Integer.parseInt(couponsBo.getUseLimit())) {
                throw new ServiceException(ServiceError.SHOPPING_GOODS_NUMBER_NOTMEET_COUPONS);
                //throw new Exception('未达到该优惠券要求的商品数量');
            }
        } else if ("2".equals(couponsBo.getUseLimitType()))//价格判断
        {
            if (fitAmount < Double.parseDouble(couponsBo.getUseLimit())) {
                throw new ServiceException(ServiceError.SHOPPING_GOODS_AMOUNT_NOTMEET_COUPONS);
                //throw new Exception('未达到该优惠券要求的商品金额');
            }
        }

        //一共优惠多少钱
        double couponAmount = couponsBo.getCouponAmount().doubleValue();
        /**
         * if ($couponInfo['alphabet'] != 'm') {
         $couponAmount = $couponInfo['coupon_amount'] < $fitAmount ? $couponInfo['coupon_amount'] : $fitAmount;
         }
         */
        if (!isShippingFree) {
            couponAmount = couponAmount < fitAmount ? couponAmount : fitAmount;
        }

        if(!isShippingFree && couponAmount > 0)
        {
            //分摊
            chargeContext.caculatePerSkuDiscount(couponsGoodsList,couponAmount, DiscountType.COUPONS);
        }

        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        chargeTotal.setCouponAlphabet(couponTypeBo.getAlphabet());
        chargeTotal.setCouponId(couponsBo.getId());
        chargeTotal.setCouponAmount(couponAmount);
        chargeTotal.setCouponTitle(couponsBo.getCouponName());
        chargeTotal.setUseCoupon(true);
        chargeTotal.setCouponCode(chargeParam.getCouponCode());
    }

    /**
     * 校验优惠券
     *
     * @return
     */
    private CouponsBo getCoupons(Integer uid, String conponCode) {
        //若优惠券状态正常，则返回CouponsBo对象，若异常则抛出异常
        logger.info("call service " + ShoppingConfig.PROMOTION_QUERY_CHECKCOUPON_REST_URL + " params({})", conponCode);
        CouponForm request = new CouponForm();
        request.setCouponCode(conponCode);
        request.setUid(String.valueOf(uid));
        return serviceCaller.call(ShoppingConfig.PROMOTION_QUERY_CHECKCOUPON_REST_URL, request, CouponsBo.class);
    }

    /**
     * 查询优惠券和产品的绑定关系
     *
     * @param couponId
     * @param productSkn
     * @return
     */
    private ProductLimitBo getBindingRelationshipFor(Integer couponId, String productSkn) {
        logger.info("call service " + ShoppingConfig.PROMOTION_QUERY_COUPONPRODLIMIT_REST_URL + " params({},{})", couponId, productSkn);
        ProductLimitReq request = new ProductLimitReq();
        request.setCouponId(couponId);
        request.setProductSkn(productSkn);
        return serviceCaller.call(ShoppingConfig.PROMOTION_QUERY_COUPONPRODLIMIT_REST_URL, request, ProductLimitBo.class);
    }
}