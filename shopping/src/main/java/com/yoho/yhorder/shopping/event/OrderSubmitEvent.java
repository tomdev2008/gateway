package com.yoho.yhorder.shopping.event;

import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.yhorder.shopping.model.Order;

import java.util.List;

/**
 * Created by wujiexiang on 16/4/22.
 */
public class OrderSubmitEvent {

    private Order order;

    private List<PromotionInfo> promotionInfoList;

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
}
