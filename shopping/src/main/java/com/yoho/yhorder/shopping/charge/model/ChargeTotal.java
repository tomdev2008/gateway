package com.yoho.yhorder.shopping.charge.model;

import com.yoho.service.model.order.model.PackageBO;
import com.yoho.service.model.order.model.PromotionBO;
import com.yoho.service.model.order.model.coupon.ShoppingCouponBO;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.service.model.order.response.audit.AuditCodPayResponse;
import com.yoho.service.model.order.response.shopping.PromotionFormula;
import lombok.Data;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by JXWU on 2015/11/21.
 */
@Data
public class ChargeTotal {

    /**
     * 所有选中SKU的sales price价格，减去vip优惠
     */
    private double orderAmount;

    /**
     * 选中的商品数量
     */
    private int selectedGoodsCount;

    /**
     * 选中的主商品数量
     */
    private int selectedMainGoodsCount;

    /**
     * 购物车中所有商品的数量，包括未选中、赠品、加价购
     */
    private int goodsCount;


    /**
     * 获取虚拟币
     */
    private double gainYohoCoin = 0;

    /**
     * 最终订单金额，即实际需支付的现金，已排除yoho币、优惠券等
     */
    private double lastOrderAmount;

    //普通商品的金额
    private double lastMainGoodsOrderAmount = 0;

    /**
     * 优惠金额
     */
    private double discountAmount;

    /**
     * VIP 享受金额
     */
    private double vipCutdownAmount = 0;

    /**
     * outlet 享受金额
     */
    private double outletCutdownAmount = 0;

    /**
     * 运费
     */
    private double shippingCost;

    /**
     * 加急服务费
     */
    private double fastShoppingCost;


    /**
     * 最终运费
     */
    public double lastShippingCost = 0;

    /**
     * 必须在线支付
     */
    private boolean mustOnlinePayment;

    /**
     * 必须在线原因
     */
    private String mustOnlinePaymentReason;

    /**
     * 优惠券类型字母
     *
     * @var string
     */
    private String couponAlphabet = "";

    /**
     * 优惠券ID
     *
     * @var int
     */
    private int couponId = 0;

    /**
     * 优惠券码
     *
     * @var string
     */
    private String couponCode = "";

    /**
     * 是否使用了优惠券
     *
     * @var bool
     */
    private boolean useCoupon = false;

    /**
     * 优惠券金额
     */
    private double couponAmount = 0;

    /**
     * 优惠券名称
     *
     * @var string
     */
    private String couponTitle = "";

    /**
     * 优惠券使用金额
     */
    private double couponUseAmount = 0;

    /**
     * 使用的YOHO币
     */
    private double useYohoCoin = 0;


    /**
     * 使用的YOHO币抵扣的运费
     */
    private double yohoCoinShippingCost = 0;


    /**
     * 使用的红包
     */
    private double useRedEnvelopes = 0;

    /**
     * 优惠码信息
     */
    private PromotionCodeChargeResult promotionCodeChargeResult = new PromotionCodeChargeResult();

    /**
     * 运费促销
     * @var array
     */
    private Map<String, Object> shippingCostPromotion = new HashMap<>();




    //private int shippingCost;


    /**
     * 促销公式
     */
    private List<PromotionFormula> promotionFormulaList = new ArrayList<PromotionFormula>();

    /**
     * 促销信息
     */
    private List<PromotionBO> promotionInfoList = new ArrayList<>();

    /**
     * 可以使用的优惠券
     */
    private List<ShoppingCouponBO> usableCouponList = new ArrayList<>();

    /**
     * 不可以使用的优惠券
     */
    private List<ShoppingCouponBO> unusableCouponList = new ArrayList<>();

    //包裹列表
    private List<PackageBO> packageList = new ArrayList<>();

    /**
     * promotionInfoList 数组中的只有促销的简短信息,是给客户端使用
     * usePromotionInfoList 保护促销全部信息,下单完成后需要记录
     */
    private List<PromotionInfo> usePromotionInfoList = new ArrayList<>();

    /**
     *  add promotion info
     * @param promotionBO promotion info
     */
    public void addPromotionInfo(PromotionBO promotionBO){
        this.promotionInfoList.add(promotionBO);
    }


    public double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(double orderAmount) {
        this.orderAmount = orderAmount;
    }

    public int getSelectedGoodsCount() {
        return selectedGoodsCount;
    }

    public void setSelectedGoodsCount(int selectedGoodsCount) {
        this.selectedGoodsCount = selectedGoodsCount;
    }

    public int getGoodsCount() {
        return goodsCount;
    }

    public void setGoodsCount(int goodsCount) {
        this.goodsCount = goodsCount;
    }

    public double getGainYohoCoin() {
        return gainYohoCoin;
    }

    public void setGainYohoCoin(double gainYohoCoin) {
        this.gainYohoCoin = gainYohoCoin;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getVipCutdownAmount() {
        return vipCutdownAmount;
    }

    public void setVipCutdownAmount(double vipCutdownAmount) {
        this.vipCutdownAmount = vipCutdownAmount;
    }

    public double getOutletCutdownAmount() {
        return outletCutdownAmount;
    }

    public void setOutletCutdownAmount(double outletCutdownAmount) {
        this.outletCutdownAmount = outletCutdownAmount;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public double getFastShoppingCost() {
        return fastShoppingCost;
    }

    public void setFastShoppingCost(double fastShoppingCost) {
        this.fastShoppingCost = fastShoppingCost;
    }

    public boolean isMustOnlinePayment() {
        return mustOnlinePayment;
    }

    public void resetMustOnlinePaymentInfo(boolean mustOnlinePayment) {
        this.mustOnlinePayment = mustOnlinePayment;
    }

    public String getCouponAlphabet() {
        return couponAlphabet;
    }

    public void setCouponAlphabet(String couponAlphabet) {
        this.couponAlphabet = couponAlphabet;
    }

    public int getCouponId() {
        return couponId;
    }

    public void setCouponId(int couponId) {
        this.couponId = couponId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public boolean isUseCoupon() {
        return useCoupon;
    }

    public void setUseCoupon(boolean useCoupon) {
        this.useCoupon = useCoupon;
    }

    public String getCouponTitle() {
        return couponTitle;
    }

    public void setCouponTitle(String couponTitle) {
        this.couponTitle = couponTitle;
    }

    public double getCouponAmount() {
        return couponAmount;
    }

    public void setCouponAmount(double couponAmount) {
        this.couponAmount = couponAmount;
    }

    public double getCouponUseAmount() {
        return couponUseAmount;
    }

    public void setCouponUseAmount(double couponUseAmount) {
        this.couponUseAmount = couponUseAmount;
    }

    public List<PromotionFormula> getPromotionFormulaList() {
        return promotionFormulaList;
    }

    public void setPromotionFormulaList(List<PromotionFormula> promotionFormulaList) {
        this.promotionFormulaList = promotionFormulaList;
    }

    public List<PromotionBO> getPromotionInfoList() {
        return promotionInfoList;
    }



    public double getLastShippingCost() {
        return lastShippingCost;
    }

    public void setLastShippingCost(double lastShippingCost) {
        this.lastShippingCost = lastShippingCost;
    }

    public double getUseYohoCoin() {
        return useYohoCoin;
    }

    public void setUseYohoCoin(double useYohoCoin) {
        this.useYohoCoin = useYohoCoin;
    }

    public double getUseRedEnvelopes() {
        return useRedEnvelopes;
    }

    public void setUseRedEnvelopes(double useRedEnvelopes) {
        this.useRedEnvelopes = useRedEnvelopes;
    }

    public Map<String, Object> getShippingCostPromotion() {
        return shippingCostPromotion;
    }

    public void setShippingCostPromotion(Map<String, Object> shippingCostPromotion) {
        this.shippingCostPromotion = shippingCostPromotion;
    }

    public void resetMustOnlinePaymentInfo(AuditCodPayResponse response) {
        if (response != null) {
            this.resetMustOnlinePaymentInfo("N".equals(response.getIsSupport()));
            this.setMustOnlinePaymentReason(response.getIsSupportMessage());
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
