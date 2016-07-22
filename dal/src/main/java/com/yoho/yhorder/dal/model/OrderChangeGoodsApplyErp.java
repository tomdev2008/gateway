package com.yoho.yhorder.dal.model;

import com.yoho.service.model.order.request.OrderChangeGoodsDetailApplyReq;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

//换货申请返回
public class OrderChangeGoodsApplyErp implements Serializable {

    private static final long serialVersionUID = 5732767518569186132L;
    private Integer uid;
    private String init_order_code;
    //源订单号
    private Long source_order_code;
    //新订单号
    private Long new_order_code;
    //erp换货申请ID
    private Integer erp_exchange_id;
    //erp退货申请ID
    private Integer erp_refund_id;
    //erp换货状态
    private Integer erp_exchange_status;
    //erp退货状态
    private Integer erp_refund_status;
    //换货商品状态
    private String exchangeGoodsStatus;
    //退货商品状态
    private String refundGoodsStatus;

    private byte refund_mode;

    private List<OrderChangeGoodsDetailApplyReq> exchange_goods_list;

    //换货申请单 用于退货表
    private Integer changeOrdId;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

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

    public Integer getErp_exchange_id() {
        return erp_exchange_id;
    }

    public void setErp_exchange_id(Integer erp_exchange_id) {
        this.erp_exchange_id = erp_exchange_id;
    }

    public Integer getErp_refund_id() {
        return erp_refund_id;
    }

    public void setErp_refund_id(Integer erp_refund_id) {
        this.erp_refund_id = erp_refund_id;
    }

    public Integer getErp_exchange_status() {
        return erp_exchange_status;
    }

    public void setErp_exchange_status(Integer erp_exchange_status) {
        this.erp_exchange_status = erp_exchange_status;
    }

    public Integer getErp_refund_status() {
        return erp_refund_status;
    }

    public void setErp_refund_status(Integer erp_refund_status) {
        this.erp_refund_status = erp_refund_status;
    }

    public String getExchangeGoodsStatus() {
        return exchangeGoodsStatus;
    }

    public void setExchangeGoodsStatus(String exchangeGoodsStatus) {
        this.exchangeGoodsStatus = exchangeGoodsStatus;
    }

    public String getRefundGoodsStatus() {
        return refundGoodsStatus;
    }

    public void setRefundGoodsStatus(String refundGoodsStatus) {
        this.refundGoodsStatus = refundGoodsStatus;
    }

    public byte getRefund_mode() {
        return refund_mode;
    }

    public void setRefund_mode(byte refund_mode) {
        this.refund_mode = refund_mode;
    }

    public List<OrderChangeGoodsDetailApplyReq> getExchange_goods_list() {
        return exchange_goods_list;
    }

    public void setExchange_goods_list(List<OrderChangeGoodsDetailApplyReq> exchange_goods_list) {
        this.exchange_goods_list = exchange_goods_list;
    }

    public Integer getChangeOrdId() {
        return changeOrdId;
    }

    public void setChangeOrdId(Integer changeOrdId) {
        this.changeOrdId = changeOrdId;
    }
}
