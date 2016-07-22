package com.yoho.yhorder.dal.domain;


import com.yoho.yhorder.dal.model.Base;

/**
 * qianjun 2016/5/3
 */
public class DeliveryAddress extends Base {


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
     * 电话
     */
    private String phone;

    /**
     * 手机
     */
    private String  mobile ;

    /**
     * 地址编码
     */
    private Integer areaCode;

    /**
     * 省
     */
    private String   province ;

    /**
     * 市
     */
    private  String  city ;

    /**
     * 区
     */
    private String  district;

    /**
     * 详细地址
     */
    private String  address;

    /**
     * 邮政编码
     */
    private Integer zipCode;

    /**
     * email邮件
     */
    private String email;

    /**
     * 收货人地址ID
     */
    private Integer addressId;

    /**
     * 收货地址修改次数
     */
    private Integer deliveryAddressUpdateTimes;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public Integer getDeliveryAddressUpdateTimes() {
        return deliveryAddressUpdateTimes;
    }

    public void setDeliveryAddressUpdateTimes(Integer deliveryAddressUpdateTimes) {
        this.deliveryAddressUpdateTimes = deliveryAddressUpdateTimes;
    }
}
