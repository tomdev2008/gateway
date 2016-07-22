package com.yoho.yhorder.dal.domain;

import com.yoho.yhorder.dal.model.Base;

import java.math.BigDecimal;

/**
 * qianjun
 * 2015/12/23.
 */
public class RefundNumber extends Base{
    private static final long serialVersionUID = 1222930960652285783L;
    private Integer productSku;
    private BigDecimal lastPrice;

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

}
