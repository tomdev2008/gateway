package com.yoho.yhorder.common.bean;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Created by JXWU on 2016/2/16.
 * 区别与购物车ShoppingCartItem
 */
public class ShoppingItem {
    private String type;
    private int sku;
    private int skn;
    private int buyNumber = 1;
    //限购码
    private String limitCode;

    private String limitProductCode;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSku() {
        return sku;
    }

    public void setSku(int sku) {
        this.sku = sku;
    }

    public int getSkn() {
        return skn;
    }

    public void setSkn(int skn) {
        this.skn = skn;
    }

    public int getBuyNumber() {
        return buyNumber;
    }

    public void setBuyNumber(int buyNumber) {
        this.buyNumber = buyNumber;
    }

    public String getLimitCode() {
        return limitCode;
    }

    public void setLimitCode(String limitCode) {
        this.limitCode = limitCode;
    }

    public String getLimitProductCode() {
        return limitProductCode;
    }

    public void setLimitProductCode(String limitProductCode) {
        this.limitProductCode = limitProductCode;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
