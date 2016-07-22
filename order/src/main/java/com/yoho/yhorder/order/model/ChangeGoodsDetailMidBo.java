package com.yoho.yhorder.order.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.yoho.product.model.BrandBo;
import com.yoho.yhorder.common.annotation.Mapping;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 换货订单商品 中间转换类
 *
 * @author lijian
 * @date 2015/12/3
 */

public class ChangeGoodsDetailMidBo implements Serializable {


    private Integer id;

    private Integer uid;


    private Integer brandId;

    /**
     * '1为正常商品，2为商品的赠品，3为订单赠品，4为商品满足条件加钱送，5为订单满足条件价钱送。
     */
    private Byte goodsType;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    private BigDecimal goodsAmount;

    private Integer sizeId;


    //判断商品品类是否显示身高体重
    private boolean shouldShowWeighInfo;

    /**
     * 商品名称
     */

    private String goodsStatus;

    private BrandBo brand;

    private String expectArrivalTime;

    private static final long serialVersionUID = -315593461752419003L;

    //商品总数
    private Integer num;

    //换货总数
    private Integer changeNum;


    private Integer erpSkuId;

    private Integer productId;


    private String productName;

    private Integer goodsId;

    private Integer isChangeFlag;// 是否换货 1 是 0 不是 换货业务处理

    /**
     * 尺码
     */
    private String sizeName;

    private Byte colorId;


    private String colorName;

    private Integer orderId;

    private Integer productSkn;

    private Integer productSku;

    private Integer productSkc;

    private Long salesPrice;

    private Integer hasShoes;

    private String goodsImg;

    /**
     * 商品名称
     */
    private String goodsName;

    @JSONField(name = "goods_type_id")
    private Integer goodsTypeId;

    private String goodsTypeName;


    private Long lastPrice;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public Byte getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(Byte goodsType) {
        this.goodsType = goodsType;
    }

    public BigDecimal getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(BigDecimal goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public BigDecimal getGoodsAmount() {
        return goodsAmount;
    }

    public void setGoodsAmount(BigDecimal goodsAmount) {
        this.goodsAmount = goodsAmount;
    }

    public Integer getSizeId() {
        return sizeId;
    }

    public void setSizeId(Integer sizeId) {
        this.sizeId = sizeId;
    }

    public boolean isShouldShowWeighInfo() {
        return shouldShowWeighInfo;
    }

    public void setShouldShowWeighInfo(boolean shouldShowWeighInfo) {
        this.shouldShowWeighInfo = shouldShowWeighInfo;
    }

    public String getGoodsStatus() {
        return goodsStatus;
    }

    public void setGoodsStatus(String goodsStatus) {
        this.goodsStatus = goodsStatus;
    }

    public BrandBo getBrand() {
        return brand;
    }

    public void setBrand(BrandBo brand) {
        this.brand = brand;
    }

    public String getExpectArrivalTime() {
        return expectArrivalTime;
    }

    public void setExpectArrivalTime(String expectArrivalTime) {
        this.expectArrivalTime = expectArrivalTime;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Integer getChangeNum() {
        return changeNum;
    }

    public void setChangeNum(Integer changeNum) {
        this.changeNum = changeNum;
    }

    public Integer getErpSkuId() {
        return erpSkuId;
    }

    public void setErpSkuId(Integer erpSkuId) {
        this.erpSkuId = erpSkuId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getIsChangeFlag() {
        return isChangeFlag;
    }

    public void setIsChangeFlag(Integer isChangeFlag) {
        this.isChangeFlag = isChangeFlag;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public Byte getColorId() {
        return colorId;
    }

    public void setColorId(Byte colorId) {
        this.colorId = colorId;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getProductSkn() {
        return productSkn;
    }

    public void setProductSkn(Integer productSkn) {
        this.productSkn = productSkn;
    }

    public Integer getProductSku() {
        return productSku;
    }

    public void setProductSku(Integer productSku) {
        this.productSku = productSku;
    }

    public Integer getProductSkc() {
        return productSkc;
    }

    public void setProductSkc(Integer productSkc) {
        this.productSkc = productSkc;
    }

    public Long getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(Long salesPrice) {
        this.salesPrice = salesPrice;
    }

    public Integer getHasShoes() {
        return hasShoes;
    }

    public void setHasShoes(Integer hasShoes) {
        this.hasShoes = hasShoes;
    }

    public String getGoodsImg() {
        return goodsImg;
    }

    public void setGoodsImg(String goodsImg) {
        this.goodsImg = goodsImg;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public Integer getGoodsTypeId() {
        return goodsTypeId;
    }

    public void setGoodsTypeId(Integer goodsTypeId) {
        this.goodsTypeId = goodsTypeId;
    }

    public String getGoodsTypeName() {
        return goodsTypeName;
    }

    public void setGoodsTypeName(String goodsTypeName) {
        this.goodsTypeName = goodsTypeName;
    }

    public Long getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(Long lastPrice) {
        this.lastPrice = lastPrice;
    }
}