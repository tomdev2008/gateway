package com.yoho.yhorder.dal.model;

import java.math.BigDecimal;

public class ReturnedGoodsList {
    private Integer id;

    private Integer returnRequestId;

    private Long orderCode;

    private Integer productSkn;

    private Integer productSkc;

    private Integer productSku;

    private Integer orderGoodsId;

    private Integer initOrderGoodsId;

    private BigDecimal lastPrice;

    private Byte goodsType;

    private Byte returnedReason;

    private Integer requisitionFormId;

    private Integer batchId;

    private String seatCodeString;

    private String imperfect;

    private String remark;

    private Byte status;

    private Integer createTime;

    private Integer updateTime;

    private Integer returnYohoCoin;

    private BigDecimal realReturnedAmount;

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

    public Integer getOrderGoodsId() {
        return orderGoodsId;
    }

    public void setOrderGoodsId(Integer orderGoodsId) {
        this.orderGoodsId = orderGoodsId;
    }

    public Integer getInitOrderGoodsId() {
        return initOrderGoodsId;
    }

    public void setInitOrderGoodsId(Integer initOrderGoodsId) {
        this.initOrderGoodsId = initOrderGoodsId;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public Byte getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(Byte goodsType) {
        this.goodsType = goodsType;
    }

    public Byte getReturnedReason() {
        return returnedReason;
    }

    public void setReturnedReason(Byte returnedReason) {
        this.returnedReason = returnedReason;
    }

    public Integer getRequisitionFormId() {
        return requisitionFormId;
    }

    public void setRequisitionFormId(Integer requisitionFormId) {
        this.requisitionFormId = requisitionFormId;
    }

    public Integer getBatchId() {
        return batchId;
    }

    public void setBatchId(Integer batchId) {
        this.batchId = batchId;
    }

    public String getSeatCodeString() {
        return seatCodeString;
    }

    public void setSeatCodeString(String seatCodeString) {
        this.seatCodeString = seatCodeString == null ? null : seatCodeString.trim();
    }

    public String getImperfect() {
        return imperfect;
    }

    public void setImperfect(String imperfect) {
        this.imperfect = imperfect == null ? null : imperfect.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
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

    public Integer getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Integer updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getReturnYohoCoin() {
        return returnYohoCoin;
    }

    public void setReturnYohoCoin(Integer returnYohoCoin) {
        this.returnYohoCoin = returnYohoCoin;
    }

    public BigDecimal getRealReturnedAmount() {
        return realReturnedAmount;
    }

    public void setRealReturnedAmount(BigDecimal realReturnedAmount) {
        this.realReturnedAmount = realReturnedAmount;
    }
}