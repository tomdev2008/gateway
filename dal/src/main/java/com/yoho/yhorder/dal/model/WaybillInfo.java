package com.yoho.yhorder.dal.model;

public class WaybillInfo extends Base {
    private static final long serialVersionUID = 6867518015551440757L;
    /**
     * 运货单ID
     */
    private Integer id;

    /**
     * 订单号
     */
    private Long orderCode;

    /**
     * 运货单号
     */
    private String waybillCode;

    /**
     * 运货单接收地址
     */
    private String addressInfo;

    /**
     * 运货单创建时间
     */
    private Integer createTime;

    /**
     * 物流公司类型
     */
    private Byte logisticsType;

    /**
     * 运货单状态
     */
    private Byte state;

    private Byte smsType;

    private String datas;

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

    public String getAddressInfo() {
        return addressInfo;
    }

    public void setAddressInfo(String addressInfo) {
        this.addressInfo = addressInfo == null ? null : addressInfo.trim();
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
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

    public Byte getSmsType() {
        return smsType;
    }

    public void setSmsType(Byte smsType) {
        this.smsType = smsType;
    }

    public String getDatas() {
        return datas;
    }

    public void setDatas(String datas) {
        this.datas = datas == null ? null : datas.trim();
    }
}