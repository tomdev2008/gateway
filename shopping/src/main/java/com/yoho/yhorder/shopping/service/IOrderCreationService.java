package com.yoho.yhorder.shopping.service;

        import com.yoho.yhorder.shopping.model.OrderCreationContext;

/**
 * Created by JXWU on 2015/12/5.
 */
public interface IOrderCreationService {

    /**
     * 创建订单
     * @param context
     */
    void create(OrderCreationContext context);
}
