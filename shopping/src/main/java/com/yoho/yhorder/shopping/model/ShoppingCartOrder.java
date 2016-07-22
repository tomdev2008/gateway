package com.yoho.yhorder.shopping.model;

import com.yoho.yhorder.shopping.utils.Constants;

/**
 * Created by JXWU on 2015/11/18.
 */
public class ShoppingCartOrder {
    /**
     * 用户id
     */
    private int uid;

    private String shoppingKey;
    /**
     * 购物车类型
     */
    private String cartType = Constants.ORDINARY_CART_TYPE;

    /**
     * 执行促销
     */
    private boolean runPromotion = true;

    /**
     * 配送方式 1普通 2加快  10Yohood自提
     */
    private int shippingManner = 1;

    /**
     * 购物车运费计算 默认不计算
     */
    public boolean needCalcShippingCost = false;


    //字符串为Y
    private boolean isJit = false;


    private int attribute;


    /**
     * 购物标记
     */
    private String shoppingTag = "";

    /**
     * 是否自选
     */
    private boolean selected = false;

    /**
     * 金额保留小数后位数
     *
     * @var int
     */
    private int amountPrecision = 1;

    private String saleChannel;

    /**
     * 购物车id
     */
    private int shoppingCartId;

    /**
     * 请求使用YOHO币
     */
    private Integer useYohoCoin = 0;

    /**
     * 优惠券码
     */
    private String couponCode;

    /**
     * 使用红包
     */
    private Integer useRedEnvelopes = 0;

    /**
     * 支付类型
     */
    private Integer paymentType = 1;

    /**
     * 客户端类型
     */
    private String clientType;

    //HTTP 头User-Agent
    private String userAgent;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getShoppingKey() {
        return shoppingKey;
    }

    public void setShoppingKey(String shoppingKey) {
        this.shoppingKey = shoppingKey;
    }

    public String getCartType() {
        return cartType;
    }

    public void setCartType(String cartType) {
        this.cartType = cartType;
    }

    public boolean isRunPromotion() {
        return runPromotion;
    }

    public void setRunPromotion(boolean runPromotion) {
        this.runPromotion = runPromotion;
    }

    public int getShippingManner() {
        return shippingManner;
    }

    public void setShippingManner(int shippingManner) {
        this.shippingManner = shippingManner;
    }

    public boolean isNeedCalcShippingCost() {
        return needCalcShippingCost;
    }

    public void setNeedCalcShippingCost(boolean needCalcShippingCost) {
        this.needCalcShippingCost = needCalcShippingCost;
    }

    public boolean isJit() {
        return isJit;
    }

    public void setIsJit(boolean isJit) {
        this.isJit = isJit;
    }

    public int getAttribute() {
        return attribute;
    }

    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }

    public String getShoppingTag() {
        return shoppingTag;
    }

    public void setShoppingTag(String shoppingTag) {
        this.shoppingTag = shoppingTag;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getSaleChannel() {
        return saleChannel;
    }

    public void setSaleChannel(String saleChannel) {
        this.saleChannel = saleChannel;
    }

    public int getAmountPrecision() {
        return amountPrecision;
    }

    public void setAmountPrecision(int amountPrecision) {
        this.amountPrecision = amountPrecision;
    }

    public int getShoppingCartId() {
        return shoppingCartId;
    }

    public void setShoppingCartId(int shoppingCartId) {
        this.shoppingCartId = shoppingCartId;
    }

    public Integer getUseYohoCoin() {
        return useYohoCoin;
    }

    public void setUseYohoCoin(Integer useYohoCoin) {
        this.useYohoCoin = useYohoCoin;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public Integer getUseRedEnvelopes() {
        return useRedEnvelopes;
    }

    public void setUseRedEnvelopes(Integer useRedEnvelopes) {
        this.useRedEnvelopes = useRedEnvelopes;
    }

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
