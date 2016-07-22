package com.yoho.yhorder.shopping.charge.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.yhorder.common.bean.ShoppingItem;
import com.yoho.yhorder.common.bean.ShoppingItemReq;
import com.yoho.yhorder.shopping.utils.Constants;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JXWU on 2015/11/18.
 */
@Data
public class ChargeParam {


    /**
     * 查询购物车的时候，传入的参数
     *
     * @param uid
     * @param shoppingKey
     * @param shoppingCartId
     * @param selected
     */
    public ChargeParam(int uid, String shoppingKey, int shoppingCartId, boolean selected) {
        this.uid = uid;
        this.shoppingKey = shoppingKey;
        this.selected = selected;
        this.shoppingCartId = shoppingCartId;
    }

    public ChargeParam() {
    }

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
     * 配送方式 1普通 2加快  10Yohood自提
     */
    private int shippingManner = 1;

    /**
     * 购物车运费计算 默认不计算
     */
    public boolean needCalcShippingCost = false;


    //字符串为Y
    private boolean isJit = false;


    /**
     * 订单属性
     * 1、正常订单
     * 2、
     * 3、虚拟订单
     * 4、
     * 5、预售订单
     * 6、
     * 7、特殊订单
     */
    private int attribute = Constants.ATTRIBUTE_NORMAL;


    /**
     * 购物标记
     */
    private String shoppingTag = "";

    /**
     * 是否自选
     */
    private boolean selected = false;


    private String saleChannel;

    /**
     * 购物车id
     */
    private int shoppingCartId;

    /**
     * 请求使用YOHO币
     */
    private double useYohoCoin = 0;

    /**
     * 优惠券码
     */
    private String couponCode;

    /**
     * 使用红包
     */
    private double useRedEnvelopes = 0;

    /**
     * 支付类型
     */
    private Integer paymentType = 1;

    /**
     * 客户端类型
     */
    private String clientType = "iphone";

    //HTTP 头User-Agent
    private String userAgent;

    /**
     * 优惠码
     */
    private String promotionCode;


    /**
     * 购物车类型
     */
    private ChargeType chargeType = ChargeType.ORDINARY;

    //是否查询用户yoho币，购物车查询接口不需要
    private boolean needQueryYohoCoin = true;

    //是否查询用户红包，购物车查询接口不需要
    private boolean needQueryRedEnvelopes = true;

    //立即购物
    List<ShoppingItem> shoppingItemList = new ArrayList<>();


    //来自未登入客户端的购物车信息
    private List<ShoppingItemReq> reqShopCartItems ;

    //是否需要稽核货到付款
    private boolean needAuditCodPay = true;

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

    public void setChargeType(String chargeType) {
        this.chargeType = ChargeType.parse(chargeType);
    }

    public ChargeType getChargeType() {
        return this.chargeType;
    }

    public String getChargeTypeName() {
        return this.chargeType.getName();
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

    public int getShoppingCartId() {
        return shoppingCartId;
    }

    public void setShoppingCartId(int shoppingCartId) {
        this.shoppingCartId = shoppingCartId;
    }

    public Double getUseRedEnvelopes() {
        return useRedEnvelopes;
    }

    public void setUseRedEnvelopes(Double useRedEnvelopes) {
        this.useRedEnvelopes = useRedEnvelopes;
    }

    public Double getUseYohoCoin() {
        return useYohoCoin;
    }

    public void setUseYohoCoin(Double useYohoCoin) {
        this.useYohoCoin = useYohoCoin;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
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

    public boolean isPreSaleCart() {
        return Constants.PRESALE_CART_TYPE.equals(this.cartType);
    }

    /**
     *  解析立即购买的商品并设置算费类型
     * @param productSkuListString
     */
    public void parseProductSkuListParameterAndSetupChargeType(String cartType,String productSkuListString) {
        shoppingItemList.clear();

        this.setCartType(cartType);
        String chargeType = cartType;
        if (StringUtils.isNotEmpty(productSkuListString)) {
            checkParsedProductSkuListParameter(productSkuListString);
            //只有限购
            chargeType = Constants.LIMITCODE_CHARGE_TYPE;
        }

        setupChargeType(chargeType);
    }

    /**
     * 解析立即购买的商品
     * @param productSkuListString
     */
    private void checkParsedProductSkuListParameter(String productSkuListString) {
        JSONArray array = null;
        try {
            array = JSON.parseArray(productSkuListString);
        } catch (Exception ex) {
            throw new ServiceException(ServiceError.SHOPPING_PACKAGE_FORMAT_IS_NULL);
        }

        if (array == null || array.size() != 1) {
            throw new ServiceException(ServiceError.SHOPPING_PACKAGE_FORMAT_IS_NULL);
        }

        for (int i = 0; i < array.size(); i++) {
            JSONObject ele = array.getJSONObject(i);
            ShoppingItem item = new ShoppingItem();
            item.setType(ele.getString("type"));
            item.setSku(ele.getIntValue("sku"));
            item.setSkn(ele.getIntValue("skn"));
            //只能买一件
            item.setBuyNumber(1);
            item.setLimitProductCode(ele.getString("limitproductcode"));
            shoppingItemList.add(item);
        }
    }

    /**
     * 设置算费类型
     */
    private void setupChargeType(String chargeType) {
        setChargeType(chargeType);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }



    public  List<ShoppingItemReq> getReqShopCartItems(){
        return reqShopCartItems;
    }

    public  void  setReqShopCartItems(String productSkuListString){
        reqShopCartItems=parseReqShopCartItems(productSkuListString);

    }
    /**
     * 解析客户端的productSkuListString
     * @param productSkuListString
     */
    public static List<ShoppingItemReq>  parseReqShopCartItems(String productSkuListString){
        List<ShoppingItemReq> rel=new ArrayList<ShoppingItemReq>();
        JSONArray array = null;
        try {
            array = JSON.parseArray(productSkuListString);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServiceException(ServiceError.SHOPPING_PACKAGE_FORMAT_IS_NULL);
        }

        if (array == null || array.size() <=0) {
            throw new ServiceException(ServiceError.SHOPPING_PACKAGE_FORMAT_IS_NULL);
        }

        //TODO frwnote check 属性不存在
        for (int i = 0; i < array.size(); i++) {
            JSONObject ele = array.getJSONObject(i);
            ShoppingItemReq item = new ShoppingItemReq();
            item.setProductSkn(ele.getIntValue("product_skn"));
            item.setNum(ele.getIntValue("num"));
            item.setSkuId(ele.getIntValue("sku_id"));
            item.setPromotionId(ele.getIntValue("promotion_id"));
            item.setSelected(ele.getString("selected"));
            rel.add(item);
        }
        return rel;
    }

}