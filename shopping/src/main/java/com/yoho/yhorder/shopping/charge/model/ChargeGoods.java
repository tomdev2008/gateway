package com.yoho.yhorder.shopping.charge.model;

import com.yoho.core.common.utils.YHMath;
import com.yoho.service.model.order.response.shopping.ShoppingGoods;
import com.yoho.yhorder.dal.model.ShoppingCartItems;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.ReflectTool;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by JXWU on 2015/11/19.
 * <p>
 * 计费 操作的时候需要用的一些信息
 * <p>
 * <p>
 * 对应一个SKU
 */
@Data
public class ChargeGoods {
    private static final Logger logger = LoggerFactory.getLogger(ChargeGoods.class);

    /**
     * 如果是返回客户端的信息就需要设置在这个字段中
     */
    private ShoppingGoods shoppingGoods;

    /**
     * 当前商品互斥的活动列表
     */
    private List<String> rejectPromotions;

    /**
     * 商品适合的促销活动
     */
    private List<String> fit_promotions;


    private String promotionType;

    // End promotion gift 扩展字段

    /**
     * 每件sku分摊的金额，包括优惠券、优惠码、红包、yoho币
     */
    private Discount discountPerSku = new Discount();

    public ChargeGoods() {

    }

    public static ChargeGoods clone(ChargeGoods src) {
        try {
            ShoppingGoods newShoppingGoods = (ShoppingGoods) BeanUtils.cloneBean(src.getShoppingGoods());
            ChargeGoods newChargeGoods = (ChargeGoods) BeanUtils.cloneBean(src);
            Discount discount = (Discount)BeanUtils.cloneBean(src.getDiscountPerSku());
            newChargeGoods.setShoppingGoods(newShoppingGoods);
            newChargeGoods.setDiscountPerSku(discount);
            return newChargeGoods;
        } catch (Exception e) {
            logger.error("clone {} exception.", ChargeGoods.class.getName(), e);
            throw new RuntimeException("clone ChargeGoods exception:", e);
        }
    }

    public int getBuyNumber() {
        return this.shoppingGoods.getBuy_number() == null ? 0 : Integer.parseInt(this.shoppingGoods.getBuy_number());
    }


    /**
     * 该商品是否满足一个促销
     *
     * @param promotionId
     * @return
     */
    public boolean isPromotionFit(String promotionId) {
        return this.fit_promotions != null && this.fit_promotions.contains(promotionId);
    }

    /**
     * 获取BigDecimal模式
     *
     * @return
     */
    public BigDecimal getRealPriceBigDecimal() {
        return   BigDecimal.valueOf(this.getShoppingGoods().getReal_price());
    }


    // --------------------------- is method -----------------------//
    public boolean isShoppingGoodsAdvanced() {
        return "Y".equals(this.shoppingGoods.getIs_advance());
    }

    public boolean isSelected() {
        return "Y".equals(this.getShoppingGoods().getSelected());
    }

    public boolean isShoppingGoodsOutlets() {
        return "Y".equals(this.shoppingGoods.getIs_outlets());
    }

    public boolean isShoppingGoodsGit() {
        return "Y".equals(this.shoppingGoods.getIs_jit());
    }

    public boolean isSpecial() {
        return Constants.IS_SPECIAL_STR.equals(shoppingGoods.getIs_special());
    }


    /**
     * 获取BigDecimal模式
     *
     * @return
     */
    public BigDecimal getSalesPriceBigDecimal() {
        return   BigDecimal.valueOf(this.getShoppingGoods().getSales_price());
    }

    /**
     * 获取总价
     *
     * @return 总价
     */
    public BigDecimal getRealPriceTotalBigDecimal() {
        return getRealPriceBigDecimal().multiply(new BigDecimal(this.getShoppingGoods().getBuy_number()));
    }

    public ChargeGoods(ShoppingCartItems goods) {
        init(goods);
    }


    public ShoppingGoods getShoppingGoods() {
        return shoppingGoods;
    }

    public void setShoppingGoods(ShoppingGoods shoppingGoods) {
        this.shoppingGoods = shoppingGoods;
    }

    public BigDecimal getRealPriceAfterDiscount() {
        Discount discount = this.getDiscountPerSku();
        double yohoCoinDiscount=0.0;
        try {
            yohoCoinDiscount= YHMath.div(discount.yohoCoinNum, com.yoho.yhorder.common.utils.Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO);
        } catch (IllegalAccessException ex) {
            logger.warn("call YHMath.div error,m is {},d is {}", discount.yohoCoinNum, com.yoho.yhorder.common.utils.Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO, ex);
        }
        return getRealPriceBigDecimal().subtract(BigDecimal.valueOf(discount.couponsAmount)).
                subtract(BigDecimal.valueOf(discount.promotionCodeAmount)).
                subtract(BigDecimal.valueOf(discount.redEnvelopeAmount)).
                subtract(BigDecimal.valueOf(yohoCoinDiscount));
    }

    public BigDecimal getRealPriceTotalBigDecimalAfterDiscount() {
        return getRealPriceAfterDiscount().multiply(new BigDecimal(this.getShoppingGoods().getBuy_number()));
    }


    /**
     * 将DO对象的属性转换为BO
     *
     * @param goods
     */
    private void init(ShoppingCartItems goods) {

        logger.debug("start to init good info: {}", goods);
        Map<String, Object> extMap = goods.getExtMap();
        if (extMap == null || extMap.isEmpty()) {
            return;
        }
        /**
         *  ShoppingGoods对象为实际算费的对象，该对象最终返回给app端，
         其属性名称、类型不能修改，在算费过程中若需要其他的属性，可在ChargeGoods上添加，
         在算费过程中ShoppingGoods对象的部分属性类型需要转换
         */
        ShoppingGoods shoppingGoods = new ShoppingGoods();
        Set<String> keys = extMap.keySet();
        for (String key : keys) {
            Object value = extMap.get(key);
            if ("promotion_type".equals(key)) {
                this.setPromotionType((String) value);
                continue;
            }
            String name = "set" + String.valueOf(key.charAt(0)).toUpperCase();
            if (key.length() > 1) {
                name += key.substring(1);
            }
            ReflectTool.injectAttributeValue(shoppingGoods, name, value);
        }

        logger.debug("add shopping goods success:{} ", shoppingGoods);
        this.setShoppingGoods(shoppingGoods);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}