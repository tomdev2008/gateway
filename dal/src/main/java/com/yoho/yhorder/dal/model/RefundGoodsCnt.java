package com.yoho.yhorder.dal.model;

import java.math.BigDecimal;

public class RefundGoodsCnt extends Base {

    private static final long serialVersionUID = -4415016332107136915L;
    /**
     * 订单号
     */
    private Long orderCode;

    /**
     * 商品skn
     */
    private Integer productSkn;

    /**
     * 商品skc
     */
    private Integer productSkc;

    /**
     * 商品sku
     */
    private Integer productSku;

    /**
     * 支付价格
     */
    private BigDecimal lastPrice;

    //对应数据汇总
    private  Integer cnt;


    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public Integer getProductSkn() {
        return productSkn;
    }

    public void setProductSkn(Integer productSkn) {
        this.productSkn = productSkn;
    }

    public Integer getProductSkc() {
        return productSkc;
    }

    public void setProductSkc(Integer productSkc) {
        this.productSkc = productSkc;
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

    public Integer getCnt() {
        return cnt;
    }

    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }
}