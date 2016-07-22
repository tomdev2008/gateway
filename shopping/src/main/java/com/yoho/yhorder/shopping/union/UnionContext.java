package com.yoho.yhorder.shopping.union;

import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.model.OrderCreationContext;

/**
 * Created by JXWU on 2015/12/8.
 */
public class UnionContext {
    private ChargeContext chargeContext;
    private OrderCreationContext orderCreationContext;

    public ChargeContext getChargeContext() {
        return chargeContext;
    }

    public void setChargeContext(ChargeContext chargeContext) {
        this.chargeContext = chargeContext;
    }

    public OrderCreationContext getOrderCreationContext() {
        return orderCreationContext;
    }

    public void setOrderCreationContext(OrderCreationContext orderCreationContext) {
        this.orderCreationContext = orderCreationContext;
    }
}
