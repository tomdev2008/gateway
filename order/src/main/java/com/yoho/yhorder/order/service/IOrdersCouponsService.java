package com.yoho.yhorder.order.service;

import com.yoho.service.model.order.model.OrdersCouponsBO;
import com.yoho.service.model.order.request.OrdersCouponsRequest;

import java.util.List;

/**
 * Created by yoho on 2016/1/12.
 */
public interface IOrdersCouponsService {

    List<OrdersCouponsBO> findOrdersCouponsBO(OrdersCouponsRequest ordersCouponsRequest);
}
