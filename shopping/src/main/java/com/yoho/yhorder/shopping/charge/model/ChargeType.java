package com.yoho.yhorder.shopping.charge.model;

import com.yoho.yhorder.shopping.utils.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by JXWU on 2016/2/2.
 */
public enum ChargeType {

    ORDINARY(Constants.ORDINARY_CHARGE_TYPE) {

        public boolean freeShippingLimit() {
            return true;
        }

        public boolean usingPromotionCode() {
            return true;
        }

        public boolean usingRedEnvelopes() {
            return true;
        }

    },

    ADVANCE(Constants.ADVANCE_CHARGE_TYPE) {

        public boolean freeShippingLimit() {
            return true;
        }

        public boolean usingPromotionCode() {
            return false;
        }

        public boolean usingRedEnvelopes() {
            return false;
        }
    },
    TICKET(Constants.TICKET_CHARGE_TYPE) {
        public boolean freeShippingLimit() {
            return false;
        }

        public boolean usingPromotionCode() {
            return false;
        }

        public boolean usingRedEnvelopes() {
            return false;
        }
    },
    LIMITCODE(Constants.LIMITCODE_CHARGE_TYPE) {

        public boolean freeShippingLimit() {
            return true;
        }

        public boolean usingPromotionCode() {
            return false;
        }

        public boolean usingRedEnvelopes() {
            return false;
        }

    },
    //可用优惠券和不可用优惠券计算
    LISTCOUPON(Constants.LISTCOUPON_CHARGE_TYPE) {

        public boolean freeShippingLimit() {
            return false;
        }

        public boolean usingPromotionCode() {
            return false;
        }

        public boolean usingRedEnvelopes() {
            return false;
        }

    },
    //使用优惠券计算
    USECOUPON(Constants.USECOUPON_CHARGE_TYPE) {
        public boolean freeShippingLimit() {
            return false;
        }

        public boolean usingPromotionCode() {
            return false;
        }

        public boolean usingRedEnvelopes() {
            return false;
        }
    };

    /**
     * 购物车类型：
     * 普通购物车、预售购物车、限购码(没有购物车)
     */

    //运费，促销计算的免邮、499、金卡免邮，只免普通运费；预售购物车不计算
    public abstract boolean freeShippingLimit();

    //计算运费，白金卡、预售、YOHOOD现场自提，运费全免；免邮券加急不免，都支持
    public boolean caculateShippingFee() {
        return true;
    }

    //优惠码，只有普通商品购物车才计算
    public abstract boolean usingPromotionCode();

    // 红包，只有普通商品购物车才计算
    public abstract boolean usingRedEnvelopes();

    //yoho币，都支持
    public boolean usingYohoCoin() {
        return true;
    }

    //限购码
   // public abstract boolean shouldLimitCodeCharge();

    public String getName() {
        return this.chargeType;
    }

    private String chargeType;

    ChargeType(String chargeType) {
        this.chargeType = chargeType;
    }

    public static ChargeType parse(String chargeTypeName) {

        ChargeType chargeType = null;
        if (StringUtils.isNotEmpty(chargeTypeName)) {
            chargeType = ChargeType.valueOf(chargeTypeName.toUpperCase());
        }

        return chargeType != null ? chargeType : ORDINARY;
    }
}
