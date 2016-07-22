package com.yoho.yhorder.dal.domain;

public class ChangeGoodsMainInfo {

    private Integer id;

    private Integer uid;

    private Long initOrderCode;

    private Long sourceOrderCode;

    private Long orderCode;

    private Byte exchangeMode;

    private Byte exchangeRequestType;

    private Byte reject;

    private String remark;

    private String expressCompany;

    private String expressNumber;

    private Integer expressId;

    private Integer erpExchangeId;

    private Byte status;

    private Integer createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
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

    public Byte getExchangeMode() {
        return exchangeMode;
    }

    public void setExchangeMode(Byte exchangeMode) {
        this.exchangeMode = exchangeMode;
    }

    public Byte getExchangeRequestType() {
        return exchangeRequestType;
    }

    public void setExchangeRequestType(Byte exchangeRequestType) {
        this.exchangeRequestType = exchangeRequestType;
    }

    public Byte getReject() {
        return reject;
    }

    public void setReject(Byte reject) {
        this.reject = reject;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getExpressCompany() {
        return expressCompany;
    }

    public void setExpressCompany(String expressCompany) {
        this.expressCompany = expressCompany == null ? null : expressCompany.trim();
    }

    public String getExpressNumber() {
        return expressNumber;
    }

    public void setExpressNumber(String expressNumber) {
        this.expressNumber = expressNumber == null ? null : expressNumber.trim();
    }

    public Integer getExpressId() {
        return expressId;
    }

    public void setExpressId(Integer expressId) {
        this.expressId = expressId;
    }

    public Integer getErpExchangeId() {
        return erpExchangeId;
    }

    public void setErpExchangeId(Integer erpExchangeId) {
        this.erpExchangeId = erpExchangeId;
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

    @Override
    public String toString() {
        return "ChangeGoodsMainInfo{" +
                "id=" + id +
                ", uid=" + uid +
                ", initOrderCode=" + initOrderCode +
                ", sourceOrderCode=" + sourceOrderCode +
                ", orderCode=" + orderCode +
                ", exchangeMode=" + exchangeMode +
                ", exchangeRequestType=" + exchangeRequestType +
                ", reject=" + reject +
                ", remark='" + remark + '\'' +
                ", expressCompany='" + expressCompany + '\'' +
                ", expressNumber='" + expressNumber + '\'' +
                ", expressId=" + expressId +
                ", erpExchangeId=" + erpExchangeId +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}