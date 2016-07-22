package com.yoho.yhorder.dal.domain;

import java.math.BigDecimal;

/**
 * 退货do
 *
 * @author CaoQi
 * @Time 2015/11/20
 */
public class RefundGoods {

    private Integer id;

    private Integer returnRequestId;

    private Long orderCode;

    private Integer productSkn;

    private Integer productSkc;

    private Integer productSku;

    private Byte goodsType;

    private BigDecimal lastPrice;

    private Byte returnedReason;

    private String remark;

    private Byte status;

    private Integer createTime;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getReturnRequestId() {
        return returnRequestId;
    }

    public void setReturnRequestId(Integer returnRequestId) {
        this.returnRequestId = returnRequestId;
    }

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

    public Byte getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(Byte goodsType) {
        this.goodsType = goodsType;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public Byte getReturnedReason() {
        return returnedReason;
    }

    public void setReturnedReason(Byte returnedReason) {
        this.returnedReason = returnedReason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }
}
