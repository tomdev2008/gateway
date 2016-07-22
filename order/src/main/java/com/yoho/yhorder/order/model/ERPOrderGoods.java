package com.yoho.yhorder.order.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Created by JXWU on 2015/12/2.
 */
public class ERPOrderGoods {
    private Integer product_skn;
    private String product_name;
    private Integer color_id;
    private String color_name;
    private Integer product_id;
    private Integer brand_id;
    private Integer goods_id;
    private Integer product_sku;
    private Integer buy_number;
    private Integer size_id;
    private String size_name;
    private Double sale_price;
    private Double real_price;
    private Double last_price;
    private Integer get_yoho_coin;
    private Double vip_discount;
    private Double real_vip_price;
    private Double vip_discount_money;
    private String is_jit;
    private Integer shop_id;
    private Integer supplier_id;
    private Integer goods_type;
    private Integer erp_sku_id;

    /**
     * 每件sku分摊的优惠券的金额，只对满足优惠券使用的普通商品
     */
    private double coupons_per;
    /**
     * 每件sku分摊的优惠码的金额，所有的普通商品
     */
    private double promo_code_per;
    /**
     * 每件sku分摊的yoho币的数量，所有的普通商品
     */
    private int yoho_coin_per;

    /**
     * 每件sku分摊的红包的金额，所有的普通商品
     */
    private double red_envelope_per;

    //同buy_number
    private Integer num;

    public Integer getProduct_skn() {
        return product_skn;
    }

    public void setProduct_skn(Integer product_skn) {
        this.product_skn = product_skn;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public Integer getColor_id() {
        return color_id;
    }

    public void setColor_id(Integer color_id) {
        this.color_id = color_id;
    }

    public String getColor_name() {
        return color_name;
    }

    public void setColor_name(String color_name) {
        this.color_name = color_name;
    }

    public Integer getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Integer product_id) {
        this.product_id = product_id;
    }

    public Integer getBrand_id() {
        return brand_id;
    }

    public void setBrand_id(Integer brand_id) {
        this.brand_id = brand_id;
    }

    public Integer getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(Integer goods_id) {
        this.goods_id = goods_id;
    }

    public Integer getProduct_sku() {
        return product_sku;
    }

    public void setProduct_sku(Integer product_sku) {
        this.product_sku = product_sku;
    }

    public Integer getBuy_number() {
        return buy_number;
    }

    public void setBuy_number(Integer buy_number) {
        this.buy_number = buy_number;
    }

    public Integer getSize_id() {
        return size_id;
    }

    public void setSize_id(Integer size_id) {
        this.size_id = size_id;
    }

    public String getSize_name() {
        return size_name;
    }

    public void setSize_name(String size_name) {
        this.size_name = size_name;
    }

    public Double getSale_price() {
        return sale_price;
    }

    public void setSale_price(Double sale_price) {
        this.sale_price = sale_price;
    }

    public Double getReal_price() {
        return real_price;
    }

    public void setReal_price(Double real_price) {
        this.real_price = real_price;
    }

    public Double getLast_price() {
        return last_price;
    }

    public void setLast_price(Double last_price) {
        this.last_price = last_price;
    }

    public Integer getGet_yoho_coin() {
        return get_yoho_coin;
    }

    public void setGet_yoho_coin(Integer get_yoho_coin) {
        this.get_yoho_coin = get_yoho_coin;
    }

    public Double getVip_discount() {
        return vip_discount;
    }

    public void setVip_discount(Double vip_discount) {
        this.vip_discount = vip_discount;
    }

    public Double getReal_vip_price() {
        return real_vip_price;
    }

    public void setReal_vip_price(Double real_vip_price) {
        this.real_vip_price = real_vip_price;
    }

    public Double getVip_discount_money() {
        return vip_discount_money;
    }

    public void setVip_discount_money(Double vip_discount_money) {
        this.vip_discount_money = vip_discount_money;
    }

    public String getIs_jit() {
        return is_jit;
    }

    public void setIs_jit(String is_jit) {
        this.is_jit = is_jit;
    }

    public Integer getShop_id() {
        return shop_id;
    }

    public void setShop_id(Integer shop_id) {
        this.shop_id = shop_id;
    }

    public Integer getSupplier_id() {
        return supplier_id;
    }

    public void setSupplier_id(Integer supplier_id) {
        this.supplier_id = supplier_id;
    }

    public Integer getGoods_type() {
        return goods_type;
    }

    public void setGoods_type(Integer goods_type) {
        this.goods_type = goods_type;
    }

    public Integer getErp_sku_id() {
        return erp_sku_id;
    }

    public void setErp_sku_id(Integer erp_sku_id) {
        this.erp_sku_id = erp_sku_id;
    }


    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public double getCoupons_per() {
        return coupons_per;
    }

    public void setCoupons_per(double coupons_per) {
        this.coupons_per = coupons_per;
    }

    public double getPromo_code_per() {
        return promo_code_per;
    }

    public void setPromo_code_per(double promo_code_per) {
        this.promo_code_per = promo_code_per;
    }

    public int getYoho_coin_per() {
        return yoho_coin_per;
    }

    public void setYoho_coin_per(int yoho_coin_per) {
        this.yoho_coin_per = yoho_coin_per;
    }

    public double getRed_envelope_per() {
        return red_envelope_per;
    }

    public void setRed_envelope_per(double red_envelope_per) {
        this.red_envelope_per = red_envelope_per;
    }

    @Override
    public String toString(){
        return ReflectionToStringBuilder.toString(this);
    }


}
