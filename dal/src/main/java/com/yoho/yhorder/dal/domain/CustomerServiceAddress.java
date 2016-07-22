package com.yoho.yhorder.dal.domain;


import com.yoho.yhorder.dal.model.Base;

/**
 * qianjun 2016/5/3
 */
public class CustomerServiceAddress extends Base {


    private static final long serialVersionUID = 6438998573706893599L;
    /**
     * 订单号
     */
    private Long orderCode;

    /**
     * 主订单号
     */
    private Long parentOrderCode;

    /**
     * 收货人
     */
    private String userName;


    /**
     * 手机
     */
    private String mobile;

    /**
     * 电话
     */
    private String phone;

    /**
     * 地址编码
     */
    private Integer areaCode;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 邮政编码
     */
    private Integer zipCode;


    private String isPreContact;

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
     * 支付类型
     */
    private Byte paymentType;

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public Long getParentOrderCode() {
        return parentOrderCode;
    }

    public void setParentOrderCode(Long parentOrderCode) {
        this.parentOrderCode = parentOrderCode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Integer areaCode) {
        this.areaCode = areaCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
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

    public String getIsPreContact() {
        return isPreContact;
    }

    public void setIsPreContact(String isPreContact) {
        this.isPreContact = isPreContact;
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

    public Byte getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Byte paymentType) {
        this.paymentType = paymentType;
    }
}
