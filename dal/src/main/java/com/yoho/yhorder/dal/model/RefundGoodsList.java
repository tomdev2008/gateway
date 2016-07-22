package com.yoho.yhorder.dal.model;

import java.math.BigDecimal;

public class RefundGoodsList extends Base {
    private static final long serialVersionUID = 3781745645023676258L;
    /**
     * 退货申请商品ID
     */
    private Integer id;

    /**
     * 申请单ID
     */
    private Integer returnRequestId;

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
     * 商品类型
     */
    private Byte goodsType;

    /**
     * 支付价格
     */
    private BigDecimal lastPrice;

    /**
     * 请退原因
     */
    private Byte returnedReason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态
     * 0   未清点
     * 10  已清点
     * 20  入库
     */
    private Byte status;

    /**
     * 创建时间
     */
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
}