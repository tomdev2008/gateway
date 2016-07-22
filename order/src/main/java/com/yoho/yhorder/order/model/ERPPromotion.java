package com.yoho.yhorder.order.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Created by JXWU on 2015/12/2.
 */
public class ERPPromotion {
    private Long order_code;
    private Integer promotion_id;
    private String promotion_title;
    private Double cutdown_amount;

    public Long getOrder_code() {
        return order_code;
    }

    public void setOrder_code(Long order_code) {
        this.order_code = order_code;
    }

    public Integer getPromotion_id() {
        return promotion_id;
    }

    public void setPromotion_id(Integer promotion_id) {
        this.promotion_id = promotion_id;
    }

    public String getPromotion_title() {
        return promotion_title;
    }

    public void setPromotion_title(String promotion_title) {
        this.promotion_title = promotion_title;
    }

    public Double getCutdown_amount() {
        return cutdown_amount;
    }

    public void setCutdown_amount(Double cutdown_amount) {
        this.cutdown_amount = cutdown_amount;
    }

    @Override
    public String toString(){
        return ReflectionToStringBuilder.toString(this);
    }
}
