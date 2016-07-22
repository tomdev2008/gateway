package com.yoho.yhorder.dal.model;

/**
 * Created by JXWU on 2015/11/19.
 */
public class ShoppingTag {
    private Integer uid;

    private String shoppingTagKey;

    private String isUse;


    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getIsUse() {
        return isUse;
    }

    public void setIsUse(String isUse) {
        this.isUse = isUse;
    }

    public String getShoppingTagKey() {
        return shoppingTagKey;
    }

    public void setShoppingTagKey(String shoppingTagKey) {
        this.shoppingTagKey = shoppingTagKey;
    }
}
