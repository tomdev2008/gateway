package com.yoho.yhorder.order.restapi.bean;

import java.io.Serializable;

/**
 * 提交的JSON有时候需要
 * 这个对象进行反JSON化
 *
 * @author CaoQi
 * @Time 2015/10/31
 */
public class ShareOrderForm implements Serializable{

    private long serialVersionUID = 1;

    /**
     * 当前的页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer limit;

    /**
     * 商品编号
     */
    private String goodsId;

    /**
     * 满意度
     */
    private Integer satisfied;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 订单编号
     */
    private String orderCode;

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setSerialVersionUID(long serialVersionUID) {
        this.serialVersionUID = serialVersionUID;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getSatisfied() {
        return satisfied;
    }

    public void setSatisfied(Integer satisfied) {
        this.satisfied = satisfied;
    }
}
