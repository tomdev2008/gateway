package com.yoho.yhorder.dal.model;

import com.yoho.service.model.order.request.OrderChangeGoodsDetailApplyReq;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Orders
 * 类描述
 *
 * @author lijian
 * @date 2015/1/3
 */
public class ChangeOrdersBo {

    public static final Byte ORDERS_STATUS_DELETE = 0;

    /**
     * 已付款
     */
    public static final String PAYMENT_STATUS_YES = "Y";

    /**
     * 未付款
     */
    public static final String PAYMENT_STATUS_NO = "N";

    private Integer id;

    /**
     * 订单号
     */
    private Long orderCode;

    /**
     * 用户ID
     */
    private Integer uid;

    /**
     * 快递单号
     */
    private String expressNumber;

    /**
     * 订单类型(渠道)
     */
    private Byte orderType;

    private String isInvoice;

    /**
     * 发票类型
     */
    private String invoicesType;

    /**
     * 发票抬头
     */
    private String invoicesPayable;

    /**
     * 使用yoho币量
     */
    private Integer yohoCoinNum;

        /**
         * 支付类型
         */
        private Byte paymentType;

        /**
         * 支付方式
         */
        private Byte payment=0;

    /**
     *
     */
    private String paymentAmount;

    /**
     * 银行编码
     */
    private String bankCode;

    /**
     * 支付状态
     */
    private String paymentStatus;

    /**
     *
     */
    private Byte shippingTypeId;

    /**
     * 运费金额
     */
    private BigDecimal shippingCost;

    /**
     * 快递公司ID
     */
    private Byte expressId;

    /**
     *
     */
    private String userName;

    /**
     * 收货人电话
     */
    private String phone;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 地区编码
     */
    private Integer areaCode;

    /**
     * 具体的地区
     */
    private String Area;

    /**
     * 地质
     */
    private String address;

    /**
     * 邮编
     */
    private Integer zipCode;

    /**
     * 注释
     */
    private String remark;

    /**
     * 到货时间
     */
    private Integer receivingTime;

    /**
     * 收货时间(周六方便收货)
     */
    private String receiptTime;

    private Byte exceptionStatus;

    /**
     *
     */
    private String isLock;

    /**
     *
     */
    private String isArrive;

    /**
     * 订单状态
     */
    private Byte status;

    /**
     * 是否取消
     */
    private String isCancel;

    /**
     * 是否可以评论
     */
    private String isComment;

    private Byte cancelType;

    private Byte exchangeStatus;

    private Byte refundStatus;

    private Integer arriveTime;

    private Integer shipmentTime;

    private Integer createTime;

    private BigDecimal amount;

    private String isPrintPrice;

    private String isPreContact;

    private String isNeedRapid;

    private Integer updateTime;

    private Byte attribute;

    private String isPayed;

    private Short activitiesId;

    private Long parentOrderCode;

    /**
     * 订单状态
     */
    private Byte ordersStatus;

    private String canCommont;

    private String expressCompany;

    private String statusStr;

    private BigDecimal goodsTotalAmount;

    private BigDecimal couponsAmount;

    private BigDecimal promotionAmount;

    private boolean isSupportRefund;

    private boolean isSupportExchange;

    private List<OrderChangeGoodsDetailApplyReq> exchangeGoodsList;

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

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getExpressNumber() {
        return expressNumber;
    }

    public void setExpressNumber(String expressNumber) {
        this.expressNumber = expressNumber;
    }

    public Byte getOrderType() {
        return orderType;
    }

    public void setOrderType(Byte orderType) {
        this.orderType = orderType;
    }

    public String getIsInvoice() {
        return isInvoice;
    }

    public void setIsInvoice(String isInvoice) {
        this.isInvoice = isInvoice;
    }

    public String getInvoicesType() {
        return invoicesType;
    }

    public void setInvoicesType(String invoicesType) {
        this.invoicesType = invoicesType;
    }

    public String getInvoicesPayable() {
        return invoicesPayable;
    }

    public void setInvoicesPayable(String invoicesPayable) {
        this.invoicesPayable = invoicesPayable;
    }

    public Integer getYohoCoinNum() {
        return yohoCoinNum;
    }

    public void setYohoCoinNum(Integer yohoCoinNum) {
        this.yohoCoinNum = yohoCoinNum;
    }

    public Byte getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Byte paymentType) {
        this.paymentType = paymentType;
    }

    public Byte getPayment() {
        return payment;
    }

    public void setPayment(Byte payment) {
        this.payment = payment;
    }

    public String getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(String paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Byte getShippingTypeId() {
        return shippingTypeId;
    }

    public void setShippingTypeId(Byte shippingTypeId) {
        this.shippingTypeId = shippingTypeId;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public Byte getExpressId() {
        return expressId;
    }

    public void setExpressId(Byte expressId) {
        this.expressId = expressId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Integer areaCode) {
        this.areaCode = areaCode;
    }

    public String getArea() {
        return Area;
    }

    public void setArea(String area) {
        Area = area;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getZipCode() {
        return zipCode;
    }

    public void setZipCode(Integer zipCode) {
        this.zipCode = zipCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getReceivingTime() {
        return receivingTime;
    }

    public void setReceivingTime(Integer receivingTime) {
        this.receivingTime = receivingTime;
    }

    public String getReceiptTime() {
        return receiptTime;
    }

    public void setReceiptTime(String receiptTime) {
        this.receiptTime = receiptTime;
    }

    public Byte getExceptionStatus() {
        return exceptionStatus;
    }

    public void setExceptionStatus(Byte exceptionStatus) {
        this.exceptionStatus = exceptionStatus;
    }

    public String getIsLock() {
        return isLock;
    }

    public void setIsLock(String isLock) {
        this.isLock = isLock;
    }

    public String getIsArrive() {
        return isArrive;
    }

    public void setIsArrive(String isArrive) {
        this.isArrive = isArrive;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getIsCancel() {
        return isCancel;
    }

    public void setIsCancel(String isCancel) {
        this.isCancel = isCancel;
    }

    public String getIsComment() {
        return isComment;
    }

    public void setIsComment(String isComment) {
        this.isComment = isComment;
    }

    public Byte getCancelType() {
        return cancelType;
    }

    public void setCancelType(Byte cancelType) {
        this.cancelType = cancelType;
    }

    public Byte getExchangeStatus() {
        return exchangeStatus;
    }

    public void setExchangeStatus(Byte exchangeStatus) {
        this.exchangeStatus = exchangeStatus;
    }

    public Byte getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(Byte refundStatus) {
        this.refundStatus = refundStatus;
    }

    public Integer getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(Integer arriveTime) {
        this.arriveTime = arriveTime;
    }

    public Integer getShipmentTime() {
        return shipmentTime;
    }

    public void setShipmentTime(Integer shipmentTime) {
        this.shipmentTime = shipmentTime;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getIsPrintPrice() {
        return isPrintPrice;
    }

    public void setIsPrintPrice(String isPrintPrice) {
        this.isPrintPrice = isPrintPrice;
    }

    public String getIsPreContact() {
        return isPreContact;
    }

    public void setIsPreContact(String isPreContact) {
        this.isPreContact = isPreContact;
    }

    public String getIsNeedRapid() {
        return isNeedRapid;
    }

    public void setIsNeedRapid(String isNeedRapid) {
        this.isNeedRapid = isNeedRapid;
    }

    public Integer getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Integer updateTime) {
        this.updateTime = updateTime;
    }

    public Byte getAttribute() {
        return attribute;
    }

    public void setAttribute(Byte attribute) {
        this.attribute = attribute;
    }

    public String getIsPayed() {
        return isPayed;
    }

    public void setIsPayed(String isPayed) {
        this.isPayed = isPayed;
    }

    public Short getActivitiesId() {
        return activitiesId;
    }

    public void setActivitiesId(Short activitiesId) {
        this.activitiesId = activitiesId;
    }

    public Long getParentOrderCode() {
        return parentOrderCode;
    }

    public void setParentOrderCode(Long parentOrderCode) {
        this.parentOrderCode = parentOrderCode;
    }

    public Byte getOrdersStatus() {
        return ordersStatus;
    }

    public void setOrdersStatus(Byte ordersStatus) {
        this.ordersStatus = ordersStatus;
    }

    public String getCanCommont() {
        return canCommont;
    }

    public void setCanCommont(String canCommont) {
        this.canCommont = canCommont;
    }

    public String getExpressCompany() {
        return expressCompany;
    }

    public void setExpressCompany(String expressCompany) {
        this.expressCompany = expressCompany;
    }

    public String getStatusStr() {
        return statusStr;
    }

    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
    }

    public BigDecimal getGoodsTotalAmount() {
        return goodsTotalAmount;
    }

    public void setGoodsTotalAmount(BigDecimal goodsTotalAmount) {
        this.goodsTotalAmount = goodsTotalAmount;
    }

    public BigDecimal getCouponsAmount() {
        return couponsAmount;
    }

    public void setCouponsAmount(BigDecimal couponsAmount) {
        this.couponsAmount = couponsAmount;
    }

    public BigDecimal getPromotionAmount() {
        return promotionAmount;
    }

    public void setPromotionAmount(BigDecimal promotionAmount) {
        this.promotionAmount = promotionAmount;
    }

    public boolean isSupportRefund() {
        return isSupportRefund;
    }

    public void setSupportRefund(boolean supportRefund) {
        isSupportRefund = supportRefund;
    }

    public boolean isSupportExchange() {
        return isSupportExchange;
    }

    public void setSupportExchange(boolean supportExchange) {
        isSupportExchange = supportExchange;
    }

    public List<OrderChangeGoodsDetailApplyReq> getExchangeGoodsList() {
        return exchangeGoodsList;
    }

    public void setExchangeGoodsList(List<OrderChangeGoodsDetailApplyReq> exchangeGoodsList) {
        this.exchangeGoodsList = exchangeGoodsList;
    }
}