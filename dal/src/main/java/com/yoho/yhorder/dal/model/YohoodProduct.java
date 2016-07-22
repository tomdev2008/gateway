package com.yoho.yhorder.dal.model;

public class YohoodProduct {
    private Integer productSku;

    private Integer productSkc;

    private Integer productSkn;

    private Short gender;

    private Integer sortId;

    private Integer brandId;

    private String isRecommend;

    private Integer buyNumber;

    private Integer presetNumber;

    private String isVote;

    private Integer founder;

    private Short status;

    private Integer createTime;

    private Integer middleSortId;

    private String isOnline;

    public Integer getProductSku() {
        return productSku;
    }

    public void setProductSku(Integer productSku) {
        this.productSku = productSku;
    }

    public Integer getProductSkc() {
        return productSkc;
    }

    public void setProductSkc(Integer productSkc) {
        this.productSkc = productSkc;
    }

    public Integer getProductSkn() {
        return productSkn;
    }

    public void setProductSkn(Integer productSkn) {
        this.productSkn = productSkn;
    }

    public Short getGender() {
        return gender;
    }

    public void setGender(Short gender) {
        this.gender = gender;
    }

    public Integer getSortId() {
        return sortId;
    }

    public void setSortId(Integer sortId) {
        this.sortId = sortId;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public String getIsRecommend() {
        return isRecommend;
    }

    public void setIsRecommend(String isRecommend) {
        this.isRecommend = isRecommend == null ? null : isRecommend.trim();
    }

    public Integer getBuyNumber() {
        return buyNumber;
    }

    public void setBuyNumber(Integer buyNumber) {
        this.buyNumber = buyNumber;
    }

    public Integer getPresetNumber() {
        return presetNumber;
    }

    public void setPresetNumber(Integer presetNumber) {
        this.presetNumber = presetNumber;
    }

    public String getIsVote() {
        return isVote;
    }

    public void setIsVote(String isVote) {
        this.isVote = isVote == null ? null : isVote.trim();
    }

    public Integer getFounder() {
        return founder;
    }

    public void setFounder(Integer founder) {
        this.founder = founder;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public Integer getMiddleSortId() {
        return middleSortId;
    }

    public void setMiddleSortId(Integer middleSortId) {
        this.middleSortId = middleSortId;
    }

    public String getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(String isOnline) {
        this.isOnline = isOnline == null ? null : isOnline.trim();
    }
}