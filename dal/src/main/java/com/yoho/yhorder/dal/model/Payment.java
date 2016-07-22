package com.yoho.yhorder.dal.model;

public class Payment {
    private Short id;

    private String payCode;

    private String payName;

    private String payFee;

    private String payIntro;

    private String payParams;

    private Short payOrder;

    private String status;

    private String payIcon;

    private Integer paymentId;

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public String getPayCode() {
        return payCode;
    }

    public void setPayCode(String payCode) {
        this.payCode = payCode == null ? null : payCode.trim();
    }

    public String getPayName() {
        return payName;
    }

    public void setPayName(String payName) {
        this.payName = payName == null ? null : payName.trim();
    }

    public String getPayFee() {
        return payFee;
    }

    public void setPayFee(String payFee) {
        this.payFee = payFee == null ? null : payFee.trim();
    }

    public String getPayIntro() {
        return payIntro;
    }

    public void setPayIntro(String payIntro) {
        this.payIntro = payIntro == null ? null : payIntro.trim();
    }

    public String getPayParams() {
        return payParams;
    }

    public void setPayParams(String payParams) {
        this.payParams = payParams == null ? null : payParams.trim();
    }

    public Short getPayOrder() {
        return payOrder;
    }

    public void setPayOrder(Short payOrder) {
        this.payOrder = payOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getPayIcon() {
        return payIcon;
    }

    public void setPayIcon(String payIcon) {
        this.payIcon = payIcon == null ? null : payIcon.trim();
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }
}