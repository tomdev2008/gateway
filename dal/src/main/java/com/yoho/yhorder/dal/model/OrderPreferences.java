package com.yoho.yhorder.dal.model;

public class OrderPreferences {
    private Integer uid;

    private String addrInfo;

    private String payInfo;

    private String invoiceInfo;

    private String deliveryInfo;

    private String isPreContact;

    private String remark;

    private String needPrintPrice;

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getAddrInfo() {
        return addrInfo;
    }

    public void setAddrInfo(String addrInfo) {
        this.addrInfo = addrInfo == null ? null : addrInfo.trim();
    }

    public String getPayInfo() {
        return payInfo;
    }

    public void setPayInfo(String payInfo) {
        this.payInfo = payInfo == null ? null : payInfo.trim();
    }

    public String getInvoiceInfo() {
        return invoiceInfo;
    }

    public void setInvoiceInfo(String invoiceInfo) {
        this.invoiceInfo = invoiceInfo == null ? null : invoiceInfo.trim();
    }

    public String getDeliveryInfo() {
        return deliveryInfo;
    }

    public void setDeliveryInfo(String deliveryInfo) {
        this.deliveryInfo = deliveryInfo == null ? null : deliveryInfo.trim();
    }

    public String getIsPreContact() {
        return isPreContact;
    }

    public void setIsPreContact(String isPreContact) {
        this.isPreContact = isPreContact == null ? null : isPreContact.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getNeedPrintPrice() {
        return needPrintPrice;
    }

    public void setNeedPrintPrice(String needPrintPrice) {
        this.needPrintPrice = needPrintPrice == null ? null : needPrintPrice.trim();
    }
}