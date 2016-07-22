package com.yoho.yhorder.dal.domain;

/**
 * 退换货商品
 * 实体类
 *
 * @author CaoQi
 * @Time 2015/11/20
 */
public class ChangeGoods {

    private Integer id;

    private Long initOrderCode;

    private Long sourceOrderCode;

    private Long orderCode;

    private Integer changePurchaseId;

    private Integer productSkn;

    private Integer productSkc;

    private Integer productSku;

    private Integer sourceProductSkc;

    private Integer sourceProductSku;

    private Byte goodsType;

    private Byte exchangeReason;

    private String remark;

    private Byte status;

    private Integer createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getInitOrderCode() {
        return initOrderCode;
    }

    public void setInitOrderCode(Long initOrderCode) {
        this.initOrderCode = initOrderCode;
    }

    public Long getSourceOrderCode() {
        return sourceOrderCode;
    }

    public void setSourceOrderCode(Long sourceOrderCode) {
        this.sourceOrderCode = sourceOrderCode;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public Integer getChangePurchaseId() {
        return changePurchaseId;
    }

    public void setChangePurchaseId(Integer changePurchaseId) {
        this.changePurchaseId = changePurchaseId;
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

    public Integer getSourceProductSkc() {
        return sourceProductSkc;
    }

    public void setSourceProductSkc(Integer sourceProductSkc) {
        this.sourceProductSkc = sourceProductSkc;
    }

    public Integer getSourceProductSku() {
        return sourceProductSku;
    }

    public void setSourceProductSku(Integer sourceProductSku) {
        this.sourceProductSku = sourceProductSku;
    }

    public Byte getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(Byte goodsType) {
        this.goodsType = goodsType;
    }

    public Byte getExchangeReason() {
        return exchangeReason;
    }

    public void setExchangeReason(Byte exchangeReason) {
        this.exchangeReason = exchangeReason;
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
