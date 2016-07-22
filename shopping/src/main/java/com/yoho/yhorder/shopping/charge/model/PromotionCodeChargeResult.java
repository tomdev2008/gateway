package com.yoho.yhorder.shopping.charge.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import com.yoho.service.model.promotion.PromotionCodeBo;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Created by JXWU on 2016/1/8.
 * 优惠码
 */
public class PromotionCodeChargeResult {

    private int promotionId = 0;

    /**
     * 优惠类型
     */
    private String discountType;
    /**
     * 优惠码
     */
    private String promotionCode = "";

    ////////////条件/////////////
    /**
     * 金额
     */
    private double amountAtLeast = 0;
    /**
     * 数量
     */
    private int countAtLeast = 0;

    ////////结果/////////
    /**
     * 折扣率，如0.8
     */
    private double discount = 0;
    /**
     * 最多折扣金额
     */
    private double discountAtMost = 0;

    /**
     * 优惠码减免金额
     */
    private double discountAmount = 0;

    /**
     * 能否使用,满足优惠条件
     */
    private boolean valid = false;


    private PromotionCodeBo promotionCodeBo;


    /**
     * 根据条件(订单应付金额和miangoods数量，计算折扣金额)
     *
     * @param lastOrderAmount
     * @param selectedMainGoodsCount
     */
    public void caculateDiscountAmount(double lastOrderAmount, int selectedMainGoodsCount) {
        //是否满足优惠条件
        this.valid = isMeetingCondition(lastOrderAmount, selectedMainGoodsCount);
        if (this.valid) {
            double expectDiscountAmount = 0;
            switch (discountType)
            {
                case "1":
                    //吊牌价折扣暂不支持
                    break;
                case "2":
                    //销售价折扣
                    expectDiscountAmount = upComputedDiscount(lastOrderAmount);
                    expectDiscountAmount = Math.min(expectDiscountAmount,discountAtMost);
                    break;
                case "3":
                    //满减
                    expectDiscountAmount = discountAtMost;
                    break;
                default:
                    break;
            }
            expectDiscountAmount = Math.min(expectDiscountAmount,lastOrderAmount);
            this.discountAmount = expectDiscountAmount;
        }
    }

    private double upComputedDiscount(double lastOrderAmount)
    {
        BigDecimal amount = BigDecimal.valueOf(lastOrderAmount);
        BigDecimal one = new BigDecimal("1");
        BigDecimal discountBigDecimal = BigDecimal.valueOf(discount);
        //有第三位小数 进1
        return up(amount.multiply(one.subtract(discountBigDecimal)));
    }

    private double up(BigDecimal bigDecimal){

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.UP);

        return Double.valueOf(df.format(bigDecimal.doubleValue()));
    }

    private boolean isMeetingCondition(double lastOrderAmount, int selectedMainGoodsCount) {
        return (lastOrderAmount >= amountAtLeast && selectedMainGoodsCount >= countAtLeast);
    }

    public int getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(int promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public double getAmountAtLeast() {
        return amountAtLeast;
    }

    public void setAmountAtLeast(double amountAtLeast) {
        this.amountAtLeast = amountAtLeast;
    }

    public int getCountAtLeast() {
        return countAtLeast;
    }

    public void setCountAtLeast(int countAtLeast) {
        this.countAtLeast = countAtLeast;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getDiscountAtMost() {
        return discountAtMost;
    }

    public void setDiscountAtMost(double discountAtMost) {
        this.discountAtMost = discountAtMost;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public PromotionCodeBo getPromotionCodeBo() {
        return promotionCodeBo;
    }

    public void setPromotionCodeBo(PromotionCodeBo promotionCodeBo) {
        this.promotionCodeBo = promotionCodeBo;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}