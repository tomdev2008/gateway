package com.yoho.yhorder.dal.domain;

import com.yoho.yhorder.dal.model.Base;

public class OrdersProcessStatus extends Base {

    private static final long serialVersionUID = 7725208160728535209L;

    private int id;

    private int ordersId;

    private int value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrdersId() {
        return ordersId;
    }

    public void setOrdersId(int ordersId) {
        this.ordersId = ordersId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}