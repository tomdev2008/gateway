package com.yoho.yhorder.dal.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class InvoiceLogs {
    private Integer id;

    private String seriesNum;

    private Integer orderId;

    private Integer type;

    private String reqParam;

    private String invoiceInfo;

    private Integer createTime;

    private Integer issueStatus;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSeriesNum() {
        return seriesNum;
    }

    public void setSeriesNum(String seriesNum) {
        this.seriesNum = seriesNum == null ? null : seriesNum.trim();
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getReqParam() {
        return reqParam;
    }

    public void setReqParam(String reqParam) {
        this.reqParam = reqParam == null ? null : reqParam.trim();
    }

    public String getInvoiceInfo() {
        return invoiceInfo;
    }

    public void setInvoiceInfo(String invoiceInfo) {
        this.invoiceInfo = invoiceInfo == null ? null : invoiceInfo.trim();
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public Integer getIssueStatus() {
        return issueStatus;
    }

    public void setIssueStatus(Integer issueStatus) {
        this.issueStatus = issueStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("seriesNum", seriesNum)
                .append("orderId", orderId)
                .append("type", type)
                .append("reqParam", reqParam)
                .append("invoiceInfo", invoiceInfo)
                .append("createTime", createTime)
                .append("issueStatus", issueStatus)
                .toString();
    }
}