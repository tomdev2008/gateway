package com.yoho.yhorder.order.restapi.bean;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * 返回给前端json 对象了类
 *
 * @author CaoQi
 * @Time 2015/11/23
 */
public class OrdersGoodsResponse {

    /**
     * 商品skn
     */
    private String product_skn;

    /**
     * 商品sku
     */
    private String product_sku;

    /**
     * 尺寸名称
     */
    private String size_name;

    /**
     * 颜色名称
     */
    private String color_name;

    /**
     * 购买数量
     */
    private String buy_number;

    /**
     * 商品名
     */
    private String product_name;

    /**
     * 商品价格
     */
    private String goods_price;

    /**
     * 商品金额
     */
    private String goods_amount;

    /**
     * 商品图片
     */
    private String goods_image;

    /**
     * 商品类型
     */
    private String goods_type;

    /**
     * 商品状态
     */
    private String goods_status;

    /**
     * 送货时间
     */
    private String expect_arrival_time;

    public String getProduct_skn() {
        return product_skn;
    }

    public void setProduct_skn(String product_skn) {
        this.product_skn = product_skn;
    }

    public String getProduct_sku() {
        return product_sku;
    }

    public void setProduct_sku(String product_sku) {
        this.product_sku = product_sku;
    }

    public String getSize_name() {
        return size_name;
    }

    public void setSize_name(String size_name) {
        this.size_name = size_name;
    }

    public String getColor_name() {
        return color_name;
    }

    public void setColor_name(String color_name) {
        this.color_name = color_name;
    }

    public String getBuy_number() {
        return buy_number;
    }

    public void setBuy_number(String buy_number) {
        this.buy_number = buy_number;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getGoods_price() {
        return goods_price;
    }

    public void setGoods_price(String goods_price) {
        this.goods_price = goods_price;
    }

    public String getGoods_amount() {
        return goods_amount;
    }

    public void setGoods_amount(String goods_amount) {
        this.goods_amount = goods_amount;
    }

    public String getGoods_image() {
        return goods_image;
    }

    public void setGoods_image(String goods_image) {
        this.goods_image = goods_image;
    }

    public String getGoods_type() {
        return goods_type;
    }

    public void setGoods_type(String goods_type) {
        this.goods_type = goods_type;
    }

    public String getGoods_status() {
        return goods_status;
    }

    public void setGoods_status(String goods_status) {
        this.goods_status = goods_status;
    }

    public String getExpect_arrival_time() {
        return expect_arrival_time;
    }

    public void setExpect_arrival_time(String expect_arrival_time) {
        this.expect_arrival_time = expect_arrival_time;
    }

    public String toString(){
        return ReflectionToStringBuilder.toString(this);
    }
}
