package com.yoho.yhorder.dal.domain;

import com.yoho.yhorder.dal.model.Base;

import java.math.BigDecimal;

/**
 * qianjun
 * 2015/12/23.
 */
public class RefundNumberStatistics extends Base{
    private static final long serialVersionUID = -8999404232924443471L;
    private Long orderCode;
    private Integer productSku;
    private BigDecimal lastPrice;
    private Integer number;

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public Integer getProductSku() {
        return productSku;
    }

    public void setProductSku(Integer productSku) {
        this.productSku = productSku;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}
