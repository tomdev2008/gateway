package com.yoho.yhorder.shopping.model;

import com.yoho.service.model.order.model.promotion.PromotionInfo;

import java.util.List;

/**
 * Created by JXWU on 2015/11/30.
 * 创建订单上下文
 */
public class OrderCreationContext {

    private UserInfo userInfo;

    private Order order;

    private List<PromotionInfo> promotionInfoList;

//    private String mobile;
//
//    private String chargeType;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<PromotionInfo> getPromotionInfoList() {
        return promotionInfoList;
    }

    public void setPromotionInfoList(List<PromotionInfo> promotionInfoList) {
        this.promotionInfoList = promotionInfoList;
    }

//    public String getMobile() {
//        return mobile;
//    }
//
//    public void setMobile(String mobile) {
//        this.mobile = mobile;
//    }
//
//    public String getChargeType() {
//        return chargeType;
//    }
//
//    public void setChargeType(String chargeType) {
//        this.chargeType = chargeType;
//    }
}
