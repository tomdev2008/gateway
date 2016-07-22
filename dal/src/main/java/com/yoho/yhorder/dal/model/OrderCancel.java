package com.yoho.yhorder.dal.model;

public class OrderCancel {
    public static final Byte CANCEL_TYPE_PC = 1;
    public static final Byte CANCEL_TYPE_IPHONE = 2;
    public static final Byte CANCEL_TYPE_IPAD = 3;
    public static final Byte CANCEL_TYPE_ANDROID = 4;
    public static final Byte CANCEL_TYPE_YOHO = 5;
    public static final Byte CANCEL_TYPE_H5 = 6;



    private Long orderCode;

    private Integer uid;

    private String reason;

    private Byte reasonId;

    private Byte canceltype;

    private Integer createTime;

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }

    public Byte getReasonId() {
        return reasonId;
    }

    public void setReasonId(Byte reasonId) {
        this.reasonId = reasonId;
    }

    public Byte getCanceltype() {
        return canceltype;
    }

    public void setCanceltype(Byte canceltype) {
        this.canceltype = canceltype;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }
}