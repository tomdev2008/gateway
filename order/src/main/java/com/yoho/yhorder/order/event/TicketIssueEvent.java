package com.yoho.yhorder.order.event;

import com.yoho.service.model.order.response.Orders;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * qianjun 2016/5/31
 */
public class TicketIssueEvent {
    private Orders orders;

    public Orders getOrders() {
        return orders;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
