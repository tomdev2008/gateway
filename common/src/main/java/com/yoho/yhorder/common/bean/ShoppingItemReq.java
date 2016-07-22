package com.yoho.yhorder.common.bean;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by frw on 2016/4/20.
 */
public class ShoppingItemReq implements Serializable{

    private static final long serialVersionUID = -2185104184057639758L;
    private Integer id;

    private Integer shoppingCartId;

    private Integer skuId;

    private Integer num;

    private Integer promotionId;

    private Byte status;

    private Integer productSkn;

    private Integer createTime;

    private Integer toCartId;
    private Integer srcCartId;

    private String selected;
    private Integer uid;

    public Map<String,Object> extMap = new HashMap<String,Object>();

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Integer getToCartId() {
        return toCartId;
    }

    public void setToCartId(Integer toCartId) {
        this.toCartId = toCartId;
    }

    public Integer getSrcCartId() {
        return srcCartId;
    }

    public void setSrcCartId(Integer srcCartId) {
        this.srcCartId = srcCartId;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getShoppingCartId() {
        return shoppingCartId;
    }

    public void setShoppingCartId(Integer shoppingCartId) {
        this.shoppingCartId = shoppingCartId;
    }

    public Integer getSkuId() {
        return skuId;
    }

    public void setSkuId(Integer skuId) {
        this.skuId = skuId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Integer getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Integer promotionId) {
        this.promotionId = promotionId;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Integer getProductSkn() {
        return productSkn;
    }

    public void setProductSkn(Integer productSkn) {
        this.productSkn = productSkn;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public Map<String, Object> getExtMap() {
        return extMap;
    }

    public void setExtMap(Map<String, Object> extMap) {
        this.extMap = extMap;
    }


}
