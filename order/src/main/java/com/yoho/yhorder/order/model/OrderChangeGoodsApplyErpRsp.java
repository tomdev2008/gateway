package com.yoho.yhorder.order.model;

import lombok.Data;

import java.io.Serializable;

//erp换货申请返回
public class OrderChangeGoodsApplyErpRsp implements Serializable {

    private static final long serialVersionUID = 5732767518569186132L;

    /** refound_id */
    private Integer id;

    private Integer uid;

    private String init_order_code;
    //源订单号
    private Long source_order_code;
    //新订单号
    private Long new_order_code;
    //erp换货申请ID
    private Integer exchange_id;
    //erp退货申请ID
    private Integer returned_id;
    //erp换货状态
    private Integer exchange_status;
    //erp退货状态
    private Integer returned_status;

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getInit_order_code() {
        return init_order_code;
    }

    public void setInit_order_code(String init_order_code) {
        this.init_order_code = init_order_code;
    }

    public Long getSource_order_code() {
        return source_order_code;
    }

    public void setSource_order_code(Long source_order_code) {
        this.source_order_code = source_order_code;
    }

    public Long getNew_order_code() {
        return new_order_code;
    }

    public void setNew_order_code(Long new_order_code) {
        this.new_order_code = new_order_code;
    }

    public Integer getExchange_id() {
        return exchange_id;
    }

    public void setExchange_id(Integer exchange_id) {
        this.exchange_id = exchange_id;
    }

    public Integer getReturned_id() {
        return returned_id;
    }

    public void setReturned_id(Integer returned_id) {
        this.returned_id = returned_id;
    }

    public Integer getExchange_status() {
        return exchange_status;
    }

    public void setExchange_status(Integer exchange_status) {
        this.exchange_status = exchange_status;
    }

    public Integer getReturned_status() {
        return returned_status;
    }

    public void setReturned_status(Integer returned_status) {
        this.returned_status = returned_status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
