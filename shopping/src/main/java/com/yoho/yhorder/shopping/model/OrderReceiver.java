package com.yoho.yhorder.shopping.model;

/**
 * Created by JXWU on 2015/11/30.
 */
public class OrderReceiver {
    /**
     * 收货人
     * @var string
     */
    public String consigneeName = "";

    /**
     * 电话
     * @var string
     */
    public String  phone = "";

    /**
     * 手机
     * @var string
     */
    public String  mobile = "";

    /**
     * 省份
     * @var string
     */
    public String   province = "";

    /**
     * 市
     * @var string
     */
    public String  city = "";

    /**
     * 区
     * @var string
     */
    public String  district = "";

    /**
     * 详细地址
     * @var string
     */
    public String  address = "";

    /**
     * 邮编
     * @var string
     */
    public String  zipCode = "";

    /**
     * email
     * @var string
     */
    public String  email = "";


    /**
     * 配送方式 1普通 2加快  10Yohood自提
     * @var int
     */
    public Integer shippingManner = 1;

    //地址编码
    private String areaCode;

    public String getConsigneeName() {
        return consigneeName;
    }

    public void setConsigneeName(String consigneeName) {
        this.consigneeName = consigneeName;
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

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getShippingManner() {
        return shippingManner;
    }

    public void setShippingManner(Integer shippingManner) {
        this.shippingManner = shippingManner;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }
}
