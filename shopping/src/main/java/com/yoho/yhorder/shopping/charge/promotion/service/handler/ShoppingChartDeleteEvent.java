package com.yoho.yhorder.shopping.charge.promotion.service.handler;

import lombok.Data;

/**
 *  删除购物车的事件
 *
 * Created by chunhua.zhang@yoho.cn on 2015/12/18.
 */
@Data
public class ShoppingChartDeleteEvent {

    private String shoppingChartId;

    private String promotionId;

    private String uid;

    private int deleteNum;


    public ShoppingChartDeleteEvent(String shoppingChartId, String promotionId, String uid, int deleteNum){
        this.shoppingChartId = shoppingChartId;
        this.promotionId = promotionId;
        this.uid = uid;
        this.deleteNum = deleteNum;
    }

    public ShoppingChartDeleteEvent(){}

}
