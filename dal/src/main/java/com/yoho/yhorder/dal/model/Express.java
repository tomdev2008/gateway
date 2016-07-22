package com.yoho.yhorder.dal.model;

/**
 * Created by sunjiexiang on 2015/11/23.
 * 物流对象
 */
public class Express extends Base{
    private static final long serialVersionUID = 8695065920110408010L;
    /**
     * 物流ID
     */
    private Integer id;

    /**
     * 物流单号
     */
    private String expressNum;

    /**
     * 通道ID
     */
    private Integer routeId;

    /**
     * 订单编号
     */
    private Long orderCode;

    /**
     * 接收地址
     */
    private String acceptAddress;

    /**
     * 接收时间
     */
    private Integer acceptTime;

    /**
     * 操作码
     */
    private Short opcode;

    /**
     * 接收备注
     */
    private String acceptRemark;

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

    public String getExpressNum() {
        return expressNum;
    }

    public void setExpressNum(String expressNum) {
        this.expressNum = expressNum == null ? null : expressNum.trim();
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public String getAcceptAddress() {
        return acceptAddress;
    }

    public void setAcceptAddress(String acceptAddress) {
        this.acceptAddress = acceptAddress == null ? null : acceptAddress.trim();
    }

    public Integer getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(Integer acceptTime) {
        this.acceptTime = acceptTime;
    }

    public Short getOpcode() {
        return opcode;
    }

    public void setOpcode(Short opcode) {
        this.opcode = opcode;
    }

    public String getAcceptRemark() {
        return acceptRemark;
    }

    public void setAcceptRemark(String acceptRemark) {
        this.acceptRemark = acceptRemark == null ? null : acceptRemark.trim();
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }
}