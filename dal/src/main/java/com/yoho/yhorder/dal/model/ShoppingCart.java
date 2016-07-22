package com.yoho.yhorder.dal.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Created by JXWU on 2015/11/17.
 */
public class ShoppingCart {

    private Integer id;

    private Integer uid;

    private String shoppingKey;

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

    public String getShoppingKey() {
        return shoppingKey;
    }

    public void setShoppingKey(String shoppingKey) {
        this.shoppingKey = shoppingKey;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
