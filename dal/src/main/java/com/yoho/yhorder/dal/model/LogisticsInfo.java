package com.yoho.yhorder.dal.model;

public class LogisticsInfo {
    private Integer id;

    private Long orderCode;

    private String waybillCode;

    private String acceptAddress;

    private String acceptRemark;

    private Byte logisticsType;

    private Byte state;

    private Integer createTime;

    private Boolean dealWithCost;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public String getWaybillCode() {
        return waybillCode;
    }

    public void setWaybillCode(String waybillCode) {
        this.waybillCode = waybillCode == null ? null : waybillCode.trim();
    }

    public String getAcceptAddress() {
        return acceptAddress;
    }

    public void setAcceptAddress(String acceptAddress) {
        this.acceptAddress = acceptAddress == null ? null : acceptAddress.trim();
    }

    public String getAcceptRemark() {
        return acceptRemark;
    }

    public void setAcceptRemark(String acceptRemark) {
        this.acceptRemark = acceptRemark == null ? null : acceptRemark.trim();
    }

    public Byte getLogisticsType() {
        return logisticsType;
    }

    public void setLogisticsType(Byte logisticsType) {
        this.logisticsType = logisticsType;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public Boolean getDealWithCost() {
        return dealWithCost;
    }

    public void setDealWithCost(Boolean dealWithCost) {
        this.dealWithCost = dealWithCost;
    }
}