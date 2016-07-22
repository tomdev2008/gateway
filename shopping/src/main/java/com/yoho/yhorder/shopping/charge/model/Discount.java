package com.yoho.yhorder.shopping.charge.model;

import com.yoho.core.common.utils.YHMath;
import com.yoho.yhorder.common.utils.Constants;
import com.yoho.yhorder.shopping.utils.MathUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Created by JXWU on 2016/1/19.
 */
@Getter
@Setter
public class Discount {

    //优惠券
    public double couponsAmount = 0;

    //优惠码
    public double promotionCodeAmount = 0;

    //yoho币数量
    public int yohoCoinNum = 0;

    //红包
    public double redEnvelopeAmount = 0;

    //促销活动减免,需要累计
    public double promotionAmount = 0;

    public void setDiscountAmount(double amount, DiscountType type) {
        switch (type) {
            case COUPONS:
                couponsAmount = YHMath.add(couponsAmount, amount);
                break;
            case PROMOTIONCODE:
                promotionCodeAmount  = YHMath.add(promotionCodeAmount,amount);
                break;
            case YOHOCOIIN:
                //yoho币数量 向下取整
                yohoCoinNum += MathUtils.downToInt(amount);
                break;
            case REDENVELOPE:
                redEnvelopeAmount = YHMath.add(redEnvelopeAmount,amount);
                break;
            case PROMOTION:
                promotionAmount = YHMath.add(promotionAmount , amount);
                break;
            default:
                break;
        }
    }


    /**
     *
     * @return
     */
    public void setYohoCoinDiscountAmountByAmount(double yohoCoinAmount) {
        yohoCoinNum+=MathUtils.downToInt(YHMath.mul(yohoCoinAmount, com.yoho.yhorder.common.utils.Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO));
    }


    /**
     * 获取当前指定优惠的金额
     * @param type
     * @return
     */
    public double getCurrentDiscountAmount(DiscountType type){
        double currentDiscountAmount=0.0;
        switch (type) {
            case COUPONS:
                currentDiscountAmount=couponsAmount;
                break;
            case PROMOTIONCODE:
                currentDiscountAmount=promotionCodeAmount ;
                break;
            case YOHOCOIIN:
                //yoho币数量
                currentDiscountAmount=yohoCoinNum;
                break;
            case REDENVELOPE:
                currentDiscountAmount=redEnvelopeAmount;
                break;
            case PROMOTION:
                currentDiscountAmount=promotionAmount;
                break;
            default:
                break;
        }
        return currentDiscountAmount;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public static  void main(String[] args)
    {
        double t = 55.;
        double a = 6.78;
        System.out.println(t+ a);
    }
}