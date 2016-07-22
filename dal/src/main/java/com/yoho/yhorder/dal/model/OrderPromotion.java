package com.yoho.yhorder.dal.model;

public class OrderPromotion {
    private Integer id;

    private Integer orderId;

    private Byte promotionType;

    private Integer promotionId;

    private String promotionData;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Byte getPromotionType() {
        return promotionType;
    }

    public void setPromotionType(Byte promotionType) {
        this.promotionType = promotionType;
    }

    public Integer getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Integer promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionData() {
        return promotionData;
    }

    public void setPromotionData(String promotionData) {
        this.promotionData = promotionData == null ? null : promotionData.trim();
    }
}